package com.example.demo.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.domain.entity.Token;
import com.example.demo.domain.entity.User;
import com.example.demo.dto.LoginDto;
import com.example.demo.jwt.TokenProvider;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.RedisUtil;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${jwt.secret}")
    String secret;

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;
    private final RedisTemplate redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public String verifyToken(String token) {
        DecodedJWT decodedJWT = getDecodedToken(token);

        String username = decodedJWT.getClaim("username").asString();
        return username;
    }

    public DecodedJWT getDecodedToken(String token){
        token = token.replaceAll("Bearer ", "");
//        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(Base64.getDecoder().decode(secret)))
//                .build()
//                .verify(token);
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(Base64.getDecoder().decode(secret)))
                .build()
                .verify(token);
        return decodedJWT;
    }

    @Transactional
    public void logout(String token) {
        //????????? ?????? Bearer ?????? ?????? ??????????????? ??? ??????!!
        //?????? ????????? !!! ???????????? ?????? ??? ?????? ?????????
        String token1=token.replaceAll("Bearer ","");
        // 1. Access Token ??????
        if (!tokenProvider.validateToken(token1)) {
            throw new AuthorizationServiceException("???????????????..");
        }

        // 2. Access Token ?????? authentication ??? ???????????????.
        Authentication authentication = tokenProvider.getAuthentication(token1);

        // 3. DB??? ????????? Refresh Token ??????
        Token token2=tokenRepository.findByUser(userRepository.findByUsername(authentication.getName()).get());
        tokenRepository.deleteById(token2.getTokenId());

        // 4. Access Token blacklist??? ???????????? ???????????????
        // ?????? ????????? ????????? ?????? ??????????????? ??????
        Long expiration = tokenProvider.getExpiration(token1);
        redisUtil.setBlackList(token1, "logout", expiration, TimeUnit.MILLISECONDS);
        //redisTemplate.opsForValue().set(token1, "logout", expiration, TimeUnit.MILLISECONDS);

    }
}
