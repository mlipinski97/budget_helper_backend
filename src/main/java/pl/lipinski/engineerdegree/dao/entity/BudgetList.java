package pl.lipinski.engineerdegree.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "budget_lists")
public class BudgetList {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double budgetValue;

    @JsonIgnore
    @OneToMany(mappedBy = "budgetList")
    private Set<Expense> expenses;

    @JsonIgnore
    @OneToMany(mappedBy = "intersectionBudgetList")
    private Set<UserBudgetListIntersection> intersections;

    public BudgetList() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getBudgetValue() {
        return budgetValue;
    }

    public void setBudgetValue(Double budgetValue) {
        this.budgetValue = budgetValue;
    }

    public Set<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(Set<Expense> expenses) {
        this.expenses = expenses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<UserBudgetListIntersection> getIntersections() {
        return intersections;
    }

    public void setIntersections(Set<UserBudgetListIntersection> intersections) {
        this.intersections = intersections;
    }
}
