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
- **Spring Security:** responsável pela autenticação e proteção das operações.
- **H2 Database:** banco de dados utilizado durante a execução.
- **Gradle:** ferramenta de gerenciamento e construção do projeto.
- **Java 21:** plataforma utilizada para executar a aplicação.

## Entidades

O sistema possui cinco entidades principais:

- **Projeto:** representa um projeto que pode conter várias tarefas.
- **Tarefa:** representa uma atividade vinculada a um projeto e que pode possuir várias categorias.
- **Usuario:** representa um usuário que pode registrar comentários.
- **Categoria:** organiza as tarefas por assunto.
- **Comentario:** registra comentários feitos por usuários em tarefas.

## Relacionamentos

O projeto utiliza os seguintes relacionamentos entre entidades:

- **OneToMany — Projeto e Tarefa:** um projeto pode possuir várias tarefas.
- **ManyToOne — Tarefa e Projeto:** várias tarefas podem pertencer ao mesmo projeto.
- **ManyToMany — Tarefa e Categoria:** uma tarefa pode possuir várias categorias, e uma categoria pode estar associada a várias tarefas.
- **ManyToOne — Comentario e Tarefa:** vários comentários podem pertencer à mesma tarefa.
- **ManyToOne — Comentario e Usuario:** vários comentários podem ser feitos pelo mesmo usuário.

O relacionamento entre tarefas e categorias utiliza a tabela intermediária `tarefa_categoria`.

## Funcionalidades

- CRUD completo de projetos.
- CRUD completo de tarefas.
- Cadastro e listagem de usuários.
- Cadastro e listagem de categorias.
- Criação e consulta de comentários.
- Consulta das tarefas de um projeto.
- Associação de categorias às tarefas.
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
| GET | `/api/categorias/{id}` | Buscar categoria por ID |

### Comentários

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/tarefas/{tarefaId}/comentarios` | Adicionar comentário |
| GET | `/api/tarefas/{tarefaId}/comentarios` | Listar comentários da tarefa |

### Associação entre tarefas e categorias

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/tarefas/{tarefaId}/categorias/{categoriaId}` | Associar categoria à tarefa |

## Filtros e ordenação

A listagem de tarefas aceita parâmetros na URL, permitindo filtrar e organizar os resultados.

**Filtrar por status:**

```text
GET /api/tarefas?status=PENDENTE
```

Retorna somente tarefas com status `PENDENTE`.

**Buscar pelo título:**

```text
GET /api/tarefas?titulo=Criar
```

Retorna tarefas cujo título contém a palavra informada.

**Ordenar pelo título em ordem crescente:**

```text
GET /api/tarefas?ordenarPor=titulo&direcao=asc
```

**Ordenar pelo título em ordem decrescente:**

```text
GET /api/tarefas?ordenarPor=titulo&direcao=desc
```

Também é possível combinar filtros e ordenação:

```text
GET /api/tarefas?status=PENDENTE&ordenarPor=titulo&direcao=asc
```

## Autenticação

As operações de exclusão (`DELETE`) exigem autenticação utilizando HTTP Basic.

Credenciais configuradas para demonstração local:

- **Usuário:** `admin`
- **Senha:** `admin123`

As demais rotas são públicas na configuração atual.

Quando uma operação protegida é solicitada sem autenticação, a API retorna `401 Unauthorized`.

**Observação:** essas credenciais são utilizadas apenas para fins acadêmicos e não devem ser utilizadas em produção.

## Logs e tratamento de erros

A API utiliza as classes `ProjetoService` e `TarefaService` para registrar operações importantes e tratar situações de erro.

Os logs permitem acompanhar ações realizadas durante a execução da aplicação, como a criação de projetos e tarefas.

Quando um projeto ou uma tarefa não é encontrado, a aplicação lança uma exceção e retorna o status HTTP `404 Not Found`.

Exemplos de consulta de registros inexistentes:

```text
GET /api/projetos/999
GET /api/tarefas/999
```

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

Após a inicialização, a API estará disponível em:

```text
http://localhost:8080
```

Para consultar os projetos cadastrados:

```text
http://localhost:8080/api/projetos
```

### Banco de dados

O projeto utiliza o banco de dados H2 configurado em memória.

Isso significa que os dados cadastrados ficam disponíveis durante a execução da aplicação, mas são perdidos quando o servidor é reiniciado.

## Exemplos de uso

Os exemplos abaixo demonstram algumas das principais funcionalidades da API.

### 1. Criar um projeto

**POST** `/api/projetos`

```json
{
  "nome": "Projeto TaskFlow",
  "descricao": "Sistema de gerenciamento de projetos e tarefas",
  "prazo": "2026-12-15"
}
```

### 2. Cadastrar um usuário

**POST** `/api/usuarios`

```json
{
  "nome": "Julia",
  "email": "julia@email.com"
}
```

### 3. Criar uma categoria

**POST** `/api/categorias`

```json
{
  "nome": "Desenvolvimento"
}
```

### 4. Criar uma tarefa

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

A tarefa fica vinculada ao projeto informado e pode ser associada às categorias cadastradas.

### 5. Adicionar um comentário

**POST** `/api/tarefas/1/comentarios`

```json
{
  "texto": "A implementação da tarefa foi iniciada.",
  "usuarioId": 1
}
```

O comentário fica associado à tarefa e ao usuário que o escreveu.

## Testes realizados

A aplicação foi testada utilizando o Postman, verificando:

- Criação, listagem, atualização e exclusão de projetos.
- Criação, listagem e atualização de tarefas.
- Cadastro e consulta de usuários e categorias.
- Associação entre tarefas e categorias.
- Criação e consulta de comentários.
- Filtragem e ordenação de tarefas.
- Retorno `404 Not Found` para registros inexistentes.
- Retorno `401 Unauthorized` ao tentar excluir sem autenticação.
- Execução de operações protegidas com autenticação.

## Status do projeto

API desenvolvida para apresentação acadêmica, com funcionalidades de gerenciamento de projetos, tarefas, usuários, categorias e comentários.

O projeto demonstra a utilização de Kotlin, Spring Boot, persistência com JPA, relacionamentos entre entidades, organização por camadas, autenticação e operações REST.