package br.com.taskflow.config

import br.com.taskflow.model.Perfil
import br.com.taskflow.model.Usuario
import br.com.taskflow.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

// Roda uma vez quando a aplicação sobe e cria o usuário ADMIN inicial (o banco H2 começa vazio).
@Component
class AdminInitializer(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.admin.email}") private val adminEmail: String,
    @Value("\${app.admin.senha}") private val adminSenha: String
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(AdminInitializer::class.java)

    override fun run(args: ApplicationArguments) {
        val email = adminEmail.trim().lowercase()

        if (usuarioRepository.existsByEmail(email)) {
            return
        }

        val hash = requireNotNull(passwordEncoder.encode(adminSenha)) { "Não foi possível gerar o hash da senha" }
        usuarioRepository.save(
            Usuario(nome = "Administrador", email = email, senha = hash, perfil = Perfil.ADMIN)
        )
        logger.info("Usuário administrador criado: {}", email)
    }
}
