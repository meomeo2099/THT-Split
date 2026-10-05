package com.tht.split.core;

/** Serial task transitions; independent of Binder so the ordering can be tested. */
public final class PairController {
    public interface Tasks {
        void clearPrimary() throws Exception;
        int launch(boolean left, int mode) throws Exception;
        int find(boolean left) throws Exception;
        int mode(int task) throws Exception;
        int stack(int task) throws Exception;
        void isolate(int task) throws Exception;
        void setMode(int task, int mode) throws Exception;
        void resize(int left, int right) throws Exception;
        void focus(int task) throws Exception;
    }
    private final Tasks tasks;
    private final boolean nativeMode;
    public PairController(Tasks tasks, boolean nativeMode) { this.tasks=tasks; this.nativeMode=nativeMode; }
    public void layout() throws Exception {
        // A display can only have one primary stack. Dismiss it before swapping roles.
        tasks.clearPrimary();
        int left=tasks.launch(true,nativeMode?1:5);
        int right=tasks.launch(false,nativeMode?1:5);
        // Android 9 setTaskWindowingMode changes the whole stack, not just this task.
        tasks.isolate(left);
        tasks.isolate(right);
        tasks.setMode(left,nativeMode?3:5);
        tasks.setMode(right,nativeMode?4:5);
        tasks.resize(left,right);
        if(tasks.stack(left)==tasks.stack(right)||tasks.mode(left)!=(nativeMode?3:5)||tasks.mode(right)!=(nativeMode?4:5))
            throw new java.io.IOException("Firmware không giữ chế độ chia yêu cầu");
        tasks.focus(right);
    }
    public void resize() throws Exception {
        int left=tasks.find(true),right=tasks.find(false);
        if(left<0||right<0||tasks.stack(left)==tasks.stack(right)||tasks.mode(left)!=(nativeMode?3:5)||tasks.mode(right)!=(nativeMode?4:5)) {
            layout(); // Reopen a closed app or restore a pair collapsed by Android.
        } else tasks.resize(left,right);
    }
}
