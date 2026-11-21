package br.com.taskflow.controller

import br.com.taskflow.dto.ComentarioRequest
import br.com.taskflow.dto.ComentarioResponse
import br.com.taskflow.service.ComentarioService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.server.ResponseStatusException
import java.security.Principal

class ComentarioControllerTest {

    private val service = mockk<ComentarioService>()

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(ComentarioController(service))
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    // Simula o usuário logado (no sistema real, quem preenche isso é o token JWT).
    private val usuarioLogado = object : Principal {
        override fun getName(): String = "ana@email.com"
    }

    private val resposta = ComentarioResponse(7L, "Comecei a tarefa", 10L, 2L, "Ana")

    @Test
    fun `POST comentarios deve usar o usuario logado como autor`() {
        every { service.criar(10L, ComentarioRequest("Comecei a tarefa"), "ana@email.com") } returns resposta

        mockMvc.perform(
            post("/api/tarefas/10/comentarios")
                .principal(usuarioLogado)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"texto":"Comecei a tarefa"}""")
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("\$.id").value(7))
            .andExpect(jsonPath("\$.autorNome").value("Ana"))
            .andExpect(jsonPath("\$.tarefaId").value(10))
    }

    @Test
    fun `POST comentarios com texto em branco deve devolver 400`() {
        mockMvc.perform(
            post("/api/tarefas/10/comentarios")
                .principal(usuarioLogado)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"texto":""}""")
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    fun `POST comentarios em tarefa inexistente deve devolver 404`() {
        every { service.criar(99L, ComentarioRequest("oi"), "ana@email.com") } throws
            ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa nao encontrada")

        mockMvc.perform(
            post("/api/tarefas/99/comentarios")
                .principal(usuarioLogado)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"texto":"oi"}""")
        )
            .andExpect(status().isNotFound())
    }

    @Test
    fun `GET comentarios deve devolver a lista`() {
        every { service.listar(10L) } returns listOf(resposta)

        mockMvc.perform(get("/api/tarefas/10/comentarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$[0].texto").value("Comecei a tarefa"))
    }
}
