# Thành phần bên thứ ba

LSPosed AndroidHiddenApiBypass v6.1:
https://github.com/LSPosed/AndroidHiddenApiBypass/tree/v6.1
Copyright (C) 2021–2025 LSPosed, Apache License 2.0.

Các file HiddenApiBypass.java, Helper.java, CoreOjClassLoader.java được giữ attribution.
Adaptation được ghi đầu mỗi file: bỏ compile-only annotations, debug constant tắt,
resolve VMRuntime bằng tên và thay List.of bằng Collections.emptyList cho Android8.
Unsafe stub là compileOnly trong third_party/hidden_api_stubs, không đóng gói trong APK.
License đầy đủ tại app/src/main/assets/licenses/LSPosed-Apache-2.0.txt, đi cùng APK.

Không sao chép mã GSplit. GSplit chỉ là nguồn tham khảo hành vi/API.
Client ADB/bridge/backend/UI/preset THT được viết riêng trong repo này.
