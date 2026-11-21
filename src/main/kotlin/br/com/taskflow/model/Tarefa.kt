package br.com.taskflow.model

import jakarta.persistence.*

@Entity
class Tarefa(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var titulo: String,

    var descricao: String,

    var status: String = "PENDENTE",

    @ManyToOne
    @JoinColumn(name = "projeto_id")
    var projeto: Projeto,

    @ManyToMany
    @JoinTable(
        name = "tarefa_categoria",
        joinColumns = [JoinColumn(name = "tarefa_id")],
        inverseJoinColumns = [JoinColumn(name = "categoria_id")]
    )
    val categorias: MutableSet<Categoria> = mutableSetOf()
)
