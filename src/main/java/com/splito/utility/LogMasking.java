package com.splito.utility;

public class LogMasking {
    public static String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 1) return "***" + (at >= 0 ? email.substring(at) : "");
        return email.charAt(0) + "***" + email.substring(at);
    }

    public static String maskPhone(String phone) {
        if (phone == null) return null;
        int n = phone.length();
        if (n <= 4) return "****";
        return "****" + phone.substring(n - 4);
    }

    public static String safe(String s) {
        if (s == null) return null;
        return s.replaceAll("[\\r\\n\\t]", "_");
    }
}
