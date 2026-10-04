# Cài và kiểm thử 0.2.0-test trên EX2

Chưa test xe thật. Android headunit phải có ADB mở sẵn; app không mở khóa ADB hoặc
thay firmware/OTA. Quan sát bố cục khi xe đang đỗ.

## Cài lần đầu
```sh
adb install -r THT-Split-0.2.0-test.apk
adb shell appops set com.tht.split SYSTEM_ALERT_WINDOW allow
```
Nếu khác chữ ký, không gỡ app khi còn dữ liệu cặp cần giữ. Dùng khóa cũ hoặc ghi lại
cấu hình trước khi chủ động cài mới. Package com.tht.split không đổi.
Mở app ít nhất một lần sau cài; app force-stop có thể không nhận boot broadcast.

## ADB nội bộ (ưu tiên)
1. Cài đặt → Kết nối & quyền → ADB nội bộ.
2. Mặc định127.0.0.1:5555. Nếu xe dùng7777 hoặc cổng khác, nhập cổng thực tế của xe.
   Host chỉ cho phép loopback hoặc địa chỉ thuộc chính màn, không máy tính/xe khác.
3. Bấm Kiểm tra / kết nối. Xác nhận “luôn cho phép” khóa THT Split nếu xe hiện hộp thoại.
   Khóa của app khác khóa máy tính: máy tính đã được cho phép không đảm bảo app đã được cho phép.
4. Nếu timeout vì chờ xác nhận: xác nhận rồi bấm lại; private RSA được giữ nguyên.
5. Sau khi bridge sẵn sàng UID2000/API xe: chọn cặp, Bắt đầu chia và quan sát hai app.
6. Bật “Chuẩn bị ADB sau khởi động / cập nhật”. Boot chưa sẵn sàng vẫn sẽ thử khi lệnh tới.

ADB nội bộ cổ điển RSA TCP, không Android11+ TLS pairing. Wi-Fi/firmware/adbd phải
sẵn sàng, không cam kết boot hook chạy được trên mọi firmware. Đổi Wi-Fi không đổi localhost.
Bridge0.1 cũ còn chạy: reboot màn rồi thử; không tự kill mọi app_process.

## Off — mở trực tiếp
Bật freeform lần đầu bằng ADB nội bộ trong app hoặc từ máy tính:
```sh
adb shell settings get global enable_freeform_support
adb shell settings get global force_resizable_activities
adb shell settings put global enable_freeform_support 1
adb shell settings put global force_resizable_activities 1
```
Ghi giá trị cũ của hai lệnh get trước khi đổi bằng máy tính. Nếu dùng nút trong app,
app lưu giá trị cũ và có nút khôi phục. Có thể cần reboot; không tự thay policy hidden API toàn hệ thống.

Tắt phiên đang chia → Cài đặt → Off. Chọn tỷ lệ trước khi mở cặp. Không có bridge,
không vạch kéo/nút tỷ lệ live; toolbar là overlay tạm, hide/show không resize app.
Quan sát native launch: firmware/app có thể bỏ qua bounds hoặc tái tạo Activity.
Off trả requested, verified=false; không coi đó là đã chia thành công.

## Quyền hệ thống
Chỉ chọn được khi Android đã cấp MANAGE_ACTIVITY_STACKS. APK ký debug thường không
được cấp; cài được APK hoặc cùng chữ ký Voice không chứng minh có quyền platform.
Không có sharedUserId trong bản thường. Provision khóa phù hợp ngoài git nếu có;
kiểm permission/certificate trên chính xe trước dùng. Không tự tạo khóa giả hoặc flash OTA.

## Lệnh bridge thủ công dự phòng
Cài đặt → Kết nối & quyền → ADB nội bộ → Lấy lệnh bridge thủ công.
Trên máy tính nhập `adb shell`, rồi dán nguyên lệnh lấy trong app có token riêng:
```sh
CLASSPATH=$(pm path com.tht.split | head -n 1 | cut -d: -f2) nohup app_process /system/bin com.tht.split.bridge.ShellServer TOKEN_RIENG_CUA_MAY >/data/local/tmp/tht-split-bridge.log 2>&1 </dev/null &
```
Không chạy chữ placeholder TOKEN_RIENG_CUA_MAY. Token bridge khác token Voice.
Nếu port bị chiếm/token không khớp: đọc log, xác minh đúng PID trước dừng; không killall app_process.

## Ma trận cần xác minh
| Ca thử | Điều kiện đạt | Trạng thái |
|---|---|---|
| ADB lần đầu | Xác nhận khóa → UID2000/version2 | Chờ xe |
| Reboot thật | Khi lệnh tới app tự kết nối, không máy tính | Chờ xe |
| Đổi Wi-Fi | localhost/cổng vẫn dùng được khi adbd hoạt động | Chờ xe |
| Native/freeform | Hai app, mode/bounds/insets thật đúng | Chờ xe |
| Resize/drag ADB | Năm tỷ lệ, kéo/thả; không mất trạng thái app | Chờ xe |
| Off | Hai app được mở đúng bounds hoặc ghi rõ firmware không hỗ trợ | Chờ xe |
| Off ratio live | UI khóa; API báo hạn chế | Chờ xe |
| Hide/show/Home/X | Đúng hành vi, không nhét launcher xe vào ô phụ | Chờ xe |
| Presets | Lưu/sửa/xóa/restart không mất cấu hình | Chờ xe |
| Voice v2 | Token/signature/callback/requested/error/busy/timeout | Chờ tích hợp Voice |
| System | Certificate/quyền thực được kiểm trước thao tác | Chờ điều kiện |

Khoảng chừa72/112px chỉ là mặc định thử. Native có thể bỏ qua OEM insets. Phải nhìn
ảnh thật; không dựa ảnh mockup hoặc các unit test để kết luận layout xe.

## Thu log và khôi phục
App → Cập nhật → Xuất log hỗ trợ. Trên máy tính:
```sh
adb logcat -d -s THTSplit AndroidRuntime
adb shell cat /data/local/tmp/tht-split-bridge.log
adb shell dumpsys activity activities
adb shell wm size
adb shell wm density
```
Log xuất từ app không gồm token/private key. Dumpsys có tên app, xem trước khi chia sẻ.
Ghi firmware/API/cổng/mode/ảnh/thao tác. Khi lỗi dùng Home/thanh xe, thử lại X sau
khi kết nối. Freeform có nút khôi phục trong app với ADB/system; Off không có quyền đổi settings.
