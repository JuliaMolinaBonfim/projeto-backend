package br.com.taskflow.service

import br.com.taskflow.dto.LoginRequest
import br.com.taskflow.dto.RegistroRequest
import br.com.taskflow.model.Perfil
import br.com.taskflow.model.Usuario
import br.com.taskflow.repository.UsuarioRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthServiceTest {

    private val usuarioRepository = mockk<UsuarioRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val jwtEncoder = mockk<JwtEncoder>()
    private val service = AuthService(usuarioRepository, passwordEncoder, jwtEncoder, 3600L)

    private val jwtFalso: Jwt = Jwt.withTokenValue("token-de-teste")
        .header("alg", "HS256")
        .subject("qualquer")
        .build()

    @Test
    fun `registrar deve salvar usuario comum com senha criptografada e e-mail em minusculo`() {
        every { usuarioRepository.existsByEmail("ana@email.com") } returns false
        every { passwordEncoder.encode("senha12345") } returns "HASH-DA-SENHA"
        val salvo = slot<Usuario>()
        every { usuarioRepository.save(capture(salvo)) } answers {
            val u = salvo.captured
            Usuario(id = 1L, nome = u.nome, email = u.email, senha = u.senha, perfil = u.perfil)
        }

        val resposta = service.registrar(RegistroRequest("Ana", "  ANA@email.com ", "senha12345"))

        assertEquals("HASH-DA-SENHA", salvo.captured.senha)
        assertEquals("ana@email.com", salvo.captured.email)
        assertEquals(Perfil.USER, salvo.captured.perfil)
        assertEquals("USER", resposta.perfil)
        assertEquals(1L, resposta.id)
    }

    @Test
    fun `registrar deve lancar 409 quando o e-mail ja existe`() {
        every { usuarioRepository.existsByEmail("ana@email.com") } returns true

        val erro = assertFailsWith<ResponseStatusException> {
            service.registrar(RegistroRequest("Ana", "ana@email.com", "senha12345"))
        }

        assertEquals(HttpStatus.CONFLICT, erro.statusCode)
        verify(exactly = 0) { usuarioRepository.save(any<Usuario>()) }
    }

    @Test
    fun `login deve gerar token com e-mail e perfil do usuario`() {
        val admin = Usuario(id = 1L, nome = "Admin", email = "admin@taskflow.com", senha = "HASH", perfil = Perfil.ADMIN)
        every { usuarioRepository.findByEmail("admin@taskflow.com") } returns admin
        every { passwordEncoder.matches("Admin@123", "HASH") } returns true
        val parametros = slot<JwtEncoderParameters>()
        every { jwtEncoder.encode(capture(parametros)) } returns jwtFalso

        val resposta = service.login(LoginRequest("admin@taskflow.com", "Admin@123"))

        assertEquals("token-de-teste", resposta.token)
        assertEquals("Bearer", resposta.tipo)
        assertEquals(3600L, resposta.expiraEmSegundos)
        assertEquals("admin@taskflow.com", parametros.captured.claims.subject)
        assertEquals(listOf("ADMIN"), parametros.captured.claims.getClaim<List<String>>("roles"))
    }

    @Test
    fun `login deve lancar 401 quando a senha esta errada`() {
        val ana = Usuario(id = 2L, nome = "Ana", email = "ana@email.com", senha = "HASH", perfil = Perfil.USER)
        every { usuarioRepository.findByEmail("ana@email.com") } returns ana
        every { passwordEncoder.matches("errada", "HASH") } returns false

        val erro = assertFailsWith<ResponseStatusException> {
            service.login(LoginRequest("ana@email.com", "errada"))
        }

        assertEquals(HttpStatus.UNAUTHORIZED, erro.statusCode)
        verify(exactly = 0) { jwtEncoder.encode(any()) }
    }

    @Test
    fun `login deve lancar 401 quando o e-mail nao existe`() {
        every { usuarioRepository.findByEmail("ninguem@email.com") } returns null

        val erro = assertFailsWith<ResponseStatusException> {
            service.login(LoginRequest("ninguem@email.com", "qualquer"))
        }

        assertEquals(HttpStatus.UNAUTHORIZED, erro.statusCode)
        verify(exactly = 0) { jwtEncoder.encode(any()) }
    }
}
