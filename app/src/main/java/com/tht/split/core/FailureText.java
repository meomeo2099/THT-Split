package com.tht.split.core;

/** Bounded diagnostics without exposing connection secrets or encoded keys. */
public final class FailureText {
    private FailureText() {}
    public static String describe(Throwable error) {
        Throwable cause=error;
        for(int i=0;i<8&&cause.getCause()!=null&&cause.getCause()!=cause;i++)cause=cause.getCause();
        String message=cause.getMessage();
        if(message==null)message="";
        message=message.replaceAll("(?i)[0-9a-f]{64}","[đã ẩn]")
                .replaceAll("[A-Za-z0-9+/]{80,}={0,2}","[đã ẩn]").replace('\n',' ').replace('\r',' ');
        if(message.length()>240)message=message.substring(0,240)+"…";
        return cause.getClass().getSimpleName()+(message.isEmpty()?"":": "+message);
    }
}
