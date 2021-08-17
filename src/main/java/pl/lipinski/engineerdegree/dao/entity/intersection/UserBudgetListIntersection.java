package pl.lipinski.engineerdegree.dao.entity.intersection;

import lombok.*;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "user_budget_intersection")
public class UserBudgetListIntersection {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User intersectionUser;

    @ManyToOne
    private BudgetList intersectionBudgetList;
}
