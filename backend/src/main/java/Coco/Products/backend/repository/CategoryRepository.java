package Coco.Products.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Coco.Products.backend.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}