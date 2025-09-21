package edu.seu.campus.server.controller;

import com.vCampus.common.ApiResponse;
import com.vCampus.entity.Book;
import com.vCampus.entity.BorrowRecord;
import com.vCampus.entity.Reservation;
import com.vCampus.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/library")
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    @GetMapping("/books")
    public ApiResponse<List<Book>> searchBooks(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(libraryService.searchBooks(keyword, page, size));
    }

    @GetMapping("/books/advanced")
    public ApiResponse<List<Book>> searchBooksAdvanced(@RequestParam(defaultValue = "") String keyword,
                                                       @RequestParam(defaultValue = "全部") String status,
                                                       @RequestParam(defaultValue = "默认(最新)") String sort,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(libraryService.searchBooksAdvanced(keyword, status, sort, page, size));
    }

    @GetMapping("/my-borrows")
    public ApiResponse<List<BorrowRecord>> myBorrows(@RequestParam String userId,
                                                     @RequestParam(required = false) String status) {
        if (status == null || status.isBlank() || "全部".equals(status)) {
            return ApiResponse.ok(libraryService.listMyBorrows(userId));
        }
        return ApiResponse.ok(libraryService.listMyBorrowsByStatus(userId, status));
    }

    @GetMapping("/my-reservations")
    public ApiResponse<List<Reservation>> myReservations(@RequestParam String userId) {
        return ApiResponse.ok(libraryService.listMyReservations(userId));
    }

    @PostMapping("/borrow")
    public ApiResponse<com.vCampus.service.ServiceResult> borrow(@RequestBody java.util.Map<String, Object> body) {
        String userId = String.valueOf(body.get("userId"));
        Integer bookId = Integer.valueOf(String.valueOf(body.get("bookId")));
        Integer days = body.get("days") == null ? 30 : Integer.valueOf(String.valueOf(body.get("days")));
        return ApiResponse.ok(libraryService.borrowBookWithReason(userId, bookId, days));
    }

    @PostMapping("/return")
    public ApiResponse<com.vCampus.service.ServiceResult> returnBook(@RequestBody java.util.Map<String, Object> body) {
        String userId = String.valueOf(body.get("userId"));
        Integer recordId = Integer.valueOf(String.valueOf(body.get("recordId")));
        Integer bookId = Integer.valueOf(String.valueOf(body.get("bookId")));
        return ApiResponse.ok(libraryService.returnBookWithReason(userId, recordId, bookId));
    }

    @PostMapping("/renew")
    public ApiResponse<com.vCampus.service.ServiceResult> renew(@RequestBody java.util.Map<String, Object> body) {
        String userId = String.valueOf(body.get("userId"));
        Integer recordId = Integer.valueOf(String.valueOf(body.get("recordId")));
        Integer days = Integer.valueOf(String.valueOf(body.get("days")));
        Integer maxTimes = body.get("maxTimes") == null ? 1 : Integer.valueOf(String.valueOf(body.get("maxTimes")));
        return ApiResponse.ok(libraryService.renewBorrowWithReason(userId, recordId, days, maxTimes));
    }

    @PostMapping("/reserve")
    public ApiResponse<com.vCampus.service.ServiceResult> reserve(@RequestBody java.util.Map<String, Object> body) {
        String userId = String.valueOf(body.get("userId"));
        Integer bookId = Integer.valueOf(String.valueOf(body.get("bookId")));
        return ApiResponse.ok(libraryService.reserveBookWithReason(userId, bookId));
    }

    @PostMapping("/cancel-reservation")
    public ApiResponse<Boolean> cancelReservation(@RequestBody java.util.Map<String, Object> body) {
        String userId = String.valueOf(body.get("userId")); // 目前未使用，仅保留参数位
        Integer reservationId = Integer.valueOf(String.valueOf(body.get("reservationId")));
        return ApiResponse.ok(libraryService.cancelReservation(userId, reservationId));
    }
}


