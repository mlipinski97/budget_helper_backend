package pl.lipinski.engineerdegree.service;


import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import pl.lipinski.engineerdegree.dao.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Iterable<Category> findAll();

    ResponseEntity<?> addCategory(String categoryName, MultipartFile categoryImage);

    ResponseEntity<?> edit(String oldCategoryName, String newCategoryName, MultipartFile categoryImage);

    ResponseEntity<?> delete(String categoryName);

    ResponseEntity<?> deleteMany(List<String> nameList);

    Optional<Category> getByName(String categoryName);
}
