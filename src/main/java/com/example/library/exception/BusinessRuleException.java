package com.example.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * بيتم رميه لما طلب صحيح شكليًا (validation) بس بيخالف قاعدة عمل،
 * زي محاولة استعارة كتاب مفيش منه نسخ متاحة، أو تجاوز حد الاستعارة المسموح.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
