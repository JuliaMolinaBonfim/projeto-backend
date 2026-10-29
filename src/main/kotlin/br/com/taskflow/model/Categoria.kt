
package br.com.taskflow.model

import jakarta.persistence.*

@Entity
class Categoria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val nome: String
)
