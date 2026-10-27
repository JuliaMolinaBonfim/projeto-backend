package br.com.taskflow.repository

import br.com.taskflow.model.Projeto
import org.springframework.data.jpa.repository.JpaRepository

interface ProjetoRepository : JpaRepository<Projeto, Long>
