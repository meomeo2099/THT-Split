package com.tht.split.core;

/** Compares requested screen pixels with bounds read back from Android. */
public final class BoundsCheck {
    private BoundsCheck() {}
    public static boolean matches(LayoutMath.Box expected,LayoutMath.Box actual,int tolerance) {
        if(tolerance<0)throw new IllegalArgumentException("Tolerance");
        return expected!=null&&actual!=null
            &&Math.abs(expected.left-actual.left)<=tolerance
            &&Math.abs(expected.top-actual.top)<=tolerance
            &&Math.abs(expected.right-actual.right)<=tolerance
            &&Math.abs(expected.bottom-actual.bottom)<=tolerance;
    }
}
