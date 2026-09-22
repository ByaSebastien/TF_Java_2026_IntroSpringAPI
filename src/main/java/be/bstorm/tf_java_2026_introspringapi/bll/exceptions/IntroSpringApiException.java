package be.bstorm.tf_java_2026_introspringapi.bll.exceptions;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = false) @ToString
public abstract class IntroSpringApiException extends RuntimeException {

    @Getter
    private String section;

    @Getter
    private final HttpStatus status;

    @Getter
    private final Object body;

    public IntroSpringApiException(HttpStatus status, Object body, String section) {
        super();
        this.status = status;
        this.body = body;
        this.section = section;
    }
}
