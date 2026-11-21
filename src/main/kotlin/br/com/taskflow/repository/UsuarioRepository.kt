
package br.com.taskflow.repository

import br.com.taskflow.model.Usuario
import org.springframework.data.jpa.repository.JpaRepository

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByEmail(email: String): Usuario?
    fun existsByEmail(email: String): Boolean
}
