package br.com.taskflow.service

import br.com.taskflow.dto.LoginRequest
import br.com.taskflow.dto.RegistroRequest
import br.com.taskflow.dto.TokenResponse
import br.com.taskflow.dto.UsuarioResponse
import br.com.taskflow.dto.toResponse
import br.com.taskflow.model.Perfil
import br.com.taskflow.model.Usuario
import br.com.taskflow.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
@Transactional
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtEncoder: JwtEncoder,
    @Value("\${app.jwt.expiracao-segundos}")
    private val expiracaoSegundos: Long
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    // Cadastro público: sempre cria um usuário comum (USER). Só o ADMIN inicial tem outro perfil.
    fun registrar(dados: RegistroRequest): UsuarioResponse {
        val email = normalizarEmail(dados.email)

        if (usuarioRepository.existsByEmail(email)) {
            logger.warn("Tentativa de cadastro com e-mail já existente: {}", email)
            throw ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com este e-mail")
        }

        val usuario = Usuario(
            nome = dados.nome,
            email = email,
            senha = gerarHash(dados.senha),
            perfil = Perfil.USER
        )

        logger.info("Cadastrando usuário: {}", email)
        return usuarioRepository.save(usuario).toResponse()
    }

    @Transactional(readOnly = true)
    fun login(dados: LoginRequest): TokenResponse {
        val email = normalizarEmail(dados.email)
        val usuario = usuarioRepository.findByEmail(email)

        // Mesma mensagem para "e-mail não existe" e "senha errada": não revela qual dos dois falhou.
        if (usuario == null || !passwordEncoder.matches(dados.senha, usuario.senha)) {
            logger.warn("Falha de login para o e-mail: {}", email)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos")
        }

        val agora = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer("taskflow-api")
            .issuedAt(agora)
            .expiresAt(agora.plusSeconds(expiracaoSegundos))
            .subject(usuario.email)
            .claim("roles", listOf(usuario.perfil.name))
            .build()

        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue

        logger.info("Login realizado com sucesso: {}", email)
        return TokenResponse(token = token, expiraEmSegundos = expiracaoSegundos)
    }

    private fun normalizarEmail(email: String) = email.trim().lowercase()

    private fun gerarHash(senha: String): String {
        return requireNotNull(passwordEncoder.encode(senha)) { "Não foi possível gerar o hash da senha" }
    }
}
