# THT Split 0.3.24-methods — các cách chia màn hình

Package `com.tht.split`, versionCode36, cùng chứng thư THT_SIGN đang dùng.
Dùng nền EX2/0.3.23 và ghép phần tương thích xe từ0.3.21-thtsign.
Không dùng lề trái/profile riêng Coolray2025. EX2 và Coolray2026 cần thử trên xe.

## Chọn phương thức điều khiển

Cài đặt → Kết nối & quyền → Phương thức:

| Phương thức | Điều kiện và hành vi |
|---|---|
| Quyền hệ thống — không ADB/Bridge | Android phải cấp MANAGE_ACTIVITY_STACKS. Gọi API task trực tiếp. Thiếu quyền thì báo lỗi, không chuyển ADB/Off. Không cần token Bridge hoặc máy tính để mở Bridge. |
| ADB nội bộ / Bridge | ADB TCP đã bật trên chính màn. Giữ RSA/token và bootstrap hiện tại; lần đầu có thể cần xác nhận khóa. Có lệnh dự phòng trong app. |
| Off — mở trực tiếp | Không dùng Bridge. Yêu cầu freeform được bật sẵn; không xác minh/sửa task đang chạy. Khóa tỷ lệ/kéo/đổi bên/đổi app/fullscreen/restore/recover. |

Bridge là phần thực thi của ADB, không phải một lựa chọn cấp quyền khác. Không thêm
Telnet, Shizuku hoặc phụ thuộc APK chia màn bên ngoài. Không tự bật adbd của firmware.
Tắt phiên chia trước khi đổi phương thức. Giao diện, Voice và boot dùng đúng lựa chọn
đã lưu; chế độ chia không tự đổi phương thức điều khiển.

Bản THT_SIGN cũ ép quyền hệ thống bất kể giá trị đang lưu. Khi cập nhật lần đầu,
giữ phương thức thực tế đó là System; từ đó mọi lựa chọn ADB/Off/System đều được nhớ.
Không đổi token Voice/RSA, preset hoặc tùy chọn khác. Bản platformtest riêng vẫn
chỉ dùng hệ thống; APK methods gốc có đủ ba cách.

## Chọn bố cục

Cài đặt → Chia màn hình:

- **Chia đôi hệ thống**: chỉ native, không thử freeform khi lỗi.
- **Cửa sổ tự do**: chỉ freeform, không tự chuyển native khi lỗi.
- **Tự động**: ưu tiên freeform; nếu tắt tương thích, freeform lỗi có thể thử native.
  Khi bật tương thích, Auto báo lỗi freeform và không tự quay lại native.

Muốn chỉ chia bằng quyền hệ thống: chọn phương thức System ở trên, rồi chọn một
trong ba bố cục. Muốn tránh native hoàn toàn: chọn System + Cửa sổ tự do.
Giữ cấu hình mode đã chọn ở bản cũ; cập nhật không tự đổi lựa chọn native cũ sang Auto.
Off luôn yêu cầu freeform, không phụ thuộc lựa chọn bố cục của System/ADB.

## Xe đang gặp lỗi mất thanh khi đổi bên

1. Bật Cài đặt → Thanh điều khiển → **Tương thích giao diện xe**.
2. Chọn System + Cửa sổ tự do hoặc Auto. Bật hỗ trợ cửa sổ tự do trong Kết nối & quyền
   nếu cần; Android có thể yêu cầu khởi động lại màn để áp dụng.
3. Thoát phiên cũ và mở cặp lại. Freeform vẫn có đổi bên/tỷ lệ. Native trong chế độ
   tương thích tạm khóa đổi bên; chọn thứ tự hai app trước khi chia.

Native tương thích giữ nguyên vùng chia khi ẩn/hiện thanh. Màn nghỉ/thức dựng lại
thanh THT nếu service còn chạy; không hứa phục hồi giao diện OEM nếu firmware lỗi.
Chỉ sửa stack thuộc cặp khi bật tương thích; gặp task ngoài cặp thì báo thoát phiên
cũ trước. Nút Gỡ kẹt/lệnh `recover` vẫn là thao tác riêng: thoát chia, về Home và
khởi động lại SystemUI; không tự chạy khi đổi phương thức hoặc ngủ/thức.

Giữ phát hiện lỗi thanh chia của0.3.23. Cooldown10phút của System lưu riêng trên
thiết bị, tồn tại khi tiến trình mở lại; ADB daemon vẫn giữ cooldown trong RAM.
Không xác định được lần crash nếu tiến trình chết trước khi ghi hoặc log không đọc được.

## Cập nhật và kiểm thử

- Cài đè APK cùng signer; không gỡ/xóa dữ liệu. Không chuyển Worker/OTA chung.
- ADB dùng Bridgev9: cần reboot màn/LDPlayer một lần để bỏ daemon cũ rồi kết nối lại.
  System không cần mở Bridge. Bootstrap sleep1/marker và12 PING250ms giữ nguyên.
- Workflow chỉ phát hành bản thử sau unit/lint/build, kiểm signer/package/code,
  test Android9 về migration, chọn Off/ADB/System đúng thực tế, thiếu quyền không
  fallback, native hide/show/wake, freeform swap/ratio, Auto freeform và Off session.
- Phép thử ADB trong CI chỉ kiểm địa chỉ cố ý đóng và không fallback; chưa kiểm
  bootstrap sau reboot trên LDPlayer9/xe. Test firmware và thanh OEM còn cần thực hiện.
