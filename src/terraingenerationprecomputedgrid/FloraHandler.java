/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terraingenerationprecomputedgrid;

import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.transform.Translate;
import static terraingenerationprecomputedgrid.LandscapeGenerator.GRASS_LINE;
import static terraingenerationprecomputedgrid.LandscapeGenerator.ROCK_LINE;

/**
 *
 * @author Mark
 */
public class FloraHandler {

    private static final int BASE_SEPARATION = 500;
    private static int worldSize;

    private static ArrayList<Flora> floraList;

    private static double lastWorldPosX = 0, lastWorldPosZ = 0;
    private static Group visibleFlora;

    public static void initialise(double worldPosX, double worldPosZ, int worldPower) {
        floraList = new ArrayList<>();
        worldSize = (int) longPowerOf2(worldPower);

        for (double loopX = -BASE_SEPARATION * 10; loopX <= BASE_SEPARATION * 10; loopX += BASE_SEPARATION) {
            for (double loopZ = -BASE_SEPARATION * 10; loopZ <= BASE_SEPARATION * 10; loopZ += BASE_SEPARATION) {
                double baseInWorldX = worldPosX + loopX;
                double baseInWorldZ = worldPosZ + loopZ;

                if (baseInWorldX >= 0 && baseInWorldX <= worldSize && baseInWorldZ >= 0 && baseInWorldZ <= worldSize) {
                    float height = -HeightGenerator.getHeight(baseInWorldX, baseInWorldZ);

                    // Make sure it's not underwater! Between grass and rock line for flora ideally
                    if (height < GRASS_LINE && height > ROCK_LINE) {
                        // Now hand over to the flora holder to generate everything
                        floraList.add(new Flora(baseInWorldX, height, baseInWorldZ));
                    }
                }
            }
        }

        // Now to add all the visible flora to the scene
        visibleFlora = new Group();
        for (int loop = 0; loop < floraList.size(); loop++) {
            Group flora = floraList.get(loop).getFloraGroup();
            flora.getTransforms().addAll(new Translate((float) floraList.get(loop).worldPositionX, 0, floraList.get(loop).worldPositionZ));
            visibleFlora.getChildren().add(flora);
        }

        // Finally update the last positions
        lastWorldPosX = worldPosX;
        lastWorldPosZ = worldPosZ;
    }

    public static void update(double newWorldPosX, double newWorldPosZ) {
        // Go backwards through the list to remove the out of sight ones
        for (int loop = floraList.size(); loop >= 0; loop--) {
            // Find the removal area
            // Test if the flora group is in the removal area
            // @@@

        }

        // Now find the points in the new area
        // @@@
        // Then finally update the last positions
        lastWorldPosX = newWorldPosX;
        lastWorldPosZ = newWorldPosZ;
    }

    public static Group getFlora() {
        return visibleFlora;
    }

    private static long longPowerOf2(int power) {
        return 1 << power;
    }
}
