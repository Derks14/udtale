package udtale.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import udtale.dto.PronunciationCheckResponse;
import udtale.dto.PronunciationFinding;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class PronunciationService {
    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatClient chatClient;

    public PronunciationService(OpenAiAudioTranscriptionModel transcriptionModel, ChatClient.Builder chatClient) {
        this.transcriptionModel = transcriptionModel;
        this.chatClient = chatClient.build();
    }

    private String transcribe(MultipartFile audio, String sessionId) {
        try {
            byte[] bytes = audio.getBytes();
            Resource resource = new ByteArrayResource(bytes){
                @Override public String getFilename() {
                    return  audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "audio.wav";
                }
             };

            log.info("[{}] bytes successfully derived from audio file", sessionId);

            OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions
                    .builder()
//                    pick your model name that supports transcription in your setup
                    .model("gpt-4o-mini-transcribe")
                    .build();


            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource, options);
            log.info("[{}] generating audio transcription prompt ", sessionId);

            AudioTranscriptionResponse transcriptionResponse = transcriptionModel.call(prompt);

            log.info("[{}] transcription audio response : {}", sessionId, prompt);

            return transcriptionResponse.getResult().getOutput();

        } catch (IOException e) {
            throw new RuntimeException("Failed to transcribe audio ", e);
        }
    }

    public PronunciationCheckResponse check(MultipartFile audio, String targetText, String sessionId) {
        log.info("[{}] about to transcribe audio ", sessionId);
        String transcript = this.transcribe(audio, sessionId);

        double similarity = normalizedSimilarity(targetText, transcript);

        String system = """
                You are a pronunciation coach.
                Given a target sentence (what the learner intended) and an ASR transcript (what their speech sounded like),
                infer likely pronunciation defects and provide actionable coaching tips.
                
                Output MUST be valid JSON matching:
                {
                  "findings":[
                    {"type":"...", "expected":"...", "observed":"...", "evidence":"...", "severity":"low|medium|high", "coachingTip":"..."}
                  ],
                  "drills":["...", "..."]
                }
                
                Rules:
                - Be cautious: ASR errors are only a proxy.
                - Focus on the most probable pronunciation-driven confusions (th/s/f, r/l, v/w, vowel length, dropped endings, extra vowels).
                - Keep coaching tips short and actionable (mouth/tongue/air/stress guidance).
                - Include 3-6 findings max and 3-6 drills max.
                """;

        String user = """
                Target: %s
                Transcript: %s
                SimilarityScore: (0..1): %.3f
                """.formatted(targetText, transcript, similarity);

        LLMResult result = chatClient.prompt()
                .system(system)
                .user(user)
                .call()
                .entity(LLMResult.class);

        return new PronunciationCheckResponse(transcript, similarity, result.findings(), result.drills);

//        double similarity = nrm
    }

    // --- Basic similarity helper (word-level / normalization) ---
    private static double normalizedSimilarity(String a, String b) {
        String na = normalise(a);
        String nb = normalise(b);
        if (na.isBlank() && nb.isBlank()) return 1.0;
        int dist = levenshtein(na, nb);
        int max = Math.max(na.length(), nb.length());
        return max == 0 ? 1.0 : Math.max(0.0, 1.0 - (double) dist / max);
    }

    private static String normalise(String s){
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s']", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static int levenshtein(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        int[] cur = new int[s2.length() + 1];

        for (int j = 0; j <= s2.length(); j++) prev[j] = j;

        for (int i = 1; i <= s1.length(); i++) {  // Start from 1, not 0
            cur[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = cur; cur = tmp;
        }
        return prev[s2.length()];
    }


    public record LLMResult(List<PronunciationFinding> findings, List<String> drills) {}






}

