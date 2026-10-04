# UI đã chốt

Bố cục app nội bộ: header THT, menu trái Chia màn hình / Cặp đã lưu / Cài đặt.
Trang chính: card app trái/phải, nút đổi bên giữa, tỷ lệ segmented, Lưu cặp, Bắt đầu chia.
Trang cặp: tên, hai app, tỷ lệ; Mở cặp; menu ⋮ có Sửa/Xóa; confirm xóa/hủy.

Cài đặt5nhóm:
1. Chia màn hình: native/freeform, top/bottom px, khôi phục khoảng chừa.
2. Thanh: tựẩn,3/5/8/10s, nútnhỏ/lớn, alpha40–100%, nútmini trái/giữa/phải.
3. Voice: bật API, copy/rotate token; không có popup nghe cố định.
4. Kết nối: overlay, pingbridge, copy bootstrapcommand, bật/khôi phụcfreeform.
5. Cập nhật: phiênbản, latestrelease check, mở GitHub tải, xuấtlog.

Palette navy #07121F, card #112233, line #2F4A60, cyan #10CBE5, text #EEF5FA,
red #F45364. Icon vẽ Canvas cùng stroke2 ở viewport40, không dùng6glyphkhácfont.
Launcher icon là vector chữTHT/vạch giữa trong adaptive mask, không đưa mockup raster vào APK.

Toolbar rất nhỏ: brand + 5ratios + apps/swap/full/home/hide/X. Hidecyan và Xred riêng.
App không vẽ lại giao diện Vietmap/YouTube hoặc HVAC; screenshot tham khảo là minh họa.
Overlay native/freeform đặt ngoài phần chừa hệ thống. Phải dùng ảnh xe thật xác nhận.
Nútmini khiẩn trảdiệntíchtoolbar; fullscreenmini khôiphụccặp. Divider hiện lúcchia.
