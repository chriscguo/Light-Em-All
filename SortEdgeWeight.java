import java.util.Comparator;

// 
class SortEdgeWeight implements Comparator<Edge> {

  // Compares two Edges by weight!
  public int compare(Edge edge1, Edge edge2) {
    Integer edge1Weight = edge1.weight;
    Integer edge2Weight = edge2.weight;
    
    return edge1Weight.compareTo(edge2Weight);
  }

}
