# THT Split 0.3.20-coolray2025

Bản cấu hình giao diện riêng theo Coolray2025-Voice-Spec.md chủ dự án cung cấp:
SX11-LA / IHU730P, Android9 API28, 1920×720 / 160dpi, launcher ecarx.launcher3.

## Sửa lỗi theo ảnh21/10
- Ảnh gconnect-screenshot (2).png: tay nắm lệch phải gần160px so với khe chia.
  Suy luận gốc frame overlay đã chừa dock, nên cộng tọa độ tuyệt đối làm lệch.
- Đo getLocationOnScreen trước vẽ, bù sai lệch trong WindowManager.LayoutParams,
  tối đa3 lần rồi ghi expected/actual/origin vào log. Cùng cơ chế cho màn chờ,
  toolbar, mini và divider; không hard-code trừ160 trên mọi firmware.
- Coolray bỏ chừa dưới112px mặc định của EX2, mặc định mới0. Giữ top72/left160
  chỉnh được. EX2 vẫn72/112/0. Giá trị người dùng đã lưu vẫn được giữ.
- Sau cài đè, nếu còn dải đen dưới, tắt chia → Cài đặt → Chia màn hình →
  **Đặt lại bố cục Coolray**, rồi mở lại cặp. Nút đặt lại thay top/bottom/left
  thành72/0/160; không xóa cặp hoặc token.
- UI test cũ chỉ kiểm Activity, chưa chứng minh vị trí overlay trên xe.
  Thêm test APK riêng kiểm window overlay thực, mô phỏng origin0/160/160+24,
  kiểm đổi vị trí/tái dùng và default bottom. Test APK không nằm trong Release.

## Thay đổi
- Giao diện màn thấp: header52dp, thẻ chọn app gọn, khoảng cách nhỏ hơn.
- Cài đặt chuyển các mục thành hàng ngang cuộn được, dành chiều rộng cho nội dung.
- Menu chính cuộn được; chiều cao nút tăng theo cỡ chữ, không ép nhỏ phông hệ thống.
- Nội dung và hộp thoại tiếp tục cuộn, dùng kích thước Android cấp thực tế,
  không chạy wm size/density trên xe hoặc ép ứng dụng khác đổi giao diện.
- Nhãn Coolray2025 và log bổ sung model/product/kích thước vùng ứng dụng/mật độ.
- Theo ảnh gconnect-screenshot.png1920×720: dock hệ thống bên trái khoảng120px,
  nội dung ứng dụng bắt đầu gần x160px. Thêm Chừa thanh hệ thống bên trái,
  mặc định thử160px trên Coolray; EX2 mặc định0px, giữ giá trị người dùng đã lưu.
- Tỷ lệ được tính trong vùng còn lại (1920−160=1760px), nên50:50 có tâm x1040,
  không phải960. Toolbar, màn chờ, nút mở lại và kéo dùng cùng khoảng chừa.
  Backend vẫn kiểm bounds thực; OEM có thể không nhận insets ở chế độ native.

## Cài đặt
Tải **THT-Split-Coolray2025.apk** trong Release này. Package gốc com.tht.split,
code32, tên0.3.20-coolray2025, không shared UID. Cùng cert platform với0.3.17/0.3.18:
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
4. Khoảng chừa trên72px, dưới0px, trái160px là cấu hình thử theo ảnh, cần so trên xe.
   Khoảng chừa trái160px ước lượng từ ảnh, chưa xác minh vùng task trên firmware.
   Cài đặt → Chia màn hình: chỉnh trái/trên/dưới theo thanh hệ thống/điều hòa thực tế,
   rồi Khôi phục cặp. Cài đè giữ khoảng chừa người dùng đã chỉnh.
5. Nếu còn lệch, gửi ảnh toàn màn và log qua Cài đặt → Cập nhật → Xuất log hỗ trợ.

CI kiểm unit/lint/build/cert/package và giao diện trên giả lập Android9 ở
1920×720/160dpi với font1.0/1.3 và vùng thấp1920×600. Kiểm tra giả lập không
chứng minh firmware Coolray chia màn hoặc tự nối sau reboot thành công.

