package be.bstorm.tf_java_2026_introspringapi.bll.exceptions.role;

import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends RoleException{

    public RoleNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Role not found");
    }

    public RoleNotFoundException(String body) {
        super(HttpStatus.NOT_FOUND, body);
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
