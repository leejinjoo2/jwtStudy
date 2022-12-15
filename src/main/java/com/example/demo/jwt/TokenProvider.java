package com.example.demo.jwt;

import com.example.demo.domain.entity.Token;
import com.example.demo.dto.TokenDto;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.util.RedisUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Component
//@RequiredArgsConstructor
public class TokenProvider implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    private static final String AUTHORITIES_KEY = "auth";
    private final String secret;
    private final long tokenValidityInMilliseconds;
    private Key key;

    private Token token;

    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private RedisUtil redisUtil;

    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDto createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        String token = Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(new Date(now))
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();

        return TokenDto.builder()
                        .token(token)
                                .tokenExpiresIn(validity.getTime())
                .build();

    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            if(redisUtil.hasKeyBlackList(token)) {
                //아마 이거 땜에 에러 나는 건가.,,,?
                return false;
            }
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다.");
            //없는 토큰일 때
            //여기는 다시 로그인 페이지로 이동하게 하기
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.");
            SecurityContextHolder.clearContext();
            //그리고 여기서 redirection login페이지 주기
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public Long getExpiration(String token){
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration();
        Long now= new Date().getTime();
        return (expiration.getTime()-now);
    }

//    @ExceptionHandler(ExpiredJwtException.class)
//    @ResponseBody
//    public String authExceptionResolver(ExpiredJwtException e) {
//        logger.info("만료된 jwt토큰입니다.");
//        SecurityContextHolder.clearContext();
//        logger.info("로그인리다이렉트 오나요");//여기 안옴 나중에 여기 뜨게 하기!(아마 프론트에서 할듯)
//        return "redirect:/api/login";
//    }



}

