package com.Food.FoodOrdering.service;

import com.Food.FoodOrdering.dto.AuthRequestDto;
import com.Food.FoodOrdering.dto.AuthResponseDto;
import com.Food.FoodOrdering.dto.RegisterRequestDto;
import com.Food.FoodOrdering.exception.BadRequestException;
import com.Food.FoodOrdering.model.User;
import com.Food.FoodOrdering.repo.UserRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepo repo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

    public AuthResponseDto registerUser(@Valid RegisterRequestDto req) {

        if(repo.existsByEmail(req.getEmail())){
            throw new BadRequestException("Email already Exists!");
        }
        var user= User.builder().userName(req.getUserName()).email(req.getEmail()).password(encoder.encode(req.getPassword())).build();
        user.getRoles().add("ROLE_USER");
        repo.save(user);
        var token = jwtService.generateToken(user.getEmail(), user.getRoles());
        return new AuthResponseDto(token);

    }


    public AuthResponseDto logIn(@Valid AuthRequestDto req) {

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (AuthenticationException ex) {
            throw new BadRequestException("Invalid credentials");
        }
        var user = repo.findByEmail(req.getEmail()).orElseThrow(() -> new BadRequestException("User not found"));
        var token = jwtService.generateToken(user.getEmail(), user.getRoles());
        return new AuthResponseDto(token);

    }
}
