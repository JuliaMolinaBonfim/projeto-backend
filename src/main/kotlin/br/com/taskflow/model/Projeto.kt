
package br.com.taskflow.model

import jakarta.persistence.*

@Entity
class Projeto(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var nome: String,

    var descricao: String,

    var prazo: String,

    // Um projeto pode possuir várias tarefas.
    @OneToMany(mappedBy = "projeto", fetch = FetchType.LAZY)
    val tarefas: MutableList<Tarefa> = mutableListOf()
)
