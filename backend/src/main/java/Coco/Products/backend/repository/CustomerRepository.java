package Coco.Products.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Coco.Products.backend.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
}