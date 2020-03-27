package pl.lipinski.engineerdegree.manager;

import org.springframework.beans.factory.annotation.Autowired;
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

    public Optional<User> findByUsername(String username){
        return userRepo.findByUsername(username);
    }

    public void deleteByUsername(String username){
        userRepo.deleteByUsername(username);
    }

    public Iterable<User> findAll(){
        return userRepo.findAll();
    }

    public User saveUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles("ROLE_USER");
        user.setEnabled(true);
        return userRepo.save(user);
    }

    public User saveAdmin(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles("ROLE_ADMIN");
        user.setEnabled(true);
        return userRepo.save(user);
    }
}
