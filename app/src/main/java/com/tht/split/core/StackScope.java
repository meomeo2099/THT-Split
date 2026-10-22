package com.tht.split.core;

/** Only a stack containing exclusively the requested pair may be collapsed in OEM mode. */
public final class StackScope {
    private StackScope() {}
    public static boolean owns(String[] names,String left,String right){
        if(names==null||names.length==0||left==null||right==null)return false;
        for(String name:names)if(!matches(name,left)&&!matches(name,right))return false;
        return true;
    }
    private static boolean matches(String name,String pkg){return name!=null&&!pkg.isEmpty()&&(name.equals(pkg)||name.startsWith(pkg+"/"));}
}
