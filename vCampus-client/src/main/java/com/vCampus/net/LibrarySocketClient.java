package com.vCampus.net;

import com.vCampus.common.ConfigManager;

public class LibrarySocketClient {
    public static LibrarySocketClient fromConfig() { return new LibrarySocketClient(); }

    public com.vCampus.net.CourseGrabResult borrow(String userId, Integer bookId, int days) {
        return new CourseGrabResult(true, "(stub) 借书成功");
    }

    public com.vCampus.net.CourseGrabResult reserve(String userId, Integer bookId) {
        return new CourseGrabResult(true, "(stub) 预约成功");
    }

    public com.vCampus.net.CourseGrabResult renew(String userId, Integer recordId, int days) {
        return new CourseGrabResult(true, "(stub) 续借成功");
    }

    public com.vCampus.net.CourseGrabResult returnBook(String userId, Integer recordId, Integer bookId) {
        return new CourseGrabResult(true, "(stub) 归还成功");
    }
}



