package com.Food.FoodOrdering.controller;


import com.Food.FoodOrdering.dto.AuthRequestDto;
import com.Food.FoodOrdering.dto.AuthResponseDto;
import com.Food.FoodOrdering.dto.RegisterRequestDto;
import com.Food.FoodOrdering.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService service;



    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerUser(@Valid @RequestBody RegisterRequestDto req){
              var res=service.registerUser(req);
              return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> logIn(@Valid @RequestBody AuthRequestDto req){
            var res=service.logIn(req);
            return ResponseEntity.ok(res);
    }

}
