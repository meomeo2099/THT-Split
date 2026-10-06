# Worker chiamanhinh · THT Split

Địa chỉ của chủ dự án: https://chiamanhinh.mavoice.workers.dev
Lúc kiểm tra06/10/2026, Worker này đang chạy Hello World, chưa có API của Split.
Đây là mã thay thế để có trang quản trị, khóa/mở thiết bị và cập nhật bắt buộc.

Giải nén gói Worker, mở terminal trong thư mục có `wrangler.jsonc`:

```sh
npx --yes wrangler@4 login
npx --yes wrangler@4 secret put ADMIN_TOKEN
npx --yes wrangler@4 deploy
```

Đăng nhập đúng tài khoản có subdomain `mavoice`. Khi lệnh secret hỏi mật khẩu,
nhập mật khẩu quản trị ngẫu nhiên ít nhất32 ký tự. Không gửi mật khẩu này cho
khách hoặc đưa vào app/mã nguồn. Tên Worker đã đặt đúng `chiamanhinh`;
deploy thay mã Hello World của Worker này, không tạo tên Worker khác.

Sau deploy mở `/api/health`: phải có service=tht-split-control, protocol=1,
storage=true, adminConfigured=true. Mở trang chính, đăng nhập mật khẩu vừa đặt.
App0.3.5 đã gắn URL trên, online lần đầu tự đăng ký được dùng ngay. Chỉ Khóa/Mở
khóa thiết bị, không duyệt, khách không nhập key. Máy đã nhận khóa giữ khóa khi
mất mạng. Máy mới chưa đăng ký hoặc hết thời hạn phải kết nối lại.

Cập nhật: nhập versionCode/tên bản/URL APK HTTPS công khai/SHA256/nội dung.
Version tối thiểu0 = tự nguyện. Chỉ nâng tối thiểu sau khi APK cùng chữ ký đã cài
đè thử được. APK test ký runner chưa phù hợp làm chuỗi cập nhật ổn định.

Gói không chứa ADMIN_TOKEN, khóa ký APK, thông tin Cloudflare hay dữ liệu thiết bị.
Durable Object CONTROL được Wrangler tạo/bind theo cấu hình; chỉ dán worker.mjs
trong trình soạn thảo web mà thiếu binding này sẽ chưa chạy API thiết bị.

Trong repo, hướng dẫn đầy đủ tại docs/REMOTE_CONTROL.md.
