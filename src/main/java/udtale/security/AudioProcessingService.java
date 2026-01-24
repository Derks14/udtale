package udtale.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;
import udtale.config.exceptions.AudioProcessingException;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class AudioProcessingService {
    private static final List<String> SUPPORTED_FORMATS = List.of("audio/wav", "audio/vnd.wave");
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    private final Tika tika = new Tika();

    public byte[] validateAndConvert(MultipartFile audioFile) throws IOException {
        if (audioFile.getSize() > MAX_FILE_SIZE) {
            throw new AudioProcessingException(HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase(), HttpStatus.PAYLOAD_TOO_LARGE,
                    String.format("File too large. Maximum size is %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

//        this is to get supported file types
        String mimeType = tika.detect(audioFile.getBytes());
        log.debug("Detected MIME type: {} ", mimeType);

        if (!SUPPORTED_FORMATS.contains(mimeType)) {
            throw new AudioProcessingException(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("Unsupported audio format: %s. Supported: %s ", mimeType, SUPPORTED_FORMATS)
            );
        }

        if(!mimeType.startsWith("audio/wav")) {
            log.info("converting {} to WAV format", mimeType);
            return convertToWAV(audioFile.getBytes(), mimeType);
        }
        return audioFile.getBytes();
    }


    private byte[] convertToWAV(byte[] audioData, String sourceFormat) throws  IOException {
        // use this code convert the audio file to WAV

        // 1. JAVE2 (Java Audio Video Encoder)
        // 2. FFmpeg command-line via ProcessBuilder
        // 3. AudioSystem with proper SPI

        log.warn("audio conversion not fully implemented. Using original data ");

        // return as is for now. ()
        return audioData;
    }

    public byte[] resampleAudio(byte[] audioData, int targetSampleRate) throws IOException {
        // use AudioSystem or external library
        return audioData;
    }
}
