# UI đã chốt

App nội bộ có header THT và menu trái: Chia màn hình / Cặp đã lưu / Cài đặt.
Trang chính có hai card chọn app trái/phải, nút đổi bên ở giữa, thanh chọn tỷ lệ,
Lưu cặp và Bắt đầu chia. Trang cặp có tên, hai app, tỷ lệ, Mở cặp và menu ⋮ Sửa/Xóa.
Xóa cần xác nhận; hủy thì giữ nguyên cặp, xóa cặp không gỡ ứng dụng.

## Cài đặt
1. Chia màn hình: native/freeform, khoảng chừa trên/dưới theo pixel, khôi phục mặc định EX2.
2. Thanh điều khiển: tự ẩn sau 3/5/8/10 giây, nút nhỏ/lớn, độ hiển thị 40–100%, nút mở lại trái/giữa/phải.
3. THT Voice: bật nhận API, xem/sao chép/đổi token; không đặt popup nghe thường trực trong Split.
4. Kết nối & quyền: quyền nổi, kiểm tra bridge, lệnh khởi động, bật/khôi phục hỗ trợ freeform.
5. Cập nhật: phiên bản, kiểm tra quyền/cập nhật Worker, tải và cài APK đã kiểm tra, xuất log. Không có nút mở trang phát hành/hướng dẫn kiểm thử/tài liệu Voice.

## Hình thức
Palette navy #07121F, card #112233, line #2F4A60, cyan #10CBE5, text #EEF5FA,
red #F45364. Icon Canvas cùng độ dày nét 2 trong viewport40; tránh trộn glyph nhiều font.
Launcher icon là vector THT có vạch chia giữa trong adaptive mask.

Thanh nhỏ có brand + 5 tỷ lệ + chọn app/đổi bên/toàn màn/Home/ẩn/X.
Nút ẩn màu cyan và X màu đỏ là hai thao tác riêng biệt.
Ẩn trả diện tích thanh cho hai app và còn nút nổi mở lại. Khi toàn màn hình, nút nổi
khôi phục cặp. Vạch kéo hiện khi đang chia; bản đầu resize hai app khi thả tay.

App không vẽ lại giao diện Vietmap/YouTube hoặc thanh HVAC của xe.
Ảnh mockup trước đó minh họa bố cục; ảnh từ APK chạy trên xe mới xác nhận đúng insets.
OEM status/HVAC phải còn dùng được. Khoảng chừa native/freeform cần đo trên firmware thật.

## Bổ sung 0.2.0-test
Kết nối & quyền có bộ chọn ADB nội bộ / Off / Quyền hệ thống, trạng thái và Kiểm tra.
ADB chỉnh host/cổng, tùy chọn chuẩn bị lúc boot và lệnh thủ công dự phòng.
Không chuyển phương thức/cổng khi phiên đang chạy hoặc còn thao tác đang xử lý.
System không được chọn khi thiếu quyền thực tế. Từ 0.2.1 Off có vạch kéo/nút tỷ lệ;
gửi lại launch bounds khi thả tay, nhãn Off yêu cầu tỷ lệ, không xác minh task.
Màn chờ che vùng app khi mở/khôi phục/đổi cặp; chờ ổn định 0/500/1500/3000ms,
từ0.2.2 mặc định0ms sau khi backend trả về. Không coi thời gian chờ là nội dung tải xong.
THT Voice có API v2: cùng signer có thể dùng receiver signature không nhập token;
khác signer giữ token, không tự đổi khóa ký app đang cài.

Từ0.2.2 bấm chia/mở preset đưa MainActivity xuống nền như bản cũ. Handle nhìn thấy
12×32dp, touch48×80dp trong suốt xung quanh; chạm không kéo không mở lại hai app.

0.2.3: thanh công cụ chừa phải40dp, dịch nhóm nút sang trái32dp so với0.2.2.

0.2.4: dialog overlay và trong app dùng chung theme navy/chữ trắng. Dropdown cài đặt,
tỷ lệ preset và ô nhập dùng màu tường minh để không bị chữ tối trên nền tối.

0.2.6: toolbar Off mờ/khóa ratio, đổi app, swap, fullscreen; không grip và không
highlight tỷ lệ như đã xác minh. X Off dừng điều khiển, giữ cửa sổ Android.
ADB/system giữ đầy đủ các nút và grip. Trang cấu hình THT đóng khi mở cặp.

## Giao diện bên trong0.3.2
Mẫu đối chiếu: ảnh “THT Split: Chia màn hình, cài đặt.png” ngày04/10/2026.
AppUi là bộ style/icon riêng cho MainActivity; Ui và tất cả thành phần overlay đang
chia giữ nguyên. Header THT nghiêng, vạch cyan riêng, tiêu đề giữa; menu có icon
cùng nét, card gradient navy nhẹ và nút cyan. Trang chính hai thẻ app, swap ở giữa,
ratio và nhóm Lưu/Bắt đầu; quick pairs cuộn ngang trên màn rộng. Cặp đã lưu có
icon hai app, tỷ lệ, Mở cặp, menu sửa/xóa với xác nhận cũ.
Màn rộng>=900dp dùng menu chính và danh mục cài đặt bên trái. Màn nhỏ dùng menu
chính trên và danh mục cài đặt cuộn ngang, các card xếp dọc và nội dung cuộn để
không giấu thao tác. Các dòng cài đặt cho nhãn xuống dòng, giữ nguyên keys/giá trị.
Trang ủng hộ: card MB Bank/STK/chủ tài khoản/nút copy bên trái và card QR bên phải;
màn nhỏ xếp dọc. QR có nền trắng/padding bên ngoài, ảnh bundle và thông tin ngân
hàng nguyên trạng, dùng offline. Không dựng/sửa payload QR.
Không thay launcher icon, toolbar, grip hoặc hành vi task/Voice/Bridge trong mốc này.
Chưa xác minh pixels thực trên LDPlayer/xe; build/lint không thay kiểm tra trực quan.

##0.3.3 — căn nhóm nút và quản lý cặp
Cài đặt → Thanh điều khiển → Vị trí nhóm nút: Bên trái / Bên phải, mặc định phải.
Giữ overlay ngang toàn chiều rộng, cùng toolbarPx/top/session bounds. Bên trái
brand104dp rồi5 ratio và6 icon; khoảng linh hoạt chuyển ra cuối, chừa mép trái40dp.
Bên phải giữ brand weight1 và chừa phải40dp như trước. mini_position độc lập.
Chỉ đổi hiển thị khi render tiếp (hiện lại thanh hoặc mở cặp), không gửi resize
riêng vì vị trí nhóm nút không đổi vùng chia. Off vẫn khóa task controls.
Cặp lưu có3 nút nhìn thấy ngay: Sửa cặp (sáng), Xóa cặp (đỏ), Mở cặp (cyan),
cao52dp; màn nhỏ chia đều hàng. Bỏ dialog menu⋮; xác nhận Xóa nêu rõ tên cặp,
giữ Hủy và màu đỏ cho nút xác nhận. Không thay nội dung QR/icon launcher.
