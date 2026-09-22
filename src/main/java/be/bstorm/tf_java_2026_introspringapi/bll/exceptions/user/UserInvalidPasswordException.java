package be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user;

import org.springframework.http.HttpStatus;

public class UserInvalidPasswordException extends UserException{

    public UserInvalidPasswordException() {
        super(HttpStatus.BAD_REQUEST, "Invalid password");
    }

    public UserInvalidPasswordException(String body) {
        super(HttpStatus.BAD_REQUEST, body);
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
