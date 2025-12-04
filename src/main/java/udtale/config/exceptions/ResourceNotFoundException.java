package udtale.config.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends UdtaleException{

    public ResourceNotFoundException(String message) {
        super("Resource not found", HttpStatus.NOT_FOUND, message);
    }
}
