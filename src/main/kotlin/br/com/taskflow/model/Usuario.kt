package br.com.taskflow.model

import jakarta.persistence.*

@Entity
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var nome: String,

    // E-mail é o "login" do usuário, por isso não pode se repetir.
    @Column(unique = true, nullable = false)
    val email: String,

    // Guarda o HASH da senha (BCrypt), nunca a senha em texto puro.
    @Column(nullable = false)
    val senha: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val perfil: Perfil = Perfil.USER
)
