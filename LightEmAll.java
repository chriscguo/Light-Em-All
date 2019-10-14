import java.awt.Color;
import java.util.*;
import javalib.impworld.*;
import javalib.worldimages.*;

// NOTE: In this implementation, I only counted rotations as steps, not powerStation movements
// Definition of a true win: Lighting every tile with the number of steps 
// taken <= number of rotations added to the board

// Current board-making algorithm in use is Prim's Algorithm
//*******************************************************************************************
// CLASS AND CONSTRUCTORS


class LightEmAll extends World implements ControlCenter {
  // Number of tiles in width and height
  int width;
  int height;
  // Number of rotations applied in total, used for high-score rotation counts
  // for steps
  int rotations;
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;
  // Random variable for testing
  Random rand;
  // Keeps track of time elapsed!
  int time;
  // Keeps track of rotations done by user!
  int steps;
  // Color wheel to represent gradient
  ArrayList<Color> colorWheel;
  // Is the game over? Used for starting a new puzzle
  boolean gameOver;
  // Win streak for getting steps <= rotations
  int streak;

  // Default constructor taking in tiles in the x and y direction
  public LightEmAll(int width, int height) {
    this.streak = 0;
    this.gameOver = false;
    this.time = 0;
    this.rotations = 0;
    this.steps = 0;
    this.width = width;
    this.height = height;
    this.rand = new Random();
    this.powerRow = 0;
    this.powerCol = 0;
    this.nodes = new ArrayList<GamePiece>();
    // Creates the board
    this.board = this.initBoard();
    // Finds the furthest node from the powerstation
    GamePiece furthest = this.findFurthest(0, 0);
    GamePiece first = this.findFurthest(furthest.col, furthest.row);
    // NOTE: Radius might be slightly off
    this.radius = (this.bfs(furthest, first).size() / 2) + 1;
    this.colorWheel = this.initColorWheel();
    this.rotateTiles();
    this.computeRadius();
  }

  // Takes in a Random for testing
  public LightEmAll(int width, int height, Random rando) {
    // NOTE: I copied over the constructor above instead of doing this(width, height)
    // so the tiles don't get randomly rotated before declaring this.rand = rando
    this.rand = rando;
    this.streak = 0;
    this.gameOver = false;
    this.time = 0;
    this.rotations = 0;
    this.steps = 0;
    this.width = width;
    this.height = height;
    this.powerRow = 0;
    this.powerCol = 0;
    this.nodes = new ArrayList<GamePiece>();
    // Creates the board
    this.board = this.initBoard();
    // Finds the furthest node from the powerstation
    GamePiece furthest = this.findFurthest(0, 0);
    GamePiece first = this.findFurthest(furthest.col, furthest.row);
    // NOTE: Radius might be slightly off
    this.radius = (this.bfs(furthest, first).size() / 2) + 1;
    this.colorWheel = this.initColorWheel();
    this.rotateTiles();
    this.computeRadius();
  }

  //*******************************************************************************************
  // DRAWING AND RENDERING METHODS

  // Draws everything!
  public WorldScene makeScene() {
    // How tall the scoreboard is going to be on the bottom
    int scoreHeight = TILE_SIZE;
    // Blank canvas
    WorldScene result = new WorldScene(this.width * TILE_SIZE,
        ((this.height * TILE_SIZE) + scoreHeight));
    // Dark gray background!
    result.placeImageXY(
        new RectangleImage(this.width * TILE_SIZE, this.height * TILE_SIZE,
            OutlineMode.SOLID, Color.DARK_GRAY),
        this.width * TILE_SIZE / 2, this.height * TILE_SIZE / 2);
    // Scoreboard:
    result.placeImageXY(new RectangleImage(this.width * TILE_SIZE, scoreHeight,
        OutlineMode.SOLID, Color.LIGHT_GRAY),
        this.width * TILE_SIZE / 2,
        (this.height * TILE_SIZE) + (scoreHeight / 2));
    // Draws time:
    int seconds = this.time % 60;
    int minutes = (this.time - seconds) / 60;
    String secText;
    String minText;
    String timeText;

    if (seconds >= 10) {
      secText = new Integer(seconds).toString();
    }
    else {
      secText = "0" + new Integer(seconds).toString();
    }
    if (minutes >= 10) {
      minText = new Integer(minutes).toString();
    }
    else {
      minText = "0" + new Integer(minutes).toString();
    }
    // Places the time
    timeText = minText + ":" + secText;
    WorldImage timeImage = new TextImage(timeText, FONT_SIZE, GRID_COLOR);
    result.placeImageXY(timeImage, this.width * TILE_SIZE / 2 + (this.width * TILE_SIZE / 4),
        (this.height * TILE_SIZE) + (scoreHeight / 2));

    // Adds the current game true winning streak if it exists
    if (this.streak > 0) {
      String streakText = "Win Streak: " + new Integer(this.streak).toString();
      WorldImage streakImage = new TextImage(streakText, FONT_SIZE, Color.green);
      result.placeImageXY(streakImage, this.width * TILE_SIZE / 2,
          this.height * TILE_SIZE + (scoreHeight / 2));
    }

    // Draws steps taken:
    // Rotations: i/n min
    // i = current number of rotations taken
    // n = minimum number of rotations applied from the minimum spanning tree
    String score = "Rotations: " + new Integer(this.steps).toString() + "/"
        + new Integer(this.rotations).toString() + " min";
    WorldImage scoreText = new TextImage(score, FONT_SIZE, GRID_COLOR);
    result.placeImageXY(scoreText, this.width * TILE_SIZE / 4,
        (this.height * TILE_SIZE) + (scoreHeight / 2));

    // Vertical grid lines
    for (int k = (TILE_SIZE + 1); k < this.width * TILE_SIZE; k += TILE_SIZE) {
      result.placeImageXY(
          new RectangleImage(1, this.height * TILE_SIZE,
              OutlineMode.SOLID, GRID_COLOR), k, this.height * TILE_SIZE / 2);
    }
    // Horizontal grid lines
    for (int k = (TILE_SIZE + 1); k < this.height * TILE_SIZE; k += TILE_SIZE) {
      result.placeImageXY(
          new RectangleImage(this.width * TILE_SIZE, 1,
              OutlineMode.SOLID, GRID_COLOR), this.width * TILE_SIZE / 2, k);
    }
    // Draws tiles!
    result = this.drawTiles(result);

    // Game over screen!
    if (this.gameOver) {
      // Definition of a true win defined above class declaration
      boolean trueWin = this.steps <= this.rotations;
      String winText;
      // Special text for true win
      if (trueWin) {
        winText = "You've Lit Em All!";
      }
      else {
        // Resets the true win streak
        winText = "You Win!";
        this.streak = 0;
      }
      // Draws the game winning screen
      WorldImage winImage = new TextImage(winText, FONT_SIZE * 2, Color.green);
      result.placeImageXY(winImage, this.width * TILE_SIZE / 2, this.height * TILE_SIZE / 2);
      // Prompt for letting the user start a new game+
      WorldImage startAgain = new TextImage("Press Enter for New Game+",
          FONT_SIZE * 2, Color.green);
      result.placeImageXY(startAgain, this.width * TILE_SIZE / 2,
          this.height * TILE_SIZE / 2 + (FONT_SIZE * 3));
    }
    return result;
  }

  // Draws all the tiles!
  public WorldScene drawTiles(WorldScene base) {
    // For indices of the nodes
    int count = 0;
    // Loops through and draws and places each tile
    // X loop
    for (int k = TILE_SIZE / 2; k < this.width * TILE_SIZE; k += TILE_SIZE) {
      // Y loop
      for (int i = TILE_SIZE / 2; i < this.height * TILE_SIZE; i += TILE_SIZE) {
        // Grabs the tile
        GamePiece tile = this.nodes.get(count++);
        base.placeImageXY(tile.draw(), k, i);
      }
    }
    return base;
  }

  //*******************************************************************************************
  // INITIALIZING FIELDS

  // Makes the Color Wheel to represent a gradient of colors
  public ArrayList<Color> initColorWheel() {
    ArrayList<Color> result = new ArrayList<Color>();
    // How much the RGB values decrease by
    int decrement = Math.max(this.width, this.height);
    // Decrements the RGB values of the base color (gold)
    int k = 0;
    // GOAL: I want the size of the color wheel to equal the radius size, so the 
    // color gradient scales depending on the size of the board
    while (result.size() <= this.radius) {
      Color current;
      // Prevents G from going below 0
      if (k <= 200) {
        // Base color: Gold (255, 200, 0)
        // Slowly turns brownish red as size of ArrayList increases
        current = new Color(255 - k, 200 - k, 0);
      }
      else {
        // If the radius is massive, color is brownish red
        current = new Color(50, 0, 0);
      }
      // Adds the color to the color wheel and decreases k
      result.add(current);
      k += decrement;
    }
    return result;
  }

  // Initializes all GamePieces and nodes
  // Creates a 2D ArrayList of random GamePieces and adds them all
  // to this.nodes
  public ArrayList<ArrayList<GamePiece>> initBoard() {
    // This is going to be the final board
    ArrayList<ArrayList<GamePiece>> result = new ArrayList<ArrayList<GamePiece>>();

    // Creates a bunch of new tiles, adds it to this.nodes
    // No wires have been created yet
    for (int k = 0; k < this.width; k++) {
      ArrayList<GamePiece> temp = new ArrayList<GamePiece>();
      for (int i = 0; i < this.height; i++) {
        GamePiece tile = new GamePiece(k, i);
        this.nodes.add(tile);
        temp.add(tile);
        // If powerstation:
        if (k == powerCol && i == powerRow) {
          tile.powerStation = true;
        }
      }
      result.add(temp);
    }


    //************************************************************************************
    // NOTE: This version is for manual generation
    // NOT IN USE
    /*
   for (int k = 0; k < nodes.size(); k++) {
     nodes.get(k).top = true;
     nodes.get(k).bottom = true;
   }

   for (int k = 0; k < this.width; k++) {
     GamePiece tile = result.get(k).get(this.height / 2);
     tile.right = true;
     tile.left = true;
   }
     */
    //************************************************************************************
    // NOTE: This version is currently for fractal generation
    // NOT IN USE
    // Extra tiles on the bottom or right
    /*
   int extraX = this.width % 4;
   int extraY = this.height % 4;

   int xBound = this.width - extraX;
   int yBound = this.height - extraY;

   // This part is for constructing fractals for perfect squares
   for (int k = 0; k < this.width; k++) {
     for (int i = 0; i < this.height; i++) {
       GamePiece tile = result.get(k).get(i);
       if ((i + 1) % 4 == 0 && ((k + 3) % 4 == 0 || (k + 3) % 4 == 1)) {
         tile.top = true;
         tile.left = true;
         tile.right = true;
       }
       else if (i % 2 == 1 && k % 2 == 1) {
         tile.left = true;
         tile.top = true;
       }
       else if (i % 2 == 1 && k % 2 == 0) {
         tile.right = true;
         tile.top = true;
       }
       else if (i % 2 == 0) {
         tile.bottom = true;
       }

       if (((k + 1) % 4 == 0 || k % 4 == 0) && (i - 1) % 4 == 0) {
         tile.bottom = true;
       }
       else if (((k + 1) % 4 == 0 || k % 4 == 0) && (i - 2) % 4 == 0) {
         tile.top = true;
       }

     }
   }


   //***********************************************************
   // Connecting quadrants between the groups of 4 fractals
   int count = 4;
   // Horizontal
   while (count < width) {
     for (int k = count / 2 - 1; k < xBound - 1; k += count) {
       for (int i = count - 1; i < yBound; i += count) {
         GamePiece tile = result.get(k).get(i);
         tile.right = true;
       }
     }

     for (int k = count / 2; k < xBound; k += count) {
       for (int i = count - 1; i < yBound; i += count) {
         GamePiece tile = result.get(k).get(i);
         tile.left = true;
       }
     }
     // Vertical
     for (int k = count - 1; k < xBound; k += count) {
       for (int i = count / 2 - 1; i < yBound - 1; i += count) {
         GamePiece tile = result.get(k).get(i);
         if (i != this.width - 1) {
           tile.bottom = true;
         }
       }
     }

     for (int k = count - 1; k < xBound; k += count) {
       for (int i = count / 2; i < yBound; i += count) {
         GamePiece tile = result.get(k).get(i);
         tile.top = true;
       }
     }
     for (int k = count; k < xBound - 1; k += count) {
       for (int i = count / 2 - 1; i < yBound - 1; i += count) {
         GamePiece tile = result.get(k).get(i);
         if (i != this.width - 1) {
           tile.bottom = true;
         }
       }
     }

     for (int k = count; k < xBound; k += count) {
       for (int i = count / 2; i < yBound; i += count) {
         GamePiece tile = result.get(k).get(i);
         tile.top = true;
       }
     }
     count *= 2;
   }

   //***********************************************************

   // Connects the outside edges and cleans them up
   // Left edge
   for (int k = 0; k < this.height; k ++) {
     GamePiece current = result.get(0).get(k);
     if (k == 0) {
       current.bottom = true;
     }
     else if (k == this.height - 1) {
       current.top = true;
       current.right = true;
     }
     else {
       current.bottom = true;
       current.top = true;
     }
   }

   // Right edge
   for (int k = 0; k < this.height; k ++) {
     GamePiece current = result.get(this.width - 1).get(k);
     if (k == 0) {
       current.bottom = true;
       current.right = false;
     }
     else if (k == this.height - 1) {
       current.top = true;
       current.left = true;
       current.right = false;
     }
     else {
       current.bottom = true;
       current.top = true;
       current.right = false;
     }
   }

   // Bottom edge
   for (int k = 1; k < this.width - 1; k++) {
     GamePiece current = result.get(k).get(this.height - 1);
     current.right = true;
     current.left = true;
     current.bottom = false;
   }
   // Cleans up the bottom
   result.get(0).get(height - 1).bottom = false;
   result.get(width - 1).get(height - 1).bottom = false;
   // Edge case
   if (this.height == 1) {
     this.nodes.get(0).right = true;
     this.nodes.get(this.nodes.size() - 1).left = true;
   }
     */

    //************************************************************************************
    // NOTE: This version is for Kruskal's Algorithm
    // NOT IN USE
    // Using Prim's Algorithm, but can comment that out and uncomment this part
    /*

    // List of Edges in minimum spanning tree
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    // Union-find data structure to keep track of what's connected
    // Key: Each node's index in this.nodes
    // Value: The representative aka the root of the tree it's in
    HashMap<Integer, Integer> representatives = new HashMap<Integer, Integer>();
    // Add all edges in the graph, sorted by edge weights
    ArrayList<Edge> worklist = this.initEdges(result);
    // Initialize every node's representative to itself
    for (int k = 0; k < this.nodes.size(); k++) {
      representatives.put(k, k);
    }

    // While not complete minimum spanning tree:
    while (worklist.size() > 0) {
      // Pick the next cheapest edge of the graph:
      Edge cheapest = worklist.get(0);
      // If same representatives or cyclic
      if (find(representatives, cheapest.fromNode)
          .equals(find(representatives, cheapest.toNode))) {
        // discard this edge, they're already connected
        worklist.remove(0);
      }
      // Else: they're not in a cycle
      else {
        // Record this edge in edgesInTree
        edgesInTree.add(cheapest);
        // Sets one rep to the other rep
        this.union(representatives,
            this.find(representatives, cheapest.fromNode),
            this.find(representatives, cheapest.toNode));
      }
    }
    // Records minimum spanning tree and creates wires depending on it
    this.mst = edgesInTree;
    this.initWires(edgesInTree);
     */
    //************************************************************************************
    // NOTE: This version is for Prim's algorithm

    // List of Edges in minimum spanning tree
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    // GamePieces in the MST
    ArrayList<GamePiece> inMST = new ArrayList<GamePiece>();
    // GamePieces not in the MST
    ArrayList<GamePiece> notMST = new ArrayList<GamePiece>();
    // Adds all nodes into notMST
    for (GamePiece n : this.nodes) {
      notMST.add(n);
    }
    // Add all edges in the graph, sorted by edge weights
    ArrayList<Edge> worklist = this.initEdges(result);
    // Random node to start with
    GamePiece startingVertex = this.nodes.get(this.rand.nextInt(this.nodes.size()));
    notMST.remove(startingVertex);
    inMST.add(startingVertex);

    while (notMST.size() > 0) {
      // NOTE: Worklist is already sorted
      int counter = 0;
      // Loops through the worklist of sorted Edges to find smallest Edge with
      // the vertices in MST inside it
      while (counter < worklist.size()) {
        Edge current = worklist.get(counter);
        boolean found = false;
        // Placeholder GamePiece
        GamePiece removeable = new GamePiece(-1, -1);
        // If the Edge leads to the created tree
        if (notMST.contains(current.fromNode) && inMST.contains(current.toNode)) {
          removeable = current.fromNode;
          found = true;
        }
        else if (notMST.contains(current.toNode) && inMST.contains(current.fromNode)) {
          removeable = current.toNode;
          found = true;
        }
        // If we found a GamePiece to connect
        if (found) {
          inMST.add(removeable);
          notMST.remove(removeable);
          worklist.remove(current);
          edgesInTree.add(current);
          break;
        }
        counter++;
      }
    }
    // Records minimum spanning tree and creates wires depending on it
    this.mst = edgesInTree;
    this.initWires(edgesInTree);

    //************************************************************************************
    return result;
  }

  // Initializes all the Edges in the graph with a random weight and sorts it
  // Returns the ArrayList of the sorted Edges
  public ArrayList<Edge> initEdges(ArrayList<ArrayList<GamePiece>> pieces) {
    ArrayList<Edge> result = new ArrayList<Edge>();

    // Creates a bunch of edges with different random weights
    // Ensures 1 edge between every node and its neighbors
    // Reasoning: If I start at the origin (top left tile), and give it an Edge
    // going to the right and down, it'll let me create a 1 connection between every
    // node with no overlaps.
    for (int k = 0; k < this.width; k++) {
      for (int i = 0; i < this.height; i++) {
        GamePiece current = pieces.get(k).get(i);
        int randomNum = this.rand.nextInt(this.nodes.size());
        // Goes down
        if ((i + 1) < this.height) {
          result.add(new Edge(current, pieces.get(k).get(i + 1), randomNum));
        }

        // Goes right
        if ((k + 1) < this.width) {
          result.add(new Edge(current, pieces.get(k + 1).get(i), randomNum));
        }
      }
    }
    // Sorts the ArrayList of Edges with a Comparator
    result.sort(new SortEdgeWeight());
    return result;
  }

  // Goes through the list of Edges and sets the wires of each GamePiece accordingly
  // EFFECT: Connects the fromNode and toNode of each Edge with wires
  public void initWires(ArrayList<Edge> edges) {
    // Basically, the way I implemented it the edges only go from top left to bottom right,
    // so comparing the row of each GamePiece will tell me if it is to the right or below
    for (int k = 0; k < edges.size(); k++) {
      Edge current = edges.get(k);
      GamePiece to = current.toNode;
      GamePiece from = current.fromNode;

      // If they aren't in the same row
      // they're above/below each other
      if (from.row != to.row) {
        from.bottom = true;
        to.top = true;
      }
      // They're next to each other
      else {
        from.right = true;
        to.left = true;
      }
    }
  }

  //*******************************************************************************************
  //KEY AND MOUSE HANDLERS


  // Left click to rotate a tile!
  // EFFECT: Rotates a tile by 90 degrees when left clicked, adds 1 to steps
  public void onMouseClicked(Posn pos, String butt) {
    if (!this.gameOver) {
      int x = pos.x;
      int y = pos.y;
      // If the Posn is out of bounds
      if (x < 0 || x >= this.width * TILE_SIZE || y < 0 || y >= this.height * TILE_SIZE) {
        return;
      }
      else {
        int xIndex = x / TILE_SIZE;
        int yIndex = y / TILE_SIZE;
        if (butt.equals("LeftButton")) {
          // Rotates the tile
          this.board.get(xIndex).get(yIndex).rotate();
          // Updates the board of what's lit up and not
          this.computeRadius();
          // Adds 1 to steps
          this.steps++;
          // Is there a winner?
          this.checkWinner();
        }
        // If it's any other button:
        else {
          return;
        }
      }
    }
  }

  // Moves the powerstation
  // EFFECT: Moves the powerstation up, down, left, right, depending on
  // user input
  public void onKeyEvent(String key) {
    if (!this.gameOver) {
      GamePiece current =
          this.board.get(powerCol).get(powerRow);

      // Moves powerstation right!
      if (key.equals("right")) {
        // If the right tile exists
        if ((this.powerCol + 1) < this.width && current.right &&
            this.board.get(powerCol + 1).get(powerRow).left) {
          current.powerStation = false;
          this.board.get(++powerCol).get(powerRow).powerStation = true;
        }
      }
      // Moves it left!
      else if (key.equals("left")) {
        // If the left tile exists
        if ((this.powerCol - 1) >= 0 && current.left &&
            this.board.get(powerCol - 1).get(powerRow).right) {
          current.powerStation = false;
          this.board.get(--powerCol).get(powerRow).powerStation = true;
        }
      }
      // Moves it up!
      else if (key.equals("up")) {
        // If the tile to the top exists
        if ((this.powerRow - 1) >= 0 && current.top &&
            this.board.get(powerCol).get(powerRow - 1).bottom) {
          current.powerStation = false;
          this.board.get(powerCol).get(--powerRow).powerStation = true;
        }
      }
      // Moves it down!
      else if (key.equals("down")) {
        // If the below tile exists
        if ((this.powerRow + 1) < this.height && current.bottom &&
            this.board.get(powerCol).get(powerRow + 1).top) {
          current.powerStation = false;
          this.board.get(powerCol).get(++powerRow).powerStation = true;
        }
      }
      // Lights everything up if it's within the radius
      this.computeRadius();
      // Is this a winning screen>
      this.checkWinner();
    }
    // Game is over!
    else {
      // User input "enter" to start another game
      if (key.equals("enter")) {
        int stepsTaken = this.steps;
        int minRotations = this.rotations;
        // Adds 1 to the true win streak if it's a true win
        if (stepsTaken <= minRotations) {
          this.streak++;
        }
        // Starts a new game:
        // NOTE: Steps taken carry over, along with powerStation position
        this.gameOver = false;
        this.time = 0;
        this.rotations = minRotations;
        this.steps = stepsTaken;
        this.nodes = new ArrayList<GamePiece>();
        this.board = this.initBoard();
        GamePiece furthest = this.findFurthest(0, 0);
        GamePiece first = this.findFurthest(furthest.col, furthest.row);
        this.radius = (this.bfs(furthest, first).size() / 2) + 1;
        this.colorWheel = this.initColorWheel();
        this.rotateTiles();
        this.computeRadius();
      }
    }
  }


  //*******************************************************************************************
  // HELPERS FOR KRUSKAL'S ALGORITHM


  // Finds the representative of the given node
  public GamePiece find(HashMap<Integer, Integer> reps, GamePiece target) {
    int targetName = this.nodes.indexOf(target);

    // If the representative is itself
    boolean sameNode = reps.get(targetName).equals(targetName);

    if (sameNode) {
      // Returns itself
      return target;
    }
    else {
      // Gets the index of its parent
      int index = reps.get(targetName);
      GamePiece temp = this.nodes.get(index);
      // Recursively finds the representative
      return this.find(reps, temp);
    }
  }

  // Sets one representative to the other
  // EFFECT: At the key corresponding to 1 GamePiece, the value is set to the
  // associated representative of the second GamePiece
  public void union(HashMap<Integer, Integer> reps, GamePiece one, GamePiece two) {
    Integer index1 = this.nodes.indexOf(one);
    Integer index2 = this.nodes.indexOf(two);
    reps.put(index2, index1);
  }
  //*******************************************************************************************
  // SEARCHING ALGORITHMS AND METHODS


  // Returns the furthest GamePiece from the given node
  public GamePiece findFurthest(int col, int row) {
    GamePiece furthest = this.board.get(col).get(row);
    int farthest = 0;
    // Powerstation manipulation to work with powered()
    // because powered() finds the minimum distance from the powerstation
    if (!furthest.powerStation) {
      furthest.powerStation = true;
      this.board.get(powerCol).get(powerRow).powerStation = false;
    }

    // Iterates through all the nodes and returns the one the furthest away
    for (int k = 0; k < this.nodes.size(); k++) {
      int current = this.powered(this.nodes.get(k), new ArrayList<GamePiece>(), 0);
      if (current > farthest) {
        farthest = current;
        furthest = this.nodes.get(k);
      }
    }
    // Resets the powerstation to its original position
    this.board.get(col).get(row).powerStation = false;
    this.board.get(powerCol).get(powerRow).powerStation = true;
    return furthest;

  }

  // Computes the radius in which the power is spread
  // EFFECT: lights up tiles if they are within the radius
  // and also changes the color of the wires depending on radius
  public void computeRadius() {
    for (int k = 0; k < this.nodes.size(); k++) {
      GamePiece current = this.nodes.get(k);
      // If it's within range of the radius of the powerstation
      if (current.powerStation
          || this.powered(current, new ArrayList<GamePiece>(), 0) <= this.radius) {
        current.lit = true;
        // Changes the color of the wire to produce a gradient
        current.powerColor = this.colorWheel.get(
            this.powered(current, new ArrayList<GamePiece>(), 0));
      }
      else {
        // Turns the tile off
        current.lit = false;
      }
    }
  }

  // Checks if the tile is powered and returns the distance from power station
  // ACC: Keeps track of the total distance and what tiles have been processed
  public int powered(GamePiece next, ArrayList<GamePiece> seen, int sum) {
    int col = next.col;
    int row = next.row;
    // Base case: Tile is the powerstation so we return the sum so far
    if (next.powerStation) {
      return sum;
    }
    else {
      // We've seen this tile so we add it to the seen
      seen.add(next);
      // To ensure that if the neighbors aren't there, these won't
      // be chosen as the minimum
      int reallyHighNumber = this.height * this.width + 1;
      int top = reallyHighNumber;
      int bot = reallyHighNumber;
      int right = reallyHighNumber;
      int left = reallyHighNumber;

      // Right
      if (next.right && (col + 1) < this.width) {
        GamePiece next2 = this.board.get(col + 1).get(row);
        // if the adjacent one is connected via left
        if (next2.left && !seen.contains(next2)) {
          right = this.powered(next2, seen, sum + 1);
        }
      }

      // Left
      if (next.left && (col - 1) >= 0) {
        GamePiece next2 = this.board.get(col - 1).get(row);
        // if the adjacent one is connected via right
        if (next2.right && !seen.contains(next2)) {
          left = this.powered(next2, seen, sum + 1);
        }
      }

      // Top
      if (next.top && (row - 1) >= 0) {
        GamePiece next2 = this.board.get(col).get(row - 1);
        // if the adjacent one is connected via bottom
        if (next2.bottom && !seen.contains(next2)) {
          top = this.powered(next2, seen, sum + 1);
        }
      }

      // Bottom
      if (next.bottom && (row + 1) < this.height) {
        GamePiece next2 = this.board.get(col).get(row + 1);
        // if the adjacent one is connected via top
        if (next2.top && !seen.contains(next2)) {
          bot = this.powered(next2, seen, sum + 1);
        }
      }

      // Selects the minimum distance from the powerstation
      return Math.min(left, Math.min(right, Math.min(top, bot)));
    }
  }

  // Breadth first search!
  // Returns an ArrayList of the nodes traveled to get to the destination
  public ArrayList<GamePiece> bfs(GamePiece from, GamePiece to) {
    ArrayList<GamePiece> seen = new ArrayList<GamePiece>();
    // If they're the same tile
    if (from.equals(to)) {
      return seen;
    }
    ArrayList<GamePiece> worklist = new ArrayList<GamePiece>();
    worklist.add(from);
    // Keeps track of where we came from
    ArrayList<Edge> cameFromEdge = new ArrayList<Edge>();

    while (!worklist.isEmpty()) {
      GamePiece next = worklist.remove(0);

      // If it's the goal
      if (next.equals(to)) {
        return reconstructFromEdge(cameFromEdge, next, from);
      }
      else if (seen.contains(next)) {
        // Does nothing because already covered
      }
      else {
        // Adds each outgoing node, if it's connected, to the worklist and
        // the list of edges to trace
        // For all outgoing nodes!
        int row = next.row;
        int col = next.col;

        // Right
        if (next.right && (col + 1) < this.width) {
          GamePiece next2 = this.board.get(col + 1).get(row);
          // if the adjacent one is connected via left
          if (next2.left && !seen.contains(next2)) {
            worklist.add(next2);
            cameFromEdge.add(new Edge(next, next2, 1));
          }
        }
        // Left
        if (next.left && (col - 1) >= 0) {
          GamePiece next2 = this.board.get(col - 1).get(row);
          // if the adjacent one is connected via right
          if (next2.right && !seen.contains(next2)) {
            worklist.add(next2);
            cameFromEdge.add(new Edge(next, next2, 1));
          }
        }
        // Top
        if (next.top && (row - 1) >= 0) {
          GamePiece next2 = this.board.get(col).get(row - 1);
          // if the adjacent one is connected via bottom
          if (next2.bottom && !seen.contains(next2)) {
            worklist.add(next2);
            cameFromEdge.add(new Edge(next, next2, 1));
          }
        }
        // Bottom
        if (next.bottom && (row + 1) < this.height) {
          GamePiece next2 = this.board.get(col).get(row + 1);
          // if the adjacent one is connected via top
          if (next2.top && !seen.contains(next2)) {
            worklist.add(next2);
            cameFromEdge.add(new Edge(next, next2, 1));
          }
        }
      }
      seen.add(next);
    }
    return new ArrayList<GamePiece>();
  }

  // Returns the vertices that are traced back to the source node from the given
  // GUARANTEE: Source is connected to end
  public ArrayList<GamePiece> reconstructFromEdge(ArrayList<Edge> edges,
      GamePiece end, GamePiece source) {
    // Resulting list of GamePieces
    ArrayList<GamePiece> result = new ArrayList<GamePiece>();
    GamePiece temp = end;
    result.add(end);
    result.add(source);
    // Base case: If they're the same tile
    if (end.equals(source)) {
      return result;
    }
    // While the current tile isn't the target:
    while (!temp.equals(source)) {
      int count = edges.size() - 1;
      // Loops through the list of edges
      while (count >= 0) {
        // If the edge's destination is the current, traces backwards
        if (edges.get(count).toNode.equals(temp)) {
          // Current is the previous node
          temp = edges.get(count).fromNode;
          if (!result.contains(temp)) {
            // Adds the node if it isn't already in the result
            result.add(temp);
          }
          // Removes the edge from the list of edges
          edges.remove(count);
        }
        // Decreases count to go through edges backwards and
        // prevent infinite looping
        count--;
      }
    }
    return result;
  }

  //**********************************************************************************************
  // RANDOM TILE ROTATIONS

  // Rotates all tiles by a random number
  // EFFECT: Adds to this.rotations the sum of all rotations applied
  public void rotateTiles() {
    int sum = 0;
    // Loops through all the GamePieces
    for (int k = 0; k < this.nodes.size(); k++) {
      // Random n number of rotations
      GamePiece current = this.nodes.get(k);

      // Rotates the tile n times if it can be rotated (not a +)
      // If it's a +, doesn't add to the number of rotations
      boolean plus = current.bottom && current.top && current.right && current.left;

      if (!plus) {
        // Number from [0, 3]
        int rotationsNum = this.rand.nextInt(4);
        for (int i = 0; i < rotationsNum; i++) {
          current.rotate();
        }
        sum += rotationsNum;
      }
      // If the tile is a --, so it can only be rotated once uniquely
      if (((current.top && current.bottom) || (current.right && current.left))
          && !plus) {
        // Number from [0, 1]
        int rotationsNum = this.rand.nextInt(2);
        if (rotationsNum == 1) {
          current.rotate();
        }
        sum += rotationsNum;
      }

    }
    this.rotations += sum;
  }

  //*******************************************************************************************
  // UTILITIES FOR BIG BANG AND CHECKING IF GAME IS OVER

  //Checks if all the tiles are lit!
  //EFFECT: Ends world if all are lit, does nothing otherwise
  public void checkWinner() {
    int count  = 0;
    for (int k = 0; k < this.nodes.size(); k++) {
      if (this.nodes.get(k).lit) {
        count++;
      }
    }
    if (count == this.nodes.size()) {
      this.gameOver = true;
    }
  }

  // Overrides bigBang(width, height)
  // EFFECT: Starts the game!
  public void bigBang() {
    super.bigBang(this.width * TILE_SIZE, this.height * TILE_SIZE + TILE_SIZE, 1.0);
  }

  // Keeps track of time
  // EFFECT: Increments this.time by 1
  public void onTick() {
    if (!this.gameOver) {
      this.time++;
    }
  }
  //*******************************************************************************************
}








