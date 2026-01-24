package udtale.config.exceptions;

import org.springframework.http.HttpStatus;


public class AudioProcessingException extends UdtaleException {

    public AudioProcessingException(String code, HttpStatus httpStatus, String message) {
        super(code, httpStatus, message);
    }

    public AudioProcessingException(String code, HttpStatus httpStatus, String message, Throwable cause) {
        super(code, httpStatus, message, cause);
    }
}
