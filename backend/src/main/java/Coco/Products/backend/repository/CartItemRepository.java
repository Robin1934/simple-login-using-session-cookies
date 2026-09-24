package Coco.Products.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Coco.Products.backend.entity.CartItem;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCustomerId(Long customerId);

    List<CartItem> findByCustomerIdAndProductId(
            Long customerId,
            Long productId
    );
}