package com.kenya.service;

import com.kenya.domain.User;

public interface UserService {
    int registerUser(User user);
    LoginResult login(int accountId, String pin);
}