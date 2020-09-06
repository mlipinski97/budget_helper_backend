package pl.lipinski.engineerdegree.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;

import java.util.Optional;

@Repository
public interface BudgetListRepo extends JpaRepository<BudgetList, Long> {
    public Iterable<BudgetList> findByName(String name);
}
