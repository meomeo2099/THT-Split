"""Android emulator smoke test of the signed APK, not a vehicle compatibility test."""
import pathlib
import re
import subprocess
import time
import xml.etree.ElementTree as ET

OUT = pathlib.Path('coolray-ui')
OUT.mkdir(exist_ok=True)

def adb(*args):
    return subprocess.check_output(['adb', *args], timeout=40)

def tree():
    adb('shell', 'uiautomator', 'dump', '/sdcard/tht-ui.xml')
    return ET.fromstring(adb('exec-out', 'cat', '/sdcard/tht-ui.xml'))

def bounds(node):
    return tuple(map(int, re.findall(r'\d+', node.attrib['bounds'])))

def find(label, clickable=False):
    for attempt in range(3):
        for node in tree().iter('node'):
            if label not in (node.get('text'), node.get('content-desc')):
                continue
            if clickable and node.get('clickable') != 'true':
                continue
            x1, y1, x2, y2 = bounds(node)
            if x2 > x1 and y2 > y1:
                return node
        time.sleep(1)
    raise AssertionError('Không thấy mục hiển thị: ' + label)

def tap(label):
    node = find(label, True)
    x1, y1, x2, y2 = bounds(node)
    adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
    time.sleep(0.5)

def capture(name):
    (OUT / (name + '.png')).write_bytes(adb('exec-out', 'screencap', '-p'))
    (OUT / (name + '.xml')).write_bytes(ET.tostring(tree(), encoding='utf-8'))

adb('install', '-r', 'THT-Split-Coolray2025.apk')
adb('shell', 'wm', 'density', '160')
adb('shell', 'settings', 'put', 'system', 'accelerometer_rotation', '0')
adb('shell', 'settings', 'put', 'system', 'user_rotation', '0')
for height, font in ((720, '1.0'), (720, '1.3'), (600, '1.0')):
    adb('shell', 'wm', 'size', '1920x'+str(height))
    adb('shell', 'settings', 'put', 'system', 'font_scale', font)
    # Restart only our installed test app, never third-party apps.
    adb('shell', 'am', 'force-stop', 'com.tht.split')
    adb('shell', 'am', 'start', '-W', '-n', 'com.tht.split/.MainActivity')
    time.sleep(2)
    suffix = str(height)+'-'+font
    start = find('Bắt đầu chia', True)
    assert bounds(start)[3] <= height, 'Nút bắt đầu bị khuất'
    capture('home-'+suffix)
    tap('Cài đặt')
    for label, title in (
        ('Chia màn hình', 'Chế độ chia'),
        ('Thanh điều khiển', 'Vị trí nhóm nút'),
        ('THT Voice', 'Kết nối THT Voice'),
        ('Kết nối & quyền', 'Kiểm tra / kết nối'),
        ('Cập nhật', 'Phiên bản & cập nhật'),
        ('Sao lưu', 'Sao lưu & khôi phục'),
    ):
        # Main navigation has the same first label. Pick the horizontal tab.
        if label == 'Chia màn hình':
            nodes = [n for n in tree().iter('node') if n.get('content-desc') == label and n.get('clickable') == 'true']
            node = max(nodes, key=lambda n: bounds(n)[0])
            x1, y1, x2, y2 = bounds(node)
            adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
            time.sleep(0.5)
        else:
            tap(label)
        find(title)
    capture('settings-'+suffix)
    tap('Ủng hộ')
    find('Ủng hộ & liên hệ')
    capture('support-'+suffix)
    tap('Cặp đã lưu')
    find('Chưa có cặp ứng dụng')
    print('UI OK: 1920x'+str(height)+' @160 dpi, font '+font, flush=True)
adb('shell', 'settings', 'put', 'system', 'font_scale', '1.0')
