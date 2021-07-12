/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terraingenerationprecomputedgrid;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import java.util.Random;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 *
 * @author Mark
 */
public class Flora {

    private static final int[] FIBONACCI_LIST = {1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144};
    // These arethe offsets into the texture coordinates array
    private static final int OFFSET_DECIDUOUS = 0, OFFSET_EVERGREEN = 4, OFFSET_BUSH = 8, OFFSET_SCRUB = 12;
    private static final float IMAGE_WIDTH = 5, IMAGE_HEIGHT = 10;
    private static final float[] FLORA_POINTS_1 = {
        -IMAGE_WIDTH, 0, 0,
        IMAGE_WIDTH, 0, 0,
        IMAGE_WIDTH, IMAGE_HEIGHT, 0,
        -IMAGE_WIDTH, IMAGE_HEIGHT, 0
    };
    private static final float[] FLORA_POINTS_2 = {
        0, 0, -IMAGE_WIDTH,
        0, 0, IMAGE_WIDTH,
        0, IMAGE_HEIGHT, IMAGE_WIDTH,
        0, IMAGE_HEIGHT, -IMAGE_WIDTH
    };
    private static Image imageTextureFlora;
    private static PhongMaterial floraMaterial;

    public double worldPositionX, worldPositionZ;
    public float height;
    private Group localFloraGroup;

    public Flora(double x, float h, double z) {
        this.worldPositionX = x;
        this.height = h;
        this.worldPositionZ = z;
        localFloraGroup = new Group();

        setup();
        createTheFloraPatch();
    }

    public Group getFloraGroup() {
        return localFloraGroup;
    }

    private static void setup() {
        if (imageTextureFlora == null) {
            imageTextureFlora = new Image("file:Flora.gif");
        }

        if (floraMaterial == null) {
            floraMaterial = new PhongMaterial();
            floraMaterial.setDiffuseMap(imageTextureFlora);
            floraMaterial.setSpecularMap(imageTextureFlora);
        }
    }

    private void createTheFloraPatch() {
        Random rng = new Random((long) (worldPositionX + worldPositionZ));

        // How many items are we going to have?
        int itemCount = FIBONACCI_LIST[rng.nextInt(FIBONACCI_LIST.length)];

        // Should always start from a different position for each group
        double startAngleOffset = rng.nextDouble() * 360;
        for (int loop = 0; loop < itemCount; loop++) {
            // Offset from group centre
            double centreOffset = rng.nextDouble() * 250;
            double posX = centreOffset * sin(toRadians(startAngleOffset + (360 * loop / itemCount)));
            double posZ = centreOffset * cos(toRadians(startAngleOffset + (360 * loop / itemCount)));

            MeshView floraMV = new MeshView();
            floraMV.setCullFace(CullFace.NONE);
            floraMV.setMaterial(new PhongMaterial(Color.BLUE));
            floraMV.setDrawMode(DrawMode.FILL);
            floraMV.setMaterial(floraMaterial);

            TriangleMesh localTM = new TriangleMesh();
            localTM.getPoints().addAll(FLORA_POINTS_1);
            localTM.getPoints().addAll(FLORA_POINTS_2);
            setTextures(localTM);

            // TODO - height dependent flora. For now just stick with the low level
            switch (rng.nextInt(3)) {
                case 0:
                    // Tree
                    localTM.getFaces().setAll(
                            0, OFFSET_DECIDUOUS + 0,
                            1, OFFSET_DECIDUOUS + 1,
                            3, OFFSET_DECIDUOUS + 3,
                            1, OFFSET_DECIDUOUS + 1,
                            2, OFFSET_DECIDUOUS + 2,
                            3, OFFSET_DECIDUOUS + 3,
                            4, OFFSET_DECIDUOUS + 0,
                            5, OFFSET_DECIDUOUS + 1,
                            7, OFFSET_DECIDUOUS + 3,
                            5, OFFSET_DECIDUOUS + 1,
                            6, OFFSET_DECIDUOUS + 2,
                            7, OFFSET_DECIDUOUS + 3
                    );

                    break;
                default:
                    // Bush
                    localTM.getFaces().setAll(
                            0, OFFSET_BUSH + 0,
                            1, OFFSET_BUSH + 1,
                            3, OFFSET_BUSH + 3,
                            1, OFFSET_BUSH + 1,
                            2, OFFSET_BUSH + 2,
                            3, OFFSET_BUSH + 3,
                            4, OFFSET_BUSH + 0,
                            5, OFFSET_BUSH + 1,
                            7, OFFSET_BUSH + 3,
                            5, OFFSET_BUSH + 1,
                            6, OFFSET_BUSH + 2,
                            7, OFFSET_BUSH + 3
                    );

                    break;
            }

            floraMV.setMesh(localTM);
            Scale flip = new Scale(1, -1, 1);
            floraMV.getTransforms().setAll(new Translate(posX, -HeightGenerator.getHeight(posX + worldPositionX, posZ + worldPositionZ), posZ), flip);
//            floraMV.getTransforms().setAll( new Translate(posX, 0, posZ),flip);
            localFloraGroup.getChildren().add(floraMV);
        }
    }

    private static void setTextures(TriangleMesh outputMesh) {
        float positionDeciduous = 0.0f, positionEvergreen = 0.25f, positionBush = 0.50f, positionScrub = 0.75f, left = 0f, right = 0.25f;
        float top = 0.05f, bottom = 0.95f;

        outputMesh.getTexCoords().clear();

        outputMesh.getTexCoords().addAll(
                positionDeciduous + left, bottom,
                positionDeciduous + right, bottom,
                positionDeciduous + right, top,
                positionDeciduous + left, top
        );
        outputMesh.getTexCoords().addAll(
                positionEvergreen + left, bottom,
                positionEvergreen + right, bottom,
                positionEvergreen + right, top,
                positionEvergreen + left, top
        );
        outputMesh.getTexCoords().addAll(
                positionBush + left, bottom,
                positionBush + right, bottom,
                positionBush + right, top,
                positionBush + left, top
        );
        outputMesh.getTexCoords().addAll(
                positionScrub + left, bottom,
                positionScrub + right, bottom,
                positionScrub + right, top,
                positionScrub + left, top
        );
    }
}
