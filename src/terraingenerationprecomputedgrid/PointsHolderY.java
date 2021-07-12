/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terraingenerationprecomputedgrid;

/**
 *
 * @author Mark
 */
public class PointsHolderY {

    private PointsHolderY lower = null, higher = null;

    private int y, pos;
    private float h, oldH;

    PointsHolderY(int y, float h, int pos) {
        this.y = y;
        this.h = h;
        this.pos = pos;
    }

    public void checkAddNext(int y, float h, int pos) {
        if (y < this.y) {
            if (lower == null) {
                lower = new PointsHolderY(y, h, pos);
            } else {
                lower.checkAddNext(y, h, pos);
            }
        } else if (y > this.y) {
            if (higher == null) {
                higher = new PointsHolderY(y, h, pos);
            } else {
                higher.checkAddNext(y, h, pos);
            }
        }
    }

    public void unwindAndReport() {
        if (lower != null) {
            lower.unwindAndReport();
        }

        System.out.print(" " + y);

        if (higher != null) {
            higher.unwindAndReport();
        }
    }

    public boolean inList(int yVal) {
        boolean found = false;

        if (yVal == this.y) {
            found = true;
        } else if (yVal < this.y && lower != null) {
            found = lower.inList(yVal);
        } else if (yVal > this.y && higher != null) {
            found = higher.inList(yVal);
        }

        return found;
    }

    public int getPointPosition(int yVal) {
        int retVal = -1;

        if (yVal == this.y) {
            retVal = pos;
        } else if (yVal < this.y && lower != null) {
            retVal = lower.getPointPosition(yVal);
        } else if (yVal > this.y && higher != null) {
            retVal = higher.getPointPosition(yVal);
        }

        return retVal;
    }

    public float getOldHeight(int yVal) {
        float retVal = 0;

        if (yVal == this.y) {
            retVal = oldH;
        } else if (yVal < this.y && lower != null) {
            retVal = lower.getOldHeight(yVal);
        } else if (yVal > this.y && higher != null) {
            retVal = higher.getOldHeight(yVal);
        }

        return retVal;
    }

    public void setHeight(int yVal, float height) {
        if (yVal == this.y) {
            this.h = height;
        } else if (yVal < this.y && lower != null) {
            lower.setHeight(yVal, height);
        } else if (yVal > this.y && higher != null) {
            higher.setHeight(yVal, height);
        }
    }

    public void copyHeights() {
        oldH = this.h;

        if (lower != null) {
            lower.copyHeights();
        }

        if (higher != null) {
            higher.copyHeights();
        }
    }
}
