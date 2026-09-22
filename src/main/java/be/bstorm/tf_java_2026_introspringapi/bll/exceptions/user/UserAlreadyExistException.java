package be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class UserAlreadyExistException extends UserException{

    public UserAlreadyExistException() {
        super(HttpStatus.CONFLICT, new HashMap<String,String>(
                Map.of("username", "Username already exists")
        ));
    }

    public UserAlreadyExistException(Map<String, String> body) {
        super(HttpStatus.CONFLICT, body);
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
