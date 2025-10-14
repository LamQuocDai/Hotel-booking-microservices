package com.hotelbooking.account.dto;

import org.springframework.stereotype.Service;

public class PaginationDTO {
    public int pageNumber;
    public int pageSize;
    public String search;
    public String sortBy;
    public String sortDirection;

    public PaginationDTO(int pageNumber, int pageSize, String search, String sortBy, String sortDirection) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.search = search;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }
}
