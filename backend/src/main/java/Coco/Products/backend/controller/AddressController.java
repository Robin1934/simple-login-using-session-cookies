package Coco.Products.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Coco.Products.backend.entity.Address;
import Coco.Products.backend.repository.AddressRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressRepository addressRepository;

    public AddressController(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    // =========================
    // ADD ADDRESS
    // =========================

    @PostMapping
    public ResponseEntity<?> addAddress(
            @RequestBody Address address,
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("customerId") == null) {

            return ResponseEntity.status(401)
                    .body("Please login first");
        }

        Long customerId =
                (Long) session.getAttribute("customerId");

        // Address entity currently uses userId,
        // so store the logged-in customer's ID there.
        address.setUserId(customerId);

        return ResponseEntity.ok(
                addressRepository.save(address));
    }

    // =========================
    // GET MY ADDRESSES
    // =========================

    @GetMapping
    public ResponseEntity<?> getAddresses(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("customerId") == null) {

            return ResponseEntity.status(401)
                    .body("Please login first");
        }

        Long customerId =
                (Long) session.getAttribute("customerId");

        List<Address> addresses =
                addressRepository.findByUserId(customerId);

        return ResponseEntity.ok(addresses);
    }

    // =========================
    // DELETE ADDRESS
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(
            @PathVariable Long id,
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("customerId") == null) {

            return ResponseEntity.status(401)
                    .body("Please login first");
        }

        Long customerId =
                (Long) session.getAttribute("customerId");

        Address address =
                addressRepository.findById(id)
                        .orElse(null);

        if (address == null) {
            return ResponseEntity.notFound().build();
        }

        if (!address.getUserId().equals(customerId)) {

            return ResponseEntity.status(403)
                    .body("You cannot delete this address");
        }

        addressRepository.delete(address);

        return ResponseEntity.ok(
                "Address deleted successfully");
    }
}