
package br.com.taskflow.model

import jakarta.persistence.*

@Entity
class Comentario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val texto: String,

    @ManyToOne
    @JoinColumn(name = "tarefa_id")
    val tarefa: Tarefa,

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    val autor: Usuario
)
