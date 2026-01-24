package udtale.models;


import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class Diagnostic {
    private String id;
    private String sentence;
    private String primaryTargets;
    private String specificSounds;
    private String prosodicFocus;
    private String expectedChallenge;
    private String contextCategory;
}
