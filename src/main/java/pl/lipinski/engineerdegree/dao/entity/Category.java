package pl.lipinski.engineerdegree.dao.entity;

import javax.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    private String categoryName;

    public Category() {
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
