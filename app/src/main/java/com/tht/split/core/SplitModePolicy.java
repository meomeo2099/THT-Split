package com.tht.split.core;

/** Explicit modes never fall back. Compatibility mode keeps auto away from native. */
public final class SplitModePolicy {
    private SplitModePolicy() {}
    public interface Operation<T> { T run(boolean nativeMode) throws Exception; }
    public static <T> T run(String mode,boolean compatible,Operation<T> operation)throws Exception {
        if("native".equals(mode))return operation.run(true);
        if("freeform".equals(mode))return operation.run(false);
        if(!"auto".equals(mode))throw new IllegalArgumentException("Chế độ chia không hợp lệ");
        try{return operation.run(false);}catch(Exception freeform){
            if(compatible)throw new java.io.IOException("Cửa sổ tự do chưa chạy. Chế độ tương thích không tự chuyển sang chia native; hãy bật hỗ trợ cửa sổ tự do và mở lại cặp.",freeform);
            return operation.run(true);
        }
    }
}
