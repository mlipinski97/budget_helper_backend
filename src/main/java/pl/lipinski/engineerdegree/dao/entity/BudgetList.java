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
    @ManyToMany(mappedBy = "budgetLists")
    private Set<User> users;

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

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user){
        users.add(user);
    }

    public void deleteUser(User user){
        users.remove(user);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
