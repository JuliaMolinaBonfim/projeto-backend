package br.com.taskflow.service

import br.com.taskflow.dto.ProjetoRequest
import br.com.taskflow.model.Projeto
import br.com.taskflow.model.Tarefa
import br.com.taskflow.repository.ProjetoRepository
import br.com.taskflow.repository.TarefaRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProjetoServiceTest {

    // Repositórios "falsos": não acessam o banco, só respondem o que programarmos.
    private val projetoRepository = mockk<ProjetoRepository>()
    private val tarefaRepository = mockk<TarefaRepository>()
    private val service = ProjetoService(projetoRepository, tarefaRepository)

    private fun projeto(id: Long = 1L) =
        Projeto(id = id, nome = "TaskFlow", descricao = "Sistema de tarefas", prazo = "2026-12-15")

    @Test
    fun `criar deve salvar o projeto e devolver o response`() {
        val salvo = slot<Projeto>()
        every { projetoRepository.save(capture(salvo)) } returns projeto(1L)

        val resposta = service.criar(ProjetoRequest("TaskFlow", "Sistema de tarefas", "2026-12-15"))

        assertEquals(1L, resposta.id)
        assertEquals("TaskFlow", resposta.nome)
        assertEquals("TaskFlow", salvo.captured.nome)
        verify(exactly = 1) { projetoRepository.save(any<Projeto>()) }
    }

    @Test
    fun `listar deve devolver todos os projetos`() {
        every { projetoRepository.findAll() } returns listOf(projeto(1L), projeto(2L))

        val resposta = service.listar()

        assertEquals(2, resposta.size)
        assertEquals(2L, resposta[1].id)
    }

    @Test
    fun `buscarPorId deve devolver o projeto quando existe`() {
        every { projetoRepository.findById(1L) } returns Optional.of(projeto(1L))

        val resposta = service.buscarPorId(1L)

        assertEquals(1L, resposta.id)
        assertEquals("2026-12-15", resposta.prazo)
    }

    @Test
    fun `buscarPorId deve lancar 404 quando nao existe`() {
        every { projetoRepository.findById(99L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> { service.buscarPorId(99L) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
    }

    @Test
    fun `atualizar deve alterar os dados do projeto`() {
        every { projetoRepository.findById(1L) } returns Optional.of(projeto(1L))
        every { projetoRepository.save(any<Projeto>()) } answers { firstArg<Projeto>() }

        val resposta = service.atualizar(1L, ProjetoRequest("Novo nome", "Nova descricao", "2027-01-31"))

        assertEquals("Novo nome", resposta.nome)
        assertEquals("Nova descricao", resposta.descricao)
        assertEquals("2027-01-31", resposta.prazo)
    }

    @Test
    fun `atualizar deve lancar 404 quando o projeto nao existe`() {
        every { projetoRepository.findById(99L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> {
            service.atualizar(99L, ProjetoRequest("a", "b", "2026-01-01"))
        }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
        verify(exactly = 0) { projetoRepository.save(any<Projeto>()) }
    }

    @Test
    fun `excluir deve apagar o projeto quando nao tem tarefas`() {
        val existente = projeto(1L)
        every { projetoRepository.findById(1L) } returns Optional.of(existente)
        every { tarefaRepository.existsByProjetoId(1L) } returns false
        every { projetoRepository.delete(any<Projeto>()) } just Runs

        service.excluir(1L)

        verify(exactly = 1) { projetoRepository.delete(existente) }
    }

    @Test
    fun `excluir deve lancar 409 quando o projeto possui tarefas`() {
        every { projetoRepository.findById(1L) } returns Optional.of(projeto(1L))
        every { tarefaRepository.existsByProjetoId(1L) } returns true

        val erro = assertFailsWith<ResponseStatusException> { service.excluir(1L) }

        assertEquals(HttpStatus.CONFLICT, erro.statusCode)
        verify(exactly = 0) { projetoRepository.delete(any<Projeto>()) }
    }

    @Test
    fun `excluir deve lancar 404 quando o projeto nao existe`() {
        every { projetoRepository.findById(99L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> { service.excluir(99L) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
    }

    @Test
    fun `listarTarefas deve devolver as tarefas do projeto`() {
        val tarefa = Tarefa(id = 10L, titulo = "Criar tela", descricao = "d", status = "PENDENTE", projeto = projeto(1L))
        every { projetoRepository.findById(1L) } returns Optional.of(projeto(1L))
        every { tarefaRepository.findByProjetoId(1L) } returns listOf(tarefa)

        val resposta = service.listarTarefas(1L)

        assertEquals(1, resposta.size)
        assertEquals("Criar tela", resposta[0].titulo)
        assertEquals(1L, resposta[0].projetoId)
    }

    @Test
    fun `listarTarefas deve lancar 404 quando o projeto nao existe`() {
        every { projetoRepository.findById(99L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> { service.listarTarefas(99L) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
    }
}
