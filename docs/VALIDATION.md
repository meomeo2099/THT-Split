# Kiểm tra 0.2.0-test — 05/10/2026

## Máy phát triển
Đã chạy clean build rồi kiểm tra lại bản cuối:
`:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` — BUILD SUCCESSFUL.
17 tests, 0 failures, 0 errors; lint 0 errors / 10 warnings.

11 bài trước kiểm tra geometry/preset bounds và parser task.
6 bài mới kiểm tra protocol ADB bằng fixture little-endian độc lập, checksum sai,
độ dài/magic sai, cấu trúc RSA2048 Android public key/Montgomery và chữ ký challenge
đã băm SHA1 (không băm lại). Không coi đây là test kết nối adbd thật.

Manifest: com.tht.split / versionCode2 / 0.2.0-test / min26 / target32.
Apksigner: v2 hợp lệ, một signer. Không thay khóa debug của máy phát triển.
Đã kiểm class_defs trong DEX: không đóng gói sun.misc.Unsafe stub compileOnly.
License LSPosed đầy đủ có trong assets APK.
SHA256 APK máy phát triển:
`85aa02946cee78d3d275c28945617d5fed877727844e402562d10a8da0497a02`

Lint ProtectedPermissions chỉ được giải thích/suppress ở permission MANAGE_ACTIVITY_STACKS:
backend system có guard runtime, không tự xin/cấp quyền cho APK thường. Không tắt lint
ProtectedPermissions toàn dự án. ExpiredTargetSdkVersion vẫn là ngoại lệ sideload đã có.

## Chưa kiểm tra trên Android thật
Chưa chạy UI/bridge/local ADB/Binder trên EX2 hoặc emulator/điện thoại.
Chưa xác minh boot/authorize RSA/insets/resize/fullscreen/Off/receiver callback thật.
Phía THTVoice chưa sửa client. Cần chạy DEVICE_TEST.md và THTVOICE_INTEGRATION.md.
Không gọi kết quả build hoặc success từ request launch là đã test xe thành công.

## GitHub Actions
Workflow main tiếp tục build/unit/lint và lưu THT-Split-test-apk 7 ngày.
Xem run tương ứng commit mới; run phiên bản0.1 không chứng minh phiên bản0.2.
APK CI debug có thể khác signer/checksum máy phát triển.

## Bản sửa0.2.1 sau phản hồi LDPlayer
Người dùng báo0.2.0 Off chia được trên LDPlayer; bản đó khóa tỷ lệ/không vạch kéo.
0.2.1 thêm yêu cầu launch bounds cho ratio/drag, vùng chạm48×80dp, màn chờ có
khoảng ổn định cấu hình. Chưa chạy bản sửa trên LDPlayer/xe; không xác nhận hết lag.
`git diff --check` đạt. Build local bị chặn do cache AGP8.7.3 không còn và không
truy cập kho phụ thuộc; cần xem workflow tương ứng commit0.2.1, không dùng17tests
bản0.2.0 làm kết quả build bản sửa. Package giữ nguyên, versionCode3.

CI bản0.2.1 đã đạt build/unit/lint tại commit d871475a25f725cfa381935d25dca40b2abbbfa8:
https://github.com/meomeo2099/THT-Split/actions/runs/37261177497
APK tải từ artifact: package com.tht.split, versionCode3,0.2.1-test,min26,target32.
SHA256: e34bc1e70d87650b0c400188f5c1c65c9e8826d0ed3795bd24ffee1fe55cd968.
APK CI dùng khóa debug runner; khóa ký máy phát triển cũ không còn trong workspace,
không đảm bảo cập nhật đè APK0.2.0. Không gỡ bản cũ để vượt lỗi chữ ký khi chưa giữ cấu hình.

## Mốc0.2.2
Sửa nền launcher sau start/preset, handle nhỏ, mặc định chờ0ms và gesture touch slop.
Chưa chạy UI trên thiết bị; cần workflow commit0.2.2 xác minh build/unit/lint.
