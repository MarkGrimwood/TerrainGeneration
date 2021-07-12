/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terraingenerationprecomputedgrid;

import static java.lang.Math.sqrt;
import java.util.Random;

/**
 *
 * @author Mark
 */
public class HeightGenerator {

    private static float scaleList[];
    private static int worldPower = 16;  // Default to 16
    private static int worldSize = (int) longPowerOf2(worldPower);

    private int searchDistanceX, searchDistanceZ;
    private int decisionPositionX, decisionPositionZ;
    private float hNW, hNE, hSW, hSE, haNWNE, haSWSE;
    private float scalingFactor;
    private float height;
    public int currentDepth;
    private int thisSearchDistanceX, thisSearchDistanceZ, nextSearchDistanceX, nextSearchDistanceZ;
    private float nextHN, nextHS, nextHW, nextHE;

    private static HeightGenerator mainNHG;
    private HeightGenerator nextNE, nextNW, nextSE, nextSW;

    public boolean usedFlag;

    public static float getHeight(
            double searchPosX, double searchPosZ
    ) {
        return getHeight((int) (searchPosX + 0.5), (int) (searchPosZ + 0.5));
    }

    public static float getHeight(
            int searchPosX, int searchPosZ
    ) {
        if (scaleList == null) {
            createScaleList();
        }

        int halfWorldSize = worldSize / 2;

        if (mainNHG == null) {
            mainNHG = new HeightGenerator(
                    worldSize, worldSize,
                    halfWorldSize, halfWorldSize,
                    0, 0, 0, 0,
                    worldPower
            );
        }

        return mainNHG.getHeightRecursive(searchPosX, searchPosZ);
    }

    private HeightGenerator(
            int searchDistanceX, int searchDistanceZ,
            int decisionPositionX, int decisionPositionZ,
            float hNW, float hNE, float hSW, float hSE,
            int currentDepth
    ) {
        this.searchDistanceX = searchDistanceX;
        this.searchDistanceZ = searchDistanceZ;
        this.decisionPositionX = decisionPositionX;
        this.decisionPositionZ = decisionPositionZ;
        this.hNW = hNW;
        this.hNE = hNE;
        this.hSW = hSW;
        this.hSE = hSE;
        this.currentDepth = currentDepth;

        this.haNWNE = (hNW + hNE) / 2f;
        this.haSWSE = (hSW + hSE) / 2f;
        this.scalingFactor = scaleList[currentDepth];
        this.height = (haNWNE + haSWSE) / 2f + randomIshFactor(decisionPositionX, decisionPositionZ, scalingFactor);

        this.thisSearchDistanceX = searchDistanceX / 2;
        this.thisSearchDistanceZ = searchDistanceZ / 2;
        this.nextSearchDistanceX = thisSearchDistanceX / 2;
        this.nextSearchDistanceZ = thisSearchDistanceZ / 2;

        this.nextHN = haNWNE + randomIshFactor(decisionPositionX, decisionPositionZ + thisSearchDistanceZ, scalingFactor);
        this.nextHS = haSWSE + randomIshFactor(decisionPositionX, decisionPositionZ - thisSearchDistanceZ, scalingFactor);
        this.nextHW = (hNW + hSW) / 2f + randomIshFactor(decisionPositionX - thisSearchDistanceX, decisionPositionZ, scalingFactor);
        this.nextHE = (hNE + hSE) / 2f + randomIshFactor(decisionPositionX + thisSearchDistanceX, decisionPositionZ, scalingFactor);
    }

    private float getHeightRecursive(
            int searchPosX, int searchPosZ
    ) {
        this.usedFlag = true;

        float returnHeight = height;

        if (currentDepth > 0) {
            if (searchPosX < decisionPositionX) {
                if (searchPosZ < decisionPositionZ) {
                    if (nextSW == null) {
                        createNextSW();
                    }
                    returnHeight = nextSW.getHeightRecursive(searchPosX, searchPosZ);
                } else {
                    if (nextNW == null) {
                        createNextNW();
                    }
                    returnHeight = nextNW.getHeightRecursive(searchPosX, searchPosZ);
                }
            } else if (searchPosZ < decisionPositionZ) {
                if (nextSE == null) {
                    createNextSE();
                }
                returnHeight = nextSE.getHeightRecursive(searchPosX, searchPosZ);
            } else {
                if (nextNE == null) {
                    createNextNE();
                }
                returnHeight = nextNE.getHeightRecursive(searchPosX, searchPosZ);
            }
        }

        return returnHeight;
    }

    private void createNextNW() {
        nextNW = new HeightGenerator(
                thisSearchDistanceX, thisSearchDistanceZ,
                decisionPositionX - nextSearchDistanceX, decisionPositionZ + nextSearchDistanceZ,
                hNW, nextHN, nextHW, height,
                currentDepth - 1
        );
    }

    private void createNextNE() {
        nextNE = new HeightGenerator(
                thisSearchDistanceX, thisSearchDistanceZ,
                decisionPositionX + nextSearchDistanceX, decisionPositionZ + nextSearchDistanceZ,
                nextHN, hNE, height, nextHE,
                currentDepth - 1
        );
    }

    private void createNextSW() {
        nextSW = new HeightGenerator(
                thisSearchDistanceX, thisSearchDistanceZ,
                decisionPositionX - nextSearchDistanceX, decisionPositionZ - nextSearchDistanceZ,
                nextHW, height, hSW, nextHS,
                currentDepth - 1
        );
    }

    private void createNextSE() {
        nextSE = new HeightGenerator(
                thisSearchDistanceX, thisSearchDistanceZ,
                decisionPositionX + nextSearchDistanceX, decisionPositionZ - nextSearchDistanceZ,
                height, nextHE, nextHS, hSE,
                currentDepth - 1
        );
    }

    public static void cleanse() {
        mainNHG.cleanseInternal();
    }

    // If there is a next level and it's not been used then dispose of it
    private void cleanseInternal() {
        if (nextNE != null) {
            nextNE.cleanseInternal();

            if (nextNE.usedFlag) {
                nextNE.usedFlag = false;
            } else {
                nextNE = null;
            }
        }

        if (nextNW != null) {
            nextNW.cleanseInternal();

            if (nextNW.usedFlag) {
                nextNW.usedFlag = false;
            } else {
                nextNW = null;
            }
        }

        if (nextSE != null) {
            nextSE.cleanseInternal();

            if (nextSE.usedFlag) {
                nextSE.usedFlag = false;
            } else {
                nextSE = null;
            }
        }

        if (nextSW != null) {
            nextSW.cleanseInternal();
            
            if (nextSW.usedFlag) {
                nextSW.usedFlag = false;
            } else {
                nextSW = null;
            }
        }
    }

    private static long longPowerOf2(int power) {
        return 1 << power;
    }

    public static float randomIshFactor(long posX, long posZ, float scalingFactor) {
        Random rNum = new Random(posX + worldSize + posZ * worldSize + (long) sqrt(posX * posX + posZ * posZ));
//        Random rNum = new Random(posX + worldSize * posZ);
//        Random rNum = new Random((long) sqrt(posX * posX + posZ * posZ));

        float result = scalingFactor * (rNum.nextFloat() - 0.45f);
//        float result = scalingFactor * 0.17f;
//        float result = 0;

        if (posX <= 0 || posX >= worldSize || posZ <= 0 || posZ >= worldSize) {
            result = -1;
        }

        return result;
    }

    private static void createScaleList() {
        int jumps = 2;
        scaleList = new float[worldPower + 1];

        float scaleModifier = 2.85f;
        float scaleStep = (scaleModifier - 1.0f) / worldPower;
        scaleList[0] = 0.01f;

        for (int loop = 1; loop <= scaleList.length / jumps; loop++) {
            scaleList[loop] = scaleList[loop - 1] * scaleModifier;
            scaleList[loop + worldPower / jumps] = scaleList[loop];

            scaleModifier -= scaleStep;
        }
//
//        for (int loop = 0; loop < scaleList.length; loop++) {
//            System.out.println("scaleList[" + loop + "] = " + scaleList[loop]);
//        }
    }

    public static void setWorldSize(int power) {
        worldPower = power;
        worldSize = (int) longPowerOf2(power);
        createScaleList();
        System.out.println("worldSize = " + worldSize);
    }
}
