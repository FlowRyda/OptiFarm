import {Injectable} from '@angular/core';
import {jsPDF} from "jspdf";

type QrItem = { name: string; blob: Blob };

@Injectable({
  providedIn: 'root'
})
export class QrcodeService {

  constructor() {
  }

  async createQrDocument(qrItems: QrItem[]) {
    const COLS = 2;
    const MAX_PER_PAGE = 6;
    const FIXED_ROWS = 3;

    const MARGIN_X = 12;
    const MARGIN_Y = 12;
    const TILE_PADDING = 6;

    const LABEL_FONT_SIZE = 10;
    const LABEL_GAP = 4;
    const LABEL_LINES_MAX = 2;

    const GRID_LINE_WIDTH = 0.3;
    const DASH: [number, number] = [1.5, 1.5];

    const doc = new jsPDF();
    const pageW = doc.internal.pageSize.getWidth();
    const pageH = doc.internal.pageSize.getHeight();

    const gridX = MARGIN_X;
    const gridY = MARGIN_Y;
    const gridW = pageW - 2 * MARGIN_X;
    const gridH = pageH - 2 * MARGIN_Y;

    const tileW = gridW / COLS;
    const tileH = gridH / FIXED_ROWS;

    const approxLineH = LABEL_FONT_SIZE * 0.45 + 3;
    const labelAreaH = LABEL_GAP + approxLineH * LABEL_LINES_MAX;

    const QR_SIZE = Math.floor(
      Math.min(
        tileW - 2 * TILE_PADDING,
        tileH - 2 * TILE_PADDING - labelAreaH
      )
    );

    for (let pageStart = 0; pageStart < qrItems.length; pageStart += MAX_PER_PAGE) {
      if (pageStart > 0) doc.addPage();

      const pageItems = qrItems.slice(pageStart, pageStart + MAX_PER_PAGE);

      doc.setLineWidth(GRID_LINE_WIDTH);
      doc.setDrawColor(160);
      doc.setLineDashPattern(DASH, 0);

      const vx = gridX + tileW;
      doc.line(vx, gridY, vx, gridY + gridH);

      for (let r = 1; r < FIXED_ROWS; r++) {
        const hy = gridY + r * tileH;
        doc.line(gridX, hy, gridX + gridW, hy);
      }

      doc.setLineDashPattern([], 0);
      doc.setDrawColor(0);
      doc.setFontSize(LABEL_FONT_SIZE);

      for (let i = 0; i < pageItems.length; i++) {
        const {name, blob} = pageItems[i];

        const row = Math.floor(i / COLS);
        const col = i % COLS;

        const tileX = gridX + col * tileW;
        const tileY = gridY + row * tileH;

        const qrX = tileX + (tileW - QR_SIZE) / 2;
        const qrY = tileY + TILE_PADDING;

        const base64 = await this.blobToBase64(blob);
        doc.addImage(base64, "PNG", qrX, qrY, QR_SIZE, QR_SIZE);

        const maxTextWidth = tileW - 2 * TILE_PADDING;
        const allLines = doc.splitTextToSize(name ?? "", maxTextWidth);
        const lines = allLines.slice(0, LABEL_LINES_MAX);

        const labelTopY = qrY + QR_SIZE + LABEL_GAP;
        for (let li = 0; li < lines.length; li++) {
          doc.text(lines[li], tileX + tileW / 2, labelTopY + li * approxLineH, {
            align: "center",
          });
        }
      }
    }

    doc.save("qr-codes.pdf");
  }

  private blobToBase64(blob: Blob): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  }

}
