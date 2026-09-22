package com.kenya.persistence;

import com.kenya.domain.User;

public interface UserDAO {
    int registerUser(User user);
    User getUserByUserId(int userId);
}