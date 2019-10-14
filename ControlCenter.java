import java.awt.Color;

// Represents all constants!
interface ControlCenter {
  // Tile size (square)
  public final int TILE_SIZE = 80;
  
  // Font size
  public final int FONT_SIZE = TILE_SIZE / 4;
  
  
  // Colors:
  public final Color WIRE_COLOR = Color.gray;
  public final Color POWERPLANT_COLOR = Color.blue;
  public final Color POWER_COLOR = Color.yellow;
  
  public final Color GRID_COLOR = Color.black;
}
