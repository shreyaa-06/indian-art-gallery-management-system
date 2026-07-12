package com.artgallery.model;

import java.util.List;

public class PagedResult<T> {
    private List<T> data;
    private int total;
    private int page;
    private int pageSize;
    private int totalPages;

    public PagedResult(List<T> data, int total, int page, int pageSize) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
    }

    public List<T> getData() { return data; }
    public int getTotal() { return total; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return totalPages; }
}
