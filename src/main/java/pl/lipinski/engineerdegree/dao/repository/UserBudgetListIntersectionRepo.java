package pl.lipinski.engineerdegree.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.UserBudgetListIntersection;

import java.util.Optional;

@Repository
public interface UserBudgetListIntersectionRepo extends JpaRepository<UserBudgetListIntersection, Long> {
    public Iterable<UserBudgetListIntersection> findAllByIntersectionUserAndAndIntersectionBudgetList(User user, BudgetList budgetList);
    public Optional<UserBudgetListIntersection> findByIntersectionUserAndIntersectionBudgetList(User user, BudgetList budgetList);
}
