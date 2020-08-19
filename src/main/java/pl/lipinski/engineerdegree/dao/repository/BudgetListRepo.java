package pl.lipinski.engineerdegree.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;

@Repository
public interface BudgetListRepo extends JpaRepository<BudgetList, Long> {
    public Iterable<BudgetList> findByName(String name);
}
