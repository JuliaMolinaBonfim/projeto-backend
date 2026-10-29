
package br.com.taskflow.repository

import br.com.taskflow.model.Tarefa
import org.springframework.data.jpa.repository.JpaRepository

interface TarefaRepository : JpaRepository<Tarefa, Long> {
    fun findByProjetoId(projetoId: Long): List<Tarefa>
}
