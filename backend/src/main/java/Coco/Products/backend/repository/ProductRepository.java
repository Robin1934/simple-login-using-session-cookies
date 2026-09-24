package Coco.Products.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Coco.Products.backend.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}