package udtale.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import udtale.config.exceptions.TranscriptionException;
import udtale.config.exceptions.UdtaleException;
import udtale.dto.Insights;
import udtale.dto.PronunciationCheckResponse;
import udtale.dto.PronunciationFinding;
import udtale.models.*;
import udtale.security.AudioProcessingService;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PronunciationService {
    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatClient chatClient;
    private final RestClient restClient;
    private final AudioProcessingService audioProcessingService;
    private final LearnerMemoryService learnerMemoryService;

    public PronunciationService(OpenAiAudioTranscriptionModel transcriptionModel, RestClient restClient, ChatClient.Builder chatClient, AudioProcessingService audioProcessingService, LearnerMemoryService learnerMemoryService) {
        this.transcriptionModel = transcriptionModel;
        this.chatClient = chatClient.build();
        this.audioProcessingService = audioProcessingService;
        this.restClient = restClient;
        this.learnerMemoryService = learnerMemoryService;
    }

    public Feedback analysePronunciation(MultipartFile audio, String targetText, Learner learner, String sessionId) {
        try {
            log.info("[{}] sending audio from learner: {}, to allosaurus to transcribe to ipa", sessionId, learner);

            String transcribedIpa = this.transcribeWithAllosaurus(audio, false,sessionId);

            log.info("[{}] sent audio successfully transcribed to ipa: {} ", sessionId, transcribedIpa);

            List<LearnerIssue> recurringIssues = this.learnerMemoryService.getRecurringIssues(learner);
            List<LearnerIssue> improvedAreas = this.learnerMemoryService.getImprovedAreas(learner);
            Map<String, Number> stats = this.learnerMemoryService.getLearnerStatistics(learner);

            String personalisedPrompt = buildPersonalisedPrompt(targetText, transcribedIpa, learner, recurringIssues,
                    improvedAreas);

//            "transliteration": "<use simple and common two letter words like UH, EH, TH, SH. etc or three basic letter words to break down words into phonetics>"

            String system = """
                    You are an expert Australian English pronunciation coach specializing in accent reduction.
                    
                    %s
                    
                    Given a target sentence ( what the learner intended ) and transcribed ipa from their recorded audio ( what their speech sounded like ),
                    Analyze the pronunciation and provide detailed feedback in JSON format with the following structure:
                    {
                      "overallAccuracy": <0-100>,
                      "mispronunciations": [
                        {
                          "word": "<word>",
                          "position": <index>,
                          "expected": "<correct pronunciation>",
                          "actual": "<how it was pronounced>",
                          "phonetic": "<IPA notation>",
                          "phoneticRespelling": <correct phonetic respelling>
                          "correction": "<specific correction>",
                          "tip": "<practical tip>",
                          "severity": "HIGH|MEDIUM|LOW"
                        }
                      ],
                      "generalTips": [
                        "<tip1>",
                        "<tip2>"
                      ],
                      "australianFeatures": {
                        "vowelIssues": ["<issue1>", "<issue2>"],
                        "rhoticity": ["<r-dropping notes>"],
                        "intonationNotes": ["<intonation feedback>"],
                        "stressPatterns": ["<stress pattern feedback>"]
                      }
                    }
                    
                    Focus on Australian English features:
                    - Vowel quality (e.g., /æ/ -> [æː] in 'bad', /eɪ/ -> [æɪ] in 'day')
                    - Non-rhotic accent (r-dropping after vowels)
                    - Rising intonation on statements
                    - Vowel reduction in unstressed syllables
                    - Broad vs Cultivated Australian features
                    
                    Be especially attentive to the learner's recurring issues. Acknowledge any improvements.
                    Be specific, actionable, and encouraging. Return ONLY valid JSON, no markdown.
                    """.formatted(personalisedPrompt);

            String user = """
                    Target: %s
                    Transcripted IPA: %s
                    """.formatted(targetText, transcribedIpa);


            Feedback feedback = chatClient.prompt()
                    .system(system)
                    .user(user)
                    .call()
                    .entity(Feedback.class);


            assert feedback != null;
            Insights insights = buildPersonalisedInsights(stats, recurringIssues, improvedAreas, feedback.getOverallAccuracy());

            List<Issue> detectedIssues = convertToDetectedIssues(feedback.getMispronunciations());
            learnerMemoryService.saveSession(learner, targetText, transcribedIpa, feedback.getOverallAccuracy(), detectedIssues, sessionId);


            return feedback;
        } catch (Exception e) {
            log.error("Error analysing pronunciation ", e);
            throw new UdtaleException("", HttpStatus.INTERNAL_SERVER_ERROR, "Error analysing pronunciation");
        }


//        double similarity = nrm
    }


    private String transcribeWithWhisper(MultipartFile audio, String sessionId) {
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
                    .model("whisper-1")
                    .temperature(0.2F)
                    .language("en")
                    .prompt("""
                            You are a phonetic transcription system
                            Transcribe the audio exactly as it is spoken using International Phonetic (IPA).
                            Do not normalise, correct or guess intended words.
                            Represent mispronunciations, substitutions, omissions, distortions and hesitations exactly as heard.
                            Use narrow IPA when needed to capture pronunciation defects include phonetic length, stress and diacritics only of audible.
                            Preserve pauses with (.) and unclear sounds with (?).
                            Output IPA symbols only, no standard spelling, no explanations, no timestamps.
                            """)
                    .build();

//            "Transcribe exactly what is heard into IPA. Do not correct pronunciation or infer intended words. Capture mispronunciations faithfully using IPA symbols and diacritics. Output IPA only"


            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource, options);
            log.info("[{}] generating audio transcription prompt ", sessionId);

            AudioTranscriptionResponse transcriptionResponse = transcriptionModel.call(prompt);

            log.info("[{}] transcription audio response : {}", sessionId, prompt);

            return transcriptionResponse.getResult().getOutput();

        } catch (IOException e) {
            throw new RuntimeException("Failed to transcribe audio ", e);
        }
    }

    private String transcribeWithAllosaurus(MultipartFile audio, Boolean includeTimestamps, String sessionId)  {
        log.info("[{}] about to transcribe audio to ipa with allosaurus ", sessionId);

//        validata and process audio
        byte[] processedAudio = null;
        try {
            processedAudio = audioProcessingService.validateAndConvert(audio);
        } catch (IOException e) {
            throw new UdtaleException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY, "audio processing failed", e.getCause());
        }


        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("audio", new ByteArrayResource(processedAudio) {
            @Override
            public String getFilename() {
                return "audio.wav";
            }
        }).contentType(MediaType.parseMediaType(Objects.requireNonNull(audio.getContentType())));

        builder.part("timestamps", String.valueOf(includeTimestamps));

        MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();

        log.info("[{}] sending transcription request to python service for recorded audio ", sessionId);


        TranscriptionResponse response = restClient.post()
                .uri("http://127.0.0.1:5000/transliterate")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(multipartBody)
                .retrieve()
                .body(TranscriptionResponse.class);

        if (Objects.isNull(response) || !response.success()) {
            String errorMessage = Objects.isNull(response) ? "Failed to reach transcription service" : response.error();
            throw new TranscriptionException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR, "Audio Transcription Failed: %s".formatted(errorMessage));
        }
        return response.ipa;
    }


    private String buildPersonalisedPrompt(String targetText,
                                           String transcribedText,
                                           Learner learner,
                                           List<LearnerIssue> recurringIssues,
                                           List<LearnerIssue> improvedAreas) {
        StringBuilder userContext = new StringBuilder();

//        get learners native language from profile.
//        add it to the learner context

        if (!recurringIssues.isEmpty()) {
            userContext.append("\nLearner's recurring pronunciation issues:\n");
            recurringIssues.stream()
                    .limit(5)
                    .forEach(
                            learnerIssue -> userContext
                                    .append("- ")
                                    .append(learnerIssue.getIssueType())
                                    .append(" (occurred ")
                                    .append(learnerIssue.getOccurrenceCount())
                                    .append(" times)\n")
                    );
        }

        if (!improvedAreas.isEmpty()) {
            userContext.append("\nAreas where learner has improved: \n");
            improvedAreas.stream()
                    .limit(3)
                    .forEach(
                            learnerIssue -> userContext
                                    .append("- ")
                                    .append(learnerIssue.getIssueType())
                                    .append("\n")
                    );
        }
        return userContext.toString();
    }

    private Insights buildPersonalisedInsights(Map<String, Number> stats,
                                               List<LearnerIssue> recurringIssues,
                                               List<LearnerIssue> improvedAreas,
                                               double currentAccuracy ) {
        int totalSessions = (int) stats.get("totalSessions");
        double avgAccuracy = (double) stats.get("averageAccuracy");
        double progress = (double) stats.get("progress");


        List<String> recurring = recurringIssues.stream()
                .limit(3)
                .map( learnerIssue -> learnerIssue.getIssueType() + "sounds")
                .toList();

        List<String> improved = improvedAreas.stream()
                .limit(3)
                .map(i -> i.getIssueType() + "sounds ")
                .toList();

        List<String> focusAreas = recurringIssues.stream()
                .limit(2)
                .map(learnerIssue -> "Practice " + learnerIssue.getIssueType().toString().toLowerCase() + "pronunciation daily")
                .toList();

        String motivationalMessage = this.generateMotivationalMessage(totalSessions, progress, currentAccuracy);

        return Insights.builder()
                .totalSessions(totalSessions)
                .averageAccuracy(avgAccuracy)
                .progressPercentage(progress)
                .focusAreas(focusAreas)
                .improvedAreas(improved)
                .recurringIssues(recurring)
                .motivationalMessage(motivationalMessage)
                .build();
    }


    private String generateMotivationalMessage(int sessions, double progress, double currentAccuracy) {
        if (sessions == 1) {
            return "Great Start!. Keep practicing to see rapid improvement";
        } else if (progress > 10) {
            return "Excellent progress! You've improved by " + String.format("%.1f%%", progress) + ". Keep up the great work!";
        } else if (currentAccuracy > 90) {
            return "Outstanding! your Australian accent is nearly perfect. Focus on the finer details now. ";
        } else if (sessions >= 10) {
            return "You're committed to improvement! Consistency is key to mastering the Australian accent. ";
        } else {
            return "You're making steady progress. Keep practicing regularly !";
        }
    }


    private List<Issue> convertToDetectedIssues(List<Mispronunciation> mispronunciations) {
       return mispronunciations.stream()
                .map( mispronunciation -> Issue.builder()
                        .word(mispronunciation.getWord())
                        .issueType(determineIssueType(mispronunciation))
                        .severity(mispronunciation.getSeverity())
                        .phonetic(mispronunciation.getPhonetic())
                        .correction(mispronunciation.getCorrection())
                        .build()
                ).toList();
    }


    private IssueType determineIssueType(Mispronunciation mispronunciation) {
        String phonetic = mispronunciation.getPhonetic().toLowerCase();

        if (phonetic.contains("vowel") || phonetic.matches(".*[aeiou].*") ) {
            return IssueType.VOWEL;
        } else if (phonetic.contains("stress")) {
            return IssueType.STRESS;
        } else if (phonetic.contains("intonation")) {
            return IssueType.INTONATION;
        } else if (phonetic.contains("r")){
            return IssueType.RHOTICITY;
        } else {
            return IssueType.CONSONANT;
        }

    }



    public record TranscriptionResponse(String filename, String ipa, Boolean success, String error) {}
    public record DerivedFeedback(Feedback feedback, Insights insights) {}
    public record LLMResult(List<PronunciationFinding> findings, List<String> drills) {}






}

