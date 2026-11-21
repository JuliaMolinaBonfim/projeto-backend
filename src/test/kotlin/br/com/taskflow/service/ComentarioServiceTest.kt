package br.com.taskflow.service

import br.com.taskflow.dto.ComentarioRequest
import br.com.taskflow.model.Comentario
import br.com.taskflow.model.Perfil
import br.com.taskflow.model.Projeto
import br.com.taskflow.model.Tarefa
import br.com.taskflow.model.Usuario
import br.com.taskflow.repository.ComentarioRepository
import br.com.taskflow.repository.TarefaRepository
import br.com.taskflow.repository.UsuarioRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ComentarioServiceTest {

    private val comentarioRepository = mockk<ComentarioRepository>()
    private val tarefaRepository = mockk<TarefaRepository>()
    private val usuarioRepository = mockk<UsuarioRepository>()
    private val service = ComentarioService(comentarioRepository, tarefaRepository, usuarioRepository)

    private val projeto = Projeto(id = 1L, nome = "TaskFlow", descricao = "d", prazo = "2026-12-15")
    private val tarefa = Tarefa(id = 10L, titulo = "Criar tela", descricao = "d", projeto = projeto)
    private val ana = Usuario(id = 2L, nome = "Ana", email = "ana@email.com", senha = "hash", perfil = Perfil.USER)

    @Test
    fun `criar deve salvar o comentario com o autor logado`() {
        every { tarefaRepository.findById(10L) } returns Optional.of(tarefa)
        every { usuarioRepository.findByEmail("ana@email.com") } returns ana
        every { comentarioRepository.save(any<Comentario>()) } answers {
            val c = firstArg<Comentario>()
            Comentario(id = 7L, texto = c.texto, tarefa = c.tarefa, autor = c.autor)
        }

        val resposta = service.criar(10L, ComentarioRequest("Comecei a tarefa"), "ana@email.com")

        assertEquals(7L, resposta.id)
        assertEquals("Comecei a tarefa", resposta.texto)
        assertEquals(10L, resposta.tarefaId)
        assertEquals(2L, resposta.autorId)
        assertEquals("Ana", resposta.autorNome)
    }

    @Test
    fun `criar deve lancar 404 quando a tarefa nao existe`() {
        every { tarefaRepository.findById(99L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> {
            service.criar(99L, ComentarioRequest("oi"), "ana@email.com")
        }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
        verify(exactly = 0) { comentarioRepository.save(any<Comentario>()) }
    }

    @Test
    fun `criar deve lancar 404 quando o usuario nao existe`() {
        every { tarefaRepository.findById(10L) } returns Optional.of(tarefa)
        every { usuarioRepository.findByEmail("fantasma@email.com") } returns null

        val erro = assertFailsWith<ResponseStatusException> {
            service.criar(10L, ComentarioRequest("oi"), "fantasma@email.com")
        }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
    }

    @Test
    fun `listar deve devolver os comentarios da tarefa`() {
        every { tarefaRepository.existsById(10L) } returns true
        every { comentarioRepository.findByTarefaId(10L) } returns listOf(
            Comentario(id = 1L, texto = "primeiro", tarefa = tarefa, autor = ana),
            Comentario(id = 2L, texto = "segundo", tarefa = tarefa, autor = ana)
        )

        val resposta = service.listar(10L)

        assertEquals(listOf("primeiro", "segundo"), resposta.map { it.texto })
    }

    @Test
    fun `listar deve lancar 404 quando a tarefa nao existe`() {
        every { tarefaRepository.existsById(99L) } returns false

        val erro = assertFailsWith<ResponseStatusException> { service.listar(99L) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
    }
}
