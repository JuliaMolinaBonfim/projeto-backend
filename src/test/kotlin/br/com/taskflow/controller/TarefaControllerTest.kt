package br.com.taskflow.controller

import br.com.taskflow.dto.CategoriaResponse
import br.com.taskflow.dto.TarefaRequest
import br.com.taskflow.dto.TarefaResponse
import br.com.taskflow.service.TarefaService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.server.ResponseStatusException

class TarefaControllerTest {

    private val service = mockk<TarefaService>()

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(TarefaController(service))
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    private val tarefaJson =
        """{"titulo":"Criar tela","descricao":"desc","status":"PENDENTE","projetoId":1,"categoriaIds":[5]}"""
    private val requisicao = TarefaRequest("Criar tela", "desc", "PENDENTE", 1L, listOf(5L))
    private val resposta = TarefaResponse(
        10L, "Criar tela", "desc", "PENDENTE", 1L, listOf(CategoriaResponse(5L, "Desenvolvimento"))
    )

    @Test
    fun `POST tarefas deve devolver 201 e a tarefa criada`() {
        every { service.criar(requisicao) } returns resposta

        mockMvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content(tarefaJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("\$.id").value(10))
            .andExpect(jsonPath("\$.projetoId").value(1))
            .andExpect(jsonPath("\$.categorias[0].nome").value("Desenvolvimento"))
    }

    @Test
    fun `POST tarefas com titulo em branco deve devolver 400`() {
        val invalido = """{"titulo":"","descricao":"desc","status":"PENDENTE","projetoId":1}"""

        mockMvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content(invalido))
            .andExpect(status().isBadRequest())

        verify(exactly = 0) { service.criar(any()) }
    }

    @Test
    fun `POST tarefas sem projetoId valido deve devolver 400`() {
        val invalido = """{"titulo":"Criar tela","descricao":"desc","status":"PENDENTE","projetoId":0}"""

        mockMvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content(invalido))
            .andExpect(status().isBadRequest())
    }

    @Test
    fun `GET tarefas deve repassar filtros e ordenacao para o service`() {
        every { service.listar("PENDENTE", "Criar", "titulo", "desc") } returns listOf(resposta)

        mockMvc.perform(
            get("/api/tarefas")
                .param("status", "PENDENTE")
                .param("titulo", "Criar")
                .param("ordenarPor", "titulo")
                .param("direcao", "desc")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$[0].titulo").value("Criar tela"))
    }

    @Test
    fun `GET tarefas sem parametros deve usar ordenacao padrao por id crescente`() {
        every { service.listar(null, null, "id", "asc") } returns listOf(resposta)

        mockMvc.perform(get("/api/tarefas"))
            .andExpect(status().isOk())

        verify(exactly = 1) { service.listar(null, null, "id", "asc") }
    }

    @Test
    fun `GET tarefas com ordenacao invalida deve devolver 400`() {
        every { service.listar(null, null, "senha", "asc") } throws
            ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo de ordenacao invalido")

        mockMvc.perform(get("/api/tarefas").param("ordenarPor", "senha"))
            .andExpect(status().isBadRequest())
    }

    @Test
    fun `GET tarefa por id inexistente deve devolver 404`() {
        every { service.buscarPorId(99L) } throws ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa nao encontrada")

        mockMvc.perform(get("/api/tarefas/99"))
            .andExpect(status().isNotFound())
    }

    @Test
    fun `PUT tarefas deve devolver a tarefa atualizada`() {
        every { service.atualizar(10L, requisicao) } returns resposta

        mockMvc.perform(put("/api/tarefas/10").contentType(MediaType.APPLICATION_JSON).content(tarefaJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.id").value(10))
    }

    @Test
    fun `DELETE tarefas deve devolver 204`() {
        every { service.excluir(10L) } just Runs

        mockMvc.perform(delete("/api/tarefas/10"))
            .andExpect(status().isNoContent())
    }

    @Test
    fun `POST associar categoria deve devolver a tarefa com a categoria`() {
        every { service.adicionarCategoria(10L, 5L) } returns resposta

        mockMvc.perform(post("/api/tarefas/10/categorias/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.categorias[0].id").value(5))
    }

    @Test
    fun `POST associar categoria repetida deve devolver 409`() {
        every { service.adicionarCategoria(10L, 5L) } throws
            ResponseStatusException(HttpStatus.CONFLICT, "A tarefa ja possui esta categoria")

        mockMvc.perform(post("/api/tarefas/10/categorias/5"))
            .andExpect(status().isConflict())
    }

    @Test
    fun `DELETE categoria da tarefa deve devolver a tarefa sem a categoria`() {
        every { service.removerCategoria(10L, 5L) } returns resposta.copy(categorias = emptyList())

        mockMvc.perform(delete("/api/tarefas/10/categorias/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.categorias").isEmpty())
    }
}
