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

CI0.2.2 build/unit/lint đạt tại commit8083a19c8135ca1504c8723ad01e4c614e55a2ec:
https://github.com/meomeo2099/THT-Split/actions/runs/37262534677
APK artifact: com.tht.split / versionCode4 /0.2.2-test/min26/target32.
SHA256: 3eb9758bc98f7e119cfa09b88bcb7c1c1935f05a54fe2df00cc8110c99ff6a24.
Cert v2 APK CI0.2.2 khác bản0.2.1 đã gửi; không cài đè được bản đó.

## Mốc0.2.5 — phục hồi cửa sổ
Commit38712989f64cddf324cd82640c64a8b219a4b4c2; GitHub Actions build/unit/lint đạt:
https://github.com/meomeo2099/THT-Split/actions/runs/37269596828
Thêm7 regression tests PairController cho thứ tự swap/rebuild và task/mode thiếu.
Test dùng fake task manager, không chứng minh Binder/LDPlayer/xe thực tế.
APK artifact: com.tht.split / versionCode7 /0.2.5-test/min26/target32.
SHA256: 7ae9e2577c6081658b3f06103cd685c16c66970faa666a6b269bcfa3beb9bf8d.
Bridge v3 cần reboot/kết nối lại để không dùng code v2 còn sống; không tự kill listener.
APK CI dùng debug signer runner, không đảm bảo cập nhật đè bản test trước.
Chưa kiểm bản mới trên LDPlayer/xe; Off không có quyền quản lý task.

## Mốc0.2.6 — stack riêng và Off
Commit e0856f96cd3ada4ccfa5e9fee2a5d0e62c3e7df4; CI build/unit/lint đạt:
https://github.com/meomeo2099/THT-Split/actions/runs/37271611577
Fake task manager mô hình hóa setMode thay cả stack và launch reuse bỏ qua mode.
Thêm3 tests shared/native/freeform/reuse,2 tests policy Off; sửa test thứ tự có focus.
Đây là kiểm logic, chưa kiểm Binder/LDPlayer/xe thực tế. Không gọi Off mở lại đã đúng.
APK: com.tht.split/versionCode8/0.2.6-test/min26/target32.
SHA256: 79d53b3b1263f12408a7b904a6fc2d1386ad999519ba7395d425df6e961afbe2.
Bridge v4 cần reboot/kết nối lại; APK CI có thể khác signer bản test cũ.
Cửa sổ Off sai từ phiên cũ phải đóng bằng Android một lần; X THT chỉ dừng overlay.

## Mốc0.2.7 — khoảng chừa secondary native
CI đầu bắt JSONException chưa khai báo trong bounds(JSONArray); đã sửa.
Bản cuối5012d4f24ae8adb8de4f0c82f6ee0157e4cb861a build/unit/lint đạt:
https://github.com/meomeo2099/THT-Split/actions/runs/37272679706
Thêm3 tests bounds: secondary tràn display, snapped ratio khác yêu cầu, sai số pixel.
Không chứng minh resizeStack/getTaskBounds trên LDPlayer/xe thực tế.
APK: com.tht.split/versionCode9/0.2.7-test/min26/target32.
SHA256: 12eccd8bb789296f147d37624fa9041c2c87ca2049a3388518c8337221eb88b6.
Bridge v5 cần reboot/kết nối lại. APK CI có thể khác signer bản cũ.
Nếu read-back khác yêu cầu, verified=false/callback requested và log có expected/actual.

## Mốc0.2.8 — chuyển toàn màn hình
Commit c391b83124fa58be2df00d74ab0b03ecd1e6587a; CI build/unit/lint đạt:
https://github.com/meomeo2099/THT-Split/actions/runs/37275295920
Thêm5 tests: reset bounds cả hai trước focus, bounds cũ dù mode đã1, stop fallback,
target đóng không làm thay đổi app còn lại, cả hai đóng không ảnh hưởng task khác.
Fake tests chỉ kiểm thứ tự/trạng thái; chưa kiểm Binder/configuration/Maps renderer
trên LDPlayer hoặc GPU xe. Ảnh UI Maps toàn màn nhưng lớp bản đồ vẽ thiếu chưa có
log bounds, không đủ chứng minh nguyên nhân là LDPlayer hoặc task bounds.
APK: com.tht.split/versionCode10/0.2.8-test/min26/target32.
SHA256: 24db6193704236693a0dc0b78502be13796e6caac6b5067597ce2640f7041b11.
Bridge v6 cần reboot/kết nối lại. APK debug CI có thể khác signer bản trước;
không gỡ app khi chưa giữ cấu hình. Cần khóa ký ổn định ngoài git trước release.
Full read-back đúng chỉ xác nhận mode/bounds, không chứng minh lớp bản đồ đã vẽ hết.
