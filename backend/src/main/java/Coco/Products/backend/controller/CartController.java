package Coco.Products.backend.controller;

import Coco.Products.backend.entity.CartItem;
import Coco.Products.backend.repository.CartRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartRepository cartRepository;

    public CartController(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // ADD TO CART
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        HttpSession session =
                httpRequest.getSession(false);

        if (session == null ||
            session.getAttribute("customerId") == null) {

            return ResponseEntity.status(401)
                    .body(Map.of("message", "Please login first"));
        }

        Long customerId =
                (Long) session.getAttribute("customerId");

        Long productId =
                Long.valueOf(request.get("productId").toString());

        int quantity =
                Integer.parseInt(request.get("quantity").toString());

        if (quantity <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Quantity must be greater than 0"));
        }

        CartItem item =
                cartRepository
                        .findByCustomerIdAndProductId(
                                customerId,
                                productId)
                        .orElse(null);

        if (item == null) {

            item = new CartItem(
                    customerId,
                    productId,
                    quantity
            );

        } else {

            item.setQuantity(
                    item.getQuantity() + quantity
            );
        }

        cartRepository.save(item);

        return ResponseEntity.ok(
                Map.of("message", "Product added to cart")
        );
    }

    // VIEW CART
    @GetMapping
    public ResponseEntity<?> getCart(
            HttpServletRequest httpRequest) {

        HttpSession session =
                httpRequest.getSession(false);

        if (session == null ||
            session.getAttribute("customerId") == null) {

            return ResponseEntity.status(401)
                    .body(Map.of("message", "Please login first"));
        }

        Long customerId =
                (Long) session.getAttribute("customerId");

        List<CartItem> cart =
                cartRepository.findByCustomerId(customerId);

        return ResponseEntity.ok(cart);
    }

    // REMOVE CART ITEM
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        HttpSession session =
                httpRequest.getSession(false);

        if (session == null ||
            session.getAttribute("customerId") == null) {

            return ResponseEntity.status(401)
                    .body(Map.of("message", "Please login first"));
        }

        Long customerId =
                (Long) session.getAttribute("customerId");

        CartItem item =
                cartRepository.findById(id)
                        .orElse(null);

        if (item == null ||
            !item.getCustomerId().equals(customerId)) {

            return ResponseEntity.notFound().build();
        }

        cartRepository.delete(item);

        return ResponseEntity.ok(
                Map.of("message", "Item removed")
        );
    }
}
