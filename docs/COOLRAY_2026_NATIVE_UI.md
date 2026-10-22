# THT Split 0.3.21-thtsign — bản thử tương thích giao diện xe

Bản này dùng nền EX2/THT_SIGN (022315f), theo chủ dự án xác nhận Coolray2026
dùng cùng cấu hình ứng dụng EX2. Không lấy nhánh Coolray2025, không thêm lề trái160px.
Giữ package com.tht.split và chứng thư THT_SIGN platform đã dùng; code33.

## Phản hồi và giới hạn
Chủ dự án dùng quyền hệ thống, ẩn/đổi bên làm màn tắt; bật lên cả thanh hệ thống xe
và thanh THT Split đều mất, hai app còn chạy. Báo cáo cho biết IHU689G/Android9/API28;
launcher theo thông tin chủ dự án là com.flyme.auto.launcher. Chưa có log đúng lúc
lỗi để chứng minh SystemUI/launcher crash hay lỗi khung chia/màn nghỉ.

Đây là bản thử giảm các thao tác gây lỗi đã báo và khôi phục overlay THT sau ngủ/thức.
Không hứa sửa được tiến trình giao diện xe nếu firmware đã lỗi. Cần thử xe và lấy log.

## Thay đổi
- Cài đặt → Thanh điều khiển → **Tương thích giao diện xe**: mặc định bật khi nhận
  IHU689G và có com.flyme.auto.launcher; EX2 khác giữ hành vi cũ. Có thể bật/tắt tay.
- Khi bật và đang chia native: ẩn/hiện thanh chỉ đổi overlay, giữ nguyên bounds hai app
  và khoảng dành cho toolbar. Không giành thêm48px khi ẩn, tránh resize native liên tục.
- **Đổi bên native trong phiên tạm khóa**, cả toolbar lẫn API. Chọn thứ tự hai app tại
  trang chính trước Bắt đầu chia; không tự chuyển sang phương thức khác.
- Màn nghỉ: gỡ overlay THT, giữ phiên; màn thức tạo lại thanh THT mà không dựng lại task.
  Chỉ áp dụng khi service còn chạy. Reboot hoặc service bị hệ thống dừng cần mở lại app.
- Khi cần dọn stack chia: chế độ tương thích chỉ cho thao tác stack chứa hoàn toàn hai
  app đã chọn; thấy launcher/task ngoài cặp thì từ chối trước khi đổi mode của stack đó.
- Log hỗ trợ thêm model/compat và screen_on/off; sao lưu nhận thêm tùy chọn bool này.

## Cài và thử
1. Tải THT-Split.apk trong Release này, cài đè bản platform/THT_SIGN cùng chữ ký.
   Không cần gỡ hoặc xóa dữ liệu. Khác signer thì không cài đè; không xoay signer để vượt lỗi.
2. Cài đặt → Thanh điều khiển → bật **Tương thích giao diện xe**, tắt phiên cũ rồi mở cặp.
3. Thử ẩn/hiện và màn nghỉ/thức. Thanh THT phải trở lại; ghi riêng thanh xe có trở lại không.
4. Đổi thứ tự hai app ở trang chọn trước khi chia; nút đổi bên trong native đang khóa có chủ ý.
5. Nếu thanh xe vẫn mất: thu log ngay sau lỗi bằng bộ thu log đã gửi, trước reboot màn.
   Cần crash/window/tasks/power cùng phiên bản APK, không chỉ ảnh sau khi đã khởi động lại.

## Xác minh
Workflow được cấu hình chạy unit/lint/build, kiểm package/code/signer/no sharedUID, và test instrumentation
Android9 giả lập1920×1080: cặp native hai app có sẵn, hide/show giữ bounds, swap bị từ chối,
scope guard không sửa primary ngoài cặp, ngủ/thức thật khôi phục overlay. Test APK không
phát hành. Đây chưa là kiểm thử firmware IHU689G. Không xuất metadata sang kênh OTA chung.

## Trạng thái 22/10/2026
Mã đã lưu trên fix/coolray2026-system-ui (f7763e5). CI39800897559 không khởi tạo
được job; lần chạy lại kết thúc startup_failure trước build. Chưa có APK/Release mới.
Local đã kiểm cú pháp Java/YAML và smoke logic policy/scope bằng JDK17; chưa chạy
Gradle/lint/instrumentation Android. Phần xác minh trên là điều kiện phải đạt trước
phát hành, chưa phải kết quả đã chạy. Cần chạy lại CI và xem log nếu còn thất bại.
