package br.com.taskflow.controller

import br.com.taskflow.dto.ErroResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

// Centraliza o tratamento de erros: toda falha sai no mesmo formato JSON (ErroResponse).
@RestControllerAdvice
class GlobalExceptionHandler {

    // Erros que os services lançam de propósito (404, 409, 400, 401...)
    @ExceptionHandler(ResponseStatusException::class)
    fun tratarStatus(e: ResponseStatusException): ResponseEntity<ErroResponse> {
        val status = e.statusCode.value()
        return ResponseEntity.status(status).body(ErroResponse(status, e.reason ?: "Erro na requisição"))
    }

    // Falha nas validações dos DTOs (@NotBlank, @Email, @Size...)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun tratarValidacao(e: MethodArgumentNotValidException): ResponseEntity<ErroResponse> {
        val detalhes = e.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        val corpo = ErroResponse(HttpStatus.BAD_REQUEST.value(), "Dados inválidos", detalhes)
        return ResponseEntity.badRequest().body(corpo)
    }

    // JSON malformado ou campo obrigatório ausente
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun tratarJsonInvalido(e: HttpMessageNotReadableException): ResponseEntity<ErroResponse> {
        val corpo = ErroResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Corpo da requisição inválido ou com campos obrigatórios ausentes"
        )
        return ResponseEntity.badRequest().body(corpo)
    }
}
