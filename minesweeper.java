import java.util.ArrayList;
import java.util.Arrays;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Collections;
import java.util.Random;


// represents a world class to animate the MineSweeper game
class GameWorld extends World {
  int numRows;
  int numCols;
  int numMines;
  Random rand;
  ArrayList<ArrayList<Cell>> gameGrid;
  boolean loseGame;
  int clock;
  int numFlagged;
  int cellsUncovered;
  boolean winGame;

  GameWorld() {
    this.numCols = 30;
    this.numRows = 16;
    this.numMines = 99;
    this.rand = new Random(7);
    this.loseGame = false;
    this.clock = 0;
    this.numFlagged = 0;
    this.cellsUncovered = 0;
    this.winGame = false;
    this.gameGrid = new ArrayList<ArrayList<Cell>>();

    for (int i = 0; i < 16; i++) {
      ArrayList<Cell> row = new ArrayList<>();
      for (int j = 0; j < 30; j++) {
        row.add(new Cell());
      }
      gameGrid.add(row);
    }

    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numCols; j++) {
        this.assignNeighbors(i, j);
      }
    }
  }

  GameWorld(int numCols, int numRows, int numMines, Random rand) {

    if (numRows <= 0 || numCols <= 0) {
      throw new IllegalArgumentException("Grid dimensions must be positive!");
    }

    if (numCols < 9 || numCols > 35) {
      throw new IllegalArgumentException("Grid must have atleast 9 columns and no more than 35 "
          + "columns!");
    }

    if (numRows > 17) {
      throw new IllegalArgumentException("Grid can't have more than 17 rows!");
    }

    if (numMines <= 0) {
      throw new IllegalArgumentException("Number of mines must be positive!");
    }

    if (numMines >= numRows * numCols) {
      throw new IllegalArgumentException("Number of mines must be less than total number of "
          + "cells in the grid!");
    }

    this.numCols = numCols;
    this.numRows = numRows;
    this.numMines = numMines;
    this.rand = rand;
    this.loseGame = false;
    this.clock = 0;
    this.numFlagged = 0;
    this.cellsUncovered = 0;
    this.winGame = false;
    this.gameGrid = new ArrayList<ArrayList<Cell>>();


    for (int i = 0; i < numRows; i++) {
      ArrayList<Cell> row = new ArrayList<>();
      for (int j = 0; j < numCols; j++) {
        row.add(new Cell());
      }
      gameGrid.add(row);
    }

    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numCols; j++) {
        this.assignNeighbors(i, j);
      }
    }
  }

  // draws the MineSweeper grid and cells and game's score
  public WorldScene makeScene() {

    WorldScene scene = getEmptyScene();

    this.drawGridLayout(scene);
    this.drawScoreTracker(scene);


    if (this.loseGame || this.winGame) {
      this.endGame(scene);
    }

    else {
      this.continueGame(scene);
    }

    return scene;
  }

  // EFFECT: draws the MineSweeper grid with gray cells, with the overall grid following the
  // dimensions of this GameWorld  
  public void drawGridLayout(WorldScene scene) {
    for (int i = 0; i < this.numRows; i++) {
      for (int j = 0; j < this.numCols; j++) {
        int x = j * 40 + 20;
        int y = i * 40 + 20;

        WorldImage cellInner = new RectangleImage(40, 40, OutlineMode.SOLID, Color.LIGHT_GRAY);
        WorldImage cellOutline = new RectangleImage(40, 40, OutlineMode.OUTLINE, Color.BLACK);

        scene.placeImageXY(cellInner, x, y);
        scene.placeImageXY(cellOutline, x, y);
      }
    }
  }

  // EFFECT: draws the bottom panel that keeps track of the player's scores, with stats being 
  // amount of time taken for the current game, # of remaining uncovered mines, # of cells 
  // uncovered, and remaining cells
  public void drawScoreTracker(WorldScene scene) {

    WorldImage time = new TextImage("Time: " + this.clock + "s", 27, Color.RED);
    scene.placeImageXY(time, this.numCols * 20, this.numRows * 40 + 20);

    WorldImage numUncoveredMines = new TextImage(
        "# remaining uncovered mines: " + Integer.toString(this.numMines - this.numFlagged), 
        20, Color.BLUE);
    scene.placeImageXY(numUncoveredMines, this.numCols * 20, this.numRows * 40 + 100);

    WorldImage numCells = new TextImage(
        "# cells uncovered: " + this.cellsUncovered, 
        20, Color.BLUE);
    scene.placeImageXY(numCells, this.numCols * 20, this.numRows * 40 + 130);

    int remainingCells = this.numRows * this.numCols - (this.numMines + this.cellsUncovered);
    WorldImage numRemCells = new TextImage(
        "# remaining cells: " + remainingCells, 
        20, Color.BLUE);

    scene.placeImageXY(numRemCells, this.numCols * 20, this.numRows * 40 + 160);

  }

  // EFFECT: ends the current MineSweeper game by displaying a message based on whether the 
  // player has won or lost and prevents the player from clicking on cells in the board
  public void endGame(WorldScene scene) {

    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < this.numCols; j++) {
        Cell individualCell = gameGrid.get(i).get(j);

        int x = j * 40 + 20;
        int y = i * 40 + 20;

        if (individualCell.isMine) {
          WorldImage mine = new CircleImage(12, OutlineMode.SOLID, Color.RED);
          scene.placeImageXY(mine, x, y);
        }

        else if (individualCell.displayNumNeighbors) {
          int numNeighbors = individualCell.countNeighboringMines();

          if (numNeighbors == 0) {
            WorldImage square = new RectangleImage(39, 39, OutlineMode.SOLID, Color.DARK_GRAY);
            scene.placeImageXY(square, x, y);
          }

          else {
            this.displaysNumberNeighbors(scene, numNeighbors, x, y);
          }
        }
      }
    }

    if (this.loseGame) {
      WorldImage loseText = new TextImage("SORRY, GAME OVER!", 30, Color.BLACK);
      scene.placeImageXY(loseText, numCols * 20, numRows * 40 + 60);
    }

    if (this.winGame) {
      WorldImage winText = new TextImage("CONGRATS, YOU WON!", 30, Color.BLACK);
      scene.placeImageXY(winText, numCols * 20, numRows * 40 + 60);
    }
  }


  public void displaysNumberNeighbors(WorldScene scene, int numNeighbors, int x, int y) {
    Color color = Color.DARK_GRAY;

    switch(numNeighbors) {
    
    case 2: 
      color = Color.BLUE;
      break;
     
    case 3: 
      color = Color.GREEN;
      
    case 4: 
      color = Color.PINK;
      break;
    
    case 5: 
      color = Color.ORANGE;
      break;
     
    case 6: 
      color = Color.MAGENTA;
      
    case 7: 
      color = Color.BLACK;
      break;
    
    case 8: 
      color = Color.YELLOW;
      break;
    }
    
    WorldImage square = new RectangleImage(39, 39, OutlineMode.SOLID, color);
    WorldImage num = new TextImage(Integer.toString(numNeighbors), 21, Color.WHITE);
    WorldImage numberedCell = new OverlayImage(num, square);
    scene.placeImageXY(numberedCell, x, y);
  }

  // EFFECT: allows the player to continue playing the game on the grid
  public void continueGame(WorldScene scene) {

    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < this.numCols; j++) {
        Cell individualCell = gameGrid.get(i).get(j);

        int x = j * 40 + 20;
        int y = i * 40 + 20;

        if (individualCell.isFlagged) {
          WorldImage flag = new EquilateralTriangleImage(22, OutlineMode.SOLID, Color.ORANGE);
          scene.placeImageXY(flag, x, y);
        }

        else if (individualCell.displayNumNeighbors) {
          int numNeighbors = individualCell.countNeighboringMines();

          if (numNeighbors == 0) {
            WorldImage square = new RectangleImage(39, 39, OutlineMode.SOLID, Color.DARK_GRAY);
            scene.placeImageXY(square, x, y);
          }

          else {
            this.displaysNumberNeighbors(scene, numNeighbors, x, y);
          }
        }
      }
    }
  }


  // EFFECT: restarts the MineSweeper game when the player presses the return key 
  // after the game is over
  public void onKeyEvent(String key) {
    boolean gameOver = this.winGame || this.loseGame;

    if (gameOver && key.equals("enter")) {
      this.restartGame();
    }
  }

  // EFFECT: Restarts the MineSweeper game by resetting all fields of GameWorld 
  // to their initial values and generating a new mine layout. 
  public void restartGame() {

    this.rand = new Random();
    this.loseGame = false;
    this.clock = 0;
    this.numFlagged = 0;
    this.cellsUncovered = 0;
    this.winGame = false;
    this.gameGrid = new ArrayList<ArrayList<Cell>>();


    for (int i = 0; i < numRows; i++) {
      ArrayList<Cell> row = new ArrayList<>();
      for (int j = 0; j < numCols; j++) {
        row.add(new Cell());
      }
      gameGrid.add(row);
    }

    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numCols; j++) {
        this.assignNeighbors(i, j);
      }
    }
  }

  // EFFECT: increments the clock counter at every tick if the current game has not finished
  public void onTick() {

    if (!this.winGame && !this.loseGame) {
      this.clock++;
    }
  }


  // EFFECT: randomly places mines on the game grid by setting the isMine field of certain cells
  // to true, while ensuring no mine is placed in the cell at the row and given column.
  void assignMines(int givenRow, int givenCol) {
    ArrayList<Integer> ints = new ArrayList<>();

    for (int i = 0; i < this.numRows * this.numCols; i++) {
      ints.add(i);
    }

    ints.remove(givenRow * this.numCols + givenCol);

    Collections.shuffle(ints, this.rand);

    for (int i = 0; i < numMines; i++) {
      int num = ints.get(i);
      int row = num / this.numCols;
      int column = num % this.numCols;

      gameGrid.get(row).get(column).isMine = true;
    }
  }

  // EFFECT: Assigns the neighbors of the cell at the given column and given row 
  // by adding them to the cell's neighbors list.
  // Neighbors include: top, top-right, top-left, bottom, bottom-right, bottom-left, left, right.
  void assignNeighbors(int row, int col) {
    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        if (i != 0 || j != 0) {

          int x = row + i;
          int y = col + j;

          if (x >= 0 && y >= 0 && x < numRows && y < numCols) {
            Cell cell = this.gameGrid.get(row).get(col);
            cell.neighbors.add(this.gameGrid.get(x).get(y));
          }
        }
      }
    }
  }

  // EFFECT: updates the game grid and cells when mouse buttons are clicked
  // left-mouse-clicks reveal cells, while right-mouse-clicks flags/unflags cells.
  public void onMouseClicked(Posn position, String button) {

    int col = position.x / 40;
    int row = position.y / 40;

    if (col >= this.numCols || row >= this.numRows || col < 0 || row < 0) {
      return;
    }
    
    if (this.cellsUncovered == 0 && button.equals("LeftButton")) {
      this.assignMines(row, col);
    }
    
    if (this.winGame || this.loseGame) {
      return;
    }

    else if (button.equals("RightButton")) {

      Cell clickedCell = this.gameGrid.get(row).get(col);

      if (!clickedCell.isFlagged) {

        clickedCell.isFlagged = true;
        this.numFlagged++;
      }
      
      else {
        clickedCell.isFlagged = false;
        this.numFlagged--;
      }
    }

    else if (button.equals("LeftButton")) {
      this.pressLeftButton(row, col);
    }
  }
  
  // EFFECT: updates the grid and its cells accordingly when the player clicks on a cell with the 
  // left mouse button
  public void pressLeftButton(int row, int col) {
    
    Cell clickedCell = this.gameGrid.get(row).get(col);

    if (clickedCell.isMine) {
      this.loseGame = true;
    }

    else if (!clickedCell.isFlagged && !clickedCell.displayNumNeighbors) {

      if (clickedCell.countNeighboringMines() == 0) {
        this.floodfill(clickedCell);

        int remainingCells = this.numRows * this.numCols - (this.numMines + this.cellsUncovered);
        if (remainingCells == 0) {
          this.winGame = true;
        }

      }
      
      else {
        clickedCell.displayNumNeighbors = true;
        this.cellsUncovered++;

        int remainingCells = this.numRows * this.numCols - (this.numMines + this.cellsUncovered);
        if (remainingCells == 0) {
          this.winGame = true;
        }
      }
    }
  }

  // EFFECT: updates the cellsUncovered field and causes a cascading “flood-fill” effect where an 
  // entire region of connected cells without mines is uncovered, stopping once it reaches a cell 
  // with a non-zero neighboring mine count.
  void floodfill(Cell c) {
    if (c.displayNumNeighbors || c.isFlagged || c.isMine) {
      return;
    }

    c.displayNumNeighbors = true;
    this.cellsUncovered++;

    if (c.countNeighboringMines() == 0) {
      for (Cell neighbor : c.neighbors) {
        floodfill(neighbor);
      }
    }
  }
}


// represents a cell of a MineSweeper grid
class Cell {
  ArrayList<Cell> neighbors;
  boolean isMine;
  boolean isFlagged;
  boolean displayNumNeighbors;

  Cell() {
    this.neighbors = new ArrayList<Cell>();
    this.isMine = false;
    this.isFlagged = false;
    this.displayNumNeighbors = false;
  }

  Cell(ArrayList<Cell> neighbors, boolean isMine) {
    this.neighbors = neighbors;
    this.isMine = isMine;
    this.isFlagged = false;
    this.displayNumNeighbors = false;
  }

  Cell(boolean isMine, boolean isFlagged, boolean displayNumNeighbors) {
    this.neighbors = new ArrayList<Cell>();
    this.isMine = isMine;
    this.isFlagged = isFlagged;
    this.displayNumNeighbors = displayNumNeighbors;
  }

  Cell(ArrayList<Cell> neighbors, boolean isMine, boolean isFlagged, boolean displayNumNeighbors) {
    this.neighbors = neighbors;
    this.isMine = isMine;
    this.isFlagged = isFlagged;
    this.displayNumNeighbors = displayNumNeighbors;
  }

  // counts the number of mines neighboring this cell
  int countNeighboringMines() {
    int count = 0;
    for (int i = 0; i < this.neighbors.size(); i++) {
      if (this.neighbors.get(i).isMine) {
        count++;
      }
    }
    return count;
  }
}

class ExamplesAnimation {

  Cell notAMine;
  GameWorld defaultGame;
  Cell mine;
  ArrayList<Cell> neighborsList;
  Cell c;
  Random random;
  GameWorld game;
  ArrayList<Cell> gameNeighbors;
  ArrayList<Cell> gameNeighborsAfterRestart;

  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;
  Cell cell6;
  Cell cell7;
  Cell cell8;
  Cell cell9;

  Cell cell10;
  Cell cell11;
  Cell cell12;
  Cell cell13;
  Cell cell14;
  Cell cell15;
  Cell cell16;
  Cell cell17;
  Cell cell18;
  
  
  // represents initial data conditions
  void initData() {
    this.notAMine = new Cell();
    this.defaultGame = new GameWorld();
    this.mine = new Cell(new ArrayList<Cell>(), true);
    this.neighborsList = new ArrayList<>(Arrays.asList(
        this.notAMine, this.mine, this.mine, this.notAMine, this.mine));
    this.c = new Cell(this.neighborsList, false);
    this.random = new Random(10);
    this.game = new GameWorld(9, 1, 3, this.random);

    this.cell1 = new Cell(false, false, true);
    this.cell2 = new Cell(false, false, true);
    this.cell3 = new Cell(false, false, true);
    this.cell4 = new Cell(true, false, false);
    this.cell5 = new Cell(false, false, false);
    this.cell6 = new Cell(false, false, false);
    this.cell7 = new Cell(false, false, false);
    this.cell8 = new Cell(true, false, false);
    this.cell9 = new Cell(true, false, false);

    this.cell1.neighbors = new ArrayList<>(Arrays.asList(this.cell2));
    this.cell2.neighbors = new ArrayList<>(Arrays.asList(this.cell1, this.cell3));
    this.cell3.neighbors = new ArrayList<>(Arrays.asList(this.cell2, this.cell4));
    this.cell4.neighbors = new ArrayList<>(Arrays.asList(this.cell3, this.cell5));
    this.cell5.neighbors = new ArrayList<>(Arrays.asList(this.cell4, this.cell6));
    this.cell6.neighbors = new ArrayList<>(Arrays.asList(this.cell5, this.cell7));
    this.cell7.neighbors = new ArrayList<>(Arrays.asList(this.cell6, this.cell8));
    this.cell8.neighbors = new ArrayList<>(Arrays.asList(this.cell7, this.cell9));
    this.cell9.neighbors = new ArrayList<>(Arrays.asList(this.cell8));

    this.gameNeighbors = new ArrayList<>(Arrays.asList(
        this.cell1, this.cell2, this.cell3, this.cell4,
        this.cell5, this.cell6, this.cell7, this.cell8, this.cell9));


    this.cell18 = new Cell(new ArrayList<>(), false, false, false);
    this.cell17 = new Cell(new ArrayList<>(Arrays.asList(this.cell18)), false, false, false);
    this.cell16 = new Cell(new ArrayList<>(Arrays.asList(this.cell17)), false, false, false);
    this.cell15 = new Cell(new ArrayList<>(Arrays.asList(this.cell16)), false, false, false);
    this.cell14 = new Cell(new ArrayList<>(Arrays.asList(this.cell15)), false, false, false);
    this.cell13 = new Cell(new ArrayList<>(Arrays.asList(this.cell14)), false, false, false);
    this.cell12 = new Cell(new ArrayList<>(Arrays.asList(this.cell13)), false, false, false);
    this.cell11 = new Cell(new ArrayList<>(Arrays.asList(this.cell12)), false, false, false);
    this.cell10 = new Cell(new ArrayList<>(Arrays.asList(this.cell11)), false, false, false);

    this.cell10.neighbors = new ArrayList<>(Arrays.asList(this.cell11));
    this.cell11.neighbors = new ArrayList<>(Arrays.asList(this.cell10, this.cell12));
    this.cell12.neighbors = new ArrayList<>(Arrays.asList(this.cell11, this.cell13));
    this.cell13.neighbors = new ArrayList<>(Arrays.asList(this.cell12, this.cell14));
    this.cell14.neighbors = new ArrayList<>(Arrays.asList(this.cell13, this.cell15));
    this.cell15.neighbors = new ArrayList<>(Arrays.asList(this.cell14, this.cell16));
    this.cell16.neighbors = new ArrayList<>(Arrays.asList(this.cell15, this.cell17));
    this.cell17.neighbors = new ArrayList<>(Arrays.asList(this.cell16, this.cell18));
    this.cell18.neighbors = new ArrayList<>(Arrays.asList(this.cell17));

    this.gameNeighborsAfterRestart = new ArrayList<>(Arrays.asList(
        this.cell10, this.cell11, this.cell12, this.cell13,
        this.cell14, this.cell15, this.cell16, this.cell17, this.cell18));
  }

  // tests the big bang game animation
  void testBigBang(Tester t) {
    int numCols = 30; 
    int numRows = 16;
    Random rand = new Random();

    GameWorld game = new GameWorld(numCols, numRows, 99, rand);

    int width = numCols * 40;
    int height = numRows * 40 + 180;

    game.bigBang(width, height, 1.0);
  }

  // tests the IllegalArgumentExceptions in the GameWorld constructors
  void testIllegalArgumentException(Tester t) {
    t.checkConstructorException(
        new IllegalArgumentException("Grid dimensions must be positive!"), 
        "GameWorld",
        -1, 0, 15, new Random(7));
    t.checkConstructorException(
        new IllegalArgumentException("Grid must have atleast 9 columns and no more than "
            + "35 columns!"), 
        "GameWorld",
        37, 15, 7, new Random(7));
    t.checkConstructorException(
        new IllegalArgumentException("Grid must have atleast 9 columns and no more than "
            + "35 columns!"), 
        "GameWorld",
        8, 10, 50, new Random(7));
    t.checkConstructorException(
        new IllegalArgumentException("Grid can't have more than 17 rows!"),
        "GameWorld",
        25, 18, 99, new Random(7));
    t.checkConstructorException(
        new IllegalArgumentException("Number of mines must be positive!"),
        "GameWorld",
        30, 16, 0, new Random(7));
    t.checkConstructorException(
        new IllegalArgumentException("Number of mines must be less than total number of "
            + "cells in the grid!"),
        "GameWorld",
        30, 16, 480, new Random(7));
  }
  
  // tests the makeScene method
  void testMakeScene(Tester t) {
    this.initData();
    
    WorldScene gameScreen = this.game.getEmptyScene();

    for (int i = 0; i < 9; i++) {
      int x = i * 40 + 20;
      int y = 20;

      WorldImage cellInner = new RectangleImage(40, 40, OutlineMode.SOLID, Color.LIGHT_GRAY);
      WorldImage cellOutline = new RectangleImage(40, 40, OutlineMode.OUTLINE, Color.BLACK);

      gameScreen.placeImageXY(cellInner, x, y);
      gameScreen.placeImageXY(cellOutline, x, y);
    }

    WorldImage time = new TextImage("Time: " + this.game.clock + "s", 27, Color.RED);
    gameScreen.placeImageXY(time, 180, 60);
    WorldImage numUncoveredMines = new TextImage(
        "# remaining uncovered mines: " + Integer.toString(3), 
        20, Color.BLUE);
    gameScreen.placeImageXY(numUncoveredMines, 180, 140);
    WorldImage numCells = new TextImage(
        "# cells uncovered: " + this.game.cellsUncovered, 
        20, Color.BLUE);
    gameScreen.placeImageXY(numCells, 180, 170);

    int remainingCells = 6;
    WorldImage numRemCells = new TextImage("# remaining cells: " + remainingCells, 
        20, Color.BLUE);
    gameScreen.placeImageXY(numRemCells, 180, 200);

    t.checkExpect(this.game.makeScene(), gameScreen);



    WorldScene gameScreen1 = this.defaultGame.getEmptyScene();

    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 30; j++) {
        int x = j * 40 + 20;
        int y = i * 40 + 20;

        WorldImage cellInner = new RectangleImage(40, 40, OutlineMode.SOLID, Color.LIGHT_GRAY);
        WorldImage cellOutline = new RectangleImage(40, 40, OutlineMode.OUTLINE, Color.BLACK);

        gameScreen1.placeImageXY(cellInner, x, y);
        gameScreen1.placeImageXY(cellOutline, x, y);
      }
    }

    WorldImage time1 = new TextImage("Time: " + this.game.clock + "s", 27, Color.RED);
    gameScreen.placeImageXY(time1, 600, 16 * 40 + 20);
    WorldImage numUncoveredMines1 = new TextImage(
        "# remaining uncovered mines: " + Integer.toString(99), 
        20, Color.BLUE);
    gameScreen.placeImageXY(numUncoveredMines1, 600, 16 * 40 + 100);
    WorldImage numCells1 = new TextImage(
        "# cells uncovered: " + this.game.cellsUncovered, 
        20, Color.BLUE);
    gameScreen.placeImageXY(numCells1, 600, 16 * 40 + 130);

    int remainingCells1 = 6;
    WorldImage numRemCells1 = new TextImage("# remaining cells: " + remainingCells1, 
        20, Color.BLUE);
    gameScreen.placeImageXY(numRemCells1, 600, 16 * 40 + 160);


    t.checkExpect(this.defaultGame.makeScene(), gameScreen1);
  }


  // tests the drawGridLayout method
  void testDrawGridLayout(Tester t) {
    this.initData();

    WorldScene grid = this.game.getEmptyScene();    
    WorldScene completedGrid = this.game.getEmptyScene();

    for (int j = 0; j < 9; j++) {
      int x = j * 40 + 20;
      int y = 20;

      WorldImage cellInner = new RectangleImage(40, 40, OutlineMode.SOLID, Color.LIGHT_GRAY);
      WorldImage cellOutline = new RectangleImage(40, 40, OutlineMode.OUTLINE, Color.BLACK);

      completedGrid.placeImageXY(cellInner, x, y);
      completedGrid.placeImageXY(cellOutline, x, y);
    }

    this.game.drawGridLayout(grid);
    t.checkExpect(grid, completedGrid);



    WorldScene grid1 = this.defaultGame.getEmptyScene();    
    WorldScene completedGrid1 = this.defaultGame.getEmptyScene();

    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 30; j++) {
        int x = j * 40 + 20;
        int y = i * 40 + 20;

        WorldImage cellInner = new RectangleImage(40, 40, OutlineMode.SOLID, Color.LIGHT_GRAY);
        WorldImage cellOutline = new RectangleImage(40, 40, OutlineMode.OUTLINE, Color.BLACK);

        completedGrid1.placeImageXY(cellInner, x, y);
        completedGrid1.placeImageXY(cellOutline, x, y);
      }
    }

    this.game.drawGridLayout(grid1);
    t.checkExpect(grid1, completedGrid1);
  }

  // tests the drawScoreTracker method
  void testDrawScoreTracker(Tester t) {
    this.initData();

    WorldScene panel = this.game.getEmptyScene();    
    WorldScene completedPanel = this.game.getEmptyScene();

    WorldImage time = new TextImage("Time: " + this.game.clock + "s", 27, Color.RED);
    completedPanel.placeImageXY(time, 180, 60);

    WorldImage numUncoveredMines = new TextImage(
        "# remaining uncovered mines: " + Integer.toString(3), 
        20, Color.BLUE);
    completedPanel.placeImageXY(numUncoveredMines, 180, 140);

    WorldImage numCells = new TextImage(
        "# cells uncovered: " + this.game.cellsUncovered, 
        20, Color.BLUE);
    completedPanel.placeImageXY(numCells, 180, 170);

    int remainingCells = 6;
    WorldImage numRemCells = new TextImage(
        "# remaining cells: " + remainingCells, 20, Color.BLUE);

    completedPanel.placeImageXY(numRemCells, 180, 200);

    this.game.drawScoreTracker(panel);
    t.checkExpect(panel, completedPanel);
    
    
    WorldScene panel1 = this.defaultGame.getEmptyScene();    
    WorldScene completedPanel1 = this.defaultGame.getEmptyScene();
    
    WorldImage numUncoveredMines1 = new TextImage(
        "# remaining uncovered mines: " + Integer.toString(
            this.defaultGame.numMines - this.defaultGame.numFlagged), 
        20, Color.BLUE);
    completedPanel1.placeImageXY(numUncoveredMines1, 
        this.defaultGame.numCols * 20, this.defaultGame.numRows * 40 + 100);

    WorldImage numCells1 = new TextImage(
        "# cells uncovered: " + this.defaultGame.cellsUncovered, 
        20, Color.BLUE);
    completedPanel1.placeImageXY(numCells1, 
        this.defaultGame.numCols * 20, this.defaultGame.numRows * 40 + 130);

    int remainingCells1 = this.defaultGame.numRows * this.defaultGame.numCols 
        - (this.defaultGame.numMines + this.defaultGame.cellsUncovered);
    WorldImage numRemCells1 = new TextImage("# remaining cells: " + remainingCells1, 
        20, Color.BLUE);

    completedPanel1.placeImageXY(
        numRemCells1, this.defaultGame.numCols * 20, this.defaultGame.numRows * 40 + 160);
    
    this.defaultGame.drawScoreTracker(panel1);
    t.checkExpect(panel1, completedPanel1);
  }

  // tests the onTick method
  void testOnTick(Tester t) {
    this.initData();

    t.checkExpect(this.defaultGame.clock, 0);
    t.checkExpect(this.defaultGame.winGame, false);
    t.checkExpect(this.defaultGame.loseGame, false);

    this.defaultGame.onTick();

    t.checkExpect(this.defaultGame.clock, 1);
    t.checkExpect(this.defaultGame.winGame, false);
    t.checkExpect(this.defaultGame.loseGame, false);



    this.game.clock = 11;
    this.game.winGame = true;
    t.checkExpect(this.game.clock, 11);
    t.checkExpect(this.game.winGame, true);
    t.checkExpect(this.game.loseGame, false);

    this.game.onTick();

    t.checkExpect(this.game.clock, 11);
    t.checkExpect(this.game.winGame, true);
    t.checkExpect(this.game.loseGame, false);


    this.initData();
    this.game.clock = 25;
    this.game.loseGame = true;
    t.checkExpect(this.game.clock, 25);
    t.checkExpect(this.game.winGame, false);
    t.checkExpect(this.game.loseGame, true);

    this.game.onTick();

    t.checkExpect(this.game.clock, 25);
    t.checkExpect(this.game.winGame, false);
    t.checkExpect(this.game.loseGame, true);
  }

  // tests the countNeighboringMines method
  void testCountNeighboringMines(Tester t) {
    this.initData();

    t.checkExpect(this.notAMine.countNeighboringMines(), 0);
    t.checkExpect(this.defaultGame.gameGrid.get(2).get(7).countNeighboringMines(), 0);
    t.checkExpect(this.game.gameGrid.get(0).get(5).countNeighboringMines(), 0);
    t.checkExpect(this.c.countNeighboringMines(), 3);
  }

  // tests the onKeyEvent method
  void testOnKeyEvent(Tester t) {
    this.initData();

    t.checkExpect(this.game.numRows, 1);
    t.checkExpect(this.game.numCols, 9);
    t.checkExpect(this.game.rand, this.random);
    t.checkExpect(this.game.loseGame, false);

    this.game.onMouseClicked(new Posn(60, 20), "LeftButton");

    this.game.winGame = true;
    this.game.clock = 5;
    this.game.numFlagged = 3;
    this.game.cellsUncovered = 6;

    t.checkExpect(this.game.gameGrid.size(), 1);
    t.checkExpect(this.game.gameGrid.get(0).size(), 9);
    t.checkExpect(this.game.gameGrid.get(0), this.gameNeighbors);

    this.game.onKeyEvent("enter");

    t.checkExpect(this.game.numRows, 1);
    t.checkExpect(this.game.numCols, 9);
    t.checkExpect(this.game.rand, new Random());
    t.checkExpect(this.game.loseGame, false);
    t.checkExpect(this.game.clock, 0);
    t.checkExpect(this.game.numFlagged, 0);
    t.checkExpect(this.game.winGame, false);
    t.checkExpect(this.game.cellsUncovered, 0);

    t.checkExpect(this.game.gameGrid.size(), 1);
    t.checkExpect(this.game.gameGrid.get(0).size(), 9);
    t.checkExpect(this.game.gameGrid.get(0), this.gameNeighborsAfterRestart);


    t.checkExpect(this.defaultGame.numRows, 16);
    t.checkExpect(this.defaultGame.numCols, 30);
    t.checkExpect(this.defaultGame.rand, new Random(7));
    t.checkExpect(this.defaultGame.winGame, false);
    t.checkExpect(this.defaultGame.loseGame, false);

    this.defaultGame.clock = 12;
    this.defaultGame.numFlagged = 5;
    this.defaultGame.cellsUncovered = 40;

    t.checkExpect(this.defaultGame.gameGrid.size(), 16);
    t.checkExpect(this.defaultGame.gameGrid.get(0).size(), 30);

    this.defaultGame.onKeyEvent("enter");

    t.checkExpect(this.defaultGame.numRows, 16);
    t.checkExpect(this.defaultGame.numCols, 30);
    t.checkExpect(this.defaultGame.rand, new Random(7));
    t.checkExpect(this.defaultGame.winGame, false);
    t.checkExpect(this.defaultGame.loseGame, false);

    this.defaultGame.clock = 12;
    this.defaultGame.numFlagged = 5;
    this.defaultGame.cellsUncovered = 40;

    t.checkExpect(this.defaultGame.gameGrid.size(), 16);
    t.checkExpect(this.defaultGame.gameGrid.get(0).size(), 30);


    this.initData();

    t.checkExpect(this.game.numRows, 1);
    t.checkExpect(this.game.numCols, 9);
    t.checkExpect(this.game.rand, this.random);
    t.checkExpect(this.game.winGame, false);

    this.game.onMouseClicked(new Posn(60, 20), "LeftButton");

    this.game.loseGame = true;
    this.game.clock = 5;
    this.game.numFlagged = 1;
    this.game.cellsUncovered = 5;

    t.checkExpect(this.game.gameGrid.size(), 1);
    t.checkExpect(this.game.gameGrid.get(0).size(), 9);
    t.checkExpect(this.game.gameGrid.get(0), this.gameNeighbors);

    this.game.onKeyEvent("enter");

    t.checkExpect(this.game.numRows, 1);
    t.checkExpect(this.game.numCols, 9);
    t.checkExpect(this.game.rand, new Random());
    t.checkExpect(this.game.loseGame, false);
    t.checkExpect(this.game.clock, 0);
    t.checkExpect(this.game.numFlagged, 0);
    t.checkExpect(this.game.winGame, false);
    t.checkExpect(this.game.cellsUncovered, 0);

    t.checkExpect(this.game.gameGrid.size(), 1);
    t.checkExpect(this.game.gameGrid.get(0).size(), 9);
    t.checkExpect(this.game.gameGrid.get(0), this.gameNeighborsAfterRestart);


    this.initData();

    t.checkExpect(this.game.numRows, 1);
    t.checkExpect(this.game.numCols, 9);
    t.checkExpect(this.game.rand, this.random);
    t.checkExpect(this.game.winGame, false);
    t.checkExpect(this.game.loseGame, false);

    this.game.onMouseClicked(new Posn(60, 20), "LeftButton");

    this.game.clock = 5;
    this.game.numFlagged = 1;
    this.game.cellsUncovered = 5;

    t.checkExpect(this.game.gameGrid.size(), 1);
    t.checkExpect(this.game.gameGrid.get(0).size(), 9);
    t.checkExpect(this.game.gameGrid.get(0), this.gameNeighbors);

    this.game.onKeyEvent("enter");

    t.checkExpect(this.game.numRows, 1);
    t.checkExpect(this.game.numCols, 9);
    t.checkExpect(this.game.rand, this.random);
    t.checkExpect(this.game.loseGame, false);
    t.checkExpect(this.game.clock, 5);
    t.checkExpect(this.game.numFlagged, 1);
    t.checkExpect(this.game.winGame, false);
    t.checkExpect(this.game.cellsUncovered, 5);

    t.checkExpect(this.game.gameGrid.size(), 1);
    t.checkExpect(this.game.gameGrid.get(0).size(), 9);
    t.checkExpect(this.game.gameGrid.get(0), this.gameNeighbors);
  }
  
  
  // tests the restartGame method
  void testRestartGame(Tester t) {
    this.initData();
    
    t.checkExpect(this.defaultGame.numRows, 16);
    t.checkExpect(this.defaultGame.numCols, 30);
    t.checkExpect(this.defaultGame.rand, new Random(7));
    t.checkExpect(this.defaultGame.winGame, false);

    this.defaultGame.loseGame = true;
    this.defaultGame.clock = 12;
    this.defaultGame.numFlagged = 5;
    this.defaultGame.cellsUncovered = 40;
    
    t.checkExpect(this.defaultGame.gameGrid.size(), 16);
    t.checkExpect(this.defaultGame.gameGrid.get(0).size(), 30);
    
    this.defaultGame.restartGame();
    
    t.checkExpect(this.defaultGame.numRows, 16);
    t.checkExpect(this.defaultGame.numCols, 30);
    t.checkExpect(this.defaultGame.rand, new Random());
    t.checkExpect(this.defaultGame.loseGame, false);
    t.checkExpect(this.defaultGame.clock, 0);
    t.checkExpect(this.defaultGame.numFlagged, 0);
    t.checkExpect(this.defaultGame.winGame, false);
    t.checkExpect(this.defaultGame.cellsUncovered, 0);

    t.checkExpect(this.defaultGame.gameGrid.size(), 16);
    t.checkExpect(this.defaultGame.gameGrid.get(0).size(), 30);
    
    
    t.checkExpect(this.game.numRows, 1);
    t.checkExpect(this.game.numCols, 9);
    t.checkExpect(this.game.rand, this.random);
    t.checkExpect(this.game.loseGame, false);
    
    this.game.onMouseClicked(new Posn(60, 20), "LeftButton");
    
    this.game.winGame = true;
    this.game.clock = 5;
    this.game.numFlagged = 3;
    this.game.cellsUncovered = 6;

    t.checkExpect(this.game.gameGrid.size(), 1);
    t.checkExpect(this.game.gameGrid.get(0).size(), 9);
    t.checkExpect(this.game.gameGrid.get(0), this.gameNeighbors);

    this.game.restartGame();

    t.checkExpect(this.game.numRows, 1);
    t.checkExpect(this.game.numCols, 9);
    t.checkExpect(this.game.rand, new Random());
    t.checkExpect(this.game.loseGame, false);
    t.checkExpect(this.game.clock, 0);
    t.checkExpect(this.game.numFlagged, 0);
    t.checkExpect(this.game.winGame, false);
    t.checkExpect(this.game.cellsUncovered, 0);

    t.checkExpect(this.game.gameGrid.size(), 1);
    t.checkExpect(this.game.gameGrid.get(0).size(), 9);
    t.checkExpect(this.game.gameGrid.get(0), this.gameNeighborsAfterRestart);
  }
  
  
  // tests the assignMines method
  void testAssignMines(Tester t) {
    this.initData();
        
    t.checkExpect(this.game.gameGrid.get(0).get(0).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(1).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(2).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(3).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(4).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(5).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(6).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(7).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(8).isMine, false);
    
    this.game.assignMines(0, 5);
    
    t.checkExpect(this.game.gameGrid.get(0).get(0).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(1).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(2).isMine, true);
    t.checkExpect(this.game.gameGrid.get(0).get(3).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(4).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(5).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(6).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(7).isMine, true);
    t.checkExpect(this.game.gameGrid.get(0).get(8).isMine, true);
    
    
    this.initData();
    
    t.checkExpect(this.game.gameGrid.get(0).get(0).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(1).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(2).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(3).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(4).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(5).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(6).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(7).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(8).isMine, false);
    
    this.game.assignMines(0, 8);
    
    t.checkExpect(this.game.gameGrid.get(0).get(0).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(1).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(2).isMine, true);
    t.checkExpect(this.game.gameGrid.get(0).get(3).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(4).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(5).isMine, false);
    t.checkExpect(this.game.gameGrid.get(0).get(6).isMine, true);
    t.checkExpect(this.game.gameGrid.get(0).get(7).isMine, true);
    t.checkExpect(this.game.gameGrid.get(0).get(8).isMine, false);
  }
  
  // tests the assignNeighbors method
  void testAssignNeighbors(Tester t) {
    this.initData();
    
    Cell leftMost = this.game.gameGrid.get(0).get(0);
    ArrayList<Cell> leftMostNeighbors = new ArrayList<>(Arrays.asList(
        this.game.gameGrid.get(0).get(1)));
    
    Cell middle = this.game.gameGrid.get(0).get(4);
    ArrayList<Cell> middleNeighbors = new ArrayList<>(Arrays.asList(
        this.game.gameGrid.get(0).get(3), 
        this.game.gameGrid.get(0).get(5)));

    t.checkExpect(leftMost.neighbors.size(), 1);
    t.checkExpect(leftMost.neighbors.containsAll(leftMostNeighbors), true);
    
    t.checkExpect(middle.neighbors.size(), 2);
    t.checkExpect(middle.neighbors.containsAll(middleNeighbors), true);
    
    
    Cell innerCell = this.defaultGame.gameGrid.get(10).get(21);

    ArrayList<Cell> innerNeighbors = new ArrayList<>(Arrays.asList(
        this.defaultGame.gameGrid.get(10).get(22),
        this.defaultGame.gameGrid.get(10).get(20),
        this.defaultGame.gameGrid.get(9).get(21),
        this.defaultGame.gameGrid.get(9).get(22),
        this.defaultGame.gameGrid.get(9).get(20),
        this.defaultGame.gameGrid.get(11).get(21),
        this.defaultGame.gameGrid.get(11).get(22),
        this.defaultGame.gameGrid.get(11).get(20)));
        
    t.checkExpect(innerCell.neighbors.size(), 8);
    t.checkExpect(innerCell.neighbors.containsAll(innerNeighbors), true);
  }
  
  
  // tests the onMouseClicked
  void testOnMouseClicked(Tester t) {
    this.initData();
    
    Cell selectedCell = this.game.gameGrid.get(0).get(1);
    
    t.checkExpect(selectedCell.isFlagged, false);
    t.checkExpect(this.game.numFlagged, 0);

    this.game.onMouseClicked(new Posn(60, 20), "RightButton");
    
    t.checkExpect(selectedCell.isFlagged, true);
    t.checkExpect(this.game.numFlagged, 1);
    
    this.game.onMouseClicked(new Posn(60, 20), "RightButton");
    t.checkExpect(selectedCell.isFlagged, false);
    t.checkExpect(this.game.numFlagged, 0);

    
    t.checkExpect(selectedCell.isMine, false);
    t.checkExpect(this.game.cellsUncovered, 0);
    t.checkExpect(selectedCell.displayNumNeighbors, false);
    t.checkExpect(selectedCell.isFlagged, false);
    this.game.onMouseClicked(new Posn(60, 20), "LeftButton");
    t.checkExpect(selectedCell.displayNumNeighbors, true);
    t.checkExpect(this.game.cellsUncovered, 3);
    t.checkExpect(selectedCell.isFlagged, false);
    
    
    Cell selected2 = this.game.gameGrid.get(0).get(3);
    selected2.isMine = true;
    
    t.checkExpect(selected2.isMine, true);
    this.game.onMouseClicked(new Posn(150, 20), "LeftButton");
    t.checkExpect(selected2.isMine, true); 
    t.checkExpect(this.game.loseGame, true); 
    
    
    this.initData();
    // when mouse position is outside the playing grid
    
    t.checkExpect(this.game.numFlagged, 0);
    t.checkExpect(this.game.cellsUncovered, 0);
    t.checkExpect(this.game.loseGame, false);
    t.checkExpect(this.game.winGame, false);
    t.checkExpect(this.game.clock, 0);
    
    this.game.onMouseClicked(new Posn(50, 100), "RightButton");
    
    t.checkExpect(this.game.numFlagged, 0);
    t.checkExpect(this.game.cellsUncovered, 0);
    t.checkExpect(this.game.loseGame, false);
    t.checkExpect(this.game.winGame, false);
    t.checkExpect(this.game.clock, 0);
  }
  
  
  // tests the floodfill method
  void testFloodfill(Tester t) {
    
    this.initData();
    
    Cell selectedCell = this.defaultGame.gameGrid.get(3).get(27);
    selectedCell.displayNumNeighbors = true;
    
    t.checkExpect(selectedCell.displayNumNeighbors, true);
    t.checkExpect(this.defaultGame.numFlagged, 0);
    t.checkExpect(this.defaultGame.cellsUncovered, 0);
    t.checkExpect(this.defaultGame.loseGame, false);
    t.checkExpect(this.defaultGame.winGame, false);
    t.checkExpect(this.defaultGame.clock, 0);

    this.defaultGame.floodfill(selectedCell);
    
    t.checkExpect(selectedCell.displayNumNeighbors, true);
    t.checkExpect(this.defaultGame.numFlagged, 0);
    t.checkExpect(this.defaultGame.cellsUncovered, 0);
    t.checkExpect(this.defaultGame.loseGame, false);
    t.checkExpect(this.defaultGame.winGame, false);
    t.checkExpect(this.defaultGame.clock, 0);
    
    
    Cell selectedCell1 = this.defaultGame.gameGrid.get(10).get(6);
    selectedCell1.isFlagged = true;
    
    t.checkExpect(selectedCell1.isFlagged, true);
    t.checkExpect(this.defaultGame.numFlagged, 0);
    t.checkExpect(this.defaultGame.cellsUncovered, 0);
    t.checkExpect(this.defaultGame.loseGame, false);
    t.checkExpect(this.defaultGame.winGame, false);
    t.checkExpect(this.defaultGame.clock, 0);

    this.defaultGame.floodfill(selectedCell1);
    
    t.checkExpect(selectedCell1.isFlagged, true);
    t.checkExpect(this.defaultGame.numFlagged, 0);
    t.checkExpect(this.defaultGame.cellsUncovered, 0);
    t.checkExpect(this.defaultGame.loseGame, false);
    t.checkExpect(this.defaultGame.winGame, false);
    t.checkExpect(this.defaultGame.clock, 0);
    
    
    Cell selectedCell2 = this.game.gameGrid.get(0).get(7);
    
    t.checkExpect(selectedCell2.isFlagged, false);
    t.checkExpect(selectedCell2.displayNumNeighbors, false);
    t.checkExpect(selectedCell2.isMine, false);
    t.checkExpect(this.game.numFlagged, 0);
    t.checkExpect(this.game.cellsUncovered, 0);
    t.checkExpect(this.game.loseGame, false);
    t.checkExpect(this.game.winGame, false);
    t.checkExpect(this.game.clock, 0);
    
    this.game.floodfill(selectedCell2);
    
    t.checkExpect(selectedCell2.isFlagged, false);
    t.checkExpect(selectedCell2.displayNumNeighbors, true);
    t.checkExpect(selectedCell2.isMine, false);
    t.checkExpect(this.game.numFlagged, 0);
    t.checkExpect(this.game.cellsUncovered, 9);
    t.checkExpect(this.game.loseGame, false);
    t.checkExpect(this.game.winGame, false);
    t.checkExpect(this.game.clock, 0);


    Cell selectedCell3 = this.defaultGame.gameGrid.get(13).get(28);

    t.checkExpect(selectedCell3.isFlagged, false);
    t.checkExpect(selectedCell3.displayNumNeighbors, false);
    t.checkExpect(selectedCell3.isMine, false);
    t.checkExpect(this.defaultGame.numFlagged, 0);
    t.checkExpect(this.defaultGame.cellsUncovered, 0);
    t.checkExpect(this.defaultGame.loseGame, false);
    t.checkExpect(this.defaultGame.winGame, false);
    t.checkExpect(this.defaultGame.clock, 0);
    
    this.defaultGame.floodfill(selectedCell3);
    
    t.checkExpect(selectedCell3.isFlagged, false);
    t.checkExpect(selectedCell3.displayNumNeighbors, true);
    t.checkExpect(selectedCell3.isMine, false);
    t.checkExpect(this.defaultGame.numFlagged, 0);
    t.checkExpect(this.defaultGame.cellsUncovered, 478);
    t.checkExpect(this.defaultGame.loseGame, false);
    t.checkExpect(this.defaultGame.winGame, false);
    t.checkExpect(this.defaultGame.clock, 0);
  }
  
  // tests the pressLeftButton method
  void testPressLeftButton(Tester t) {
    this.initData();
    
    Cell selectedCell = this.game.gameGrid.get(0).get(1);
    
    t.checkExpect(selectedCell.isMine, false);
    t.checkExpect(this.game.cellsUncovered, 0);
    t.checkExpect(selectedCell.displayNumNeighbors, false);
    t.checkExpect(selectedCell.isFlagged, false);
    this.game.pressLeftButton(0, 1);
    t.checkExpect(selectedCell.displayNumNeighbors, true);
    t.checkExpect(this.game.cellsUncovered, 9);
    t.checkExpect(selectedCell.isFlagged, false);


    Cell selected2 = this.game.gameGrid.get(0).get(3);
    selected2.isMine = true;

    t.checkExpect(selected2.isMine, true);
    this.game.pressLeftButton(0, 3);
    t.checkExpect(selected2.isMine, true); 
    t.checkExpect(this.game.loseGame, true); 
  }
}