package com.learning.dao;

import com.learning.model.User;

import java.util.List;

public record UserPage(List<User> users, long totalUsers, int page, int totalPages) {
}
