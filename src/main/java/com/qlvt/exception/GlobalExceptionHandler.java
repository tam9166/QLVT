package com.qlvt.exception;

import org.springframework.ui.Model;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.OptimisticLockException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class, IllegalStateException.class})
    public String handleBusiness(RuntimeException exception, Model model) {
        model.addAttribute("title", "Không thể thực hiện thao tác");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException exception, Model model) {
        model.addAttribute("title", "Không tìm thấy dữ liệu");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidation(MethodArgumentNotValidException exception, Model model) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Dữ liệu nhập chưa hợp lệ");
        model.addAttribute("title", "Dữ liệu chưa hợp lệ");
        model.addAttribute("message", message);
        return "error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException exception, Model model) {
        model.addAttribute("title", "Không có quyền truy cập");
        model.addAttribute("message", exception.getMessage() == null ? "Bạn không có quyền thực hiện thao tác này." : exception.getMessage());
        return "error";
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    public String handleOptimisticLock(Exception exception, Model model) {
        model.addAttribute("title", "Dữ liệu tồn kho vừa thay đổi");
        model.addAttribute("message", "Dữ liệu tồn kho vừa được người khác cập nhật. Vui lòng tải lại trang và thực hiện lại thao tác.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handle(Exception exception, Model model) {
        model.addAttribute("title", "Lỗi hệ thống");
        model.addAttribute("message", exception.getMessage() == null ? "Có lỗi xảy ra, vui lòng thử lại." : exception.getMessage());
        return "error";
    }
}