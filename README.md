# Budget AI API — API Inteligente com Reconhecimento de Fala e Spring Boot

API de orçamento financeiro pessoal com **IA Generativa** e **voz**: o usuário fala um comando
("gastei 45 reais no mercado hoje"), a API transcreve o áudio, o LLM decide qual função da aplicação
executar (Tool Calling), grava no banco e devolve a resposta em **áudio**.

## Fluxo

```
áudio (.mp3/.wav) ──► Transcription API (Whisper) ──► texto
texto ──► ChatClient (+ memória + Tool Calling) ──► BudgetService ──► PostgreSQL
resposta em texto ──► Speech API (TTS) ──► áudio (.mp3)
```

## Stack

Java 21 · Spring Boot 3.4 · Spring AI 1.0 (OpenAI) · Spring Data JPA · PostgreSQL (Docker) · Maven

## Arquitetura

| Camada | Classe | Responsabilidade |
|---|---|---|
| web | `TransactionController` | CRUD REST de transações e saldo |
| web | `AssistantController` | `/chat`, `/transcribe`, `/speak` e `/voice` |
| service | `AssistantService` | Orquestra ChatClient, transcrição e síntese de voz |
| service | `BudgetService` | Regras de negócio (compartilhadas por REST e IA) |
| tools | `BudgetTools` | Funções expostas ao LLM com `@Tool` |
| domain / repository | `Transaction`, `TransactionRepository` | Persistência JPA |

Conceitos do Spring AI usados: `ChatClient` (system prompt, advisors, memória de conversa),
`@Tool`/`@ToolParam` (Tool Calling), `OpenAiAudioTranscriptionModel` e `OpenAiAudioSpeechModel`.

## Como rodar

```bash
# 1. banco
docker compose up -d

# 2. chave da OpenAI
export OPENAI_API_KEY=sk-...        # Windows (PowerShell): $env:OPENAI_API_KEY="sk-..."

# 3. aplicação
./mvnw spring-boot:run              # ou: mvn spring-boot:run
```

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/transactions` | Cria transação |
| GET | `/api/transactions?from=&to=` | Lista (opcionalmente por período) |
| GET | `/api/transactions/balance` | Saldo |
| DELETE | `/api/transactions/{id}` | Remove |
| POST | `/api/assistant/chat` | Chat por texto com tool calling |
| POST | `/api/assistant/transcribe` | Áudio → texto |
| POST | `/api/assistant/speak` | Texto → áudio mp3 |
| POST | `/api/assistant/voice` | Áudio → IA → áudio (fluxo completo) |

### Exemplos

```bash
curl -X POST localhost:8080/api/transactions -H 'Content-Type: application/json' \
  -d '{"description":"salário","amount":3000,"type":"INCOME","category":"renda"}'

curl -X POST localhost:8080/api/assistant/chat -H 'Content-Type: application/json' \
  -d '{"message":"gastei 45 reais no mercado hoje"}'

curl -X POST localhost:8080/api/assistant/chat -H 'Content-Type: application/json' \
  -d '{"message":"qual meu saldo?"}'

curl -X POST localhost:8080/api/assistant/voice -F "audio=@comando.mp3" -o resposta.mp3
```

## Auditoria

Cada transação guarda `createdAt` e `source` (`API` ou `ASSISTANT`), permitindo saber se foi criada
manualmente ou pela IA.

## Roadmap

- [ ] Autenticação (Spring Security + JWT) e transações por usuário
- [ ] Orçamento por categoria com alertas de limite
- [ ] Confirmação por voz antes de registrar valores altos
- [ ] Streaming de resposta e front-end web com gravação de áudio
- [ ] Trocar de provedor (Ollama / Azure OpenAI) apenas por configuração
- [ ] Testes de integração com `MockMvc` e mock do ChatClient
