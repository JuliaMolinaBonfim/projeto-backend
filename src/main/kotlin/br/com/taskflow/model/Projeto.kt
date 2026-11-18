
package br.com.taskflow.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
class Projeto(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val nome: String,

    val descricao: String,

    val prazo: String,

    // Um projeto pode possuir várias tarefas.
    @OneToMany(mappedBy = "projeto", fetch = FetchType.LAZY)
    @get:JsonIgnore
    val tarefas: MutableList<Tarefa> = mutableListOf()
)
