package com.example.demo.util;

import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
//@NoArgsConstructor
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;
    //private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    public static Optional<String> getCurrentUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            //logger.debug("Security Context에 인증 정보가 없습니다.");
            return Optional.empty();
        }

        String username = null;
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            username = springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }

        return Optional.ofNullable(username);
    }

    public Long getCurrentUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Security Context에 인증 정보가 없습니다.");
        }
        System.out.println(authentication.getName());
        Long userId;
        userId= userRepository.findOneWithAuthoritiesByUsername(authentication.getName()).get().getUserId();

        //여기 나중에 해결하고 가야함 !!!!
        return userId;
    }

}