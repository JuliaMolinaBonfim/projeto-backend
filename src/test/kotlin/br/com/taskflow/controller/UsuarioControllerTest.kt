package br.com.taskflow.controller

import br.com.taskflow.dto.UsuarioResponse
import br.com.taskflow.service.UsuarioService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class UsuarioControllerTest {

    private val service = mockk<UsuarioService>()

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(UsuarioController(service))
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @Test
    fun `GET usuarios deve devolver a lista sem senha`() {
        every { service.listar() } returns listOf(
            UsuarioResponse(1L, "Admin", "admin@taskflow.com", "ADMIN"),
            UsuarioResponse(2L, "Ana", "ana@email.com", "USER")
        )

        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$[0].perfil").value("ADMIN"))
            .andExpect(jsonPath("\$[1].email").value("ana@email.com"))
            .andExpect(jsonPath("\$[0].senha").doesNotExist())
    }
}
