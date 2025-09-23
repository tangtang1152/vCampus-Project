package com.vCampus.common;

/**
 * 图书馆模块会话
 * 保存客户端侧的筛选、排序、分页与缓存元数据，便于实现 C/S 风格与异步加载。
 */
public final class LibrarySession {

    private static volatile String keyword;
    private static volatile String bookStatusFilter; // "全部"/"正常"/"下架"
    private static volatile String sortOption;       // "默认(最新)"/"书名↑"/...
    private static volatile String borrowStatus;     // 我的借阅筛选："全部"/"借出"/"已还"/"逾期"
    private static volatile int currentPage = 1;
    private static volatile int pageSize = 10;
    private static volatile long lastRefreshMillis;

    private LibrarySession() {}

    public static String getKeyword() { return keyword; }
    public static void setKeyword(String value) { keyword = value; }

    public static String getBookStatusFilter() { return bookStatusFilter; }
    public static void setBookStatusFilter(String value) { bookStatusFilter = value; }

    public static String getSortOption() { return sortOption; }
    public static void setSortOption(String value) { sortOption = value; }

    public static String getBorrowStatus() { return borrowStatus; }
    public static void setBorrowStatus(String value) { borrowStatus = value; }

    public static int getCurrentPage() { return currentPage; }
    public static void setCurrentPage(int value) { currentPage = Math.max(1, value); }

    public static int getPageSize() { return pageSize; }
    public static void setPageSize(int value) { pageSize = value <= 0 ? 10 : value; }

    public static long getLastRefreshMillis() { return lastRefreshMillis; }
    public static void setLastRefreshMillis(long value) { lastRefreshMillis = value; }
}


