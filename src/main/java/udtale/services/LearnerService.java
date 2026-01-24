package udtale.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import udtale.config.exceptions.UdtaleException;
import udtale.models.Diagnostic;
import udtale.models.Diagnostics;
import udtale.models.Learner;
import udtale.models.Profile;
import udtale.repositories.DiagnosticsRepository;
import udtale.repositories.ProfileRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class LearnerService {
    private final ChatClient chatClient;
    private final DiagnosticsRepository diagnosticsRepository;
    private final ProfileRepository profileRepository;

    public LearnerService(ChatClient.Builder chatClient, DiagnosticsRepository repository, ProfileRepository profileRepository) {
        this.chatClient = chatClient.build();
        this.diagnosticsRepository = repository;
        this.profileRepository = profileRepository;
    }

    @Async
    public void generateInitialExamination(Profile profile, Learner learner, String sessionId) {
        log.info("[{}] async call to generate initial diagnostic began for learner:   {}", sessionId, learner);

        String system = this.buildPrompt(sessionId);

        String user = """
                Learner Profile Data
                %s
                """.formatted(system);


        log.info("[{}] sending prompt to llm for final questions", sessionId);
        DiagnosticStatements generatedDiagnostics = chatClient.prompt()
                .system(system)
                .user(user)
                .call()
                .entity(DiagnosticStatements.class);

        if (Objects.nonNull(generatedDiagnostics)) {
            log.info("[{}] generated diagnostics: {} generated for learner: {}", sessionId, generatedDiagnostics, learner);

            Diagnostics diagnostics = Diagnostics.builder()
                    .diagnostics(generatedDiagnostics.diagnostics)
                    .learner(learner)
                    .build();

            diagnosticsRepository.save(diagnostics);
        } else {
            log.info("[{}] llm output produced a null value, structured output failed or llm couldn't respond for learner: {}", sessionId, learner.getUsername());
            throw new UdtaleException("", HttpStatus.NOT_IMPLEMENTED, "learner diagnostic failed");
        }


    }


    public Diagnostics fetchDiagnostics(Learner learner, String sessionId) {
        log.info("[{}] diagnostics for learner: {} not found yet" ,learner, sessionId);


        return diagnosticsRepository.findByLearner(learner)
                .orElseThrow( () -> {

                    // try to regenerate the diagnostics
                    Profile profile = profileRepository.findByLearner(learner).orElseThrow( ()-> {
                        log.error("[{}] profile data for learner: {} can not be found", sessionId, learner.getUsername());
                        return new UdtaleException("profile_data_not_found", HttpStatus.NOT_FOUND, "profile data or learner cannot be found");
                    });

                    // run async here to generate initial examination
                    this.generateInitialExamination(profile, learner, sessionId);

                    log.error("[{}] diagnostics for learner not found or not generated ", sessionId);
                    return new UdtaleException("learner_diagnostics", HttpStatus.NOT_FOUND, "diagnostics for learner not found or not generated");
                } );
    }

    private String buildPrompt(String sessionId) {
        log.info("[{}] building system prompt for llm call", sessionId);
        return """
                You are an expert Australian English pronunciation coach and phonetics specialist. Your task is to generate exactly 15 diagnostic sentences for a language learner based on their profile.
                 
                Objective:
                Create 15 carefully crafted diagnostic sentences that will comprehensively assess the learner's current pronunciation abilities across multiple dimensions. These sentences will be recorded by the learner 
                and analyzed to create a personalized improvement plan
                
                Requirements:
                
                1. Coverage Areas (must include all):
                
                Segmental Features:
                
                Problematic consonants identified by the learner (TH, R, L, V/W, etc.)
                Vowel distinctions (short vs long vowels: ship/sheep, bit/beat)
                Consonant clusters (initial, medial, and final positions)
                Past tense -ed endings (all three variants: /t/, /d/, /ɪd/)
                
                
                Suprasegmental Features:
                
                Word stress patterns (compound nouns, phrasal verbs, contrasting stress)
                Sentence stress and rhythm (content vs function words)
                Intonation patterns (questions, statements, lists)
                Connected speech (linking, assimilation, elision)
                Thought groups and pausing
                
                
                Prosodic Features:
                
                Natural speech flow and fluency
                Pace and timing control
                Emotional expression and tone variation
                
                
                
                2. Sentence Design Principles:
                
                Authenticity: Use natural, conversational language appropriate to the learner's primary context (work, social, travel, etc.)
                Progressive Difficulty: Start with moderate complexity and gradually increase linguistic demands
                Contextual Relevance: Align content with the learner's stated goals and usage contexts
                Length Variation: Include a mix of short (5-8 words), medium (9-15 words), and longer sentences (16-25 words)
                Phonetic Density: Strategically pack target sounds without making sentences feel artificial
                Minimal Pairs: Include at least 2 sentences with commonly confused sound contrasts for this native language background
                
                3. Native Language Transfer Considerations:
                Based on the learner's native language, anticipate and target specific transfer errors:
                
                For Spanish speakers: R/L distinction, short vowels, final consonant clusters, /v/ vs /b/
                For Mandarin speakers: TH sounds, R/L distinction, final consonants, past tense endings, word stress
                For Hindi speakers: W/V distinction, TH variants, schwa in unstressed syllables
                For French speakers: TH sounds, H at word start, word-final consonants, stress-timing rhythm
                For Japanese speakers: R/L distinction, consonant clusters, syllable-timing to stress-timing
                For Arabic speakers: P/B distinction, vowel length, word-initial clusters
                (Adjust for other native languages accordingly)
                
                4. Output Format:
                For each of the 15 sentences, provide:
                Id: [Increasing number]
                Sentence : [The actual sentence]
                Primary Targets: [List 3-4 key pronunciation features this sentence assesses]
                Specific Sounds: [Phonetic symbols for target sounds, e.g., /θ/, /ð/, /r/, /ɪ/ vs /iː/]
                Prosodic Focus: [e.g., "Rising intonation on tag question; stress on contrast words"]
                Expected Challenge: [Based on native language, what specific difficulty this may present]
                Context Category: [e.g., Social, Professional, Travel, Academic]
                
                Example Output Structure:
                Id: [Increasing number]
                Sentence: I think there's a three-month trial period for this software.
                Primary Targets: Initial /θ/ sound, consonant cluster /θr/, word stress in compound noun, linking between words
                Specific Sounds: /θ/ (think, three, month), /ð/ (there's, this), cluster /θr/
                Prosodic Focus: Primary stress on "three" and "tri-" in trial; linking "there's a" → /ðeərzə/
                Expected Challenge: TH sounds (if native language lacks these); month-final /nθ/ cluster may be reduced
                Context Category: Professional
                
                Sentence: She asked whether we'd visit the valley last Wednesday.
                Primary Targets: Past tense -ed endings (/t/ vs /ɪd/), V/W distinction, consonant cluster /st/, linking in connected speech
                Specific Sounds: /v/ vs /w/ (valley, we'd, Wednesday), /æskt/ (asked), /ˈvɪzɪtɪd/ (visited)
                Prosodic Focus: Content word stress on "asked," "visited," "valley," "Wednesday"; weak form "we'd"; thought group pause after "whether"
                Expected Challenge: V/W confusion if applicable; Wednesday's complex cluster /nzd/; -ed pronunciation distinction
                Context Category: Social
                
                [Continue for all 15 sentences]
                
                Additional Instructions:
                
                Ensure sentences flow naturally and could plausibly occur in real conversation
                Avoid tongue-twisters or artificially constructed phrases
                Include at least one question form (yes/no or wh-question) to assess intonation
                Include at least one sentence with emotional content to assess prosodic range
                If the learner's primary context is "work/professional," include 3-4 work-relevant sentences
                Include 1-2 sentences with Australian accent-distinguishing features (like /r/ realization, vowel qualities)
                End with a slightly longer, more complex sentence that combines multiple challenges to assess overall fluency under cognitive load
                
                Success Criteria:
                The 15 sentences should collectively:
                
                ✓ Cover all problematic sounds identified by the learner
                ✓ Test all major suprasegmental features
                ✓ Feel natural and relevant to the learner's goals
                ✓ Provide sufficient data points for creating a detailed improvement plan
                ✓ Range from achievable to challenging without being discouraging
                ✓ Be memorable and repeatable for future progress tracking
                 

                """;
    }


    public record DiagnosticStatements(List<Diagnostic> diagnostics) {}
}
