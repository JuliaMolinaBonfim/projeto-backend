package br.com.taskflow.service

import br.com.taskflow.dto.TarefaRequest
import br.com.taskflow.model.Categoria
import br.com.taskflow.model.Projeto
import br.com.taskflow.model.Tarefa
import br.com.taskflow.repository.CategoriaRepository
import br.com.taskflow.repository.ComentarioRepository
import br.com.taskflow.repository.ProjetoRepository
import br.com.taskflow.repository.TarefaRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TarefaServiceTest {

    private val tarefaRepository = mockk<TarefaRepository>()
    private val projetoRepository = mockk<ProjetoRepository>()
    private val categoriaRepository = mockk<CategoriaRepository>()
    private val comentarioRepository = mockk<ComentarioRepository>()
    private val service = TarefaService(
        tarefaRepository, projetoRepository, categoriaRepository, comentarioRepository
    )

    private val projeto = Projeto(id = 1L, nome = "TaskFlow", descricao = "d", prazo = "2026-12-15")
    private val categoria = Categoria(id = 5L, nome = "Desenvolvimento")

    private fun tarefa(
        id: Long = 10L,
        titulo: String = "Criar tela",
        status: String = "PENDENTE",
        categorias: MutableSet<Categoria> = mutableSetOf()
    ) = Tarefa(id = id, titulo = titulo, descricao = "desc", status = status, projeto = projeto, categorias = categorias)

    private fun requisicao(categoriaIds: List<Long> = emptyList()) = TarefaRequest(
        titulo = "Criar tela",
        descricao = "desc",
        status = "PENDENTE",
        projetoId = 1L,
        categoriaIds = categoriaIds
    )

    // ---------- criar ----------

    @Test
    fun `criar deve salvar a tarefa com projeto e categorias`() {
        every { projetoRepository.findById(1L) } returns Optional.of(projeto)
        every { categoriaRepository.findById(5L) } returns Optional.of(categoria)
        val salva = slot<Tarefa>()
        every { tarefaRepository.save(capture(salva)) } answers {
            val t = salva.captured
            Tarefa(id = 10L, titulo = t.titulo, descricao = t.descricao, status = t.status,
                projeto = t.projeto, categorias = t.categorias)
        }

        val resposta = service.criar(requisicao(listOf(5L)))

        assertEquals(10L, resposta.id)
        assertEquals(1L, resposta.projetoId)
        assertEquals(1, resposta.categorias.size)
        assertEquals("Desenvolvimento", resposta.categorias[0].nome)
    }

    @Test
    fun `criar deve lancar 404 quando o projeto nao existe`() {
        every { projetoRepository.findById(1L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> { service.criar(requisicao()) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
        verify(exactly = 0) { tarefaRepository.save(any<Tarefa>()) }
    }

    @Test
    fun `criar deve lancar 404 quando uma categoria nao existe`() {
        every { projetoRepository.findById(1L) } returns Optional.of(projeto)
        every { categoriaRepository.findById(99L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> { service.criar(requisicao(listOf(99L))) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
        verify(exactly = 0) { tarefaRepository.save(any<Tarefa>()) }
    }

    // ---------- listar (filtros e ordenacao) ----------

    @Test
    fun `listar deve filtrar por status`() {
        every { tarefaRepository.findAll(any<Sort>()) } returns listOf(
            tarefa(id = 1L, titulo = "A", status = "PENDENTE"),
            tarefa(id = 2L, titulo = "B", status = "CONCLUIDA"),
            tarefa(id = 3L, titulo = "C", status = "PENDENTE")
        )

        val resposta = service.listar("pendente", null, "id", "asc")

        assertEquals(listOf(1L, 3L), resposta.map { it.id })
    }

    @Test
    fun `listar deve filtrar por parte do titulo ignorando maiusculas`() {
        every { tarefaRepository.findAll(any<Sort>()) } returns listOf(
            tarefa(id = 1L, titulo = "Criar tela inicial"),
            tarefa(id = 2L, titulo = "Testar API")
        )

        val resposta = service.listar(null, "CRIAR", "id", "asc")

        assertEquals(1, resposta.size)
        assertEquals("Criar tela inicial", resposta[0].titulo)
    }

    @Test
    fun `listar deve pedir ordenacao decrescente ao repositorio`() {
        val ordenacao = slot<Sort>()
        every { tarefaRepository.findAll(capture(ordenacao)) } returns emptyList()

        service.listar(null, null, "titulo", "desc")

        assertEquals(Sort.Direction.DESC, ordenacao.captured.getOrderFor("titulo")?.direction)
    }

    @Test
    fun `listar deve lancar 400 para campo de ordenacao invalido`() {
        val erro = assertFailsWith<ResponseStatusException> { service.listar(null, null, "senha", "asc") }

        assertEquals(HttpStatus.BAD_REQUEST, erro.statusCode)
    }

    @Test
    fun `listar deve lancar 400 para direcao invalida`() {
        val erro = assertFailsWith<ResponseStatusException> { service.listar(null, null, "id", "cima") }

        assertEquals(HttpStatus.BAD_REQUEST, erro.statusCode)
    }

    // ---------- buscar / atualizar / excluir ----------

    @Test
    fun `buscarPorId deve lancar 404 quando nao existe`() {
        every { tarefaRepository.findById(99L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> { service.buscarPorId(99L) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
    }

    @Test
    fun `atualizar deve alterar os campos e as categorias`() {
        every { tarefaRepository.findById(10L) } returns Optional.of(tarefa())
        every { projetoRepository.findById(1L) } returns Optional.of(projeto)
        every { categoriaRepository.findById(5L) } returns Optional.of(categoria)
        every { tarefaRepository.save(any<Tarefa>()) } answers { firstArg<Tarefa>() }

        val dados = TarefaRequest("Novo titulo", "nova desc", "CONCLUIDA", 1L, listOf(5L))
        val resposta = service.atualizar(10L, dados)

        assertEquals("Novo titulo", resposta.titulo)
        assertEquals("CONCLUIDA", resposta.status)
        assertEquals(listOf(5L), resposta.categorias.map { it.id })
    }

    @Test
    fun `excluir deve apagar os comentarios e depois a tarefa`() {
        val existente = tarefa()
        every { tarefaRepository.findById(10L) } returns Optional.of(existente)
        every { comentarioRepository.deleteByTarefaId(10L) } just Runs
        every { tarefaRepository.delete(any<Tarefa>()) } just Runs

        service.excluir(10L)

        verify(exactly = 1) { comentarioRepository.deleteByTarefaId(10L) }
        verify(exactly = 1) { tarefaRepository.delete(existente) }
    }

    @Test
    fun `excluir deve lancar 404 quando a tarefa nao existe`() {
        every { tarefaRepository.findById(99L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> { service.excluir(99L) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
        verify(exactly = 0) { tarefaRepository.delete(any<Tarefa>()) }
    }

    // ---------- associacao tarefa x categoria (N:N) ----------

    @Test
    fun `adicionarCategoria deve associar a categoria a tarefa`() {
        every { tarefaRepository.findById(10L) } returns Optional.of(tarefa())
        every { categoriaRepository.findById(5L) } returns Optional.of(categoria)
        every { tarefaRepository.save(any<Tarefa>()) } answers { firstArg<Tarefa>() }

        val resposta = service.adicionarCategoria(10L, 5L)

        assertEquals(listOf(5L), resposta.categorias.map { it.id })
    }

    @Test
    fun `adicionarCategoria deve lancar 409 quando a tarefa ja tem a categoria`() {
        every { tarefaRepository.findById(10L) } returns Optional.of(tarefa(categorias = mutableSetOf(categoria)))
        every { categoriaRepository.findById(5L) } returns Optional.of(categoria)

        val erro = assertFailsWith<ResponseStatusException> { service.adicionarCategoria(10L, 5L) }

        assertEquals(HttpStatus.CONFLICT, erro.statusCode)
    }

    @Test
    fun `adicionarCategoria deve lancar 404 quando a categoria nao existe`() {
        every { tarefaRepository.findById(10L) } returns Optional.of(tarefa())
        every { categoriaRepository.findById(99L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> { service.adicionarCategoria(10L, 99L) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
    }

    @Test
    fun `removerCategoria deve desassociar a categoria da tarefa`() {
        every { tarefaRepository.findById(10L) } returns Optional.of(tarefa(categorias = mutableSetOf(categoria)))
        every { tarefaRepository.save(any<Tarefa>()) } answers { firstArg<Tarefa>() }

        val resposta = service.removerCategoria(10L, 5L)

        assertEquals(0, resposta.categorias.size)
    }

    @Test
    fun `removerCategoria deve lancar 404 quando a tarefa nao possui a categoria`() {
        every { tarefaRepository.findById(10L) } returns Optional.of(tarefa())

        val erro = assertFailsWith<ResponseStatusException> { service.removerCategoria(10L, 5L) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
    }
}
