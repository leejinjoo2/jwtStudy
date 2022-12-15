package com.example.demo.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.domain.entity.Authority;
import com.example.demo.domain.entity.Token;
import com.example.demo.domain.entity.User;
import com.example.demo.dto.LoginDto;
import com.example.demo.dto.UserDto;
import com.example.demo.jwt.TokenProvider;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    private TokenRepository tokenRepository;
    private TokenProvider tokenProvider;
    private final SecurityUtil securityUtil;

    @Transactional
    public User signup(UserDto userDto) {
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
            throw new RuntimeException("이미 가입되어 있는 유저입니다.");
        }

        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();

        User user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .nickname(userDto.getNickname())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void logout(){
        SecurityContextHolder.clearContext();
        System.out.println("로그아웃 성공");
    }

    @Transactional
    public User findByTokens(Token token){
        return userRepository.findByTokens(token);
    }

    @Transactional
    public User findByUsername(String username){
        return userRepository.findByUsername(username).get();
    }


    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getMyUserWithAuthorities() {
        return SecurityUtil.getCurrentUsername().flatMap(userRepository::findOneWithAuthoritiesByUsername);
    }


    public UserDto getMyInfoBySecurity(){
        return userRepository.findById(securityUtil.getCurrentUserId()).map(UserDto::of).orElseThrow(()->new RuntimeException("로그인 유저 정보가 없습니다"));
    }
}