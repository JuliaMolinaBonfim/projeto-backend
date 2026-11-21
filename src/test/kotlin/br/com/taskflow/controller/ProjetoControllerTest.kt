package br.com.taskflow.controller

import br.com.taskflow.dto.ProjetoRequest
import br.com.taskflow.dto.ProjetoResponse
import br.com.taskflow.dto.TarefaResponse
import br.com.taskflow.service.ProjetoService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.server.ResponseStatusException

class ProjetoControllerTest {

    private val service = mockk<ProjetoService>()

    // Monta só o controller (sem subir o Spring inteiro nem a segurança): é um teste unitário.
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(ProjetoController(service))
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    private val projetoJson = """{"nome":"TaskFlow","descricao":"Sistema","prazo":"2026-12-15"}"""
    private val resposta = ProjetoResponse(1L, "TaskFlow", "Sistema", "2026-12-15")

    @Test
    fun `POST projetos deve devolver 201 e o projeto criado`() {
        every { service.criar(ProjetoRequest("TaskFlow", "Sistema", "2026-12-15")) } returns resposta

        mockMvc.perform(post("/api/projetos").contentType(MediaType.APPLICATION_JSON).content(projetoJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("\$.id").value(1))
            .andExpect(jsonPath("\$.nome").value("TaskFlow"))
    }

    @Test
    fun `POST projetos com nome em branco deve devolver 400`() {
        val invalido = """{"nome":"","descricao":"Sistema","prazo":"2026-12-15"}"""

        mockMvc.perform(post("/api/projetos").contentType(MediaType.APPLICATION_JSON).content(invalido))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("\$.status").value(400))

        verify(exactly = 0) { service.criar(any()) }
    }

    @Test
    fun `POST projetos com prazo em formato invalido deve devolver 400`() {
        val invalido = """{"nome":"TaskFlow","descricao":"Sistema","prazo":"15/12/2026"}"""

        mockMvc.perform(post("/api/projetos").contentType(MediaType.APPLICATION_JSON).content(invalido))
            .andExpect(status().isBadRequest())
    }

    @Test
    fun `GET projetos deve devolver a lista`() {
        every { service.listar() } returns listOf(resposta)

        mockMvc.perform(get("/api/projetos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$[0].nome").value("TaskFlow"))
    }

    @Test
    fun `GET projetos por id deve devolver o projeto`() {
        every { service.buscarPorId(1L) } returns resposta

        mockMvc.perform(get("/api/projetos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.id").value(1))
    }

    @Test
    fun `GET projetos por id inexistente deve devolver 404`() {
        every { service.buscarPorId(99L) } throws ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto nao encontrado")

        mockMvc.perform(get("/api/projetos/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("\$.status").value(404))
            .andExpect(jsonPath("\$.mensagem").value("Projeto nao encontrado"))
    }

    @Test
    fun `PUT projetos deve devolver o projeto atualizado`() {
        every { service.atualizar(1L, ProjetoRequest("TaskFlow", "Sistema", "2026-12-15")) } returns resposta

        mockMvc.perform(put("/api/projetos/1").contentType(MediaType.APPLICATION_JSON).content(projetoJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.nome").value("TaskFlow"))
    }

    @Test
    fun `DELETE projetos deve devolver 204`() {
        every { service.excluir(1L) } just Runs

        mockMvc.perform(delete("/api/projetos/1"))
            .andExpect(status().isNoContent())

        verify(exactly = 1) { service.excluir(1L) }
    }

    @Test
    fun `DELETE projetos com tarefas deve devolver 409`() {
        every { service.excluir(1L) } throws ResponseStatusException(HttpStatus.CONFLICT, "Projeto possui tarefas")

        mockMvc.perform(delete("/api/projetos/1"))
            .andExpect(status().isConflict())
    }

    @Test
    fun `GET tarefas do projeto deve devolver a lista`() {
        every { service.listarTarefas(1L) } returns listOf(
            TarefaResponse(10L, "Criar tela", "desc", "PENDENTE", 1L, emptyList())
        )

        mockMvc.perform(get("/api/projetos/1/tarefas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$[0].titulo").value("Criar tela"))
            .andExpect(jsonPath("\$[0].projetoId").value(1))
    }
}
