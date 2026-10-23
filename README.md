# Factory Events

Solução fullstack para recebimento, registro e consulta de eventos enviados por equipamentos de fábrica.

- **Backend:** Java 17, Spring Boot 3.5, Spring Data JPA, H2 (em memória), Lombok, springdoc-openapi (Swagger UI), Maven
- **Frontend:** React 18 + Vite, Vitest e Testing Library

## Pré-requisitos

- JDK 17 ou superior (o projeto é compilado com `--release 17`)
- Maven 3.9+
- Node.js 20+ e npm

## Como executar

### Backend

```bash
cd backend
mvn spring-boot:run
```

API disponível em `http://localhost:8080`.

Documentação interativa (Swagger UI): `http://localhost:8080/swagger-ui.html`. A especificação OpenAPI em JSON fica em `http://localhost:8080/v3/api-docs`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Interface disponível em `http://localhost:5173`. A URL da API pode ser alterada pela variável `VITE_API_URL` (veja `frontend/.env.example`).

## Como executar os testes

```bash
cd backend
mvn test
```

```bash
cd frontend
npm test
```

Cobertura dos testes:

- `EventServiceTest`: regras de negócio com repositório e handlers mockados (Mockito)
- `EventControllerTest`: contrato HTTP e códigos de status (`@WebMvcTest`)
- `EventFlowIntegrationTest`: fluxo completo com banco, incluindo duplicidade e auditoria
- `RequestIdFilterTest`: geração, reaproveitamento e limpeza do `X-Request-Id`
- Frontend (Vitest + Testing Library): `App.test.jsx` (fluxo completo), `EventForm`, `EventTable`, `Alert`, hook `useEvents`, serviço `api` e utilitários `datetime`, com a API mockada

## API

| Método | Rota                  | Descrição                         |
|--------|-----------------------|-----------------------------------|
| POST   | `/api/events`         | Registra um evento                |
| GET    | `/api/events`         | Lista eventos (mais recentes primeiro) |
| GET    | `/api/events/{id}`    | Consulta um evento pelo `eventId` |
| GET    | `/api/audits`         | Lista os registros de auditoria   |
| GET    | `/actuator/health`    | Health check                      |
| GET    | `/actuator/info`      | Informações da aplicação          |
| GET    | `/actuator/metrics`   | Métricas (JVM, HTTP, etc.)        |
| GET    | `/swagger-ui.html`    | Documentação interativa (Swagger UI) |
| GET    | `/v3/api-docs`        | Especificação OpenAPI (JSON)      |

Exemplo:

```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"eventId":"EVT-1001","eventType":"PRODUCTION","description":"Production line started","timestamp":"2026-05-20T10:00:00"}'
```

### Códigos HTTP

| Código | Situação                                                        |
|--------|-----------------------------------------------------------------|
| 201    | Evento registrado (header `Location` aponta para o recurso)     |
| 400    | Campos ausentes/inválidos, tipo de evento desconhecido, JSON malformado |
| 404    | Evento não encontrado                                           |
| 409    | Evento já processado (`eventId` duplicado)                      |
| 500    | Erro inesperado                                                 |

Todos os erros seguem o mesmo formato:

```json
{
  "timestamp": "2026-10-23T14:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Evento já processado: EVT-1001",
  "details": []
}
```

## Observabilidade

- **Correlation ID:** toda requisição recebe um `X-Request-Id`. Se o cliente enviar o header, o valor é reaproveitado; caso contrário, um UUID é gerado. O id é devolvido no header da resposta e incluído em todas as linhas de log da requisição (`[%X{requestId}]`), facilitando o rastreamento ponta a ponta.
- **Log de acesso:** o `RequestIdFilter` registra método, URI, status e duração de cada chamada em `/api/**`.
- **Níveis de log:** `INFO` é o padrão. Os logs de entrada dos controllers estão em `DEBUG` para não duplicar o log de acesso. Para vê-los, defina `logging.level.com.factory.events=DEBUG` em `application.properties` ou use `--logging.level.com.factory.events=DEBUG` ao iniciar.
- **Actuator:** `/actuator/health` (com detalhes), `/actuator/info` e `/actuator/metrics`.

```bash
curl -i -H "X-Request-Id: demo-123" http://localhost:8080/api/events
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

## Arquitetura

```
backend/src/main/java/com/factory/events
├── controller   endpoints REST
├── service      regras de negócio e transação
├── handler      estratégia por tipo de evento
├── repository   acesso a dados (Spring Data JPA)
├── domain       entidades e enum
├── dto          contratos de entrada e saída
├── exception    exceções de negócio e tratamento global
└── config       CORS, filtro de correlation ID e metadados do OpenAPI

frontend/src
├── components   Alert, EventForm, EventTable
├── hooks        useEvents
├── services     cliente HTTP da API
└── utils        formatação de datas
```

Fluxo de um evento: `Controller` valida o payload → `EventService` verifica duplicidade e persiste → `EventHandler` do tipo correspondente é executado na mesma transação.

## Principais decisões técnicas

- **Idempotência em duas camadas:** `existsByEventId` retorna rapidamente o 409 no caso comum, e a constraint única em `event_id` garante a regra sob concorrência. A violação da constraint é convertida em `DuplicateEventException`.
- **Strategy para tipos de evento:** cada tipo possui um `EventHandler`. Novos tipos exigem apenas uma nova implementação, sem alterar o serviço.
- **Auditoria transacional:** o evento QUALITY e seu registro de auditoria são gravados na mesma transação; uma falha na auditoria desfaz o registro do evento.
- **Tratamento centralizado de erros:** `@RestControllerAdvice` padroniza as respostas e mapeia cada exceção ao status HTTP adequado. Erros inesperados são logados e não expõem detalhes internos.
- **Chave técnica separada do `eventId`:** a entidade usa um id gerado e o `eventId` externo é uma coluna única, evitando que `save()` sobrescreva silenciosamente um evento existente.
- **Records como DTOs:** entidades não são expostas na API.
- **Lombok nas entidades:** `@Getter`, `@NoArgsConstructor(PROTECTED)` (exigido pelo JPA) e `@RequiredArgsConstructor` substituem getters e construtores manuais.
- **Swagger/OpenAPI:** `springdoc-openapi` gera a documentação a partir dos controllers (`@Tag` e `@Operation`), sem manter um contrato escrito à mão.
- **Java 17:** alinhado à versão LTS usada em ambientes corporativos; o Spring Boot 3.5 exige no mínimo o Java 17.
- **H2 em memória:** atende ao escopo do teste sem dependências externas. A troca por outro banco exige apenas alterar as propriedades de datasource.
- **Observabilidade:** logs por `eventId` com correlation ID (`X-Request-Id`) via MDC, log de acesso por requisição e Spring Actuator (`health`, `info`, `metrics`).
- **Frontend:** componentes pequenos, camada de serviço isolada, hook para o estado da listagem e exibição das mensagens retornadas pela API.

## Possíveis melhorias futuras

- Banco persistente (PostgreSQL) com migrações via Flyway
- Paginação e filtros (tipo, período) na listagem
- Autenticação dos equipamentos (API key ou OAuth2)
- Processamento assíncrono via fila (Kafka/RabbitMQ) para alto volume
- Tela de consulta da auditoria no frontend
- Métricas customizadas e tracing distribuído
- Dockerfile e docker-compose para backend e frontend
- Pipeline de CI executando os testes
