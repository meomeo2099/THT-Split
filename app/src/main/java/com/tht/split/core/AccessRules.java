package com.tht.split.core;

/** Public application policy. Installation never requires enrollment or an offline lease. */
public final class AccessRules {
    private AccessRules() {}
    public static String denial(boolean configured, String state, int minimum, int installed) {
        if (!configured) return "";
        if ("locked".equals(state)) return "Ứng dụng đang tạm khóa";
        if (installed < minimum) return "Cần cập nhật phiên bản bắt buộc";
        return "";
    }
    public static boolean exitCommand(String action) {
        return "stop".equals(action) || "home".equals(action) || "hide".equals(action) || "recover".equals(action);
    }
}
