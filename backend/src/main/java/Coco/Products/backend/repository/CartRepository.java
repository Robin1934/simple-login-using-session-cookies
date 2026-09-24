package Coco.Products.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Coco.Products.backend.entity.CartItem;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCustomerId(Long customerId);

    Optional<CartItem> findByCustomerIdAndProductId(
            Long customerId,
            Long productId
    );
}