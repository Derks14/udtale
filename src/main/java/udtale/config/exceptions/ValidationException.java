package udtale.config.exceptions;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ValidationException extends UdtaleException{

    private final Map<String, String> fieldErrors;

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super("payload_validation_failed", HttpStatus.UNPROCESSABLE_ENTITY, message);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
