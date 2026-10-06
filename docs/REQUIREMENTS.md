# Yêu cầu chốt ngày 04/10/2026

## Chốt06/10 — ADB và liên hệ
- Báo rõ lỗi ADB và phản hồi ngay khi bấm Kiểm tra, không chờ cập nhật qua mạng.
- Thêm QR Zalo đúng ảnh chủ dự án gửi và Facebook100004637940665 trong Ủng hộ.
- Giữ STK/QR ngân hàng và cơ chế dùng ngay không đăng ký.

## Chốt mới06/10 — ưu tiên hơn yêu cầu đăng ký cũ
- Bỏ đăng ký thiết bị để ai cài cũng sử dụng ngay, kể cả cài lần đầu offline.
- Bỏ mã thiết bị, private credential đăng ký, duyệt/heartbeat/lease và khóa từng máy.
- Giữ cập nhật/mandatory và khóa/mở chung toàn ứng dụng; mặc định mở.
- Worker chưa hoạt động không chặn người mới dùng. Khóa/min đã nhận giữ cache.


## Chốt06/10/2026
- Gắn Worker của chủ dự án: https://chiamanhinh.mavoice.workers.dev cho khóa/mở
  và cập nhật. Worker hiện Hello World phải được thay bằng mã quản trị có CONTROL.
- Thay launcher icon theo mẫu đã chọn: THT + vạch giữa, khung cyan/nền xanh đêm.
- Tối ưu độ trễ: bỏ chờ bootstrap dư, policy mạng chạy riêng khi cache còn quyền,
  giữ window overlay khi đổi tỷ lệ; giữ kiểm UID/version/bounds và gate bảo mật.
- Chỉ gọi đo mượt/chạy xe thành công sau quan sát trên thiết bị, không từ CI.

- App độc lập: THT Split (`com.tht.split`). Có bộ điều khiển chia màn riêng, không gọi app chia màn bên ngoài.
- EX2 IHU629G MT8788, Android 9, 6 GB RAM, 1920×1080/160dpi là đích kiểm thử đầu tiên.
- UI tiếng Việt, navy/cyan; icon chữ THT có vạch chia giữa. Thanh icon nhỏ, cùng kích thước/nét.
- Chọn 2 app bên trái/phải; tỷ lệ 30:70,40:60,50:50,60:40,70:30 và kéo vạch giữa.
- Đổi từng app, đổi bên; toàn màn hình một bên và khôi phục cặp; Home; X tắt chia về app trái.
- Nút ẩn thanh riêng; ẩn vẫn giữ 2 app, có nút nổi để mở lại. Tự ẩn sau vài giây.
- Cặp đã lưu: tạo, đặt tên, mở, sửa (app/tỷ lệ/tên), xóa có xác nhận. Xóa cặp không gỡ app.
- Nhớ cấu hình lần trước; app chưa cài phải báo lỗi, không thay app âm thầm.
- Không làm popup Voice thường trực. Nhận API từ THT Voice, di chuyển/tự tắt popup do Voice quản lý.
- Cài đặt: chia màn hình, thanh điều khiển, Voice API, kết nối/quyền, phiên bản/cập nhật/log.
- Chừa thanh hệ thống và điều hòa của xe. Khoảng chừa phải chỉnh được và kiểm tra trên xe.
- Repo lưu sơ đồ, tài liệu nguồn, danh sách việc và kiểm thử; commit theo từng mốc.

Không đưa HVAC, cửa/kính, camera, TTS/STT vào phạm vi ứng dụng này.

## Chốt bổ sung 05/10/2026
- Phương thức: ADB nội bộ mặc định / Off mở cửa sổ trực tiếp / quyền hệ thống có điều kiện.
- Bỏ Telnet/Shizuku, không cần app hỗ trợ bên ngoài.
- Tự mở bridge khi cần, tùy chọn chuẩn bị lúc boot; giữ khóa RSA ADB và token qua reboot.
- API v2 có requestId và kết quả, giữ token v1; thêm signature API khi hai app cùng signer.
- Off không dùng shell; không hứa resize task hoặc bố cục đã thành công nếu chỉ gửi launch.
- Giữ khóa ký/package, commit tiếng Việt, cập nhật hướng dẫn tích hợp trên GitHub.

## Điều chỉnh Off theo phản hồi05/10/2026
Khóa các thao tác task không chạy được trên toolbar Off: tỷ lệ/kéo/đổi bên/đổi app/
toàn màn/khôi phục. API Voice cùng giới hạn. X chỉ dừng điều khiển Off, giữ cửa sổ
Android để tránh lần mở sau tái dùng fullscreen. ADB/system giữ đầy đủ chức năng.

## Chốt giao diện/quản lý05/10/2026
- Bỏ nút tài liệu Voice, hướng dẫn kiểm thử và mở trang phát hành khỏi app.
- Cập nhật trong app, có phiên bản tối thiểu bắt buộc; kiểm APK trước cài.
- Không nhập key; tự đăng ký thiết bị, chủ dự án khóa/mở từ xa (bỏ duyệt theo chốt mới).
- Người dùng chọn Worker Cloudflare riêng cho THT Split.
- Thêm ủng hộ có QR/STK thật của chủ dự án; đã nhận thông tin và link QR của chủ dự án.
- Không lưu thông tin đăng nhập/khóa ký/admin credential vào git; được nhúng
  profile ủng hộ công khai do chủ dự án yêu cầu. Endpoint và signer cần
  cấu hình trước bản dùng chính thức; không giả định đã deploy.

## Chốt tiếp05/10/2026 18:05
- STK, tên chủ tài khoản và QR được nhúng sẵn, dùng khi offline/chưa có Worker.
- Không cần duyệt: thiết bị tự đăng ký và được dùng ngay; quản trị chỉ khóa/mở.
- Khóa đang lưu phải giữ khi migrate pending cũ hoặc khôi phục credential.
- Giữ phần cập nhật bắt buộc của yêu cầu trước; ủng hộ không phụ thuộc license.

## Chốt giao diện bên trong05/10/2026 23:12
- Sửa trang chọn app, cặp đã lưu, cài đặt và làm trang ủng hộ đẹp hơn theo mẫu navy/cyan.
- Giữ nguyên giao diện và hành vi khi đang chia: toolbar, grip, tỷ lệ, swap, fullscreen, ẩn/X.
- Không thay backend ADB/Bridge/Off/system, API Voice, profile/bytes QR hoặc cơ chế quản lý từ xa.
- Phạm vi này không thay launcher icon; icon và component mới chỉ dùng trong MainActivity.

## Chốt23:41 — trái/phải và Sửa/Xóa
- Thêm hai lựa chọn Bên trái/Bên phải cho nhóm nút trên thanh ngang như ảnh gửi;
  cập nhật phạm vi giữ toolbar của chốt23:12 chỉ ở phần căn nhóm nút.
- Sửa/Xóa cặp rõ hơn: nút riêng, Xóa đỏ, giữ xác nhận và không gỡ hai app.
- Giữ tất cả lệnh/geometry/chức năng chia và các giới hạn Off.

## Phản hồi23:57 — hộp thoại khó đọc
- Sửa hộp xác nhận không có nền rõ, chữ lẫn trang phía sau; rà và sửa tất cả
  phần chọn/nhập/xác nhận/thông báo có cùng vấn đề trong cài đặt.
- Giữ validation, xác nhận trước thao tác và toàn bộ chức năng kết nối/chia.
