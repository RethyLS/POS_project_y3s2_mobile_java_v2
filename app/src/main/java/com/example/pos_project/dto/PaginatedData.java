package com.example.pos_project.dto;

import com.google.gson.annotations.Expose;
import java.util.List;

public class PaginatedData<T> {
    @Expose
    private int current_page;
    @Expose
    private List<T> data;
    @Expose
    private int per_page;
    @Expose
    private int total;
    @Expose
    private int last_page;

    public PaginatedData() {}

    public int getCurrentPage() { return current_page; }
    public void setCurrentPage(int current_page) { this.current_page = current_page; }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }

    public int getPerPage() { return per_page; }
    public void setPerPage(int per_page) { this.per_page = per_page; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getLastPage() { return last_page; }
    public void setLastPage(int last_page) { this.last_page = last_page; }
}