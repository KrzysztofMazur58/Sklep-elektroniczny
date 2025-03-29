package com.example.sklepElektroniczny.repository;

import com.example.sklepElektroniczny.entity.AppRole;
import com.example.sklepElektroniczny.entity.Role;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
