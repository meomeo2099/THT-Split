// Only named release assets are exposed. The GitHub token never leaves server-side fetches.
const MAX_APK_BYTES = 64 * 1024 * 1024;
export function normalizeCertSha256(value) {
  const normalized = typeof value === 'string' ? value.trim().replace(/:/g, '').toLowerCase() : '';
  if (!/^[a-f0-9]{64}$/.test(normalized)) throw Error('RELEASE_CERT_SHA256 phải là SHA256 chứng chỉ, đủ 64 ký tự hex');
  return normalized;
}
export function githubSettings(env, includePrerelease = false) {
  const repo = typeof env.GITHUB_REPO === 'string' ? env.GITHUB_REPO.trim() : '';
  if (!/^[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+$/.test(repo)) throw Error('Chưa cấu hình GITHUB_REPO đúng dạng chủ/repo');
  const token = typeof env.GITHUB_TOKEN === 'string' ? env.GITHUB_TOKEN.trim() : '';
  if (!token || /[\r\n]/.test(token)) throw Error('Chưa cấu hình Secret GITHUB_TOKEN');
  const cert = normalizeCertSha256(env.RELEASE_CERT_SHA256);
  return {repo, token, cert, includePrerelease, key: `${repo}|${cert}|${includePrerelease}`};
}
async function githubFetch(url, settings, accept, authenticated = true) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 8000);
  try {
    const headers = {'Accept': accept, 'User-Agent': 'THT-Split-Update'};
    if (authenticated) {
      headers.Authorization = 'Bearer ' + settings.token;
      headers['X-GitHub-Api-Version'] = '2026-03-10';
    }
    return await fetch(url, {headers, redirect: 'manual', signal: controller.signal});
  } catch { throw Error('Không kết nối được GitHub, thử lại sau'); }
  finally { clearTimeout(timer); }
}
function apiUrl(settings, suffix) { return `https://api.github.com/repos/${settings.repo}/${suffix}`; }
function githubStatus(response) {
  if (response.status === 401 || response.status === 403) return 'GitHub từ chối: kiểm tra token, Contents: Read hoặc giới hạn API';
  if (response.status === 404) return 'GitHub không thấy repo/asset: kiểm tra tên repo và quyền token';
  return 'GitHub trả HTTP ' + response.status;
}
async function limitedText(response, maximum) {
  if (!response.body) throw Error('GitHub trả nội dung trống');
  if (Number(response.headers.get('Content-Length')) > maximum) { await response.body.cancel(); throw Error('Metadata GitHub vượt giới hạn'); }
  const reader = response.body.getReader(); const parts = []; let size = 0;
  try {
    while (true) {
      let timer;
      const item = await Promise.race([reader.read(), new Promise((_, reject) => { timer = setTimeout(() => reject(Error('Đọc metadata GitHub quá lâu')), 8000); })]).finally(() => clearTimeout(timer));
      if (item.done) break;
      size += item.value.byteLength;
      if (size > maximum) throw Error('Metadata GitHub vượt giới hạn');
      parts.push(item.value);
    }
    const bytes = new Uint8Array(size); let offset = 0;
    for (const part of parts) { bytes.set(part, offset); offset += part.byteLength; }
    return new TextDecoder().decode(bytes);
  } finally { await reader.cancel().catch(() => {}); }
}
async function assetResponse(settings, id) {
  let url = apiUrl(settings, 'releases/assets/' + id);
  for (let redirects = 0; redirects <= 3; redirects++) {
    const response = await githubFetch(url, settings, 'application/octet-stream', redirects === 0);
    if ([301, 302, 303, 307, 308].includes(response.status)) {
      const location = response.headers.get('Location'); await response.body?.cancel();
      if (!location || redirects === 3) throw Error('Chuyển hướng asset GitHub không hợp lệ');
      const next = new URL(location, url);
      if (next.protocol !== 'https:' || next.username || next.password ||
          !(next.hostname === 'github.com' || next.hostname.endsWith('.githubusercontent.com'))) throw Error('GitHub chuyển hướng đến máy chủ không được phép');
      url = next.toString(); // No Authorization is forwarded to the CDN.
      continue;
    }
    if (response.status !== 200) { await response.body?.cancel(); throw Error(githubStatus(response)); }
    return response;
  }
  throw Error('Không tải được asset GitHub');
}
function namedAsset(release, name) {
  const assets = Array.isArray(release.assets) ? release.assets.filter(a => a.name === name && a.state === 'uploaded') : [];
  return assets.length === 1 && Number.isSafeInteger(assets[0].id) && assets[0].id > 0 ? assets[0] : null;
}
export async function githubLatest(settings) {
  const response = await githubFetch(apiUrl(settings, 'releases?per_page=20'), settings, 'application/vnd.github+json');
  if (response.status !== 200) { await response.body?.cancel(); throw Error(githubStatus(response)); }
  let releases;
  try { releases = JSON.parse(await limitedText(response, 2 * 1024 * 1024)); }
  catch (e) { if (e instanceof SyntaxError) throw Error('Danh sách Release không phải JSON hợp lệ'); throw e; }
  if (!Array.isArray(releases)) throw Error('Danh sách Release không hợp lệ');
  const candidates = releases.filter(r => !r.draft && (settings.includePrerelease || !r.prerelease));
  candidates.sort((a, b) => Date.parse(b.published_at || 0) - Date.parse(a.published_at || 0));
  for (const release of candidates.slice(0, 10)) {
    const apk = namedAsset(release, 'THT-Split.apk');
    const metadata = namedAsset(release, 'update.json');
    const checksum = namedAsset(release, 'THT-Split.apk.sha256');
    if (!apk || !metadata || !checksum) continue; // Old debug releases have no trusted update manifest.
    let manifest;
    try { manifest = JSON.parse(await limitedText(await assetResponse(settings, metadata.id), 16384)); }
    catch (e) { if (e instanceof SyntaxError) throw Error('update.json không hợp lệ'); throw e; }
    if (manifest.schema !== 1 || manifest.packageName !== 'com.tht.split' || manifest.apkAsset !== 'THT-Split.apk' ||
        !Number.isSafeInteger(manifest.versionCode) || manifest.versionCode <= 0 ||
        typeof manifest.versionName !== 'string' || release.tag_name !== 'v' + manifest.versionName ||
        !/^[a-fA-F0-9]{64}$/.test(manifest.sha256 || '') ||
        !Number.isSafeInteger(apk.size) || apk.size <= 0 || apk.size > MAX_APK_BYTES || manifest.sizeBytes !== apk.size) throw Error('Metadata APK trên Release không khớp');
    if (normalizeCertSha256(manifest.certificateSha256) !== settings.cert) throw Error('Release khác chứng chỉ THT Split đã cấu hình');
    const digest = manifest.sha256.toLowerCase();
    if (apk.digest && apk.digest.toLowerCase() !== 'sha256:' + digest) throw Error('SHA256 asset GitHub không khớp update.json');
    const checksumText = await limitedText(await assetResponse(settings, checksum.id), 512);
    if (!new RegExp('^' + digest + '[ \\t]+\\*?THT-Split\\.apk\\s*$','i').test(checksumText)) throw Error('Tệp checksum không khớp update.json');
    return {key: settings.key, assetId: apk.id, sizeBytes: apk.size, sha256: digest, versionCode: manifest.versionCode,
      versionName: manifest.versionName, certificateSha256: settings.cert,
      notes: typeof manifest.notes === 'string' ? manifest.notes.slice(0, 4000) : '', tag: release.tag_name};
  }
  return null;
}
export function githubPublicLatest(snapshot, origin) {
  return snapshot ? {versionCode: snapshot.versionCode, versionName: snapshot.versionName,
    apkUrl: `${origin}/api/apk/${snapshot.assetId}`, sha256: snapshot.sha256, notes: snapshot.notes} : null;
}
export async function githubApk(settings, approved) {
  const upstream = await assetResponse(settings, approved.assetId);
  const length = upstream.headers.get('Content-Length');
  if (!upstream.body || (length !== null && Number(length) !== approved.sizeBytes)) {
    await upstream.body?.cancel(); throw Error('Kích thước APK GitHub không khớp metadata');
  }
  let bytes = 0;
  const limiter = new TransformStream({
    transform(chunk, controller) {
      bytes += chunk.byteLength;
      if (bytes > approved.sizeBytes || bytes > MAX_APK_BYTES) throw Error('APK vượt kích thước cho phép');
      controller.enqueue(chunk);
    },
    flush() { if (bytes !== approved.sizeBytes) throw Error('APK tải chưa đủ dữ liệu'); }
  });
  return new Response(upstream.body.pipeThrough(limiter), {headers: {
    'Content-Type': 'application/vnd.android.package-archive', 'Content-Length': String(approved.sizeBytes),
    'Content-Disposition': 'attachment; filename="THT-Split.apk"', 'Cache-Control': 'no-store',
    'X-Content-Type-Options': 'nosniff', 'Referrer-Policy': 'no-referrer'
  }});
}
