package pl.lipinski.engineerdegree.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.repository.UserRepo;

import java.util.Optional;

@Service
public class UserManager {

    UserRepo userRepo;
    PasswordEncoder passwordEncoder;

    @Autowired
    public UserManager(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public void deleteByUsername(String username) {
        userRepo.deleteByUsername(username);
    }

    public Iterable<User> findAll() {
        return userRepo.findAll();
    }

    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles("ROLE_USER");
        user.setEnabled(true);
        userRepo.save(user);
    }

    public void saveAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles("ROLE_ADMIN");
        user.setEnabled(true);
        userRepo.save(user);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void dbFiller() {
        userRepo.save(new User("admin", passwordEncoder.encode("admin"), true, "ROLE_ADMIN"));
        userRepo.save(new User("user", passwordEncoder.encode("user"), true, "ROLE_USER"));
    }
}
