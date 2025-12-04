package udtale.config.exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;


@Getter
public class UdtaleException extends RuntimeException {
    private final String code;
    private final HttpStatus httpStatus;

    public UdtaleException(String code, HttpStatus httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public UdtaleException(String code, HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
