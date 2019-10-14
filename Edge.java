// An edge of the minimum spanning tree
// TODO: Part 3
class Edge implements ControlCenter {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;
  
  public Edge(GamePiece from, GamePiece to, int weight) {
    this.fromNode = from;
    this.toNode = to;
    this.weight = weight;
  }
  
  
}
