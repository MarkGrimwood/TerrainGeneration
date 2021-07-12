/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terraingenerationprecomputedgrid;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.pow;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.KP_DOWN;
import static javafx.scene.input.KeyCode.KP_LEFT;
import static javafx.scene.input.KeyCode.KP_RIGHT;
import static javafx.scene.input.KeyCode.KP_UP;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.P;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javax.imageio.ImageIO;



/**
 *
 * @author Mark
 */
public class TerrainGenerationPrecomputedGrid extends Application {

    private static final int WORLD_SIZE = 30;
//    private static final int START_X = 1073741824, START_Z = 1073741824;
//    private static final int START_X = 536870912, START_Z = 536870912;
    private static final int START_X = 0, START_Z = 0;
    private static final float CEILING = 5000f;
    private static double speed = 1f;

    private static final boolean SHOW_SEA_LEVEL = true;
    private static final boolean SHOW_GRID = !true;
    private static final boolean SHOW_LANDSCAPE = !SHOW_GRID;
    private static final boolean SHOW_FLORA = true;
    private static final boolean CREATE_MAP = !true;

    private static final int SCREEN_WIDTH = 1300, SCREEN_HEIGHT = 700, FOV = 30;
    private static final int CLIPPING_POWER = 20, CLIPPING_DISTANCE = (int) pow(2, CLIPPING_POWER);
    private static double cameraPan = 45.0, cameraTilt = 0.0;

    private static double worldPosX = START_X, worldPosY = 0, worldPosZ = START_Z, viewHeightOffset = 2;

    private static PerspectiveCamera camera;
    private static MeshView worldMeshView, worldGridView, seaLevelMeshView;
    private static LandscapeGenerator theWorld;
    private static Group worldOfTrees;
    private static Image imageTextureLandscape;

    private static TimingHelper worldCreationStopwatch;

    public static void main(String[] args) {
        if (imageTextureLandscape == null) {
            try {
                imageTextureLandscape = new Image("file:BasicLandscape1.png");
                System.out.println("Image size: " + imageTextureLandscape.getWidth() + ", " + imageTextureLandscape.getHeight());
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
        }

        HeightGenerator.setWorldSize(WORLD_SIZE);

        if (CREATE_MAP) {
            createMap();
        }

        seaLevel();
        createWorld(worldPosX, worldPosZ);

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setResizable(false);
        Scene scene = new Scene(createContent());
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setOnKeyPressed(
                event -> {
                    if (event.isShiftDown()) {
                        switch (event.getCode()) {
                            case UP:
                            case KP_UP:
                                if (speed < 1024) {
                                    speed *= 2;
                                }
                                break;
                            case DOWN:
                            case KP_DOWN:
                                if (speed > 1) {
                                    speed /= 2;
                                }
                                break;
                        }
                    } else if (event.isControlDown()) {
                        switch (event.getCode()) {
                            case UP:
                            case KP_UP:
                                cameraTilt += 0.5;
                                moveCamera();
                                break;
                            case DOWN:
                            case KP_DOWN:
                                cameraTilt -= 0.5;
                                moveCamera();
                                break;
                            case LEFT:
                            case KP_LEFT:
                                cameraPan += 359;
                                cameraPan %= 360;
                                moveCamera();
                                break;
                            case RIGHT:
                            case KP_RIGHT:
                                cameraPan += 1;
                                cameraPan %= 360;
                                moveCamera();
                                break;
                        }
                    } else if (event.isAltDown()) {
                        switch (event.getCode()) {
                            case UP:
                            case KP_UP:
                                viewHeightOffset += speed;
                                moveWorld();
                                break;
                            case DOWN:
                            case KP_DOWN:
                                viewHeightOffset -= speed;
                                moveWorld();
                                break;
                        }
                    } else {
                        switch (event.getCode()) {
                            case UP:
                            case KP_UP:
                                updatePosition(0);
                                break;
                            case DOWN:
                            case KP_DOWN:
                                updatePosition(180);
                                break;
                            case LEFT:
                            case KP_LEFT:
                                updatePosition(-90);
                                break;
                            case RIGHT:
                            case KP_RIGHT:
                                updatePosition(90);
                                break;
                            case P:
                                System.out.println(
                                        "p = " + cameraPan
                                        + ", t = " + cameraTilt
                                        + ", x = " + worldPosX
                                        + ", y = " + worldPosY
                                        + ", z = " + worldPosZ
                                );
                                break;
                            case C:
                                WritableImage test = scene.snapshot(null);
                                try {
                                    String fileName = "p" + cameraPan
                                    + "_t" + cameraTilt
                                    + "_x" + worldPosX
                                    + "_y" + worldPosY
                                    + "_z" + worldPosZ;
                                    File outputfile = new File(fileName + ".png");
                                    ImageIO.write(SwingFXUtils.fromFXImage(test, null), "png", outputfile);
                                } catch (IOException e) {
                                    System.out.println("Caught " + e.getMessage());
                                }
                                break;
                        }
                    }
                }
        );
    }

    private void updatePosition(double modification) {
        double nextX = 0, nextZ = 0;

        nextX = worldPosX + (speed * Math.sin(Math.toRadians(cameraPan + modification)));
        nextZ = worldPosZ + (speed * Math.cos(Math.toRadians(cameraPan + modification)));

        if (nextX >= 0 && nextX <= (1 << WORLD_SIZE)) {
            worldPosX = nextX;
        }
        if (nextZ >= 0 && nextZ <= (1 << WORLD_SIZE)) {
            worldPosZ = nextZ;
        }

        moveWorld();
    }

    public Parent createContent() throws Exception {
        // Build the Scene Graph
        Group root = new Group();
        root.getChildren().add(initialiseLight());
//        root.getChildren().add(initialiseFillLight());

        if (SHOW_SEA_LEVEL) {
            root.getChildren().add(seaLevelMeshView);
        }
        if (SHOW_LANDSCAPE) {
            root.getChildren().add(worldMeshView);
        }
        if (SHOW_GRID) {
            root.getChildren().add(worldGridView);
        }
        if (SHOW_FLORA) {
            root.getChildren().add(worldOfTrees);
        }

        // Use a SubScene       
        SubScene subScene = new SubScene(root, SCREEN_WIDTH, SCREEN_HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.SKYBLUE);
        subScene.setCamera(initialiseCamera());

        Group group = new Group();
        group.getChildren().add(subScene);

        return group;
    }

    private static void moveCamera() {
        camera.getTransforms().clear();
        camera.getTransforms().addAll(new Rotate(cameraPan, Rotate.Y_AXIS));
        camera.getTransforms().addAll(new Rotate(cameraTilt, Rotate.X_AXIS));
    }

    private static void moveWorld() {
        if (viewHeightOffset < 0) {
            viewHeightOffset = 0;
        } else if (viewHeightOffset > CEILING) {
            viewHeightOffset = CEILING;
        }

        worldPosY = viewHeightOffset + HeightGenerator.getHeight(worldPosX, worldPosZ);
//        worldPosY =  CEILING;

        if (worldPosY < viewHeightOffset) {
            worldPosY = viewHeightOffset;
        }

        worldCreationStopwatch.startTimer();
        theWorld.update(worldPosX, worldPosZ);
        worldCreationStopwatch.pauseTimer();

        TriangleMesh landscapeMesh = theWorld.getTriangleMesh();
        double moveX = (worldPosX + 0.5) % 1;
        double moveZ = (worldPosZ + 0.5) % 1;

        if (SHOW_LANDSCAPE) {
            worldMeshView.setMesh(landscapeMesh);
            worldMeshView.getTransforms().setAll(new Translate(-moveX, worldPosY, -moveZ));
        }
        if (SHOW_GRID) {
            worldGridView.setMesh(landscapeMesh);
            worldGridView.getTransforms().setAll(new Translate(-moveX, worldPosY, -moveZ));
        }
        if (SHOW_SEA_LEVEL) {
            seaLevelMeshView.getTransforms().setAll(new Translate(0, worldPosY, 0));
        }
        if (SHOW_FLORA) {
            worldOfTrees.getTransforms().setAll(new Translate(-worldPosX, worldPosY, -worldPosZ));
        }
    }

    public static PerspectiveCamera initialiseCamera() {
        camera = new PerspectiveCamera(true);
        camera.setFarClip(CLIPPING_DISTANCE);
        camera.setFieldOfView(FOV);
        camera.setVerticalFieldOfView(false);
        moveCamera();

        String desc = "Camera rotation = " + cameraPan
                + ", worldPosX = " + worldPosX
                + ", worldPosY = " + worldPosY
                + ", worldPosZ = " + worldPosZ;
        System.out.println(desc);

        return camera;
    }

    public static PointLight initialiseLight() {
        // Get the height for the camera
        float lightY = 50000;   // HeightGenerator.getHeight(worldPosX, worldPosZ);

        PointLight light = new PointLight();
        light.setColor(Color.WHITE);
        light.getTransforms().setAll(new Translate(-100000, -lightY - 100000, -100000));

        return light;
    }

    public static PointLight initialiseFillLight() {
        // Get the height for the camera
        float lightY = 50000;   //HeightGenerator.getHeight(worldPosX, worldPosZ);

        PointLight light = new PointLight();
        light.setColor(Color.DARKOLIVEGREEN);
        light.getTransforms().setAll(new Translate(100000, -lightY - 100000, 100000));

        return light;
    }

    private static void seaLevel() {
        float referencePlaneSize = CLIPPING_DISTANCE;

        // These are the required points. Order isn't important, and they don't define triangles, just points
        float[] meshCoords = {
            -referencePlaneSize, 0, -referencePlaneSize,
            referencePlaneSize, 0, -referencePlaneSize,
            referencePlaneSize, 0, referencePlaneSize,
            -referencePlaneSize, 0, referencePlaneSize
        };

        // These are references to the positions in mesh coordinates above, paired with the position in the texture array
        int[] triangleWinding = {
            0, 0, 1, 0, 3, 0,
            1, 0, 2, 0, 3, 0
        };

        TriangleMesh seaLevelMesh = new TriangleMesh();
        seaLevelMesh.getTexCoords().addAll(0, 0);
        seaLevelMesh.getPoints().addAll(meshCoords);
        seaLevelMesh.getFaces().addAll(triangleWinding);

        if (SHOW_SEA_LEVEL) {
            seaLevelMeshView = new MeshView(seaLevelMesh);
            seaLevelMeshView.setCullFace(CullFace.BACK);
            seaLevelMeshView.setMaterial(new PhongMaterial(Color.BLUE));
            seaLevelMeshView.setDrawMode(DrawMode.FILL);
        }
    }

    private static void createWorld(double posX, double posZ) {
        PhongMaterial groundMaterial = new PhongMaterial();
        groundMaterial.setDiffuseMap(imageTextureLandscape);

        if (worldCreationStopwatch == null) {
            worldCreationStopwatch = new TimingHelper("World Creation", 1000);
        }

        // Create the world
        theWorld = new LandscapeGenerator(posX, posZ, WORLD_SIZE > CLIPPING_POWER ? CLIPPING_POWER : WORLD_SIZE);

        worldCreationStopwatch.report();
        worldCreationStopwatch.pauseTimer();

        if (SHOW_LANDSCAPE) {
            worldMeshView = new MeshView(theWorld.getTriangleMesh());
            worldMeshView.setCullFace(CullFace.BACK);
            worldMeshView.setDrawMode(DrawMode.FILL);
//            worldMeshView.setMaterial(new PhongMaterial(Color.GREEN));
            worldMeshView.setMaterial(groundMaterial);
        }

        if (SHOW_GRID) {
            worldGridView = new MeshView(theWorld.getTriangleMesh());
            worldGridView.setCullFace(CullFace.NONE);
            worldGridView.setDrawMode(DrawMode.LINE);
            worldGridView.setMaterial(new PhongMaterial(Color.DARKGREEN));
        }

        if (SHOW_FLORA) {
            FloraHandler.initialise(worldPosX, worldPosZ, WORLD_SIZE);
            worldOfTrees = FloraHandler.getFlora();
        }

        moveWorld();
    }

    private static void createMap() {
        System.out.println("Creating map");

        int mapDim = 4096;
        BufferedImage mapImage = new BufferedImage(mapDim, mapDim, BufferedImage.TYPE_INT_RGB);

        int step = ((int) pow(2, WORLD_SIZE)) / mapDim / 1;

        System.out.println("mapDim = " + mapDim + ", step = " + step + ", side = " + (mapDim * step));

        for (int loopX = 0; loopX < mapDim; loopX++) {
            for (int loopY = 0; loopY < mapDim; loopY++) {
                // Get the height
                float height = -HeightGenerator.getHeight(loopX * step, loopY * step);

                // Set the colour according the height rule
                if (height < -5000) {
                    // Snow
                    int val = (int) (-height - 5000) * 63 / 3000 + 192;
                    if (val > 255) {
                        val = 255;
                    }
                    mapImage.setRGB(loopX, mapDim - loopY - 1, (((val << 8) + val) << 8) + val);
                } else if (height < -3000) {
                    // Rock
                    int val = (int) (-height - 3000) * 63 / 2000 + 64;
                    if (val > 127) {
                        val = 127;
                    }
                    mapImage.setRGB(loopX, mapDim - loopY - 1, ((val << 8) + val) << 8);
                } else if (height >= 0) {
                    // Water
                    mapImage.setRGB(loopX, mapDim - loopY - 1, 0x000000FF);
                } else {
                    // Grass
                    int val = (int) (-height - 3000) * 63 / 3000 + 192;
                    if (val > 255) {
                        val = 255;
                    }
                    mapImage.setRGB(loopX, mapDim - loopY - 1, val << 8);
                }
            }

            HeightGenerator.cleanse();
        }

        File outputfile = new File("map.png");
        try {
            ImageIO.write(mapImage, "png", outputfile);
        } catch (IOException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        System.out.println("Mappping finished");
    }
}
