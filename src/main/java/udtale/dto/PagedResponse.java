package udtale.dto;

import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {
    private List<T> data;
    private int page;
    private int pages;
    private int size;
    private long total;
    private boolean first;
    private boolean last;

    public PagedResponse(Page<T> page) {
        this.data = page.getContent();
        this.page = page.getNumber();
        this.pages = page.getTotalPages();
        this.size = page.getSize();
        this.total = page.getTotalElements();
        this.first = page.isFirst();
        this.last = page.isLast();
    }


}
