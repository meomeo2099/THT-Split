# THT Split 0.3.19-coolray2025

Bản cấu hình giao diện riêng theo Coolray2025-Voice-Spec.md chủ dự án cung cấp:
SX11-LA / IHU730P, Android9 API28, 1920×720 / 160dpi, launcher ecarx.launcher3.

## Thay đổi
- Giao diện màn thấp: header52dp, thẻ chọn app gọn, khoảng cách nhỏ hơn.
- Cài đặt chuyển các mục thành hàng ngang cuộn được, dành chiều rộng cho nội dung.
- Menu chính cuộn được; chiều cao nút tăng theo cỡ chữ, không ép nhỏ phông hệ thống.
- Nội dung và hộp thoại tiếp tục cuộn, dùng kích thước Android cấp thực tế,
  không chạy wm size/density trên xe hoặc ép ứng dụng khác đổi giao diện.
- Nhãn Coolray2025 và log bổ sung model/product/kích thước vùng ứng dụng/mật độ.

## Cài đặt
Tải **THT-Split-Coolray2025.apk** trong Release này. Package gốc com.tht.split,
code31, tên0.3.19-coolray2025, không shared UID. Cùng cert platform với0.3.17/0.3.18:
`c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8`.
Có thể cài đè bản platform cùng chữ ký bằng `adb install -r THT-Split-Coolray2025.apk`.
Sao lưu cấu hình trước; không cần gỡ/xóa dữ liệu bản cùng chữ ký.
Bản thường ký tht-split khác chữ ký nên không cài đè được.

Đây là APK bổ sung trên nhánh release/coolray2025-0.3.19, chưa thay bản EX2/main.
Không xuất update.json hoặc chuyển kênh Worker chung sang bản Coolray; cài thủ công
bản này. Worker, khóa/mở và kiểm chứng APK vẫn theo bản platform hiện có.

## Cần thử trên xe
1. Mở trang chọn app, cặp đã lưu, các mục cài đặt và ủng hộ: chữ rõ, cuộn đủ,
   nút thao tác không bị khuất. Thử cả cỡ chữ lớn.
2. Kết nối & quyền: kiểm chứng chỉ/quyền MANAGE_ACTIVITY_STACKS trên chính Coolray.
   Cert của EX2 chưa chứng minh Coolray có cùng cert. Bản này vẫn system-only,
   không tự cấp quyền, không thêm shared UID, không đoán giao thức ecarx_splitscreen.
3. Chia hai app, đổi tỷ lệ/đổi bên/toàn màn/khôi phục, tắt/mở màn rồi thử lại.
4. Khoảng chừa72/112px hiện là giá trị thử từ bản trước, **chưa đo trên Coolray**.
   Cài đặt → Chia màn hình: chỉnh trên/dưới theo thanh hệ thống/điều hòa thực tế,
   rồi Khôi phục cặp. Cài đè giữ khoảng chừa người dùng đã chỉnh.
5. Nếu còn lệch, gửi ảnh toàn màn và log qua Cài đặt → Cập nhật → Xuất log hỗ trợ.

CI kiểm unit/lint/build/cert/package và giao diện trên giả lập Android9 ở
1920×720/160dpi với font1.0/1.3 và vùng thấp1920×600. Kiểm tra giả lập không
chứng minh firmware Coolray chia màn hoặc tự nối sau reboot thành công.
