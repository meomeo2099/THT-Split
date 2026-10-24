package com.tht.split.core;

import java.util.*;

/**
 * Android 9 ROMs can crash the docked divider while a pair is being split (R02 1.3.0 reference,
 * THTVoice docs/reference/ihu629g-20261022-r02-130/SPLIT_SCREEN.md). The crash buffer is read
 * before and after a native layout; a new marker line means stop instead of retrying into a hung
 * screen, and native split is refused for a cooldown. No transport is switched automatically.
 */
public final class DividerCrashGuard {
    public static final String[] MARKERS={"DockedStackDividerController","WindowState.getFrameLw"};
    /** logcat -e pattern for the markers. */
    public static final String PATTERN="DockedStackDividerController|WindowState\\.getFrameLw";
    public static final long COOLDOWN_MS=10*60*1000L;
    public static final String CRASH_MESSAGE="ROM vừa lỗi thanh chia đôi; đã dừng để tránh treo màn hình. Chọn Cửa sổ tự do hoặc thử lại sau 10 phút";
    public static final String COOLDOWN_MESSAGE="ROM vừa lỗi thanh chia đôi; tạm không dùng chia đôi hệ thống 10 phút. Chọn Cửa sổ tự do hoặc thử lại sau";
    private DividerCrashGuard() {}

    static Set<String> markerLines(String log) {
        Set<String> lines=new HashSet<>();
        if(log==null)return lines;
        for(String line:log.split("\n")){
            String l=line.trim();
            for(String m:MARKERS)if(l.contains(m)){lines.add(l);break;}
        }
        return lines;
    }

    /** True when [after] holds a marker line [before] did not. Unreadable logs never count as a crash. */
    public static boolean crashedSince(String before,String after) {
        if(before==null||after==null)return false;
        Set<String> seen=markerLines(before);
        for(String l:markerLines(after))if(!seen.contains(l))return true;
        return false;
    }

    public static boolean coolingDown(long crashAt,long now) {
        return crashAt>0&&now>=crashAt&&now-crashAt<COOLDOWN_MS;
    }
}
