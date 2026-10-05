# Cài và kiểm thử 0.2.7-test trên EX2

Chưa test xe thật. Android headunit phải có ADB mở sẵn; app không mở khóa ADB hoặc
thay firmware/OTA. Quan sát bố cục khi xe đang đỗ.

## Cài lần đầu
```sh
adb install -r THT-Split-0.2.7-test.apk
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

Tắt phiên đang chia → Cài đặt → Off. Từ0.2.6, khóa tỷ lệ/kéo/đổi bên/đổi app/
fullscreen/restore; chỉ mở cặp và quản lý overlay/Home. X dừng overlay, giữ cửa sổ
Android, không gửi fullscreen cho hai app. Đóng cửa sổ sai từ phiên cũ bằng nút X
của Android rồi mở lại một lần; Off không sửa mode/bounds của task cũ.
Chọn tỷ lệ trước lúc mở; firmware có thể bỏ qua bounds của task đang tồn tại.
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
| Off ratio/drag | Thử 30/50/70 và kéo/thả; quan sát bounds thật, callback requested | Chờ LDPlayer/xe |
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

## Kiểm tra bản sửa LDPlayer0.2.1
1. Giữ Off, mở hai app, thử30:70 →50:50 →70:30. Ghi app/API nếu cửa sổ không đổi.
2. Kéo vạch riêng THT Split rồi thả, phân biệt vạch hệ thống của LDPlayer.
3. Cài đặt → Thanh điều khiển → Chờ ổn định: thử1,5s và3s với app khởi động nguội.
4. Thử app thiếu/quyền nổi bị thu hồi: màn chờ phải dọn, không kẹt overlay.
5. Tắt/khôi phục/swap/replace rồi ẩn/hiện: không tạo task trùng hoặc mất cặp.

## Kiểm tra0.2.2 theo phản hồi LDPlayer
- Start và preset: launcher phía sau hai cửa sổ, không còn trang cấu hình Split.
- Dấu kéo nhỏ12×32dp, chạm đơn không đổi tỷ lệ/mở app, kéo/thả không nhảy vị trí.
- Off: kiểm thực tế cửa sổ đổi hay chỉ handle đổi. Nếu task không nhận launch bounds,
  thử ADB nội bộ/freeform; không gọi Off resize thành công chỉ vì handle di chuyển.
- Thử Không chờ và1,5s; mở app thiếu phải dọn overlay và báo lỗi.

## Kiểm tra phục hồi0.2.5 / bridge v3
Cài APK, khởi động lại LDPlayer/màn để dừng bridge v2 rồi kết nối lại bằng cách
đang dùng. Không killall app_process. Nếu dùng thủ công, lấy/chạy lại lệnh bridge.
1. ADB/native: mở Maps/YouTube, đổi bên qua lại5 lần; không duplicate primary.
2. Đóng trái rồi bấm tỷ lệ/hiện thanh; cặp phải mở lại. Lặp với app phải.
3. Fullscreen từng bên rồi khôi phục5 lần; không ô đen/chồng hai app cùng bên.
4. Đổi app trái/phải khi đang chia; primary cũ phải được dọn.
5. Tỷ lệ/ẩn/hiện trên cặp khỏe không relaunch; kiểm nội dung app còn giữ.
6. X về app trái fullscreen và giữ dữ liệu. Ghi SDK/mode/ảnh/log nếu lỗi.
Off không có quyền sửa task, kiểm riêng và không suy kết quả ADB sang Off.

## Kiểm tra0.2.6 / bridge v4
1. ADB: cài, reboot LDPlayer/màn và kết nối lại để nạp bridge4.
2. Native → fullscreen → restore, đóng một app → mở lại, swap5 lần:
   không caption freeform/không trang cấu hình THT phía sau YouTube.
3. ADB/freeform → tắt → native; hai app ở stack khác nhau. Log bridge có stack IDs.
4. Off: đóng hai cửa sổ sai từ bản cũ bằng Android một lần, mở cùng cặp → X của THT
   → mở lại5 lần; X THT giữ cửa sổ, X Android đóng app. Kiểm tra bounds thật.
5. Off: ratio/apps/swap/full mờ và không bấm được; không grip. Voice ratio/swap/
   replace/fullscreen/restore nhận UNSUPPORTED_OPERATION, không đổi session.
6. Off hide/show/Home vẫn chạy. Khi task đã fullscreen từ ngoài app, Off không
   đảm bảo dựng lại được; đóng cửa sổ đó hoặc chuyển ADB, không tạo task trùng.

## Kiểm tra0.2.7 / bridge v5
Cài, reboot/kết nối bridge lại. ADB/native: thử30/40/50/60/70 khi thanh hiện, ẩn,
và sau fullscreen/restore. Hai app phải cùng top/bottom, không nằm sau toolbar
và không vào vùng điều hòa. Đổi khoảng chừa rồi khôi phục cặp để áp dụng.
Nếu toast Android chưa giữ bounds: xuất log hỗ trợ (last_layout có expected/actual
cả hai bên). Có thể chọn cửa sổ tự do thủ công; không fallback tự động.
Verified chỉ xác nhận mode/stack/bounds read-back, không xác nhận nội dung đã vẽ.
