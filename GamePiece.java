
import java.awt.Color;
import javalib.worldimages.*;

// A node on the gameboard
class GamePiece implements ControlCenter {
  //in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  // If power reaches this tile
  boolean lit;
  
  // Power color!
  Color powerColor;

  public GamePiece(int col, int row) {
    this.row = row;
    this.col = col;
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.powerStation = false;
    this.lit = false;
    powerColor = POWER_COLOR;
  }

  // Draws the tile!
  public WorldImage draw() {
    int offset1 = TILE_SIZE / 8;
    int offset2 = TILE_SIZE / 2;
    int pin = TILE_SIZE / 4;
    
    WorldImage result = new EmptyImage();
    
    Color wireColor = WIRE_COLOR;
    if (this.lit) {
      wireColor = this.powerColor;
    }
    
    result = new OverlayImage(new RectangleImage(offset1, offset1,
        OutlineMode.SOLID, wireColor), result);
    
    // Top wire
    if (top) {
      WorldImage wire = new RectangleImage(offset1, offset2,
          OutlineMode.SOLID, wireColor).movePinhole(0, pin);
      result = new OverlayImage(wire, result);
    }
    // Bottom wire
    if (bottom) {
      WorldImage wire = new RectangleImage(offset1, offset2,
          OutlineMode.SOLID, wireColor).movePinhole(0, -pin);
      result = new OverlayImage(wire, result);
    }
    // Right wire
    if (right) {
      WorldImage wire = new RectangleImage(offset2, offset1,
          OutlineMode.SOLID, wireColor).movePinhole(-pin, 0);
      result = new OverlayImage(wire, result);
    }
    // Left wire
    if (left) {
      WorldImage wire = new RectangleImage(offset2, offset1,
          OutlineMode.SOLID, wireColor).movePinhole(pin, 0);
      result = new OverlayImage(wire, result);
    }
    // If the powerstation exists on this tile
    if (powerStation) {
      WorldImage triangle = 
          new EquilateralTriangleImage(TILE_SIZE / 3, OutlineMode.SOLID, POWERPLANT_COLOR);
      WorldImage triangle2 = 
          new EquilateralTriangleImage(TILE_SIZE / 3, OutlineMode.SOLID, Color.cyan);
      triangle2 = new RotateImage(triangle2, 180);
      WorldImage triangle3 = 
          new EquilateralTriangleImage(TILE_SIZE / 5, OutlineMode.SOLID, GRID_COLOR);
      triangle3 = new RotateImage(triangle3, 180);
      WorldImage twoTriangles = new OverlayImage(triangle, triangle2);
      WorldImage threeTriangles = new OverlayImage(triangle3, twoTriangles);
      result = new OverlayImage(threeTriangles, result);
    }

    return result;
  }
  
  // Rotates the tile by 90 degrees
  public void rotate() {
    // Boolean to prevent right from triggering bottom conditional
    boolean temp = false;
    
    if (this.right) {
      this.right = false;
      temp = true;
    }
    if (this.top) {
      this.top = false;
      this.right = true;
    }
    if (this.left) {
      this.left = false;
      this.top = true;
    }
    if (this.bottom) {
      this.bottom = false;
      this.left = true;
    }
    if (temp) {
      this.bottom = true;
    }
    
  }
  
}
