# Feature Specification: Autenticação de Usuário com JWT

**Feature Branch**: `001-jwt-auth`

**Created**: 2026-06-28

**Status**: Draft

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Cadastro de Novo Usuário (Priority: P1)

Um visitante que ainda não tem conta no FitFlow deve conseguir criar sua conta
fornecendo nome, e-mail e senha. Após o cadastro bem-sucedido, ele já recebe acesso
imediato ao sistema sem precisar fazer login separadamente.

**Why this priority**: É o pré-requisito de tudo. Sem cadastro não há usuário; sem
usuário não há nenhuma outra funcionalidade do sistema disponível.

**Independent Test**: O fluxo pode ser testado isoladamente criando uma requisição de
cadastro e verificando que o sistema retorna uma resposta de sucesso com token de acesso
e que o usuário existe e pode ser encontrado pelo e-mail informado.

**Acceptance Scenarios**:

1. **Given** um visitante não autenticado com um e-mail nunca antes cadastrado,
   **When** ele fornece nome, e-mail válido e senha com pelo menos 6 caracteres,
   **Then** o sistema cria a conta, armazena a senha de forma segura (nunca em texto
   puro) e retorna um token de acesso com os dados básicos do usuário (id, nome, email).

2. **Given** um visitante tenta se cadastrar com um e-mail que já existe no sistema,
   **When** ele submete o formulário de cadastro,
   **Then** o sistema recusa o cadastro e retorna uma mensagem clara de que o e-mail já
   está em uso, sem revelar informações sobre o usuário existente.

3. **Given** um visitante preenche o formulário de cadastro,
   **When** ele fornece um e-mail com formato inválido (ex: sem @, sem domínio),
   **Then** o sistema recusa o cadastro e retorna mensagem descritiva do erro de
   validação antes de qualquer operação no banco de dados.

4. **Given** um visitante preenche o formulário de cadastro,
   **When** ele fornece uma senha com menos de 6 caracteres,
   **Then** o sistema recusa o cadastro e informa o requisito mínimo de senha.

5. **Given** um visitante preenche o formulário de cadastro,
   **When** ele deixa o campo de nome vazio,
   **Then** o sistema recusa o cadastro e informa que o nome é obrigatório.

---

### User Story 2 — Login de Usuário Existente (Priority: P2)

Um usuário já cadastrado deve conseguir acessar o sistema informando seu e-mail e senha.
Após autenticação bem-sucedida, ele recebe um token que será usado para acessar todas as
funcionalidades protegidas do sistema.

**Why this priority**: Sem login, um usuário já cadastrado não consegue mais usar o
sistema após fechar o navegador. É a porta de entrada recorrente de qualquer sessão.

**Independent Test**: O fluxo pode ser testado de forma independente criando um usuário
previamente e então tentando autenticá-lo, verificando que o token retornado é válido e
que o acesso a rotas protegidas é concedido.

**Acceptance Scenarios**:

1. **Given** um usuário cadastrado com e-mail e senha conhecidos,
   **When** ele fornece as credenciais corretas,
   **Then** o sistema retorna um token de acesso válido junto com os dados básicos do
   usuário (id, nome, email).

2. **Given** um usuário cadastrado,
   **When** ele fornece a senha errada para seu e-mail,
   **Then** o sistema recusa o acesso com mensagem genérica ("credenciais inválidas")
   sem revelar se o erro foi no e-mail ou na senha.

3. **Given** um visitante tenta fazer login,
   **When** ele fornece um e-mail que não existe no sistema,
   **Then** o sistema retorna a mesma mensagem genérica de credenciais inválidas (sem
   confirmar ou negar a existência do e-mail — proteção contra enumeração de usuários).

4. **Given** um usuário tenta fazer login,
   **When** ele deixa o campo de e-mail ou senha em branco,
   **Then** o sistema recusa a requisição com validação de campos obrigatórios.

---

### User Story 3 — Acesso a Rotas Protegidas com Token (Priority: P3)

Um usuário autenticado deve conseguir acessar as funcionalidades protegidas do sistema
apresentando seu token nas requisições. Usuários sem token válido devem ser bloqueados.

**Why this priority**: Sem o mecanismo de proteção de rotas, todas as funcionalidades
futuras do sistema ficariam expostas sem controle de acesso.

**Independent Test**: Pode ser testado de forma independente usando o token obtido no
login para acessar qualquer rota protegida, e verificando que sem token (ou com token
inválido/expirado) o acesso é negado.

**Acceptance Scenarios**:

1. **Given** um usuário autenticado com token válido,
   **When** ele faz uma requisição a qualquer rota protegida incluindo o token no cabeçalho,
   **Then** o sistema processa a requisição normalmente.

2. **Given** qualquer cliente (autenticado ou não),
   **When** ele faz uma requisição a uma rota protegida sem incluir token,
   **Then** o sistema recusa com resposta indicando que autenticação é necessária.

3. **Given** um cliente com token expirado ou com assinatura inválida (adulterado),
   **When** ele tenta acessar uma rota protegida,
   **Then** o sistema recusa o acesso com resposta indicando que o token é inválido ou
   expirado, sem revelar detalhes técnicos da falha.

---

### Edge Cases

- O que acontece se dois cadastros simultâneos tentam registrar o mesmo e-mail?
  O sistema deve garantir unicidade via constraint no banco de dados, e apenas um
  deve ser aceito.
- O que acontece se o e-mail tem espaços ao redor (ex: " user@email.com ")?
  O sistema deve tratar (trim) antes de validar e persistir.
- Qual o tempo de expiração do token? 24 horas como padrão.
- Um token válido continua funcionando se o usuário mudar a senha? Para o MVP, sim —
  invalidação de tokens anteriores ao trocar senha é pós-MVP.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE permitir que qualquer visitante crie uma conta fornecendo
  nome, e-mail e senha.
- **FR-002**: O sistema DEVE rejeitar cadastros com e-mails já existentes e retornar
  mensagem de erro descritiva.
- **FR-003**: O sistema DEVE rejeitar cadastros com dados inválidos (e-mail malformado,
  senha muito curta, nome vazio) antes de qualquer operação no banco.
- **FR-004**: O sistema DEVE armazenar senhas de forma irreversível e segura — nunca em
  texto puro.
- **FR-005**: O sistema DEVE permitir que usuários cadastrados autentiquem com e-mail e
  senha e recebam um token de acesso.
- **FR-006**: O sistema DEVE retornar mensagem genérica para credenciais incorretas,
  sem distinguir se o erro foi no e-mail ou na senha.
- **FR-007**: O sistema DEVE emitir um token com tempo de expiração de 24 horas.
- **FR-008**: O sistema DEVE bloquear o acesso a todas as rotas não públicas para
  requisições sem token válido.
- **FR-009**: O sistema DEVE aceitar requisições com token válido e processar
  normalmente.
- **FR-010**: O sistema DEVE rejeitar tokens expirados e tokens com assinatura inválida.
- **FR-011**: O sistema DEVE normalizar o e-mail (trim + lowercase) antes de validar e
  persistir.

### Rotas Públicas (sem autenticação)

- Cadastro de usuário
- Login de usuário

### Rotas Protegidas (requerem token válido)

- Todas as demais rotas do sistema.

### Key Entities

- **Usuário**: representa uma pessoa com conta no sistema. Possui identidade única
  (e-mail), nome de exibição, credencial segura (senha hasheada) e rastreamento de
  criação/atualização. É o dono de todos os dados do sistema (treinos, sessões, etc.).
- **Token de acesso**: artefato temporário emitido após autenticação bem-sucedida.
  Carrega a identidade do usuário e tem validade definida. Não é persistido no banco.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Um novo usuário consegue completar o cadastro em menos de 30 segundos
  informando apenas nome, e-mail e senha.
- **SC-002**: Um usuário cadastrado consegue realizar o login em menos de 10 segundos.
- **SC-003**: 100% das rotas não públicas do sistema são inacessíveis sem um token
  válido — nenhuma rota protegida deve vazar dados sem autenticação.
- **SC-004**: Tentativas com credenciais inválidas não revelam se o e-mail existe no
  sistema (proteção contra enumeração de usuários).
- **SC-005**: Tokens expirados ou adulterados são rejeitados em 100% dos casos — não há
  bypass possível.

---

## Assumptions

- O sistema opera com um único tipo de usuário — não há papel de administrador nesta
  feature (será adicionado em fase posterior).
- A autenticação é stateless (sem sessões no servidor). O token é o único mecanismo
  de identidade após o login.
- Recuperação de senha (forgot password) está fora do escopo desta feature.
- Edição de perfil (alterar nome, e-mail ou senha) está fora do escopo desta feature.
- O cliente (frontend Angular) é responsável por armazenar o token localmente e
  incluí-lo no cabeçalho de cada requisição protegida.
- A invalidação antecipada de tokens (logout com blacklist) é pós-MVP. Para o MVP,
  o logout é apenas do lado do cliente (descarte do token).
- Requisito mínimo de senha: 6 caracteres. Sem requisitos de complexidade no MVP.
- Tempo de expiração do token: 24 horas.
