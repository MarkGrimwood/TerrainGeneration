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
public class PointsHolderOverall {

    private PointsHolderX xList;

    PointsHolderOverall() {
        xList = null;
    }

    PointsHolderOverall(int xVal, int yVal, float hVal, int posVal) {
        if (xList == null) {
            xList = new PointsHolderX(xVal, yVal, hVal, posVal);
        } else {
            addXY(xVal, yVal, hVal, posVal);
        }
    }

    public void addXY(int xVal, int yVal, float hVal, int posVal) {
        if (xList == null) {
            xList = new PointsHolderX(xVal, yVal, hVal, posVal);
        } else {
            xList.checkAddNext(xVal, yVal, hVal, posVal);
        }
    }

    public boolean inListXY(int xVal, int yVal) {
        return xList == null ? false : xList.inList(xVal, yVal);
    }

    public int getPointPosition(int xVal, int yVal) {
        return xList == null ? -1 : xList.getPointPosition(xVal, yVal);
    }

    public float getOldHeight(int xVal, int yVal) {
        return xList == null ? 0 : xList.getOldHeight(xVal, yVal);
    }

    public void setHeight(int xVal, int yVal, float hVal) {
        xList.setHeight(xVal, yVal, hVal);
    }

    public void unwindAndReport() {
        xList.unwindAndReport();
    }

    public void copyHeights() {
        xList.copyHeights();
    }
}
