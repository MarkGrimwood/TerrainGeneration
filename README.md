# TerrainGeneration
An old Java project resurrected as a code demo

I wrote this back in 2016 when I had an idea for a game and wanted to learn Java. I needed to create an island and rather than use something predefined I started playing with terrain generation, which briefly became an obsession. The game never happened in the end, but the exploration of techniques was fun. I'd got as far as adding trees for a limited area before I stopped work on it

The code needs JavaFX to run. It does work with Java 8, but in later versions JavaFX isn't bundled with Java and I haven't been able to get it running with the latest version yet

I used NetBeans as an IDE and I've uploaded the whole project directory here complete with some of the screenshots taken. It renders ok when run in NetBeans, but outside of that it's basic colours and blocks instead of trees. I might revisit it sometime to fix that

I've had it working outside the IDE by using one of the commands:
  Navigating to the build/classes/ directory and typing java terraingenerationprecomputedgrid.TerrainGenerationPrecomputedGrid
  Navigating to the dist/ directory and typing java -jar TerrainGenerationPrecomputedGrid.jar
  Double clicking on the .jar file in the dist/ directory (works in Windows)

Controls are:
  Up and down arrows: move forwards and backwards
  Left and right arrows: move left and right
  Shift with up and down arrows: increase and decrease movement speed
  Control with up and down arrows: pan camera up and down
  Control with left and right arrows: rotate camera left and right
  Alt with up and down arrows: move up and down
  C: saves a .png of the current view
  P: print location - used for debugging purposes
