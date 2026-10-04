# Kiểm tra bản 0.1.0-test

## Đã thực hiện ở máy phát triển
Lệnh: `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
Kết quả cuối: BUILD SUCCESSFUL;11 tests,0 failures,0 errors; lint0 errors/11 warnings.

Tests thật kiểm tra:
- Bounds EX2 không chiếm vùng trên72px/dưới112px và48px toolbar.
- Ẩn thanh trả đúng chiều cao cho cả hai app, không đổi chiều rộng.
- Ratios 30–70 và divider lẻ không chồng bounds.
- Clamp kéo và từ chối insets sai không tạo viewport âm.
- Parser TaskRecord Android9/Task mới, UID prefix/component, không match package gần giống.

APK đã kiểm bằng apksigner verify: chữ ký v2 hợp lệ,1 signer.
Manifest aapt: com.tht.split, versionCode 1,0.1.0-test,min 26,target 32.
SHA256 APK tải về từ máy phát triển:
`a970e17688fd4c8f0836ac5bb2604731de2b7ff2c3206782aebbf429c4da5751`

Chỉ tắt lint ExpiredTargetSdkVersion cho deployment sideload headunit target 32;
không tắt kiểm tra API mới/permission chung. Cảnh báo còn lại gồm fixed landscape,
receiver token-auth exported, localization, adaptive icon metadata và physical-left positioning.
Đây không phải lint baseline bỏ qua lỗi build.

## CI
Run https://github.com/meomeo2099/THT-Split/actions/runs/37216288157 thành công
ở commit7c84d3e. Run đầu fail tại setup SDK vì package tools đã bỏ; đã sửa khai báo packages
thành platforms;android-35,build-tools;35.0.0,platform-tools. Workflow upload APK test giữ 7 ngày.
Mỗi push main code mới cần xem run tương ứng; không dùng success run cũ làm bằng chứng code mới.
APK CI debug có chữ ký khác máy phát triển nên checksum có thể khác dù cùng source.

## Chưa thực hiện
Chưa chạy UI trên thiết bị/emulator, chưa test bridge app_process/Binder trên EX2,
chưa xác minh insets OEM, native/freeform/resize/fullscreen/restore hoặc broadcast Voice thật.
SharedPreferences/preset CRUD và receiver token chưa có Android instrumentation test.
Không đánh dấu11unit tests là bằng chứng cho toàn bộ các phần này. Ma trận nằm ở DEVICE_TEST.md.
