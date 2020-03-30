package pl.lipinski.engineerdegree.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @JsonIgnore
    @Transient
    private String passwordConfirmation;

    @Column(nullable = false)
    private boolean isEnabled;

    @Column(nullable = false)
    private String roles;

    @JsonIgnore
    @OneToMany(mappedBy = "expenseOwner")
    private Set<Expense> expenses;

    @JsonIgnore
    @ManyToMany
    private Set<BudgetList> budgetLists;

    public User() {
    }

    //for testing purpouse only.
    public User(String username, String password, boolean isEnabled, String roles) {
        this.username = username;
        this.password = password;
        this.isEnabled = isEnabled;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Set<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(Set<Expense> expenses) {
        this.expenses = expenses;
    }

    public Set<BudgetList> getBudgetLists() {
        return budgetLists;
    }

    public void setBudgetLists(Set<BudgetList> budgetLists) {
        this.budgetLists = budgetLists;
    }
}
