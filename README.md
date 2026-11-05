# PokéTeam

Aplicativo Android escolar em Kotlin e Jetpack Compose para consultar a PokéAPI e montar um time de até seis Pokémon por usuário.

## API utilizada
- PokéAPI: https://pokeapi.co/api/v2/
- GET /pokemon?limit=151: lista inicial
- GET /pokemon/{id}: detalhes do Pokémon
- Imagens: repositório de sprites da PokéAPI

## Funcionalidades
- Cadastro e login locais com validação e hash SHA-256
- Sessão persistente em SharedPreferences
- Pokédex com busca por nome, carregamento e erro
- Detalhes por rota tipada
- Favoritos por usuário persistidos no Room, limitados a seis
- Abas Pokédex, Meu time e Perfil

## Estrutura
- network: Retrofit e modelos da API
- data: Room e Repository
- viewmodel: estados e lógica
- userinterface: telas Compose e navegação

## Como executar
Abra no Android Studio com JDK 17 e SDK 35, sincronize o Gradle e execute o módulo app em emulador API 26+.

## Prints
Adicionar imagens de login, cadastro, lista, detalhe, favoritos e erro após testar no emulador.

## Divisão de tarefas
| Integrante | O que fez |
|---|---|
| preencher | preencher |

## O que aprendemos
Preencher uma frase por integrante após a revisão.

**Observação:** a autenticação local é didática. Em produção, utilize autenticação de servidor e hash de senha com salt e algoritmo próprio.
