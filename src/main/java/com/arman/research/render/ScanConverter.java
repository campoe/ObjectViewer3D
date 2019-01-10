package com.arman.research.render;

import com.arman.research.geom.polygons.Polygon3f;
import com.arman.research.geom.vectors.Vector3f;

public class ScanConverter {

    public static final int SCALE_BITS = 16;
    public static final int SCALE = 1 << SCALE_BITS;
    public static final int SCALE_MASK = SCALE - 1;

    private View view;
    private Scan[] scans;
    private int top;
    private int bottom;

    public static class Scan {

        public int left;
        public int right;

        public void setBounds(int x) {
            if (x < left) {
                left = x;
            }
            if (x - 1 > right) {
                right = x - 1;
            }
        }

        public void clear() {
            left = Integer.MAX_VALUE;
            right = Integer.MIN_VALUE;
        }

        public boolean isValid() {
            return left <= right;
        }

        public void setTo(int left, int right) {
            this.left = left;
            this.right = right;
        }

        public boolean equals(Scan s) {
            return left == s.left && right == s.right;
        }

        public int getLeft() {
            return left;
        }

        public int getRight() {
            return right;
        }

    }

    public ScanConverter(View view) {
        this.view = view;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public Scan getScan(int y) {
        return scans[y];
    }

    private void clearScan() {
        for (int i = top; i <= bottom; i++) {
            scans[i].clear();
        }
        top = Integer.MAX_VALUE;
        bottom = Integer.MIN_VALUE;
    }

    public boolean convert(Polygon3f p) {
        int height = view.getY() + view.getHeight();
        scans = new Scan[height];
        for (int i = 0; i < height; i++) {
            scans[i] = new Scan();
        }
        top = 0;
        bottom = height - 1;
        clearScan();
        int minX = view.getX();
        int maxX = view.getX() + view.getWidth() - 1;
        int minY = view.getY();
        int maxY = view.getY() + view.getHeight() - 1;
        int vertexCount = p.getVertexCount();
        for (int i = 0; i < vertexCount; i++) {
            Vector3f v1 = p.getVertex(i);
            Vector3f v2 = p.getVertex((i + 1) % vertexCount);
            if (v1.getY() > v2.getY()) {
                Vector3f temp = v1;
                v1 = v2;
                v2 = temp;
            }
            float dy = v2.getY() - v1.getY();
            if (dy == 0) {
                continue;
            }
            int sy = (int) Math.max(Math.ceil(v1.getY()), minY);
            int ey = (int) Math.min(Math.floor(v2.getY()), maxY);
            top = Math.min(top, sy);
            bottom = Math.max(bottom, ey);
            float dx = v2.getX() - v1.getX();
            if (dx == 0) {
                int x = (int) Math.ceil(v1.getX());
                x = Math.min(maxX + 1, Math.max(x, minX));
                for (int y = sy; y <= ey; y++) {
                    scans[y].setBounds(x);
                }
            } else {
                float gradient = dx / dy;
                float sx = v1.getX() + (sy - v1.getY()) * gradient;
                if (sx < minX) {
                    int y = (int) (v1.getY() + (minX - v1.getX()) / gradient);
                    y = Math.min(y, ey);
                    while (sy <= y) {
                        scans[sy].setBounds(minX);
                        sy++;
                    }
                } else if (sx > maxX) {
                    int y = (int) (v1.getY() + (maxX - v1.getX()) / gradient);
                    y = Math.min(y, ey);
                    while (sy <= y) {
                        scans[sy].setBounds(maxX + 1);
                        sy++;
                    }
                }
                if (sy > ey) {
                    continue;
                }
                float ex = v1.getX() + (ey - v1.getY()) * gradient;
                if (ex < minX) {
                    int y = (int) Math.ceil(v1.getY() + (minX - v1.getX()) / gradient);
                    y = Math.max(y, sy);
                    while (ey >= y) {
                        scans[ey].setBounds(minX);
                        ey--;
                    }
                } else if (ex > maxX) {
                    int y = (int) Math.ceil(v1.getY() + (maxX - v1.getX()) / gradient);
                    y = Math.max(y, sy);
                    while (ey >= y) {
                        scans[ey].setBounds(maxX + 1);
                        ey--;
                    }
                }
                if (sy > ey) {
                    continue;
                }
                int xScaled = (int) (SCALE * v1.getX() + SCALE * (sy - v1.getY()) * dx / dy) + SCALE_MASK;
                int dxScaled = (int) (dx * SCALE / dy);
                for (int y = sy; y <= ey; y++) {
                    scans[y].setBounds(xScaled >> SCALE_BITS);
                    xScaled += dxScaled;
                }
            }
        }
        for (int i = top; i <= bottom; i++) {
            if (scans[i].isValid()) {
                return true;
            }
        }
        return false;
    }

}
