package pl.lipinski.engineerdegree;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.repository.UserRepo;
import pl.lipinski.engineerdegree.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
class EngineerdegreeApplicationTests {

    @Autowired
    private UserService entityManager;

    @Autowired
    private UserRepo userRepo;

    @Test
    public void whenFindByName_thenReturnEmployee() {
        // given
        User alex = new User();
        alex.setUsername("user");
        // when
        User found = userRepo.findByUsername(alex.getUsername()).get();

        // then
        assertEquals(found.getUsername(),alex.getUsername());
    }

}
