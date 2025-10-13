package com.hotelbooking.account.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaginationResponse<T> {
    private List<T> items;
    private Paging paging;

    public PaginationResponse(List<T> items, Paging paging) {
        this.items = items;
        this.paging = paging;
    }

    @Getter
    @Setter
    public class Paging {
        private int total;
        private int pageNumber;
        private int pageSize;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;


        public Paging(int total, int pageNumber, int pageSize, int totalPages, boolean hasNext, boolean hasPrevious) {
            this.total = total;
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.totalPages = totalPages;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }
    }
}
