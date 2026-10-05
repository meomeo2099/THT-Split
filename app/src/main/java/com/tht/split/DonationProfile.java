package com.tht.split;

/** Public donation details supplied and authorized for inclusion by the publisher. */
public final class DonationProfile {
    private DonationProfile() {}
    public static final String BANK = "MB Bank";
    public static final String BANK_BIN = "970422";
    public static final String ACCOUNT = "67777777778";
    public static final String HOLDER = "TRAN HUU TAI";
    public static final String QR_ASSET = "donation-qr.jpg";
    // Original image from https://img.vietqr.io/image/MB-67777777778-qr_only.png.
    // The response is JPEG; its original bytes are bundled so the QR works offline.
}
