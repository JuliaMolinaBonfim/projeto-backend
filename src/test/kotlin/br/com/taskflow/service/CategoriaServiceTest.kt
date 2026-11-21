package br.com.taskflow.service

import br.com.taskflow.dto.CategoriaRequest
import br.com.taskflow.model.Categoria
import br.com.taskflow.repository.CategoriaRepository
import br.com.taskflow.repository.TarefaRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CategoriaServiceTest {

    private val categoriaRepository = mockk<CategoriaRepository>()
    private val tarefaRepository = mockk<TarefaRepository>()
    private val service = CategoriaService(categoriaRepository, tarefaRepository)

    private fun categoria(id: Long = 1L) = Categoria(id = id, nome = "Desenvolvimento")

    @Test
    fun `criar deve salvar e devolver a categoria`() {
        every { categoriaRepository.save(any<Categoria>()) } returns categoria(1L)

        val resposta = service.criar(CategoriaRequest("Desenvolvimento"))

        assertEquals(1L, resposta.id)
        assertEquals("Desenvolvimento", resposta.nome)
    }

    @Test
    fun `listar deve devolver todas as categorias`() {
        every { categoriaRepository.findAll() } returns listOf(categoria(1L), categoria(2L))

        assertEquals(2, service.listar().size)
    }

    @Test
    fun `buscarPorId deve lancar 404 quando nao existe`() {
        every { categoriaRepository.findById(99L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> { service.buscarPorId(99L) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
    }

    @Test
    fun `atualizar deve alterar o nome`() {
        every { categoriaRepository.findById(1L) } returns Optional.of(categoria(1L))
        every { categoriaRepository.save(any<Categoria>()) } answers { firstArg<Categoria>() }

        val resposta = service.atualizar(1L, CategoriaRequest("Testes"))

        assertEquals("Testes", resposta.nome)
    }

    @Test
    fun `excluir deve apagar a categoria quando nao esta em uso`() {
        val existente = categoria(1L)
        every { categoriaRepository.findById(1L) } returns Optional.of(existente)
        every { tarefaRepository.existsByCategoriasId(1L) } returns false
        every { categoriaRepository.delete(any<Categoria>()) } just Runs

        service.excluir(1L)

        verify(exactly = 1) { categoriaRepository.delete(existente) }
    }

    @Test
    fun `excluir deve lancar 409 quando a categoria esta em uso`() {
        every { categoriaRepository.findById(1L) } returns Optional.of(categoria(1L))
        every { tarefaRepository.existsByCategoriasId(1L) } returns true

        val erro = assertFailsWith<ResponseStatusException> { service.excluir(1L) }

        assertEquals(HttpStatus.CONFLICT, erro.statusCode)
        verify(exactly = 0) { categoriaRepository.delete(any<Categoria>()) }
    }

    @Test
    fun `excluir deve lancar 404 quando a categoria nao existe`() {
        every { categoriaRepository.findById(99L) } returns Optional.empty()

        val erro = assertFailsWith<ResponseStatusException> { service.excluir(99L) }

        assertEquals(HttpStatus.NOT_FOUND, erro.statusCode)
    }
}
