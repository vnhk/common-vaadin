package com.bervan.common.service;

import com.bervan.common.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiKeyService {
    private final UserDetailsService userDetailsService;
    @Value("${api.keys}")
    private List<String> API_KEYS = new ArrayList<>();

    @Value("${api.keys.usernames}")
    private List<String> users = new ArrayList<>();

    public ApiKeyService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public User getUserByAPIKey(String apiKey) {
        int i = API_KEYS.indexOf(apiKey);
        UserDetails userDetails = userDetailsService.loadUserByUsername(users.get(i));
        return (User) userDetails;
    }


}
