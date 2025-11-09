package com.Food.FoodOrdering.service;

import com.Food.FoodOrdering.model.User;
import com.Food.FoodOrdering.model.UserPrincipal;
import com.Food.FoodOrdering.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepo repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        User user = repo.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("Not found"));

        return new UserPrincipal(user);




    }
}
