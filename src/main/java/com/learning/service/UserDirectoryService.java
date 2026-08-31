package com.learning.service;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
import com.learning.dao.UserPage;
import com.learning.dao.UserPageRequest;

import java.sql.SQLException;
import java.util.Set;

public class UserDirectoryService {
    private static final int PAGE_SIZE = 10;
    private static final Set<String> SORT_FIELDS = Set.of("id", "username", "email", "created");
    private final UserDao userDao;

    public UserDirectoryService() {
        this(new HibernateUserDao());
    }

    UserDirectoryService(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserPage findUsers(String search, String sort, String direction, String pageParameter) throws SQLException {
        String safeSearch = search == null ? "" : search.trim();
        String safeSort = SORT_FIELDS.contains(sort) ? sort : "id";
        boolean ascending = "asc".equalsIgnoreCase(direction);
        int page = parsePage(pageParameter);
        UserPage result = userDao.findPage(new UserPageRequest(safeSearch, safeSort, ascending, page, PAGE_SIZE));
        if (page > result.totalPages()) {
            return userDao.findPage(new UserPageRequest(safeSearch, safeSort, ascending, result.totalPages(), PAGE_SIZE));
        }
        return result;
    }

    private int parsePage(String value) {
        try {
            int page = Integer.parseInt(value);
            return page > 0 ? page : 1;
        } catch (NumberFormatException | NullPointerException exception) {
            return 1;
        }
    }
}
