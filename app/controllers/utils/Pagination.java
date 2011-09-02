package controllers.utils;

/**
 * 
 * @author f.meurisse
 */
public class Pagination {
    private int page;
    private int pageSize;
    private int pageCount;
    private long elementCount;

    public static final int DEFAULT_PER_PAGE = 15;
    public static final int MAX_PAGE = 11;

    public Pagination() {
        this(DEFAULT_PER_PAGE);
    }

    public Pagination(int pageSize) {
        this.pageSize = pageSize;
        this.page = 1;
    }

    public void setElementCount(long elementCount) {
        this.elementCount = elementCount;
        updatePageCount();
    }

    public long getElementCount() {
        return this.elementCount;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        updatePageCount();
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public int getPageCount() {
        return this.pageCount;
    }

    public void setPage(int page) {
        this.page = page;
        if (this.page < 1) {
            this.page = 1;
        }
    }

    public int getPage() {
        return this.page;
    }

    private void updatePageCount() {
        this.pageCount = (int) Math.ceil((double) this.elementCount / this.pageSize);
        if (this.page > this.pageCount) {
            this.page = this.pageCount;
        }
    }
}
