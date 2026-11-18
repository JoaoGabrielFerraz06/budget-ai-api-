package com.example.budgetai.web;

import com.example.budgetai.service.AssistantService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private static final MediaType MP3 = MediaType.valueOf("audio/mpeg");

    private final AssistantService assistant;

    public AssistantController(AssistantService assistant) {
        this.assistant = assistant;
    }

    /** Chat por texto. */
    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> body,
                                    @RequestParam(defaultValue = "default") String conversationId) {
        return Map.of("reply", assistant.chat(conversationId, body.get("message")));
    }

    /** Somente transcrição: áudio -> texto. */
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> transcribe(@RequestPart("audio") MultipartFile audio) throws IOException {
        return Map.of("text", assistant.transcribe(audio));
    }

    /** Somente síntese: texto -> áudio mp3. */
    @PostMapping(value = "/speak", produces = "audio/mpeg")
    public ResponseEntity<byte[]> speak(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok().contentType(MP3).body(assistant.speak(body.get("text")));
    }

    /** Fluxo completo por voz: áudio -> texto -> IA (tools) -> áudio mp3. */
    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mpeg")
    public ResponseEntity<byte[]> voice(@RequestPart("audio") MultipartFile audio,
                                        @RequestParam(defaultValue = "default") String conversationId)
            throws IOException {
        String question = assistant.transcribe(audio);
        String reply = assistant.chat(conversationId, question);
        byte[] mp3 = assistant.speak(reply);
        return ResponseEntity.ok()
                .contentType(MP3)
                .header("X-Transcription", URLEncoder.encode(question, StandardCharsets.UTF_8))
                .header("X-Reply-Text", URLEncoder.encode(reply, StandardCharsets.UTF_8))
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "X-Transcription, X-Reply-Text")
                .body(mp3);
    }
}
