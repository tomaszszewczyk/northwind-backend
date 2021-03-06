package tomaszszewczyk.controllers;

import lombok.Data;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tomaszszewczyk.entities.Category;
import tomaszszewczyk.repositories.CategoryRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Data
    public static class CategoryDTO {
        private Integer categoryID;
        private String categoryName;
        private String description;
    }

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DozerBeanMapper mapper;

    @Transactional
    @GetMapping
    public List<CategoryDTO> getAll() {
        return categoryRepository
                .findAll()
                .stream()
                .map(x -> mapper.map(x, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    @GetMapping("/{id}")
    public CategoryDTO getByID(@PathVariable Integer id) {
        return mapper.map(categoryRepository.findByID(id), CategoryDTO.class);
    }

    @Transactional
    @PostMapping
    public CategoryDTO createCategory(@RequestBody CategoryDTO category) {
        Category categoryToCreate = mapper.map(category, Category.class);
        Category createdCategory = categoryRepository.save(categoryToCreate);
        return mapper.map(createdCategory, CategoryDTO.class);
    }

    @Transactional
    @PutMapping("/{id}")
    public CategoryDTO updateCategory(@PathVariable Integer id, @RequestBody CategoryDTO newCategory) {
        Category category = categoryRepository.findByID(id);
        newCategory.setCategoryID(id);
        mapper.map(newCategory, category);
        Category saved = categoryRepository.save(category);
        return mapper.map(saved, CategoryDTO.class);
    }

    @Transactional
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Integer id) {
        categoryRepository.delete(id);
    }

    @Transactional
    @GetMapping("/{id}/products")
    public List<ProductController.ProductDTO> getProducts(@PathVariable Integer id) {
        return categoryRepository
                .findByID(id)
                .getProducts()
                .stream()
                .map(x -> mapper.map(x, ProductController.ProductDTO.class))
                .collect(Collectors.toList());
    }
}
