package com.example.budgetai.service;

import com.example.budgetai.tools.BudgetTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** Orquestra o fluxo: áudio -> texto -> LLM (+ tools) -> texto -> áudio. */
@Service
public class AssistantService {

    private static final String SYSTEM_PROMPT = """
            Você é um assistente de orçamento financeiro pessoal. Responda sempre em português do Brasil,
            de forma curta e clara (suas respostas podem ser lidas em voz alta).
            Use as ferramentas disponíveis para registrar transações e consultar saldos; nunca invente valores.
            Se faltar informação essencial (como o valor), pergunte ao usuário.
            Interprete datas relativas ("hoje", "ontem") usando a ferramenta today.
            """;

    private final ChatClient chatClient;
    private final BudgetTools budgetTools;
    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final OpenAiAudioSpeechModel speechModel;

    public AssistantService(ChatClient.Builder builder,
                            BudgetTools budgetTools,
                            OpenAiAudioTranscriptionModel transcriptionModel,
                            OpenAiAudioSpeechModel speechModel) {
        ChatMemory memory = MessageWindowChatMemory.builder().maxMessages(20).build();
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(memory).build(),
                        new SimpleLoggerAdvisor())
                .build();
        this.budgetTools = budgetTools;
        this.transcriptionModel = transcriptionModel;
        this.speechModel = speechModel;
    }

    /** Texto -> resposta do LLM (com tool calling). */
    public String chat(String conversationId, String message) {
        return chatClient.prompt()
                .user(message)
                .tools(budgetTools)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    /** Áudio -> texto (Whisper). */
    public String transcribe(MultipartFile audio) throws IOException {
        String filename = audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "audio.mp3";
        ByteArrayResource resource = new ByteArrayResource(audio.getBytes()) {
            @Override
            public String getFilename() {
                return filename; // a API da OpenAI usa a extensão para identificar o formato
            }
        };
        return transcriptionModel.call(resource);
    }

    /** Texto -> áudio (TTS). */
    public byte[] speak(String text) {
        return speechModel.call(text);
    }
}
