package pl.lipinski.engineerdegree.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.intersection.FriendshipIntersection;

import java.util.Date;
import java.util.Optional;

@Repository
public interface ExpenseRepo extends JpaRepository<Expense, Long> {
    public Iterable<Expense> findAllByBudgetList(BudgetList budgetList);
    public Optional<Expense> findByBudgetListAndId(BudgetList budgetList, Long id);

    @Query("SELECT e FROM Expense e WHERE e.expenseOwner.username = :#{#userName} " +
            "AND e.dateOfExpense BETWEEN :#{#startDate} AND :#{#endDate}")
    public Iterable<Expense> findAllByDateAndExpenseOwner(@Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate,
                                                          @Param("userName") String username);

}
