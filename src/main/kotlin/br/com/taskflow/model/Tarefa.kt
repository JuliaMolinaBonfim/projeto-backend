
package br.com.taskflow.model

import jakarta.persistence.*

@Entity
class Tarefa(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val titulo: String,

    val descricao: String,

    val status: String = "PENDENTE",

    @ManyToOne
    @JoinColumn(name = "projeto_id")
    val projeto: Projeto,

    @ManyToMany
    @JoinTable(
        name = "tarefa_categoria",
        joinColumns = [JoinColumn(name = "tarefa_id")],
        inverseJoinColumns = [JoinColumn(name = "categoria_id")]
    )
    val categorias: MutableSet<Categoria> = mutableSetOf()
)
