#!/usr/bin/env python3
"""Generate update metadata from the APK actually built and signed; contains no secrets."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess

def build_metadata(apk, tag, expected_cert, sdk):
    tools = Path(sdk) / 'build-tools' / '35.0.0'
    signature = subprocess.check_output([str(tools / 'apksigner'), 'verify', '--verbose', '--print-certs', str(apk)], text=True)
    fingerprints = re.findall(r'^Signer #\d+ certificate SHA-256 digest: ([a-fA-F0-9]{64})$', signature, re.M)
    pin = expected_cert.strip().replace(':', '').lower()
    if not re.fullmatch('[a-f0-9]{64}', pin) or fingerprints != [pin]:
        raise ValueError('APK không dùng đúng chứng chỉ cố định THT Split')
    badging = subprocess.check_output([str(tools / 'aapt'), 'dump', 'badging', str(apk)], text=True)
    package = re.search(r"^package: name='([^']+)' versionCode='([0-9]+)' versionName='([^']+)'", badging, re.M)
    if not package or package.group(1) != 'com.tht.split' or tag != 'v' + package.group(3):
        raise ValueError('Package/tag không khớp APK đã build')
    size = apk.stat().st_size
    if not 0 < size <= 64 * 1024 * 1024:
        raise ValueError('APK vượt giới hạn kích thước')
    return {'schema': 1, 'packageName': package.group(1), 'versionCode': int(package.group(2)),
            'versionName': package.group(3), 'apkAsset': 'THT-Split.apk', 'sizeBytes': size,
            'sha256': hashlib.sha256(apk.read_bytes()).hexdigest(), 'certificateSha256': pin,
            'notes': 'Cập nhật THT Split ' + package.group(3)}

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('apk', type=Path)
    parser.add_argument('--tag', required=True)
    args = parser.parse_args()
    metadata = build_metadata(args.apk, args.tag, os.environ['EXPECTED_CERT_SHA256'], os.environ['ANDROID_HOME'])
    Path('update.json').write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    Path('THT-Split.apk.sha256').write_text(metadata['sha256'] + '  THT-Split.apk\n', encoding='ascii')
    print('Đã tạo update.json từ package, phiên bản và chữ ký APK thực tế')
