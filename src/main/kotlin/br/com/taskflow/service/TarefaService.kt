package br.com.taskflow.service

import br.com.taskflow.dto.TarefaRequest
import br.com.taskflow.dto.TarefaResponse
import br.com.taskflow.dto.toResponse
import br.com.taskflow.model.Categoria
import br.com.taskflow.model.Projeto
import br.com.taskflow.model.Tarefa
import br.com.taskflow.repository.CategoriaRepository
import br.com.taskflow.repository.ComentarioRepository
import br.com.taskflow.repository.ProjetoRepository
import br.com.taskflow.repository.TarefaRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class TarefaService(
    private val tarefaRepository: TarefaRepository,
    private val projetoRepository: ProjetoRepository,
    private val categoriaRepository: CategoriaRepository,
    private val comentarioRepository: ComentarioRepository
) {
    private val logger = LoggerFactory.getLogger(TarefaService::class.java)

    fun criar(dados: TarefaRequest): TarefaResponse {
        val projeto = buscarProjeto(dados.projetoId)
        val categorias = buscarCategorias(dados.categoriaIds)

        logger.info("Criando tarefa: {}", dados.titulo)
        val tarefa = Tarefa(
            titulo = dados.titulo,
            descricao = dados.descricao,
            status = dados.status,
            projeto = projeto,
            categorias = categorias
        )
        return tarefaRepository.save(tarefa).toResponse()
    }

    @Transactional(readOnly = true)
    fun listar(
        status: String?,
        titulo: String?,
        ordenarPor: String,
        direcao: String
    ): List<TarefaResponse> {
        if (ordenarPor !in CAMPOS_ORDENACAO) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo de ordenação inválido")
        }

        if (direcao.lowercase() !in setOf("asc", "desc")) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Direção inválida. Use asc ou desc")
        }

        val ordenacao = Sort.by(
            if (direcao.equals("desc", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC,
            ordenarPor
        )

        return tarefaRepository.findAll(ordenacao)
            .filter { status == null || it.status.equals(status, ignoreCase = true) }
            .filter { titulo == null || it.titulo.contains(titulo, ignoreCase = true) }
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: Long): TarefaResponse {
        return buscarEntidade(id).toResponse()
    }

    fun atualizar(id: Long, dados: TarefaRequest): TarefaResponse {
        val tarefa = buscarEntidade(id)
        val projeto = buscarProjeto(dados.projetoId)
        val categorias = buscarCategorias(dados.categoriaIds)

        tarefa.titulo = dados.titulo
        tarefa.descricao = dados.descricao
        tarefa.status = dados.status
        tarefa.projeto = projeto
        tarefa.categorias.clear()
        tarefa.categorias.addAll(categorias)

        logger.info("Atualizando tarefa: {}", id)
        return tarefaRepository.save(tarefa).toResponse()
    }

    fun excluir(id: Long) {
        val tarefa = buscarEntidade(id)
        logger.warn("Excluindo tarefa: {} (e seus comentários)", id)
        comentarioRepository.deleteByTarefaId(id)
        tarefaRepository.delete(tarefa)
    }

    // Associa uma categoria já existente a uma tarefa já existente
    fun adicionarCategoria(tarefaId: Long, categoriaId: Long): TarefaResponse {
        val tarefa = buscarEntidade(tarefaId)

        val categoria = categoriaRepository.findById(categoriaId).orElseThrow {
            logger.warn("Categoria não encontrada: {}", categoriaId)
            ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada")
        }

        if (tarefa.categorias.any { it.id == categoriaId }) {
            logger.warn("Tarefa {} já possui a categoria {}", tarefaId, categoriaId)
            throw ResponseStatusException(HttpStatus.CONFLICT, "A tarefa já possui esta categoria")
        }

        tarefa.categorias.add(categoria)
        logger.info("Categoria {} adicionada à tarefa {}", categoriaId, tarefaId)
        return tarefaRepository.save(tarefa).toResponse()
    }

    // Remove a associação entre a tarefa e a categoria (não apaga a categoria)
    fun removerCategoria(tarefaId: Long, categoriaId: Long): TarefaResponse {
        val tarefa = buscarEntidade(tarefaId)

        val removeu = tarefa.categorias.removeIf { it.id == categoriaId }
        if (!removeu) {
            logger.warn("Tarefa {} não possui a categoria {}", tarefaId, categoriaId)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "A tarefa não possui esta categoria")
        }

        logger.warn("Categoria {} removida da tarefa {}", categoriaId, tarefaId)
        return tarefaRepository.save(tarefa).toResponse()
    }

    // ----- métodos internos -----

    private fun buscarEntidade(id: Long): Tarefa {
        return tarefaRepository.findById(id).orElseThrow {
            logger.warn("Tarefa não encontrada: {}", id)
            ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")
        }
    }

    private fun buscarProjeto(projetoId: Long): Projeto {
        return projetoRepository.findById(projetoId).orElseThrow {
            logger.warn("Projeto não encontrado: {}", projetoId)
            ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado")
        }
    }

    private fun buscarCategorias(ids: List<Long>): MutableSet<Categoria> {
        return ids.distinct().map { categoriaId ->
            categoriaRepository.findById(categoriaId).orElseThrow {
                logger.warn("Categoria não encontrada: {}", categoriaId)
                ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada: $categoriaId")
            }
        }.toMutableSet()
    }

    companion object {
        private val CAMPOS_ORDENACAO = setOf("id", "titulo", "status")
    }
}
