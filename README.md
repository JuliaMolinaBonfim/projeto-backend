# TaskFlow API

## Sobre o projeto

O **TaskFlow** é uma API REST desenvolvida para gerenciar projetos e tarefas. O sistema permite cadastrar projetos, criar tarefas, atribuir categorias e registrar comentários feitos por usuários.

Este projeto foi desenvolvido como atividade acadêmica da disciplina de Backend.

## Integrantes do grupo

- Edoarda Cenci Pirai
- Fernanda Rodrigues Pinho
- Isabela Louise da Costa Silva
- Julia Molina Bonfim

## Tecnologias utilizadas

- Kotlin
- Spring Boot 4
- Spring Data JPA
- Spring Security
- H2 Database
- Gradle
- Java 21

## Entidades

O sistema possui cinco entidades principais:

- **Projeto:** representa um projeto que contém tarefas.
- **Tarefa:** representa uma atividade vinculada a um projeto.
- **Usuario:** representa um usuário que pode registrar comentários.
- **Categoria:** organiza as tarefas por assunto.
- **Comentario:** registra comentários feitos por usuários em tarefas.

## Relacionamentos

O projeto utiliza diferentes tipos de relacionamento entre entidades:

- **ManyToOne:** várias tarefas podem pertencer ao mesmo projeto.
- **ManyToOne:** vários comentários podem pertencer à mesma tarefa.
- **ManyToOne:** vários comentários podem ser feitos pelo mesmo usuário.
- **ManyToMany:** uma tarefa pode ter várias categorias, e uma categoria pode estar associada a várias tarefas.

O relacionamento entre tarefas e categorias utiliza a tabela intermediária `tarefa_categoria`.

## Funcionalidades

- CRUD completo de projetos.
- CRUD completo de tarefas.
- Cadastro e listagem de usuários.
- Cadastro e listagem de categorias.
- Criação e consulta de comentários.
- Consulta das tarefas de um projeto.
- Filtros de tarefas por título e status.
- Ordenação de tarefas.
- Autenticação para operações de exclusão.
- Logs de operações importantes.
- Tratamento de erros para registros não encontrados.

## Endpoints

### Projetos

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/projetos` | Criar projeto |
| GET | `/api/projetos` | Listar projetos |
| GET | `/api/projetos/{id}` | Buscar projeto por ID |
| PUT | `/api/projetos/{id}` | Atualizar projeto |
| DELETE | `/api/projetos/{id}` | Excluir projeto |
| GET | `/api/projetos/{id}/tarefas` | Listar tarefas de um projeto |

### Tarefas

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/tarefas` | Criar tarefa |
| GET | `/api/tarefas` | Listar tarefas |
| GET | `/api/tarefas/{id}` | Buscar tarefa por ID |
| PUT | `/api/tarefas/{id}` | Atualizar tarefa |
| DELETE | `/api/tarefas/{id}` | Excluir tarefa |

### Usuários

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/usuarios` | Cadastrar usuário |
| GET | `/api/usuarios` | Listar usuários |

### Categorias

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/categorias` | Cadastrar categoria |
| GET | `/api/categorias` | Listar categorias |

### Comentários

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/tarefas/{tarefaId}/comentarios` | Adicionar comentário |
| GET | `/api/tarefas/{tarefaId}/comentarios` | Listar comentários da tarefa |

## Filtros e ordenação

A listagem de tarefas aceita parâmetros na URL.

Exemplos:

```text
GET /api/tarefas?status=PENDENTE
GET /api/tarefas?titulo=Criar
GET /api/tarefas?ordenarPor=titulo&direcao=asc
GET /api/tarefas?status=PENDENTE&ordenarPor=titulo&direcao=desc
```

## Autenticação

As operações `DELETE` exigem autenticação HTTP Basic.

Credenciais configuradas para demonstração local:

- **Usuário:** `admin`
- **Senha:** `admin123`

As demais rotas são públicas na configuração atual.

**Observação:** essas credenciais são apenas para fins acadêmicos e não devem ser utilizadas em produção.

## Como executar o projeto

**Pré-requisitos:**

- Java 21
- Git (opcional)
- IntelliJ IDEA ou outra IDE compatível com Kotlin

Na pasta principal do projeto, execute:

**Windows:**

```powershell
.\gradlew.bat bootRun
```

Após a inicialização, a API estará disponível em:

```text
http://localhost:8080
```

Para consultar os projetos:

```text
http://localhost:8080/api/projetos
```

**Observação:** o banco H2 está configurado em memória. Os dados cadastrados são perdidos quando a aplicação é reiniciada.

## Exemplos de uso da API

Os exemplos abaixo demonstram as principais funcionalidades implementadas no TaskFlow.

Todos os comandos podem ser executados no PowerShell, com a aplicação Spring Boot em execução.

**Importante:** como o banco H2 está em memória, os dados são apagados ao reiniciar a aplicação. Os exemplos consideram que os primeiros registros recebem ID `1`.

### 1. Criar um projeto

**POST** `/api/projeto`s

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/projetos" -Method POST -ContentType "application/json" -Body '{"nome":"Projeto Faculdade","descricao":"Desenvolvimento da API TaskFlow","prazo":"2026-11-30"}'
```

Exemplo de resposta:

```json
{
  "id": 1,
  "nome": "Projeto Faculdade",
  "descricao": "Desenvolvimento da API TaskFlow",
  "prazo": "2026-11-30"
}
```

### 2. Cadastrar um usuário

**POST** `/api/usuarios`

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/usuarios" -Method POST -ContentType "application/json" -Body '{"nome":"Maria","email":"maria@email.com"}'
```

### 3. Criar uma categoria

**POST** `/api/categorias`

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/categorias" -Method POST -ContentType "application/json" -Body '{"nome":"Backend"}'
```

### 4. Criar uma tarefa associada ao projeto e à categoria

**POST** `/api/tarefas`

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tarefas" -Method POST -ContentType "application/json" -Body '{"titulo":"Criar API","descricao":"Implementar endpoints REST","status":"PENDENTE","projetoId":1,"categoriaIds":[1]}'
```

Esse exemplo demonstra dois relacionamentos:

- A tarefa pertence ao projeto de ID `1` (`ManyToOne`).
- A tarefa está associada à categoria de ID `1` (`ManyToMany`).

### 5. Adicionar um comentário à tarefa

**POST** `/api/tarefas/1/comentarios`

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tarefas/1/comentarios" -Method POST -ContentType "application/json" -Body '{"texto":"Precisamos finalizar os testes","usuarioId":1}'
```

O comentário fica associado à tarefa e ao usuário que o escreveu.

### 6. Consultar os registros

**Listar projetos:**

```text
GET http://localhost:8080/api/projetos
```

**Buscar um projeto pelo ID:**

```text
GET http://localhost:8080/api/projetos/1
```

**Listar usuários:**

```text
GET http://localhost:8080/api/usuarios
```

**Listar categorias:**

```text
GET http://localhost:8080/api/categorias
```

**Listar tarefas:**

```text
GET http://localhost:8080/api/tarefas
```

**Buscar uma tarefa pelo ID:**

```text
GET http://localhost:8080/api/tarefas/1
```

**Listar tarefas de um projeto:**

```text
GET http://localhost:8080/api/projetos/1/tarefas
```

**Listar comentários de uma tarefa:**

```text
GET http://localhost:8080/api/tarefas/1/comentarios
```

Esses endereços podem ser abertos diretamente no navegador.

### 7. Filtrar tarefas por status

**GET** `/api/tarefas?status=PENDENTE`

```text
http://localhost:8080/api/tarefas?status=PENDENTE
```

Retorna somente tarefas com status `PENDENTE`.

### 8. Buscar tarefas pelo título

**GET** `/api/tarefas?titulo=Criar`

```text
http://localhost:8080/api/tarefas?titulo=Criar
```

Retorna tarefas cujo título contém a palavra `Criar`.

### 9. Ordenar tarefas

**GET** `/api/tarefas?ordenarPor=titulo&direcao=desc`

```text
http://localhost:8080/api/tarefas?ordenarPor=titulo&direcao=desc
```

Ordena as tarefas pelo título em ordem decrescente.

Também é possível combinar filtros e ordenação:

```text
http://localhost:8080/api/tarefas?status=PENDENTE&ordenarPor=titulo&direcao=asc
```

### 10. Atualizar um projeto

**PUT** `/api/projetos/1`

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/projetos/1" -Method PUT -ContentType "application/json" -Body '{"nome":"Projeto Atualizado","descricao":"API com novas funcionalidades","prazo":"2026-12-15"}'
```

### 11. Atualizar uma tarefa

**PUT** `/api/tarefas/1`

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tarefas/1" -Method PUT -ContentType "application/json" -Body '{"titulo":"Criar API","descricao":"Endpoints implementados","status":"CONCLUIDA","projetoId":1,"categoriaIds":[1]}'
```

### 12. Testar a autenticação

As operações de exclusão exigem autenticação HTTP Basic.

**Tentativa sem autenticação:**

```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/tarefas/1" -Method DELETE
} catch {
    Write-Host "Status HTTP:" ([int]$_.Exception.Response.StatusCode)
}
```

Resultado esperado:

```text
Status HTTP: 401
```

O código `401` indica que a exclusão foi bloqueada por falta de autenticação.

**Exclusão com autenticação:**

```powershell
$senha = ConvertTo-SecureString "admin123" -AsPlainText -Force
$credencial = New-Object System.Management.Automation.PSCredential("admin", $senha)

Invoke-RestMethod -Uri "http://localhost:8080/api/tarefas/1" -Method DELETE -Credential $credencial
```

Para excluir um projeto, a rota protegida é:

```text
DELETE /api/projetos/1
```

**Atenção:** execute as exclusões somente depois dos demais testes, porque elas removem os registros.

### 13. Logs e tratamento de erros

A API utiliza as classes `ProjetoService` e `TarefaService` para registrar operações importantes e tratar erros.

Exemplos de mensagens nos logs:

```text
INFO  ProjetoService : Criando projeto: Projeto Faculdade
INFO  TarefaService  : Criando tarefa: Criar API
WARN  ProjetoService : Projeto não encontrado: 999
WARN  TarefaService  : Tarefa não encontrada: 999
```

Ao consultar um registro inexistente, a API retorna o status HTTP `404 Not Found`.

Exemplo:

```text
GET http://localhost:8080/api/projetos/999
```

### Resumo dos recursos demonstrados

Os exemplos apresentados demonstram:

- Cadastro e consulta de cinco entidades.
- CRUD completo de projetos e tarefas.
- Associação entre projetos, tarefas, categorias, usuários e comentários.
- Relacionamentos `ManyToOne` e `ManyToMany`.
- Filtros e ordenação por parâmetros de consulta.
- Autenticação HTTP Basic para exclusões.
- Logs e tratamento de exceções.


## Vídeo de apresentação

Link do vídeo no YouTube:

## Status do projeto

API desenvolvida para apresentação acadêmica, com funcionalidades de gerenciamento de projetos, tarefas, usuários, categorias e comentários.