package udtale.config.exceptions;

import org.springframework.http.HttpStatus;


public class TranscriptionException extends UdtaleException {

    public TranscriptionException(String code, HttpStatus httpStatus, String message) {
        super(code, httpStatus, message);
    }

    public TranscriptionException(String code, HttpStatus httpStatus, String message, Throwable cause) {
        super(code, httpStatus, message, cause);
    }
}
