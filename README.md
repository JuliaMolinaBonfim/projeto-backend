# TaskFlow API

## Sobre o projeto

O **TaskFlow** é uma API REST desenvolvida para gerenciar projetos e tarefas. O sistema permite cadastrar projetos, criar tarefas, atribuir categorias, cadastrar usuários e registrar comentários relacionados às tarefas.

O objetivo do projeto é organizar atividades de forma estruturada, permitindo acompanhar tarefas vinculadas a projetos, seus status e suas categorias.

Este projeto foi desenvolvido como atividade acadêmica da disciplina de Back-End Web Services.

## Integrantes do grupo

- Edoarda Cenci Pirai
- Fernanda Rodrigues Pinho
- Isabela Louise da Costa Silva
- Julia Molina Bonfim

## Link para o vídeo

https://youtu.be/e-vNAVJ4CBw?is=r4l3WhRMm970d_-8


## Tecnologias utilizadas

- **Kotlin:** linguagem utilizada no desenvolvimento da aplicação.
- **Spring Boot 4:** framework utilizado para estruturar e executar a API.
- **Spring Data JPA:** responsável pela persistência e pelo mapeamento das entidades.
- **Spring Security + JWT:** autenticação por token (OAuth2 Resource Server com Nimbus) e autorização por perfil.
- **Bean Validation:** validação dos dados recebidos nas requisições.
- **H2 Database:** banco de dados utilizado durante a execução.
- **JUnit 5 + MockK + MockMvc:** testes unitários e de integração.
- **Gradle:** ferramenta de gerenciamento e construção do projeto.
- **Java 21:** plataforma utilizada para executar a aplicação.

## Arquitetura

O projeto é organizado em camadas:

```text
controller  ->  service  ->  repository  ->  banco (H2)
    |              |
   DTOs         regras de negócio + logs (SLF4J)
```

- **controller:** recebe as requisições HTTP, valida o corpo (`@Valid`) e delega ao service. Nunca acessa repositórios.
- **service:** concentra as regras de negócio, os logs e a conversão entre entidade e DTO.
- **repository:** acesso ao banco de dados (Spring Data JPA).
- **model:** entidades JPA. **Não são expostas na API.**
- **dto:** objetos de entrada (`*Request`) e de saída (`*Response`).
- **config:** configuração de segurança (JWT) e criação do administrador inicial.

### DTOs

Os controllers **não recebem nem devolvem entidades JPA**. Cada recurso possui DTOs próprios:

| Recurso | Entrada | Saída |
|---|---|---|
| Autenticação | `LoginRequest`, `RegistroRequest` | `TokenResponse`, `UsuarioResponse` |
| Projeto | `ProjetoRequest` | `ProjetoResponse` |
| Tarefa | `TarefaRequest` | `TarefaResponse` (inclui `projetoId` e lista de `CategoriaResponse`) |
| Categoria | `CategoriaRequest` | `CategoriaResponse` |
| Comentário | `ComentarioRequest` | `ComentarioResponse` |

Benefícios: a senha (hash) do usuário nunca é devolvida, o cliente não consegue alterar campos internos (como `id`) e a API fica independente do modelo do banco.

## Entidades

O sistema possui cinco entidades principais:

- **Projeto:** representa um projeto que pode conter várias tarefas.
- **Tarefa:** representa uma atividade vinculada a um projeto e que pode possuir várias categorias.
- **Usuario:** representa um usuário do sistema (com e-mail, senha criptografada e perfil `USER` ou `ADMIN`).
- **Categoria:** organiza as tarefas por assunto.
- **Comentario:** registra comentários feitos por usuários em tarefas.

## Relacionamentos

- **OneToMany — Projeto e Tarefa:** um projeto pode possuir várias tarefas.
- **ManyToOne — Tarefa e Projeto:** várias tarefas podem pertencer ao mesmo projeto.
- **ManyToMany — Tarefa e Categoria:** uma tarefa pode possuir várias categorias, e uma categoria pode estar associada a várias tarefas.
- **ManyToOne — Comentario e Tarefa:** vários comentários podem pertencer à mesma tarefa.
- **ManyToOne — Comentario e Usuario:** vários comentários podem ser feitos pelo mesmo usuário.

O relacionamento entre tarefas e categorias utiliza a tabela intermediária `tarefa_categoria`.

## Autenticação e segurança (JWT)

A API usa **JWT (JSON Web Token)**. O fluxo é:

1. O usuário faz login em `POST /api/auth/login` e recebe um token.
2. Em todas as demais requisições, envia o token no cabeçalho `Authorization: Bearer <token>`.
3. A API valida a assinatura e a validade do token a cada requisição (sem sessão no servidor).

### Regras de acesso

| Rota | Quem acessa |
|---|---|
| `POST /api/auth/registrar` e `POST /api/auth/login` | Público |
| `GET`, `POST`, `PUT` em `/api/projetos`, `/api/tarefas`, `/api/categorias` e comentários | Usuário autenticado (`USER` ou `ADMIN`) |
| Qualquer `DELETE` em `/api/**` | Somente `ADMIN` |
| `GET /api/usuarios` | Somente `ADMIN` (expõe e-mails) |
| Qualquer outra rota | Exige autenticação (negado por padrão) |

Respostas de erro de segurança:

- **401 Unauthorized:** token ausente, inválido ou expirado (ou login com credenciais erradas).
- **403 Forbidden:** token válido, mas o perfil não tem permissão (ex.: `USER` tentando `DELETE`).

### Decisões de segurança adotadas

- **Senhas com BCrypt:** o banco guarda apenas o hash; a senha nunca é devolvida pela API.
- **Usuários no banco de dados** (e não em memória), com e-mail único.
- **Cadastro público cria sempre perfil `USER`.** O perfil `ADMIN` só existe para o administrador inicial.
- **Autor do comentário vem do token**, não do corpo da requisição: ninguém comenta em nome de outra pessoa.
- **Mensagem de login genérica** ("E-mail ou senha inválidos"), sem revelar se o e-mail existe.
- **Sessão stateless e CSRF desativado:** adequado para API que autentica por token no cabeçalho.
- **Token com expiração** (1 hora por padrão) e **chave secreta configurável** por variável de ambiente (`JWT_SECRET`).
- **Validação de entrada** (Bean Validation) em todos os DTOs de entrada.

### Administrador inicial (apenas para demonstração local)

Ao iniciar, a aplicação cria automaticamente um administrador (o banco H2 começa vazio):

- **E-mail:** `admin@taskflow.com`
- **Senha:** `Admin@123`

Os valores podem ser trocados pelas variáveis de ambiente `ADMIN_EMAIL` e `ADMIN_SENHA`, e a chave do JWT por `JWT_SECRET` (mínimo 32 caracteres). Os valores padrão existem apenas para fins acadêmicos e **não devem ser usados em produção**.

## Funcionalidades

- Cadastro de usuário e login com JWT.
- CRUD completo de projetos.
- CRUD completo de tarefas.
- CRUD de categorias.
- Listagem de usuários (ADMIN).
- Criação e consulta de comentários.
- Consulta das tarefas de um projeto.
- Associação e remoção de categorias nas tarefas.
- Filtros de tarefas por título e status.
- Ordenação de tarefas.
- Autorização por perfil (`DELETE` somente para `ADMIN`).
- Logs de operações importantes (SLF4J) nos services.
- Tratamento de erros padronizado (JSON) para 400, 401, 403, 404 e 409.

## Endpoints

### Autenticação

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/auth/registrar` | Cadastrar usuário (perfil USER) | Público |
| POST | `/api/auth/login` | Fazer login e obter o token | Público |

### Projetos

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/projetos` | Criar projeto | Autenticado |
| GET | `/api/projetos` | Listar projetos | Autenticado |
| GET | `/api/projetos/{id}` | Buscar projeto por ID | Autenticado |
| PUT | `/api/projetos/{id}` | Atualizar projeto | Autenticado |
| DELETE | `/api/projetos/{id}` | Excluir projeto (409 se tiver tarefas) | ADMIN |
| GET | `/api/projetos/{id}/tarefas` | Listar tarefas de um projeto | Autenticado |

### Tarefas

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/tarefas` | Criar tarefa | Autenticado |
| GET | `/api/tarefas` | Listar tarefas (com filtros e ordenação) | Autenticado |
| GET | `/api/tarefas/{id}` | Buscar tarefa por ID | Autenticado |
| PUT | `/api/tarefas/{id}` | Atualizar tarefa | Autenticado |
| DELETE | `/api/tarefas/{id}` | Excluir tarefa (e seus comentários) | ADMIN |

### Usuários

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/usuarios` | Listar usuários | ADMIN |

### Categorias

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/categorias` | Cadastrar categoria | Autenticado |
| GET | `/api/categorias` | Listar categorias | Autenticado |
| GET | `/api/categorias/{id}` | Buscar categoria por ID | Autenticado |
| PUT | `/api/categorias/{id}` | Atualizar categoria | Autenticado |
| DELETE | `/api/categorias/{id}` | Excluir categoria (409 se estiver em uso) | ADMIN |

### Comentários

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/tarefas/{tarefaId}/comentarios` | Adicionar comentário (autor = usuário logado) | Autenticado |
| GET | `/api/tarefas/{tarefaId}/comentarios` | Listar comentários da tarefa | Autenticado |

### Associação entre tarefas e categorias

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/tarefas/{tarefaId}/categorias/{categoriaId}` | Associar categoria à tarefa | Autenticado |
| DELETE | `/api/tarefas/{tarefaId}/categorias/{categoriaId}` | Remover categoria da tarefa | ADMIN |

## Filtros e ordenação

A listagem de tarefas aceita parâmetros na URL:

```text
GET /api/tarefas?status=PENDENTE
GET /api/tarefas?titulo=Criar
GET /api/tarefas?ordenarPor=titulo&direcao=asc
GET /api/tarefas?ordenarPor=titulo&direcao=desc
GET /api/tarefas?status=PENDENTE&ordenarPor=titulo&direcao=asc
```

Campos permitidos para ordenação: `id`, `titulo` e `status`. Valores inválidos retornam `400 Bad Request`.

## Logs e tratamento de erros

Todos os services (`ProjetoService`, `TarefaService`, `CategoriaService`, `ComentarioService`, `UsuarioService` e `AuthService`) registram operações importantes com SLF4J (`info` para criações/atualizações/logins e `warn` para exclusões, falhas e recursos não encontrados).

Os erros são tratados de forma centralizada pela classe `GlobalExceptionHandler` e sempre devolvem JSON no formato:

```json
{
  "status": 404,
  "mensagem": "Projeto não encontrado",
  "detalhes": [],
  "timestamp": "2026-11-21T14:00:00Z"
}
```

Erros de validação (`400`) listam cada campo inválido em `detalhes`.

## Como executar o projeto

### Pré-requisitos

- Java 21 instalado.
- IntelliJ IDEA ou outra IDE compatível com Kotlin.
- Git (opcional).

### Executando a aplicação

Na pasta principal do projeto, abra o terminal e execute:

**Windows:**

```powershell
.\gradlew bootRun
```

**Linux / macOS:**

```bash
./gradlew bootRun
```

Após a inicialização, a API estará disponível em `http://localhost:8080`.

### Banco de dados

O projeto utiliza o banco de dados H2 em memória. Os dados ficam disponíveis durante a execução, mas são perdidos quando o servidor é reiniciado (o administrador inicial é recriado a cada inicialização).

## Executando os testes

```powershell
.\gradlew test
```

O relatório HTML fica em `build/reports/tests/test/index.html`.

Os testes estão organizados em:

- **Testes unitários de services** (`src/test/.../service`): usam MockK para simular os repositórios e verificam regras de negócio (404, 409, filtros, ordenação, associação de categorias, login e cadastro).
- **Testes unitários de controllers** (`src/test/.../controller`): usam MockMvc em modo *standalone* com o service simulado e verificam status HTTP, JSON de resposta e validação dos DTOs.
- **Teste de integração de segurança** (`src/test/.../security`): sobe a aplicação completa e confere, com tokens JWT reais, os cenários 401, 403, 409 e o fluxo completo usando DTOs.

## Exemplos de uso

### 1. Fazer login (administrador inicial)

**POST** `/api/auth/login`

```json
{
  "email": "admin@taskflow.com",
  "senha": "Admin@123"
}
```

Resposta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9....",
  "tipo": "Bearer",
  "expiraEmSegundos": 3600
}
```

Nas próximas requisições, envie o cabeçalho:

```text
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 2. Cadastrar um usuário comum

**POST** `/api/auth/registrar`

```json
{
  "nome": "Julia",
  "email": "julia@email.com",
  "senha": "senha12345"
}
```

### 3. Criar um projeto

**POST** `/api/projetos`

```json
{
  "nome": "Projeto TaskFlow",
  "descricao": "Sistema de gerenciamento de projetos e tarefas",
  "prazo": "2026-12-15"
}
```

### 4. Criar uma categoria

**POST** `/api/categorias`

```json
{
  "nome": "Desenvolvimento"
}
```

### 5. Criar uma tarefa

**POST** `/api/tarefas`

```json
{
  "titulo": "Desenvolver tela inicial",
  "descricao": "Criar a interface inicial do TaskFlow",
  "status": "PENDENTE",
  "projetoId": 1,
  "categoriaIds": [1]
}
```

### 6. Adicionar um comentário

**POST** `/api/tarefas/1/comentarios`

```json
{
  "texto": "A implementação da tarefa foi iniciada."
}
```

O autor do comentário é o usuário dono do token enviado.

### 7. Excluir (somente ADMIN)

**DELETE** `/api/tarefas/1` com o token do administrador retorna `204 No Content`. Com o token de um usuário comum, retorna `403 Forbidden`; sem token, `401 Unauthorized`.

## Status do projeto

API desenvolvida para apresentação acadêmica, com funcionalidades de gerenciamento de projetos, tarefas, usuários, categorias e comentários, autenticação JWT, autorização por perfil, DTOs e testes automatizados.
