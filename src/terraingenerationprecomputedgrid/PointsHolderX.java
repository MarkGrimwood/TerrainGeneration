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
public class PointsHolderX {

    private PointsHolderX lower = null, higher = null;

    private int x;
    private PointsHolderY yList;

    public PointsHolderX(int x, int y, float h, int pos) {
        this.x = x;
        this.yList = new PointsHolderY(y, h, pos);
    }

    public void checkAddNext(int x, int y, float h, int pos) {
        if (x < this.x) {
            if (lower == null) {
                lower = new PointsHolderX(x, y, h, pos);
            } else {
                lower.checkAddNext(x, y, h, pos);
            }
        } else if (x > this.x) {
            if (higher == null) {
                higher = new PointsHolderX(x, y, h, pos);
            } else {
                higher.checkAddNext(x, y, h, pos);
            }
        } else if (yList == null) {
            yList = new PointsHolderY(y, h, pos);
        } else {
            yList.checkAddNext(y, h, pos);
        }
    }

    public void unwindAndReport() {
        if (lower != null) {
            lower.unwindAndReport();
        }

        System.out.print("\nx:" + x + " : ");
        yList.unwindAndReport();

        if (higher != null) {
            higher.unwindAndReport();
        }
    }

    public boolean inList(int xVal, int yVal) {
        boolean found = false;

        if (xVal == this.x) {
            found = yList.inList(yVal);
        } else if (xVal < this.x && lower != null) {
            found = lower.inList(xVal, yVal);
        } else if (xVal > this.x && higher != null) {
            found = higher.inList(xVal, yVal);
        }

        return found;
    }

    public int getPointPosition(int xVal, int yVal) {
        int retVal = -1;

        if (xVal == this.x) {
            retVal = yList.getPointPosition(yVal);
        } else if (xVal < this.x && lower != null) {
            retVal = lower.getPointPosition(xVal, yVal);
        } else if (xVal > this.x && higher != null) {
            retVal = higher.getPointPosition(xVal, yVal);
        }

        return retVal;
    }

    public float getOldHeight(int xVal, int yVal) {
        float retVal = 0;

        if (xVal == this.x) {
            retVal = yList.getOldHeight(yVal);
        } else if (xVal < this.x && lower != null) {
            retVal = lower.getOldHeight(xVal, yVal);
        } else if (xVal > this.x && higher != null) {
            retVal = higher.getOldHeight(xVal, yVal);
        }

        return retVal;
    }

    public void setHeight(int xVal, int yVal, float height) {
        if (xVal == this.x) {
            yList.setHeight(yVal, height);
        } else if (xVal < this.x && lower != null) {
            lower.setHeight(xVal, yVal, height);
        } else if (xVal > this.x && higher != null) {
            higher.setHeight(xVal, yVal, height);
        }
    }

    public void copyHeights() {
        yList.copyHeights();
        if (lower != null) {
            lower.copyHeights();
        }
        if (higher != null) {
            higher.copyHeights();
        }
    }
}
