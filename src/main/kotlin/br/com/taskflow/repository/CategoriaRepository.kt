
package br.com.taskflow.repository

import br.com.taskflow.model.Categoria
import org.springframework.data.jpa.repository.JpaRepository

interface CategoriaRepository : JpaRepository<Categoria, Long>
