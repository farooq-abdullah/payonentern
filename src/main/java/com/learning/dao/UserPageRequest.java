package com.learning.dao;

public record UserPageRequest(String search, String sortField, boolean ascending, int page, int pageSize) {
}
