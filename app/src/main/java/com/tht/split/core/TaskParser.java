package com.tht.split.core;
import java.util.regex.*;
/** Fallback only; shell bridge first uses getAllStackInfos via named methods. */
public final class TaskParser {
    public static int find(String dump,String pkg) {
        Pattern p=Pattern.compile("(?:TaskRecord|Task)\\{[^\\n}]*?#(\\d+)[^\\n}]*?(?:A=|a=|\\s)(?:\\d+:)?"+Pattern.quote(pkg)+"(?=[/\\s}])");
        Matcher m=p.matcher(dump);if(m.find())return Integer.parseInt(m.group(1));
        return -1;
    }
}
