package com.example.demo.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.domain.entity.Token;
import com.example.demo.domain.entity.User;
import com.example.demo.dto.LoginDto;
import com.example.demo.dto.TokenDto;
import com.example.demo.jwt.JwtFilter;
import com.example.demo.jwt.TokenProvider;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserService;
import io.jsonwebtoken.Jwt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Transient;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;


@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Value("${jwt.token-validity-in-seconds}")
    long tokenValidityInSeconds;

    public AuthController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        TokenDto jwt = tokenProvider.createToken(authentication);

        Date now = new Date();

        LocalTime localTime = LocalTime.now().plusSeconds(tokenValidityInSeconds);
        LocalDateTime localDateTime = localTime.atDate(LocalDate.now());
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);

        System.out.println(userRepository.findByUsername(authentication.getName()).get()==null);

        Token token = Token.builder()
                .user(userRepository.findByUsername(authentication.getName()).get())
                .tokenName(jwt.getToken())
                .createdDate(now)
                .expiredDate(date)
                .build();

        tokenRepository.save(token);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        logger.info("로그인을 하는 나는 여기로 옵니다.");

        return new ResponseEntity<>(new TokenDto(jwt.getToken(), jwt.getTokenExpiresIn()), httpHeaders, HttpStatus.OK);
    }

    @PostMapping("/user/logout")
    public void logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        DecodedJWT decodedToken=authService.getDecodedToken(token);
        authService.logout(token);
        SecurityContextHolder.getContext().setAuthentication(null);
//        그럼 이 때 리다이렉션으로 홈화면으로 이동하기
    }
}