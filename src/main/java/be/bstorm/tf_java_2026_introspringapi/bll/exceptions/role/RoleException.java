package be.bstorm.tf_java_2026_introspringapi.bll.exceptions.role;

import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.IntroSpringApiException;
import org.springframework.http.HttpStatus;

public abstract class RoleException extends IntroSpringApiException {

    public RoleException(HttpStatus status, Object body) {
        super(status, body, "role");
    }
}
