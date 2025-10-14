package com.hotelbooking.account.validation;

import java.util.regex.Pattern;

public class PhoneValidator {

    // Regex cho số điện thoại Việt Nam
    private static final String VIETNAM_PHONE_REGEX = "^(\\+84|0)(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$";

    // Regex cho số điện thoại quốc tế (format E.164)
    private static final String INTERNATIONAL_PHONE_REGEX = "^\\+[1-9]\\d{1,14}$";

    // Regex cho số điện thoại đơn giản (chỉ số và dấu +)
    private static final String SIMPLE_PHONE_REGEX = "^[\\+]?[0-9]{10,15}$";

    private static final Pattern VIETNAM_PHONE_PATTERN = Pattern.compile(VIETNAM_PHONE_REGEX);
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile(INTERNATIONAL_PHONE_REGEX);
    private static final Pattern SIMPLE_PHONE_PATTERN = Pattern.compile(SIMPLE_PHONE_REGEX);

    /**
     * Validate số điện thoại Việt Nam
     * Hỗ trợ các định dạng:
     * - 0xxxxxxxxx (10 số bắt đầu bằng 0)
     * - +84xxxxxxxxx (bắt đầu bằng +84)
     *
     * @param phone Số điện thoại cần validate
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    public static boolean isValidVietnamesePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        // Loại bỏ khoảng trắng và dấu gạch ngang
        String cleanPhone = phone.replaceAll("[\\s\\-]", "");

        return VIETNAM_PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validate số điện thoại quốc tế theo chuẩn E.164
     * Format: +[country code][subscriber number]
     *
     * @param phone Số điện thoại cần validate
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    public static boolean isValidInternationalPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        // Loại bỏ khoảng trắng và dấu gạch ngang
        String cleanPhone = phone.replaceAll("[\\s\\-]", "");

        return INTERNATIONAL_PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validate số điện thoại đơn giản
     * Chấp nhận 10-15 chữ số, có thể bắt đầu bằng dấu +
     *
     * @param phone Số điện thoại cần validate
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    public static boolean isValidSimplePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        // Loại bỏ khoảng trắng và dấu gạch ngang
        String cleanPhone = phone.replaceAll("[\\s\\-]", "");

        return SIMPLE_PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validate số điện thoại tổng hợp
     * Kiểm tra cả định dạng Việt Nam và quốc tế
     *
     * @param phone Số điện thoại cần validate
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    public static boolean isValidPhone(String phone) {
        return isValidVietnamesePhone(phone) || isValidInternationalPhone(phone);
    }

    /**
     * Chuẩn hóa số điện thoại Việt Nam về định dạng +84xxxxxxxxx
     *
     * @param phone Số điện thoại đầu vào
     * @return Số điện thoại đã được chuẩn hóa hoặc null nếu không hợp lệ
     */
    public static String normalizeVietnamesePhone(String phone) {
        if (!isValidVietnamesePhone(phone)) {
            return null;
        }

        String cleanPhone = phone.replaceAll("[\\s\\-]", "");

        // Nếu bắt đầu bằng 0, thay thế bằng +84
        if (cleanPhone.startsWith("0")) {
            return "+84" + cleanPhone.substring(1);
        }

        // Nếu đã có +84, giữ nguyên
        if (cleanPhone.startsWith("+84")) {
            return cleanPhone;
        }

        return cleanPhone;
    }

    /**
     * Lấy thông tin về loại số điện thoại
     *
     * @param phone Số điện thoại cần kiểm tra
     * @return Thông tin về loại số điện thoại
     */
    public static String getPhoneType(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "INVALID";
        }

        if (isValidVietnamesePhone(phone)) {
            return "VIETNAMESE";
        } else if (isValidInternationalPhone(phone)) {
            return "INTERNATIONAL";
        } else if (isValidSimplePhone(phone)) {
            return "SIMPLE";
        } else {
            return "INVALID";
        }
    }
}
