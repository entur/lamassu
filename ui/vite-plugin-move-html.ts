import type { Plugin } from 'vite';
import { promises as fs } from 'fs';
import path from 'path';

export function moveHtmlPlugin(): Plugin {
  return {
    name: 'move-html',
    enforce: 'post',
    async closeBundle() {
      const outDir = path.resolve(__dirname, '../src/main/resources/static');

      // Move and fix admin.html
      const adminSrc = path.join(outDir, 'admin.html');
      const adminDest = path.join(outDir, 'admin', 'ui', 'index.html');
      await fs.mkdir(path.dirname(adminDest), { recursive: true });
      await fs.rename(adminSrc, adminDest);
      await injectDynamicBase(adminDest, 'admin');

      // Move and fix status.html
      const statusSrc = path.join(outDir, 'status.html');
      const statusDest = path.join(outDir, 'status', 'ui', 'index.html');
      await fs.mkdir(path.dirname(statusDest), { recursive: true });
      await fs.rename(statusSrc, statusDest);
      await injectDynamicBase(statusDest, 'status');

      console.log('✓ Moved HTML files to correct directories');
      console.log('✓ Injected dynamic base path detection');
    },
  };
}

/**
 * Inject a dynamic <base> tag and convert absolute paths to relative
 * This makes the app work behind reverse proxies with path prefixes
 */
async function injectDynamicBase(htmlPath: string, appType: 'admin' | 'status') {
  let html = await fs.readFile(htmlPath, 'utf-8');

  // Create a script that sets the base tag dynamically
  // The base must include the full path to the UI directory, not just the prefix
  const baseScript = `
    <script>
      (function() {
        var path = window.location.pathname;
        // Extract everything up to and including /${appType}/ui/
        var match = path.match(/^(.*\\/${appType}\\/ui\\/)/);
        var basePath = match ? match[1] : '/${appType}/ui/';
        document.write('<base href="' + basePath + '">');
      })();
    </script>`;

  // Inject the script right after <head>
  html = html.replace('<head>', '<head>' + baseScript);

  // Convert absolute paths to relative paths so they work with <base> tag
  // Base path is /mobility/v2/status/ui/ (or /status/ui/ for direct access)
  // File structure: static/status/ui/, static/common/, static/i18n.css
  // Order matters! Most specific patterns first.
  // /status/ui/file.js -> file.js (same directory as HTML)
  // /admin/ui/file.js -> file.js (same directory as HTML)
  // /common/file.js -> ../../common/file.js (up 2 levels: ui->status->static, then into common)
  // /file.css -> ../../file.css (up 2 levels to static root)

  const appUiPattern = new RegExp(`"/${appType}/ui/([^"]+)"`, 'g');
  html = html.replace(appUiPattern, '"$1"');

  const commonPattern = /"\/common\/([^"]+)"/g;
  html = html.replace(commonPattern, '"../../common/$1"');

  const rootAssetPattern = /"\/([^/\s"']+\.(css|js|png|jpg|svg|woff|woff2|ttf))"/g;
  html = html.replace(rootAssetPattern, '"../../$1"');

  await fs.writeFile(htmlPath, html, 'utf-8');
}
