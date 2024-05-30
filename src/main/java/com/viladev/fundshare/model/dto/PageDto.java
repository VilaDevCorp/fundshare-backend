package com.viladev.fundshare.model.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PageDto<T> {
    int pageNumber;
    boolean hasNext;
    List<T> data;

    public PageDto(int pageNumber, boolean hasNext, List<T> data) {
        this.pageNumber = pageNumber;
        this.hasNext = hasNext;
        this.data = data;
    }
}
