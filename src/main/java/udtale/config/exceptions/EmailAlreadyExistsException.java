package udtale.config.exceptions;


import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends UdtaleException {
    public EmailAlreadyExistsException(String message) {
        super("duplicate_email", HttpStatus.CONFLICT, message);
    }
}
