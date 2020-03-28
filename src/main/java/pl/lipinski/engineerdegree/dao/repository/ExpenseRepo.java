package pl.lipinski.engineerdegree.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lipinski.engineerdegree.dao.entity.Expense;

@Repository
public interface ExpenseRepo extends JpaRepository<Expense, Long> {
}
