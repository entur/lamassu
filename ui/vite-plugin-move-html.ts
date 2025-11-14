import type { Plugin } from 'vite';
import { promises as fs } from 'fs';
import path from 'path';

export function moveHtmlPlugin(): Plugin {
  return {
    name: 'move-html',
    enforce: 'post',
    async closeBundle() {
      const outDir = path.resolve(__dirname, '../src/main/resources/static');

      // Move admin.html to admin/ui/index.html
      const adminSrc = path.join(outDir, 'admin.html');
      const adminDest = path.join(outDir, 'admin', 'ui', 'index.html');
      await fs.mkdir(path.dirname(adminDest), { recursive: true });
      await fs.rename(adminSrc, adminDest);

      // Move status.html to status/ui/index.html
      const statusSrc = path.join(outDir, 'status.html');
      const statusDest = path.join(outDir, 'status', 'ui', 'index.html');
      await fs.mkdir(path.dirname(statusDest), { recursive: true });
      await fs.rename(statusSrc, statusDest);

      console.log('✓ Moved HTML files to correct directories');
    },
  };
}
