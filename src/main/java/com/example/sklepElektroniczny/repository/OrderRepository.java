package com.example.sklepElektroniczny.repository;

import com.example.sklepElektroniczny.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByEmail(String email);

}
