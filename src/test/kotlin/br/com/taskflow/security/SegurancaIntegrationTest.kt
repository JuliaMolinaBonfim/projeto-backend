package br.com.taskflow.security

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.web.FilterChainProxy
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import kotlin.test.assertFalse

// Teste de INTEGRAÇÃO: sobe a aplicação inteira (com banco H2 e filtros de segurança)
// e confere as regras de acesso de ponta a ponta usando tokens JWT de verdade.
@SpringBootTest
class SegurancaIntegrationTest {

    @Autowired
    lateinit var contexto: WebApplicationContext

    @Autowired
    lateinit var filtroSeguranca: FilterChainProxy

    private lateinit var mockMvc: MockMvc

    // Credenciais do ADMIN criado pelo AdminInitializer (valores padrão do application.yaml)
    private val adminEmail = "admin@taskflow.com"
    private val adminSenha = "Admin@123"

    @BeforeEach
    fun preparar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto)
            .addFilters<DefaultMockMvcBuilder>(filtroSeguranca)
            .build()
    }

    // ---------- funções auxiliares ----------

    private fun registrar(nome: String, email: String, senha: String) {
        mockMvc.perform(
            post("/api/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"$nome","email":"$email","senha":"$senha"}""")
        ).andExpect(status().isCreated())
    }

    private fun login(email: String, senha: String): String {
        val corpo = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","senha":"$senha"}""")
        )
            .andExpect(status().isOk())
            .andReturn().response.contentAsString

        return Regex("\"token\":\"([^\"]+)\"").find(corpo)!!.groupValues[1]
    }

    private fun tokenDeUsuarioComum(email: String): String {
        registrar("Usuario Teste", email, "senha12345")
        return login(email, "senha12345")
    }

    private fun tokenDeAdmin(): String = login(adminEmail, adminSenha)

    private fun extrairId(json: String): Long {
        return Regex("\"id\":(\\d+)").find(json)!!.groupValues[1].toLong()
    }

    private fun postAutenticado(url: String, token: String, corpo: String) =
        mockMvc.perform(
            post(url)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo)
        )

    // ---------- testes ----------

    @Test
    fun `requisicao sem token deve receber 401`() {
        mockMvc.perform(get("/api/projetos"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    fun `token invalido deve receber 401`() {
        mockMvc.perform(get("/api/projetos").header("Authorization", "Bearer isto.nao.e-um-token"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    fun `login com senha errada deve receber 401`() {
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$adminEmail","senha":"senha-errada"}""")
        ).andExpect(status().isUnauthorized())
    }

    @Test
    fun `cadastro com e-mail repetido deve receber 409`() {
        registrar("Bia", "bia@email.com", "senha12345")

        mockMvc.perform(
            post("/api/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"Bia 2","email":"BIA@email.com","senha":"senha12345"}""")
        ).andExpect(status().isConflict())
    }

    @Test
    fun `admin com token valido consegue listar projetos`() {
        mockMvc.perform(get("/api/projetos").header("Authorization", "Bearer ${tokenDeAdmin()}"))
            .andExpect(status().isOk())
    }

    @Test
    fun `usuario comum consegue consultar mas nao consegue excluir`() {
        val token = tokenDeUsuarioComum("carlos@email.com")

        mockMvc.perform(get("/api/projetos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk())

        mockMvc.perform(delete("/api/projetos/1").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden())
    }

    @Test
    fun `usuario comum nao pode listar usuarios mas admin pode`() {
        val tokenUsuario = tokenDeUsuarioComum("diana@email.com")

        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer $tokenUsuario"))
            .andExpect(status().isForbidden())

        val corpo = mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer ${tokenDeAdmin()}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$[0].email").exists())
            .andReturn().response.contentAsString

        // A senha (nem o hash) jamais pode aparecer na resposta
        assertFalse(corpo.contains("senha"))
        assertFalse(corpo.contains("\$2a\$"))
    }

    @Test
    fun `admin passa pela seguranca no DELETE e recebe 404 para id inexistente`() {
        mockMvc.perform(delete("/api/projetos/99999").header("Authorization", "Bearer ${tokenDeAdmin()}"))
            .andExpect(status().isNotFound())
    }

    @Test
    fun `fluxo completo com DTOs - criar projeto tarefa categoria comentario e excluir`() {
        val usuario = tokenDeUsuarioComum("eva@email.com")
        val admin = tokenDeAdmin()

        // cria projeto
        val projetoJson = postAutenticado(
            "/api/projetos", usuario, """{"nome":"TaskFlow","descricao":"Sistema","prazo":"2026-12-15"}"""
        ).andExpect(status().isCreated()).andReturn().response.contentAsString
        val projetoId = extrairId(projetoJson)

        // cria categoria
        val categoriaJson = postAutenticado("/api/categorias", usuario, """{"nome":"Desenvolvimento"}""")
            .andExpect(status().isCreated()).andReturn().response.contentAsString
        val categoriaId = extrairId(categoriaJson)

        // cria tarefa ligada ao projeto e à categoria
        val tarefaJson = postAutenticado(
            "/api/tarefas", usuario,
            """{"titulo":"Criar tela","descricao":"desc","status":"PENDENTE","projetoId":$projetoId,"categoriaIds":[$categoriaId]}"""
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("\$.projetoId").value(projetoId))
            .andExpect(jsonPath("\$.categorias[0].nome").value("Desenvolvimento"))
            .andReturn().response.contentAsString
        val tarefaId = extrairId(tarefaJson)

        // comenta: o autor vem do token, não do corpo da requisição
        postAutenticado("/api/tarefas/$tarefaId/comentarios", usuario, """{"texto":"Comecei a tarefa"}""")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("\$.autorNome").value("Usuario Teste"))

        // usuário comum não exclui nada
        mockMvc.perform(delete("/api/tarefas/$tarefaId").header("Authorization", "Bearer $usuario"))
            .andExpect(status().isForbidden())

        // regras de integridade: projeto com tarefa e categoria em uso => 409
        mockMvc.perform(delete("/api/projetos/$projetoId").header("Authorization", "Bearer $admin"))
            .andExpect(status().isConflict())
        mockMvc.perform(delete("/api/categorias/$categoriaId").header("Authorization", "Bearer $admin"))
            .andExpect(status().isConflict())

        // admin exclui a tarefa (e os comentários dela), depois o projeto e a categoria
        mockMvc.perform(delete("/api/tarefas/$tarefaId").header("Authorization", "Bearer $admin"))
            .andExpect(status().isNoContent())
        mockMvc.perform(delete("/api/projetos/$projetoId").header("Authorization", "Bearer $admin"))
            .andExpect(status().isNoContent())
        mockMvc.perform(delete("/api/categorias/$categoriaId").header("Authorization", "Bearer $admin"))
            .andExpect(status().isNoContent())

    }
}
