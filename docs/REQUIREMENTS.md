# Yêu cầu chốt ngày 04/10/2026

## Chốt10/10 — sửa lỗi sau restart màn EX2
- Mới cài THT Split dùng được, restart màn rồi Failed transaction khi dùng.
- Dựa thông tin IHU629G Android9/firmware121, phiên ADB root/su; sửa Bridge,
  giữ quy trình tự nối đã ổn định, không coi root UID bảo đảm mọi Binder call.
- Gửi bản cùng chữ ký để thử trước/sau reboot; không làm thay services.jar.


## Làm rõ07/10 — đúng thanh trắng hệ thống
- Ảnh040145 chỉ tay nắm trắng của Android; muốn gộp phần kéo vào đó, dài thêm.
- Bỏ thanh riêng; dùng pill trắng kéo dài trùng vị trí native, không sửa SystemUI.
- Native thả tay áp dụng tỷ lệ rồi cập nhật tay nắm, tránh hai dấu lệch lúc kéo.


## Chốt07/10 — thanh trắng
- Đồng màu thanh kéo với vạch trắng giữa hai app; tay nắm trắng dài thêm chút.
- Giữ vùng kéo toàn chiều cao, cùng tâm đường chia; thả tay áp dụng tỷ lệ.


## Chốt07/10 — màn chờ và thanh kéo
- Thiết kế lại phần load khi mở hai ứng dụng theo màu THT Split, gom giữa màn.
- Làm thanh kéo đẹp hơn và kéo được ở bất kỳ điểm trên hàng dọc vùng chia.
- Giữ backend ổn định/giới hạn Off/khoảng chừa xe; chưa triển khai3 ứng dụng.


## Chốt07/10 — ủng hộ/liên hệ
- Làm trang ủng hộ và liên hệ gọn gàng hơn; giữ QR ngân hàng/Zalo, STK/chủ tài
  khoản và Facebook đã cung cấp. Nút sao chép/mở liên hệ vẫn dễ bấm.
- Chế độ3 app ghim1 mới là ý tưởng, chưa triển khai; test chia đôi trước.


## Chốt06/10 18:32 — tạo bản mới thử cập nhật
- Phát hành0.3.10-test/code22 với cùng khóa ký cố định của0.3.9-test/code21.
- Giữ chức năng/Bridge/Worker, dùng bản mới để thử cập nhật đè và giữ cấu hình.
- Không bỏ kiểm chữ ký, không tự gỡ app hoặc thay khóa ký để vượt lỗi bản cũ.

## Chốt06/10 14:43 — mật khẩu quản trị ngắn hơn, giữ dữ liệu Worker
- Cho phép ADMIN_TOKEN/ADMIN_PASSWORD từ8 đến256 ký tự; giữ mật khẩu cũ hợp lệ.
- Gửi gói Worker cập nhật, giữ CONTROL/class/namespace và toàn bộ config/cache.
- Không đổi APK/keystore/Bridge hoặc xóa dữ liệu khi nâng Worker.

## Chốt06/10 14:17 — Worker tự cập nhật qua GitHub
- Làm theo cơ chế biến GITHUB_REPO/GITHUB_TOKEN/RELEASE_CERT_SHA256 như ảnh Voice.
- Dùng repo THT-Split riêng; token chỉ ở Worker, đọc repo private và proxy APK.
- Tự lấy metadata/SHA file APK; giữ khóa/mở chung, mandatory và dùng ngay.
- SHA cert Split cố định khác cert Voice, không xoay key cho giống ảnh.
- Không thêm đăng ký thiết bị/VIN/license; giữ toàn bộ chia/UI/ADB0.3.8.

## Chốt lưu quy tắc06/10 — không lặp lại hồi quy Bridge
- Giữ khoảng 1 giây ngay trong phiên shell ADB trước bootstrap marker như0.3.4/0.3.8.
- Không coi chờ/poll phía app sau khi shell đóng là tương đương; không bỏ để tối ưu
  nếu chưa có phép thử LDPlayer9 sau reboot, tự mở Bridge không dùng lệnh dự phòng.
- Đối chiếu cùng cấu hình/bản cũ và giữ kiểm UID/version/token. Build đạt chưa đủ.
- Ghi tách dữ kiện:0.3.4 được người dùng xác nhận;0.3.8 cần thử; race là giả thuyết.
- Quy tắc này ưu tiên hơn yêu cầu bỏ chờ bootstrap ở các mốc tối ưu cũ bên dưới.

## Phản hồi06/10 — hồi quy ADB trên LDPlayer9
- Chủ dự án xác nhận0.3.4 hoạt động bình thường, nâng bản mới Bridge không chạy.
- Ưu tiên đối chiếu và khôi phục luồng hoạt động cũ, giữ UI/liên hệ/cập nhật mới.

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

## Chốt tối ưu06/10/2026 — sau xác nhận cập nhật OK
- Thực hiện cả5 phần: picker/icon nền, tiến trình tải APK, sao lưu/khôi phục
  cấu hình, tái dùng overlay và thời gian thao tác cục bộ.
- Giữ khóa ký cố định, bootstrap ADB ổn định, backend/Voice/Off và giao diện chia.
- Sao lưu chỉ cài đặt/cặp, không credential/policy/session; khôi phục có xác
  nhận và chỉ khi đã tắt chia. Không tự gỡ/cài app hoặc bỏ kiểm signer.
- Không gọi tối ưu là đã mượt trên xe khi mới có build/unit test.
