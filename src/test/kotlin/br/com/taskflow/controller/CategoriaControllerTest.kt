package br.com.taskflow.controller

import br.com.taskflow.dto.CategoriaRequest
import br.com.taskflow.dto.CategoriaResponse
import br.com.taskflow.service.CategoriaService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
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

class CategoriaControllerTest {

    private val service = mockk<CategoriaService>()

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(CategoriaController(service))
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    private val resposta = CategoriaResponse(1L, "Desenvolvimento")

    @Test
    fun `POST categorias deve devolver 201`() {
        every { service.criar(CategoriaRequest("Desenvolvimento")) } returns resposta

        mockMvc.perform(
            post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("""{"nome":"Desenvolvimento"}""")
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("\$.nome").value("Desenvolvimento"))
    }

    @Test
    fun `POST categorias com nome em branco deve devolver 400`() {
        mockMvc.perform(
            post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("""{"nome":"  "}""")
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    fun `GET categorias deve devolver a lista`() {
        every { service.listar() } returns listOf(resposta)

        mockMvc.perform(get("/api/categorias"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$[0].id").value(1))
    }

    @Test
    fun `GET categoria inexistente deve devolver 404`() {
        every { service.buscarPorId(99L) } throws ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria nao encontrada")

        mockMvc.perform(get("/api/categorias/99"))
            .andExpect(status().isNotFound())
    }

    @Test
    fun `PUT categorias deve devolver a categoria atualizada`() {
        every { service.atualizar(1L, CategoriaRequest("Testes")) } returns CategoriaResponse(1L, "Testes")

        mockMvc.perform(
            put("/api/categorias/1").contentType(MediaType.APPLICATION_JSON).content("""{"nome":"Testes"}""")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.nome").value("Testes"))
    }

    @Test
    fun `DELETE categorias deve devolver 204`() {
        every { service.excluir(1L) } just Runs

        mockMvc.perform(delete("/api/categorias/1"))
            .andExpect(status().isNoContent())
    }

    @Test
    fun `DELETE categoria em uso deve devolver 409`() {
        every { service.excluir(1L) } throws ResponseStatusException(HttpStatus.CONFLICT, "Categoria em uso")

        mockMvc.perform(delete("/api/categorias/1"))
            .andExpect(status().isConflict())
    }
}
