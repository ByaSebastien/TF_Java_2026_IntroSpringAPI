package be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user;


import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.IntroSpringApiException;
import org.springframework.http.HttpStatus;

public abstract class UserException extends IntroSpringApiException {

    public UserException(HttpStatus status, Object body) {
        super(status, body, "user");
    }
}
