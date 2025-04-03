package com.example.sklepElektroniczny.repository;

import com.example.sklepElektroniczny.entity.CartElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartElementRepository extends JpaRepository<CartElement, Long> {

    @Query("SELECT ci FROM CartElement ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    CartElement findCartElementByProductIdAndCartId(Long cartId, Long elementId);

    @Modifying
    @Query("DELETE FROM CartElement ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    void deleteCartItemByProductIdAndCartId(Long cartId, Long productId);
}
