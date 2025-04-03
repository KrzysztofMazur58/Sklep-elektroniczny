package com.example.sklepElektroniczny.util;


import com.example.sklepElektroniczny.entity.User;
import com.example.sklepElektroniczny.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    @Autowired
    private UserRepository userRepo;

    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepo.findByUserName(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + auth.getName()));

        return currentUser.getEmail();
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepo.findByUserName(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + auth.getName()));

        return currentUser.getUserId();
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepo.findByUserName(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + auth.getName()));
    }
}

