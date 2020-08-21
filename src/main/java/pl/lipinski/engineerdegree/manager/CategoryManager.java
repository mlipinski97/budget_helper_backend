package pl.lipinski.engineerdegree.manager;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.lipinski.engineerdegree.dao.entity.Category;
import pl.lipinski.engineerdegree.dao.repository.CategoryRepo;

import java.util.Optional;

@Service
public class CategoryManager {

    CategoryRepo categoryRepo;

    @Autowired
    public CategoryManager(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public Iterable<Category> findAll() {
        return categoryRepo.findAll();
    }

    public Category add(Category category) {
        return categoryRepo.save(category);
    }

    public Optional<Category> findByName(String categoryName) {
        return categoryRepo.findByCategoryName(categoryName);
    }

    public void editCategory(Category Category) {
        categoryRepo.save(Category);
    }

}
