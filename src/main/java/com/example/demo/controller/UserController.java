package com.example.demo.controller;

import com.example.demo.domain.entity.User;
import com.example.demo.dto.UserDto;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signup(
            @Valid @RequestBody UserDto userDto
    ) {
        return ResponseEntity.ok(userService.signup(userDto));
    }

    @GetMapping("/user/me")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<UserDto> getMyUserInfo() {
        UserDto myInfoBySecurity = userService.getMyInfoBySecurity();
        //System.out.println(myInfoBySecurity.toString());
        //여기 안오는거 확인
        return ResponseEntity.ok((myInfoBySecurity));
    }

//    @GetMapping("/user/{username}")
//    @PreAuthorize("hasAnyRole('ADMIN')")
//    public ResponseEntity<User> getUserInfo(@PathVariable String username) {
//        return ResponseEntity.ok(userService.getUserWithAuthorities(username).get());
//    }

}