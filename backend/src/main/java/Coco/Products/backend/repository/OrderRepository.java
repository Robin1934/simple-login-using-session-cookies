package Coco.Products.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Coco.Products.backend.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}
