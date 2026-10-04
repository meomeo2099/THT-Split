# Cài và kiểm thử EX2

Bản đầu: test APK, chưa xác nhận trên xe. Không thay firmware/OTA. Android headunit phải
có ADB mở sẵn; tài liệu này không mở khóa ADB. Dùng trên xe đang đỗ để quan sát layout.

## 1. Build và cài
```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Nếu khác chữ ký: không uninstall khi còn preset cần giữ. Dùng lại khóa ký cũ để build,
hoặc xuất/ghi lại preset trước khi chủ động cài mới. Package luôn com.tht.split.

## 2. Cấp overlay
Mở app → Cài đặt → Kết nối & quyền → Cấp quyền hiển thị nổi.
Có thể dùng trên máy có ADB:
```sh
adb shell appops set com.tht.split SYSTEM_ALERT_WINDOW allow
```
OEM có thể hạn chế appops/settings; kiểm tra trạng thái thật trong app.

## 3. Chạy bridge (mỗi lần reboot / cập nhật APK)
Mở app → Cài đặt → Kết nối & quyền → Lấy lệnh khởi động bridge.
Trên máy tính/điện thoại đã kết nối ADB:
```sh
adb shell
```
Sau đó dán **nguyên lệnh riêng của máy lấy trong app**, dạng:
```sh
CLASSPATH=$(pm path com.tht.split | head -n 1 | cut -d: -f2) nohup app_process /system/bin   com.tht.split.bridge.ShellServer TOKEN_RIENG_CUA_MAY   >/data/local/tmp/tht-split-bridge.log 2>&1 </dev/null &
```
Không dùng placeholder TOKEN_RIENG_CUA_MAY để chạy. Token thật phải đủ 64 hex.
App kiểm tra bridge phải báo UID2000 (shell) hoặc 0; chưa có UID đúng thì chưa thử chia.
Bridge ở localhost39871. Nếu Address already in use / token không đúng: đọc log,
dừng đúng tiến trình bridge cũ rồi chạy lại; không dùng killall app_process.
Ví dụ sau khi xác minh PID bằng `ps -A | grep com.tht.split.bridge.ShellServer`:
```sh
kill PID_CUA_THT_SPLIT_BRIDGE
```
Không đóng app server khác. Reboot cũng dừng bridge. `nohup` không có trên firmware nào
thì bỏ từ nohup và giữ phiên adb shell trong thời gian test.

## 4. Ma trận test
| Kiểm tra | Cách làm / kết quả cần có | Trạng thái |
|---|---|---|
| Hai app | Chọn app launcher thật trên máy; Bắt đầu chia | Chờ xe |
| Native | Mode3/4, không recents/filler đè app | Chờ xe |
| 5 tỷ lệ | Chọn lần lượt30/40/50/60/70; nhìn bounds hai app | Chờ xe |
| Kéo vạch | Kéo, thả; 2 cửa sổ cập nhật, không task mới | Chờ xe |
| Thanh ẩn/hiện | Chevron trả chiều cao thanh; vẫn2app; nút nổi hiện lại | Chờ xe |
| Tự ẩn | 3/5/8/10 giây, tương tác reset timer | Chờ xe |
| Đổi từng app | Thay bên trái/phải, app còn lại giữ nội dung | Chờ xe |
| Đổi bên | Nội dung hai bên đổi; không force-stop mất nhạc | Chờ xe |
| Toàn màn/restore | Chọn từng bên; bấm nút nổi khôi phục cặp | Chờ xe |
| Home/X | Home launcher; X app trái toàn màn, service/overlay dừng | Chờ xe |
| App tự đóng | X không kẹt chỉ vì app bên kia không còn task | Chờ xe |
| OEM bars | Thanh trạng thái, HVAC không bị che; chỉnh top/bottom | Chờ xe |
| Preset CRUD | Tạo, mở, sửa app/tên/tỷ lệ, xóa/hủy xác nhận, rebootapp | Chờ UI máy |
| API Voice | Bật API, token đúng/sai; lệnh trong VOICE_API | Chờ tích hợp |
| Mất bridge | Có lỗi rõ; không báo thành công hoặc tự cấp quyền | Chờ xe |
| Freeform | Bật hỗ trợ, reboot nếu cần; mode5 và caption OEM | Chờ xe |

Khoảng chừa72/112px chỉ là mặc định tham khảo. Native resizeDockedStack có thể bỏ qua
insets hoặc ép chia ngang tùy OEM; nếu vậy không dùng ảnh mockup để kết luận đã đúng.
Freeform có caption; chưa có NoCaption cho Android9. Không tự force-stop/gỡ app.
Thao tác replace/swap dùng launch lại task hiện có; việc OEM tái tạo Activity phải đo thực tế.

## 5. Thu log khi lỗi
Trong app → Cập nhật → Xuất log hỗ trợ (không token).
Trên máy có ADB:
```sh
adb logcat -d -s THTSplit AndroidRuntime
adb shell cat /data/local/tmp/tht-split-bridge.log
adb shell dumpsys activity activities
adb shell wm size
adb shell wm density
```
Dumpsys có tên app/hoạt động; xem và ẩn thông tin cá nhân trước chia sẻ.
Ghi firmware, API, mode, app packages, thao tác, ảnh thật và lỗi. Không chỉ gửi “không chạy”.

## 6. Khôi phục
X hoặc Home; nếu bridge mất, mở launcher bằng thanh OEM. Khởi động lại bridge, thử X.
Freeform global settings có nút khôi phục giá trị cũ; nếu bật xong reboot phải kết nối bridge
lại trước khi khôi phục. Không đổi hidden_api_policy, không flash OTA hoặc gỡ app xe.
