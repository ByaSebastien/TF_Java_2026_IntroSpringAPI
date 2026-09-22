package be.bstorm.tf_java_2026_introspringapi.bll.services.impls;

import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.role.RoleNotFoundException;
import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user.UserAlreadyExistException;
import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user.UserInvalidPasswordException;
import be.bstorm.tf_java_2026_introspringapi.bll.exceptions.user.UserNotFoundException;
import be.bstorm.tf_java_2026_introspringapi.bll.services.AuthService;
import be.bstorm.tf_java_2026_introspringapi.dal.repositories.RoleRepository;
import be.bstorm.tf_java_2026_introspringapi.dal.repositories.UserRepository;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.Role;
import be.bstorm.tf_java_2026_introspringapi.dl.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(User user) {
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistException();
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName("user")
                .orElseThrow(() -> new RoleNotFoundException("Role 'user' not found"));

        user.setRole(role);

        return userRepository.save(user);
    }

    @Override
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new UserInvalidPasswordException("Invalid password for user " + username);
        }

        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
    }
}
