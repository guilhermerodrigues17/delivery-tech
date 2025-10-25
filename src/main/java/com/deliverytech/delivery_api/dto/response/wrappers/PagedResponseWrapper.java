package com.deliverytech.delivery_api.dto.response.wrappers;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Getter
public class PagedResponseWrapper<T> {

    private final List<T> content;
    private final PagedModel.PageMetadata page;
    private final PageLinks links;

    private PagedResponseWrapper(Page<T> pageData) {
        this.content = pageData.getContent();
        this.page = new PagedModel.PageMetadata(
                pageData.getSize(),
                pageData.getNumber(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
        this.links = buildLinks(pageData);
    }

    public static <T> PagedResponseWrapper<T> of(Page<T> pageData) {
        return new PagedResponseWrapper<>(pageData);
    }

    private PageLinks buildLinks(Page<T> pageData) {
        UriComponentsBuilder uriBuilder = ServletUriComponentsBuilder.fromCurrentRequestUri();
        String next = null;
        String prev = null;

        String first = uriBuilder.replaceQueryParam("page", 0).build().toUriString();

        String last = uriBuilder.replaceQueryParam(
                "page", pageData.getTotalPages() > 0 ? pageData.getTotalPages() - 1 : 0).build().toUriString();

        if (pageData.hasNext()) {
            next = uriBuilder.replaceQueryParam("page", pageData.getNumber() + 1)
                    .build().toUriString();
        }

        if (pageData.hasPrevious()) {
            prev = uriBuilder.replaceQueryParam("page", pageData.getNumber() - 1)
                    .build().toUriString();
        }

        return new PageLinks(first, last, next, prev);
    }

    private record PageLinks(
            String first,
            String last,
            String next,
            String prev
    ) {}
}
