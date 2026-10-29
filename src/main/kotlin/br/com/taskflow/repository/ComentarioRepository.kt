
package br.com.taskflow.repository

import br.com.taskflow.model.Comentario
import org.springframework.data.jpa.repository.JpaRepository

interface ComentarioRepository : JpaRepository<Comentario, Long> {
    fun findByTarefaId(tarefaId: Long): List<Comentario>
}
