package com.tht.split.core;

/** Policy remains cached across reboot; an offline error never clears a lock. */
public final class AccessRules {
    private AccessRules() {}
    public static String denial(boolean configured, boolean known, String state, int minimum,
                                int installed, long checkedAt, long now, long offlineSeconds) {
        if (!configured) return ""; // Development builds explicitly show remote control as unconfigured.
        if (!known) return "Cần kết nối mạng để đăng ký thiết bị";
        if ("locked".equals(state)) return "Thiết bị đã bị khóa";
        if (!"enabled".equals(state)) return "Thiết bị đang chờ nhà phát hành duyệt";
        if (installed < minimum) return "Cần cập nhật phiên bản bắt buộc";
        if (now < checkedAt || now - checkedAt > offlineSeconds * 1000L)
            return "Cần kết nối mạng để xác nhận quyền sử dụng";
        return "";
    }
    public static boolean exitCommand(String action) {
        return "stop".equals(action) || "home".equals(action) || "hide".equals(action);
    }
}
