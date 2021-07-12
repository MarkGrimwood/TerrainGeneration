/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terraingenerationprecomputedgrid;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.shape.TriangleMesh;

/**
 *
 * @author Mark
 *
 */
public class LandscapeGenerator {

    private static final int DETAIL_LEVEL = 16;

    public static final float GRASS_LINE = -5, ROCK_LINE = -3000, SNOW_AND_ROCK_LINE = -4000, SNOW_LINE = -5000;

    private static int[] distanceBandDistances, distanceBandValues;

    private List<float[]> pointsList;
    private List<int[]> squareList;
    private List<int[]> triangleList;

    private TriangleMesh outputMesh;
    private PointsHolderOverall speedPoints;

    private int lastAdjustedWorldPositionX = 0, lastAdjustedWorldPositionY = 0;

    public LandscapeGenerator(double posX, double posY, int gridSizePower) {
        speedPoints = new PointsHolderOverall();

        distanceBandDistances = new int[gridSizePower + 1];
        distanceBandValues = new int[gridSizePower + 1];

        for (int loop = 0; loop < distanceBandDistances.length; loop++) {
            distanceBandDistances[loop] = (int) pow(2, loop - 1) * DETAIL_LEVEL;
            distanceBandValues[loop] = (int) pow(2, loop - 1);
//            System.out.println("distanceBandList[" + loop + "] = " + distanceBandValues[loop] + "," + distanceBandDistances[loop]);
        }

        int gridSize = (int) pow(2, gridSizePower);
        pointsList = Collections.synchronizedList(new ArrayList());
        squareList = Collections.synchronizedList(new ArrayList());
        triangleList = Collections.synchronizedList(new ArrayList());

        System.out.println("gridSizePower = " + gridSizePower + ", gridSize = " + gridSize);

        createPointsList(0, 0, gridSize);
        createTriangles();

        fixGrid();
        update(posX, posY);

        System.out.println("Points = " + (outputMesh.getPoints().size() / outputMesh.getPointElementSize()) + ", Triangles = " + (outputMesh.getFaces().size() / outputMesh.getFaceElementSize()));

        System.out.println("Finished setupGrid");
    }

    public void update(double newWorldPositionX, double newWorldPositionY) {
        speedPoints.copyHeights();

        // Adjust world position so it's in integers
        int newAdjustedWorldPositionX = (int) (newWorldPositionX + 0.5);
        int newAdjustedWorldPositionY = (int) (newWorldPositionY + 0.5);

        // What's the difference between the current and last adjusted positions?
        int changeX = newAdjustedWorldPositionX - lastAdjustedWorldPositionX;
        int changeY = newAdjustedWorldPositionY - lastAdjustedWorldPositionY;

        // Update all the point heights
        int countSpeedUsage = 0, countStandardUsage = 0;
        if (changeX != 0 || changeY != 0) {
            for (int loop = 0; loop < outputMesh.getPoints().size(); loop += 3) {
                // This is the position in the display grid, not the world
                int gridX = (int) outputMesh.getPoints().get(loop + 0);
                int gridY = (int) outputMesh.getPoints().get(loop + 2);

                float height = 0;
                if (speedPoints.inListXY(gridX + changeX, gridY + changeY)) {
                    countSpeedUsage++;

                    // Get an already calculated height
                    height = speedPoints.getOldHeight(gridX + changeX, gridY + changeY);
                } else {
                    countStandardUsage++;

                    // Regenerate the height
                    height = -HeightGenerator.getHeight(newAdjustedWorldPositionX + gridX, newAdjustedWorldPositionY + gridY);
                }

                // Set the height in the display list
                outputMesh.getPoints().set(loop + 1, height);

                // Store the new height regardless of source
                speedPoints.setHeight((int) gridX, (int) gridY, height);
            }

            HeightGenerator.cleanse();
        }
//
//        System.out.println("\t Speed usage: " + countSpeedUsage + "\t Standard usage: " + countStandardUsage);

        // Update the last updated adjusted positions
        lastAdjustedWorldPositionX = newAdjustedWorldPositionX;
        lastAdjustedWorldPositionY = newAdjustedWorldPositionY;

        // Change all the textures for the new positions
        for (int loop = 0; loop < outputMesh.getFaces().size(); loop += 6) {
            float h1 = outputMesh.getPoints().get(1 + 3 * outputMesh.getFaces().get(loop));
            float h2 = outputMesh.getPoints().get(1 + 3 * outputMesh.getFaces().get(loop + 2));
            float h3 = outputMesh.getPoints().get(1 + 3 * outputMesh.getFaces().get(loop + 4));

            int textureOffset = getTextureOffset(
                    h1, h2, h3,
                    outputMesh.getPoints().get(0 + 3 * outputMesh.getFaces().get(loop)),
                    outputMesh.getPoints().get(0 + 3 * outputMesh.getFaces().get(loop + 2)),
                    outputMesh.getPoints().get(0 + 3 * outputMesh.getFaces().get(loop + 4)),
                    outputMesh.getPoints().get(2 + 3 * outputMesh.getFaces().get(loop)),
                    outputMesh.getPoints().get(2 + 3 * outputMesh.getFaces().get(loop + 2)),
                    outputMesh.getPoints().get(2 + 3 * outputMesh.getFaces().get(loop + 4))
            );
            outputMesh.getFaces().set(loop + 1, textureOffset + 0);
            outputMesh.getFaces().set(loop + 3, textureOffset + 1);
            outputMesh.getFaces().set(loop + 5, textureOffset + 3);
        }
    }

    public void fixGrid() {
        outputMesh = new TriangleMesh();
        setTextures(outputMesh);

        // Add the points to the output
        for (int loop = 0; loop < pointsList.size(); loop++) {
            float[] p = pointsList.get(loop);
            outputMesh.getPoints().addAll(p[0], p[1], p[2]);
        }

        // Define the triangles.
        for (int loop = 0; loop < triangleList.size(); loop++) {
            int[] t = triangleList.get(loop);
            outputMesh.getFaces().addAll(t[0], 3, t[1], 2, t[2], 0);

            // Disables smoothing
//            outputMesh.getFaceSmoothingGroups().addAll(0);
        }
    }

    public TriangleMesh getTriangleMesh() {
        return outputMesh;
    }

    private void createPointsList(int minX, int minY, int thisGridSize) {
        int nextGridSize = thisGridSize / 2;
        int centreOffset = nextGridSize;
        int centreX = minX + centreOffset, centreY = minY + centreOffset;

        double distance = sqrt((long) centreX * (long) centreX + (long) centreY * (long) centreY);
        int distanceBand = getDistanceBanding(distance);

        if (thisGridSize <= distanceBand) {
            createSquare(minX, minY, thisGridSize);
        } else {
            // Do recursive calls
            // NW
            createPointsList(minX, minY + nextGridSize, nextGridSize);
            // NE
            createPointsList(minX + nextGridSize, minY + nextGridSize, nextGridSize);
            // SW
            createPointsList(minX, minY, nextGridSize);
            // SE
            createPointsList(minX + nextGridSize, minY, nextGridSize);
        }
    }

    private void createSquare(int left, int bottom, int size) {
        //  Add the points to the squares list. Triangle analysis will happen later
        squareList.add(new int[]{left, bottom, size});

        // Add to the points list too
        addPointToList(left, bottom);
        addPointToList(left + size, bottom);
        addPointToList(left, bottom + size);
        addPointToList(left + size, bottom + size);
    }

    private void createTriangles() {
        for (int loop = 0; loop < squareList.size(); loop++) {
            int[] sl = squareList.get(loop);
            int left = sl[0];
            int bottom = sl[1];
            int size = sl[2];

            boolean isLeftSplit = doesLeftSplit(left, bottom, size);
            boolean isBottomSplit = doesBottomSplit(left, bottom, size);

            if (isLeftSplit && isBottomSplit) {
                addTriangle(left, bottom + size / 2, left + size, bottom + size, left, bottom + size);
                addTriangle(left + size / 2, bottom, left + size, bottom + size, left, bottom + size / 2);
                addTriangle(left, bottom, left + size / 2, bottom, left, bottom + size / 2);
                addTriangle(left, bottom, left + size / 2, bottom, left, bottom + size / 2);
                addTriangle(left + size, bottom, left + size, bottom + size, left + size / 2, bottom);
            } else if (isLeftSplit) {
                addTriangle(left, bottom, left + size, bottom, left, bottom + size / 2);
                addTriangle(left, bottom + size / 2, left + size, bottom, left + size, bottom + size);
                addTriangle(left, bottom + size / 2, left + size, bottom + size, left, bottom + size);
            } else if (isBottomSplit) {
                addTriangle(left, bottom, left + size / 2, bottom, left, bottom + size);
                addTriangle(left + size / 2, bottom, left + size, bottom + size, left, bottom + size);
                addTriangle(left + size / 2, bottom, left + size, bottom, left + size, bottom + size);
            } else {
                // Just two triangles required here
                addTriangle(left, bottom, left + size, bottom, left, bottom + size);
                addTriangle(left + size, bottom, left + size, bottom + size, left, bottom + size);
            }
        }
    }

    private boolean doesLeftSplit(int posX, int posY, int size) {
        int testX = posX;
        int testY = posY + size / 2;

        return speedPoints.inListXY(testX, testY);
    }

    private boolean doesRightSplit(int posX, int posY, int size) {
        int testX = posX + size;
        int testY = posY + size / 2;

        return speedPoints.inListXY(testX, testY);
    }

    private boolean doesBottomSplit(int posX, int posY, int size) {
        int testX = posX + size / 2;
        int testY = posY;

        return speedPoints.inListXY(testX, testY);
    }

    private boolean doesTopSplit(int posX, int posY, int size) {
        int testX = posX + size / 2;
        int testY = posY + size;

        return speedPoints.inListXY(testX, testY);
    }

    private void addTriangle(int grid1X, int grid1Y, int grid2X, int grid2Y, int grid3X, int grid3Y) {
        int rot1X = 0, rot1Y = 0, rot2X = 0, rot2Y = 0;

        for (int loop = 0; loop < 4; loop++) {
            switch (loop) {
                case 0:
                    rot1X = 1;
                    rot1Y = 0;
                    rot2X = 0;
                    rot2Y = 1;
                    break;
                case 1:
                    rot1X = 0;
                    rot1Y = -1;
                    rot2X = 1;
                    rot2Y = 0;
                    break;
                case 2:
                    rot1X = -1;
                    rot1Y = 0;
                    rot2X = 0;
                    rot2Y = -1;
                    break;
                case 3:
                    rot1X = 0;
                    rot1Y = 1;
                    rot2X = -1;
                    rot2Y = 0;
                    break;
            }

            triangleList.add(
                    new int[]{
                        addPointToList(grid1X * rot1X + grid1Y * rot1Y, grid1X * rot2X + grid1Y * rot2Y),
                        addPointToList(grid2X * rot1X + grid2Y * rot1Y, grid2X * rot2X + grid2Y * rot2Y),
                        addPointToList(grid3X * rot1X + grid3Y * rot1Y, grid3X * rot2X + grid3Y * rot2Y)
                    }
            );
        }
    }

    private int addPointToList(int gridX, int gridY) {
        float height = -HeightGenerator.getHeight(gridX, gridY);

        int position = speedPoints.getPointPosition(gridX, gridY);

        if (position < 0) {
            pointsList.add(new float[]{gridX, height, gridY});
            position = pointsList.size() - 1;
            speedPoints.addXY(gridX, gridY, height, position);
        }

        return position;
    }

    private int getDistanceBanding(double distance) {
        int band = 0;

        for (int loop = 0; loop < distanceBandDistances.length; loop++) {
            if (distance < distanceBandDistances[loop]) {
                band = distanceBandValues[loop];
                break;
            }
        }
        return band;
    }

    private int getTextureOffset(
            float y1, float y2, float y3,
            float x1, float x2, float x3,
            float z1, float z2, float z3
    ) {
        // Default to grass
        int offset = 4;
        float averageHeight = (y1 + y2 + y3) / 3f;

        if (averageHeight < SNOW_LINE) {
            // This is high, so it's snow
            offset = 12;
        } else if (averageHeight < SNOW_AND_ROCK_LINE) {
//            // Rock and snow zone
//            if (greaterSlope(y1, y2, y3, x1, x2, x3, z1, z2, z3)) {
//                // Too high a slope to hold snow
//                offset = 8;
//            } else {
            offset = 12;
//            }
        } else if (averageHeight < ROCK_LINE) {
            // This is not high enough for snow, but above the tree line, so it's rock
            offset = 8;
        } else if (y1 > GRASS_LINE && y2 > GRASS_LINE && y3 > GRASS_LINE) {
//            if (greaterSlope(y1, y2, y3, x1, x2, x3, z1, z2, z3)) {
//                // Too great a slope for sand
//                offset = 8;
//            } else {
            offset = 0;
//            }
        } else if (greaterSlope(y1, y2, y3, x1, x2, x3, z1, z2, z3)) {
            // Too high a slope to hold grass
            offset = 8;
        }

        return offset;
    }

    private boolean greaterSlope(
            float y1, float y2, float y3,
            float x1, float x2, float x3,
            float z1, float z2, float z3
    ) {
        boolean result = false;

        // Get highest and lowest X and difference
        float xLow = (x1 < x2 ? x1 : x2);
        xLow = (xLow < x3 ? xLow : x3);
        float xHigh = (x1 > x2 ? x1 : x2);
        xHigh = (xHigh > x3 ? xHigh : x3);
        float xDiff = Math.abs(xHigh - xLow);

        // Get highest and lowest Y and difference
        float hLow = (y1 < y2 ? y1 : y2);
        hLow = (hLow < y3 ? hLow : y3);
        float hHigh = (y1 > y2 ? y1 : y2);
        hHigh = (hHigh > y3 ? hHigh : y3);
        float hDiff = Math.abs(hHigh - hLow);

        // Get highest and lowest X and difference
        float zLow = (z1 < z2 ? z1 : z2);
        zLow = (zLow < z3 ? zLow : z3);
        float zHigh = (z1 > z2 ? z1 : z2);
        zHigh = (zHigh > z3 ? zHigh : z3);
        float zDiff = Math.abs(zHigh - zLow);

        float diff = (xDiff > zDiff ? xDiff : zDiff);

        if (hDiff > diff) {
            result = true;
        }

        return result;
    }

    private void setTextures(TriangleMesh mesh) {
        float positionSand = 0.0f, positionGrass = 0.25f, positionRock = 0.50f, positionSnow = 0.75f, left = 0.01f, right = 0.24f;
        float top = 0.1f, bottom = 0.9f;

        mesh.getTexCoords().clear();

        mesh.getTexCoords().addAll(
                positionSand + left, top,
                positionSand + right, top,
                positionSand + right, bottom,
                positionSand + left, bottom
        );

        mesh.getTexCoords().addAll(
                positionGrass + left, top,
                positionGrass + right, top,
                positionGrass + right, bottom,
                positionGrass + left, bottom
        );

        mesh.getTexCoords().addAll(
                positionRock + left, top,
                positionRock + right, top,
                positionRock + right, bottom,
                positionRock + left, bottom
        );

        mesh.getTexCoords().addAll(
                positionSnow + left, top,
                positionSnow + right, top,
                positionSnow + right, bottom,
                positionSnow + left, bottom
        );
    }
}
