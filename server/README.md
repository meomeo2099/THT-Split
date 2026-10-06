# Worker chiamanhinh · THT Split0.3.6

Địa chỉ: https://chiamanhinh.mavoice.workers.dev

Ai cài app0.3.6 cũng dùng ngay, không đăng ký thiết bị hoặc nhập key. Mới cài
offline/Worker chưa hoạt động vẫn dùng được. Worker dành cho cập nhật và khóa/
mở toàn ứng dụng; không còn danh sách hay khóa từng thiết bị.

Giải nén gói, mở terminal trong thư mục có wrangler.jsonc:

```sh
npx --yes wrangler@4 login
npx --yes wrangler@4 secret put ADMIN_TOKEN
npx --yes wrangler@4 deploy
```

Đăng nhập tài khoản subdomain mavoice; Worker name đã đúng chiamanhinh. Nhập mật
khẩu quản trị ngẫu nhiên ít nhất32 ký tự khi secret hỏi. Không nhúng vào app/git.
CONTROL SQLite được cấu hình tự động; giữ namespace khi nâng, không xóa dữ liệu.
Chỉ dán source trong trình soạn thảo web mà thiếu binding CONTROL chưa đủ.

Health sau deploy: service=tht-split-control, protocol=2, registration=false,
storage=true, adminConfigured=true. GET /api/config không cần auth và không ghi
hồ sơ. Route đăng ký/quản lý từng thiết bị cũ trả410; dữ liệu cũ không được dùng.
Trang chính đăng nhập mật khẩu quản trị để Mở cho mọi người/Tạm khóa toàn ứng
dụng và nhập metadata cập nhật. Máy nhận khóa giữ cache khi mất mạng.

Cập nhật: versionCode/tên bản/URL APK HTTPS công khai/SHA256/nội dung.
Version tối thiểu0 = tự nguyện; chỉ tăng sau khi APK cùng signer cài đè thử được.
Bản test runner thay signer nên chưa là chuỗi cập nhật ổn định.

Gói không có ADMIN_TOKEN, khóa ký, đăng nhập Cloudflare hoặc dữ liệu thiết bị.
Hướng dẫn đầy đủ trong docs/REMOTE_CONTROL.md của repo.
