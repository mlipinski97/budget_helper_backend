package pl.lipinski.engineerdegree.service;

import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.intersection.UserBudgetListIntersection;

import java.util.Optional;

public interface UserBudgetListIntersectionService {
    Optional<UserBudgetListIntersection> findById(Long id);

    Iterable<UserBudgetListIntersection> findAllByIntersectionBudgetList(BudgetList budgetList);

    void deleteById(Long id);

    Iterable<UserBudgetListIntersection> findAllByIntersectionUser(User user);

    Optional<UserBudgetListIntersection> findByIntersectionUserAndIntersectionBudgetList(User user, BudgetList budgetList);

    UserBudgetListIntersection save(User user, BudgetList budgetList);
}
