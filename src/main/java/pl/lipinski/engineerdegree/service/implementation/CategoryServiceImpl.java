package pl.lipinski.engineerdegree.service.implementation;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.lipinski.engineerdegree.dao.entity.Category;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.repository.CategoryRepo;
import pl.lipinski.engineerdegree.service.CategoryService;
import pl.lipinski.engineerdegree.service.UserService;
import pl.lipinski.engineerdegree.util.error.ControllerError;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pl.lipinski.engineerdegree.util.error.ErrorCodes.*;
import static pl.lipinski.engineerdegree.util.error.ErrorMessages.*;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final UserService userService;

    public CategoryServiceImpl(CategoryRepo categoryRepo, UserService userService) {
        this.categoryRepo = categoryRepo;
        this.userService = userService;
    }

    public Iterable<Category> findAll() {
        return categoryRepo.findAll();
    }

    public Optional<Category> getByName(String categoryName) {
        return categoryRepo.findByCategoryName(categoryName);
    }

    public ResponseEntity<?> addCategory(String categoryName,
                                         MultipartFile categoryImage) {
        Optional<Category> possibleDuplicate = categoryRepo.findByCategoryName(categoryName);
        if (possibleDuplicate.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    CATEGORY_ALREADY_EXISTS_ERROR_CODE.getValue(),
                    Collections.singletonList(CATEGORY_ALREADY_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Category category = new Category();
        category.setCategoryName(categoryName);
        if (categoryImage != null) {
            String fileName = StringUtils.cleanPath(categoryImage.getName());
            if (fileName.contains("..")) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        IMAGE_EXTENSION_NOT_VALID_ERROR_CODE.getValue(),
                        Collections.singletonList(IMAGE_EXTENSION_NOT_VALID_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }

            try {
                category.setCategoryImage(categoryImage.getBytes());
            } catch (IOException e) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        UNABLE_TO_GET_BYTES_FROM_IMAGE_ERROR_CODE.getValue(),
                        Collections.singletonList(UNABLE_TO_GET_BYTES_FROM_IMAGE_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
        }
        return ResponseEntity.ok(categoryRepo.save(category));
    }

    public ResponseEntity<?> edit(String oldCategoryName,
                                  String newCategoryName,
                                  MultipartFile categoryImage) {
        Optional<Category> possibleDuplicate = categoryRepo.findByCategoryName(newCategoryName);
        if (possibleDuplicate.isPresent() && !oldCategoryName.equals(newCategoryName)) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    CATEGORY_ALREADY_EXISTS_ERROR_CODE.getValue(),
                    Collections.singletonList(CATEGORY_ALREADY_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<Category> categoryToUpdate = categoryRepo.findByCategoryName(oldCategoryName);
        if (!categoryToUpdate.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    CATEGORY_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(CATEGORY_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        categoryToUpdate.get().setCategoryName(newCategoryName);
        if (categoryImage != null) {
            String fileName = StringUtils.cleanPath(categoryImage.getName());
            if (fileName.contains("..")) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        IMAGE_EXTENSION_NOT_VALID_ERROR_CODE.getValue(),
                        Collections.singletonList(IMAGE_EXTENSION_NOT_VALID_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
            try {
                categoryToUpdate.get().setCategoryImage(categoryImage.getBytes());
            } catch (IOException e) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        UNABLE_TO_GET_BYTES_FROM_IMAGE_ERROR_CODE.getValue(),
                        Collections.singletonList(UNABLE_TO_GET_BYTES_FROM_IMAGE_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
        }
        categoryRepo.save(categoryToUpdate.get());
        return ResponseEntity.ok(categoryToUpdate.get());
    }

    public ResponseEntity<?> delete(String categoryName) {
        Category categoryToUpdate = categoryRepo.findByCategoryName(categoryName).orElseThrow(EntityNotFoundException::new);
        if (!validateAdminPermissions()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        categoryToUpdate.setDeleted(true);
        categoryRepo.save(categoryToUpdate);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> deleteMany(List<String> nameList) {
        for (String name : nameList) {
            Category categoryToUpdate = categoryRepo.findByCategoryName(name).orElseThrow(EntityNotFoundException::new);
            if (!validateAdminPermissions()) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                        Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
            categoryToUpdate.setDeleted(true);
            categoryRepo.save(categoryToUpdate);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean validateAdminPermissions() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(name);
        if (!user.isPresent()) {
            return false;
        }
        return user.get().getRoles().equals("ROLE_ADMIN");
    }
}
