package Coco.Products.backend.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Coco.Products.backend.entity.Address;
import Coco.Products.backend.entity.CartItem;
import Coco.Products.backend.entity.Order;
import Coco.Products.backend.entity.OrderItem;
import Coco.Products.backend.entity.Product;
import Coco.Products.backend.repository.AddressRepository;
import Coco.Products.backend.repository.CartRepository;
import Coco.Products.backend.repository.OrderItemRepository;
import Coco.Products.backend.repository.OrderRepository;
import Coco.Products.backend.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;

    public OrderController(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            AddressRepository addressRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
    }

    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<?> checkout(
            @RequestParam Long addressId,
            HttpServletRequest request) {

        // 1. Check login
        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("customerId") == null) {

            return ResponseEntity.status(401)
                    .body(Map.of(
                            "message",
                            "Please login first"
                    ));
        }

        Long customerId =
                (Long) session.getAttribute("customerId");

        // 2. Check address
        Address address =
                addressRepository.findById(addressId)
                        .orElse(null);

        if (address == null) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Address not found"
                    ));
        }

        if (!address.getUserId().equals(customerId)) {

            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "This address does not belong to you"
                    ));
        }

        // 3. Get customer's cart items
        List<CartItem> cartItems =
                cartRepository.findByCustomerId(customerId);

        if (cartItems.isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Cart is empty"
                    ));
        }

        // 4. Calculate total
        double totalAmount = 0.0;

        for (CartItem cartItem : cartItems) {

            Product product =
                    productRepository.findById(
                            cartItem.getProductId()
                    ).orElse(null);

            if (product == null) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Product not found: "
                                        + cartItem.getProductId()
                        ));
            }

            if (product.getStock()
                    < cartItem.getQuantity()) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Not enough stock for: "
                                        + product.getName()
                        ));
            }

            totalAmount +=
                    product.getPrice()
                            * cartItem.getQuantity();
        }

        // 5. Create order
        Order order = new Order();

        order.setUserId(customerId);
        order.setAddressId(addressId);

        // Convert double to BigDecimal
        order.setTotalAmount(
                BigDecimal.valueOf(totalAmount)
        );

        order.setStatus("PLACED");
        order.setPaymentStatus("PENDING");

        Order savedOrder =
                orderRepository.save(order);

        // 6. Create order items
        for (CartItem cartItem : cartItems) {

            Product product =
                    productRepository.findById(
                            cartItem.getProductId()
                    ).orElseThrow();

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setOrderId(
                    savedOrder.getId()
            );

            orderItem.setProductId(
                    product.getId()
            );

            orderItem.setQuantity(
                    cartItem.getQuantity()
            );

            // Product price is double
            // OrderItem price expects BigDecimal
            orderItem.setPrice(
                    BigDecimal.valueOf(
                            product.getPrice()
                    )
            );

            orderItemRepository.save(orderItem);

            // Reduce stock
            product.setStock(
                    product.getStock()
                            - cartItem.getQuantity()
            );

            productRepository.save(product);
        }

        // 7. Clear customer's cart
        cartRepository.deleteAll(cartItems);

        // 8. Response
        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "message",
                "Order placed successfully"
        );

        response.put(
                "orderId",
                savedOrder.getId()
        );

        response.put(
                "totalAmount",
                totalAmount
        );

        response.put(
                "status",
                savedOrder.getStatus()
        );

        response.put(
                "paymentStatus",
                savedOrder.getPaymentStatus()
        );

        return ResponseEntity.ok(response);
    }

    // Get logged-in customer's orders
    @GetMapping
    public ResponseEntity<?> getMyOrders(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("customerId") == null) {

            return ResponseEntity.status(401)
                    .body(Map.of(
                            "message",
                            "Please login first"
                    ));
        }

        Long customerId =
                (Long) session.getAttribute("customerId");

        List<Order> orders =
                orderRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                customerId
                        );

        return ResponseEntity.ok(orders);
    }

    // Get one order
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(
            @PathVariable Long id,
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("customerId") == null) {

            return ResponseEntity.status(401)
                    .body(Map.of(
                            "message",
                            "Please login first"
                    ));
        }

        Long customerId =
                (Long) session.getAttribute("customerId");

        Order order =
                orderRepository.findById(id)
                        .orElse(null);

        if (order == null) {

            return ResponseEntity.notFound()
                    .build();
        }

        if (!order.getUserId().equals(customerId)) {

            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "You cannot view this order"
                    ));
        }

        List<OrderItem> items =
                orderItemRepository.findByOrderId(id);

        Map<String, Object> response =
                new HashMap<>();

        response.put("order", order);
        response.put("items", items);

        return ResponseEntity.ok(response);
    }
}