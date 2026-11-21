package br.com.taskflow.controller

import br.com.taskflow.dto.LoginRequest
import br.com.taskflow.dto.RegistroRequest
import br.com.taskflow.dto.TokenResponse
import br.com.taskflow.dto.UsuarioResponse
import br.com.taskflow.service.AuthService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.server.ResponseStatusException

class AuthControllerTest {

    private val service = mockk<AuthService>()

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(AuthController(service))
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @Test
    fun `POST registrar deve devolver 201 sem expor a senha`() {
        every { service.registrar(RegistroRequest("Ana", "ana@email.com", "senha12345")) } returns
            UsuarioResponse(1L, "Ana", "ana@email.com", "USER")

        mockMvc.perform(
            post("/api/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"Ana","email":"ana@email.com","senha":"senha12345"}""")
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("\$.email").value("ana@email.com"))
            .andExpect(jsonPath("\$.perfil").value("USER"))
            .andExpect(jsonPath("\$.senha").doesNotExist())
    }

    @Test
    fun `POST registrar com e-mail invalido deve devolver 400`() {
        mockMvc.perform(
            post("/api/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"Ana","email":"isso-nao-e-email","senha":"senha12345"}""")
        )
            .andExpect(status().isBadRequest())

        verify(exactly = 0) { service.registrar(any()) }
    }

    @Test
    fun `POST registrar com senha curta deve devolver 400`() {
        mockMvc.perform(
            post("/api/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"Ana","email":"ana@email.com","senha":"123"}""")
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    fun `POST login deve devolver o token`() {
        every { service.login(LoginRequest("ana@email.com", "senha12345")) } returns
            TokenResponse(token = "abc.def.ghi", expiraEmSegundos = 3600L)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"ana@email.com","senha":"senha12345"}""")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.token").value("abc.def.ghi"))
            .andExpect(jsonPath("\$.tipo").value("Bearer"))
            .andExpect(jsonPath("\$.expiraEmSegundos").value(3600))
    }

    @Test
    fun `POST login com credenciais erradas deve devolver 401`() {
        every { service.login(LoginRequest("ana@email.com", "errada")) } throws
            ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos")

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"ana@email.com","senha":"errada"}""")
        )
            .andExpect(status().isUnauthorized())
    }
}
