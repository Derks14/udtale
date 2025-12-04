package udtale.config.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends UdtaleException{

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT.getReasonPhrase(), HttpStatus.CONFLICT, message);
    }
}
