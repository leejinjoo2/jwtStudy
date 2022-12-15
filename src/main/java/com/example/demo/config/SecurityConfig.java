package com.example.demo.config;

import com.example.demo.jwt.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // @PreAuthorize 사용을 위함
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final RedisTemplate redisTemplate;

    private final CorsConfig corsConfig;

    public SecurityConfig(
            TokenProvider tokenProvider,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            RedisTemplate redisTemplate,
            CorsConfig corsConfig) {
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.redisTemplate = redisTemplate;
        this.corsConfig = corsConfig;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http
                .cors().and()
                .csrf().disable()

                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                .and()
                .authorizeRequests() //HttpServletRequest를 사용하는 요청들에 대한 접근 제한을 설정하겠다는 의미
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .antMatchers("/api/admin").hasAnyAuthority("ROLE_ADMIN")

                .antMatchers("/api/signup","/api/login","/api/user/article/page").permitAll()//나머자 요청들은 모두 ok
                .antMatchers("/api/user/**", "/api/user/article/one").authenticated()

                .and()
                .apply(new JwtSecurityConfig(tokenProvider, redisTemplate))

                .and() // 로그아웃 설정
                .logout()
                .logoutUrl("/logout")
                //.logoutSuccessUrl("/main")
                .invalidateHttpSession(true);

        JwtFilter customFilter = new JwtFilter(tokenProvider, redisTemplate);
        http.addFilterBefore(corsConfig.corsFilter(), SecurityContextPersistenceFilter.class)
                .addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
