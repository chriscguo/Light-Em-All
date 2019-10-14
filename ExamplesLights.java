import java.awt.Color;
import java.util.*;

import javalib.worldimages.*;
import tester.Tester;
// NOTE: Width and Height of the gameboard is in the ControlCenter interface
// Examples and testing

class ExamplesLights implements ControlCenter {
  LightEmAll game0;
  LightEmAll game1;
  LightEmAll game2;
  LightEmAll game3;
  LightEmAll game4;
  LightEmAll game5;

  GamePiece t1;
  GamePiece t2;
  GamePiece t3;
  GamePiece t4;

  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;

  // Initializes examples
  public void initData() {
    Random rand = new Random(2);
    game0 = new LightEmAll(8, 8);
    game1 = new LightEmAll(8, 8, rand);
    game2 = new LightEmAll(8, 8, rand);
    game3 = new LightEmAll(8, 8, rand);
    game4 = new LightEmAll(8, 8, rand);
    game5 = new LightEmAll(4, 4, rand);
    t1 = new GamePiece(2, 3);
    t2 = new GamePiece(0, 0);
    t3 = new GamePiece(3, 4);
    t4 = new GamePiece(8, 2);
    e1 = new Edge(t1, t2, 2);
    e2 = new Edge(t2, t3, 5);
    e3 = new Edge(t3, t4, 0);
    e4 = new Edge(t4, t2, 12);
  }

  // Tests makeScene() and drawTiles(WorldScene) visually
  public void testMakeScene(Tester t) {
    this.initData();
    game0.bigBang();
  }
  
  // Tests game over and restart stuff
  public void testNewGame(Tester t) {
    this.initData();
    ArrayList<GamePiece> temp = game1.nodes;
    game1.rotations = 100000;
    game1.time = 10000;
    game1.steps = 12;
    game1.gameOver = true;
    game1.onKeyEvent("enter");
    t.checkExpect(game1.nodes.equals(temp), false);
    t.checkExpect(game1.rotations, 100106);
    t.checkExpect(game1.steps, 12);
    t.checkExpect(game1.time, 0);
    t.checkExpect(game1.streak, 1);
    t.checkExpect(game1.gameOver, false);
    game1.rotations = 12;
    game1.time = 10000;
    game1.steps = 12000;
    game1.gameOver = true;
    game1.onKeyEvent("enter");
    t.checkExpect(game1.rotations, 102);
    t.checkExpect(game1.steps, 12000);
    t.checkExpect(game1.time, 0);
  }

  // Tests initBoard()
  public void testInitBoard(Tester t) {
    this.initData();
    t.checkExpect(game1.board.size(), 8);
    t.checkExpect(game1.board.get(0).size(), 8);
    t.checkExpect(game1.board.get(7).size(), 8);
    t.checkExpect(game1.nodes.size(), 8 * 8);
  }

  // Tests onMouseClicked(Posn, String)
  public void testOnMouseClicked(Tester t) {
    this.initData();
    game1.onMouseClicked(new Posn(10200020,2030400), "LeftButton");
    t.checkExpect(game1, game1);
    GamePiece temp = game1.nodes.get(0);
    temp.rotate();
    game1.onMouseClicked(new Posn(10,10), "LeftButton");
    t.checkExpect(game1.nodes.get(0), temp);
    temp.rotate();
    game1.onMouseClicked(new Posn(10,10), "LeftButton");
    t.checkExpect(game1.nodes.get(0), temp);
    temp.rotate();
    game1.onMouseClicked(new Posn(10,10), "LeftButton");
    t.checkExpect(game1.nodes.get(0), temp);
    temp.rotate();
    game1.onMouseClicked(new Posn(10,10), "LeftButton");
    t.checkExpect(game1.nodes.get(0), temp);
    game1.onMouseClicked(new Posn(10,10), "RightButton");
    t.checkExpect(game1.nodes.get(0), temp);
    game1.onMouseClicked(new Posn(10,10), "UnknownButton");
    t.checkExpect(game1.nodes.get(0), temp);
    t.checkExpect(game1.steps, 4);
  }

  // Tests checkWinner()
  public void testCheckWinner(Tester t) {
    this.initData();
    t.checkExpect(game1.gameOver, false);
    for (GamePiece n : game1.nodes) {
      n.lit = true;
    }
    game1.checkWinner();
    t.checkExpect(game1.gameOver, true);
  }

  // Tests onKeyEvent(String)
  public void testOnKeyEvent(Tester t) {
    this.initData();
    game1.onKeyEvent("right");
    t.checkExpect(game1.powerCol, 1);
    t.checkExpect(game1.powerRow, 0);
    game1.onKeyEvent("left");
    t.checkExpect(game1.powerCol, 0);
    t.checkExpect(game1.powerRow, 0);
    game1.onKeyEvent("up");
    t.checkExpect(game1.powerCol, 0);
    t.checkExpect(game1.powerRow, 0);
    game1.onKeyEvent("down");
    t.checkExpect(game1.powerCol, 0);
    t.checkExpect(game1.powerRow, 0);
    game1.onKeyEvent("right");
    t.checkExpect(game1.powerCol, 1);
    t.checkExpect(game1.powerRow, 0);
    game1.onKeyEvent("up");
    t.checkExpect(game1.powerCol, 1);
    t.checkExpect(game1.powerRow, 0);
    game1.onKeyEvent("up");
    t.checkExpect(game1.powerCol, 1);
    t.checkExpect(game1.powerRow, 0);
    game1.onKeyEvent("down");
    game1.onKeyEvent("left");
    game1.onKeyEvent("down");
    game1.onKeyEvent("down");
    game1.onKeyEvent("left");
    t.checkExpect(game1.powerCol, 0);
    t.checkExpect(game1.powerRow, 0);
  }
  
  // Tests onTick()
  public void testOnTick(Tester t) {
    this.initData();
    t.checkExpect(game1.time, 0);
    game1.onTick();
    t.checkExpect(game1.time, 1);
    game1.onTick();
    t.checkExpect(game1.time, 2);
    game1.onTick();
    t.checkExpect(game1.time, 3);
    game1.onTick();
    t.checkExpect(game1.time, 4);
    game1.onTick();
    t.checkExpect(game1.time, 5);
    game1.onTick();
    t.checkExpect(game1.time, 6);
    game1.gameOver = true;
    game1.onTick();
    t.checkExpect(game1.time, 6);
    game1.onTick();
    t.checkExpect(game1.time, 6);
    game1.onTick();
    t.checkExpect(game1.time, 6);
  }

  // Tests computeRadius()
  public void testComputeRadius(Tester t) {
    this.initData();
    game1.board.get(game1.powerCol).get(game1.powerRow).powerStation = false;
    game1.powerCol = 0;
    game1.powerRow = 0;
    game1.nodes.get(0).powerStation = true;
    game1.computeRadius();
    t.checkExpect(game1.board.get(0).get(0).lit, true);
    t.checkExpect(game1.nodes.get(game1.nodes.size() - 1).lit, false);
    t.checkExpect(game1.nodes.get(1).lit, false);
    t.checkExpect(game1.nodes.get(2).lit, false);
    t.checkExpect(game1.nodes.get(8).lit, true);
    t.checkExpect(game1.nodes.get(63).lit, false);
  }

  // Tests powered(GamePiece, ArrayList<GamePiece>, int)
  public void testPowered(Tester t) {
    this.initData();
    t.checkExpect(game1.powered(game1.nodes.get(0), new ArrayList<GamePiece>(), 0), 0);
    t.checkExpect(game1.powered(game1.nodes.get(12), new ArrayList<GamePiece>(), 0), 65);
    t.checkExpect(game1.powered(game1.nodes.get(8), new ArrayList<GamePiece>(), 0), 1);
    t.checkExpect(game1.powered(game1.nodes.get(63), new ArrayList<GamePiece>(), 0), 65);
    t.checkExpect(game1.powered(game1.nodes.get(63), new ArrayList<GamePiece>(), 22), 65);
  }

  // Tests draw()
  public void testDraw(Tester t) {
    WorldImage result = new EmptyImage();
    result = new OverlayImage(new RectangleImage(TILE_SIZE / 8, TILE_SIZE / 8,
        OutlineMode.SOLID, WIRE_COLOR), result);
    this.initData();
    t.checkExpect(t1.draw(), result);
    t1.powerStation = true;
    WorldImage triangle1 = new EquilateralTriangleImage(TILE_SIZE / 3, 
        OutlineMode.SOLID, Color.cyan);
    triangle1 = new RotateImage(triangle1, 180);
    WorldImage triangle3 = new EquilateralTriangleImage(TILE_SIZE / 5, 
        OutlineMode.SOLID, GRID_COLOR);
    triangle3 = new RotateImage(triangle3, 180);
    WorldImage twoTriangles = new OverlayImage(
        new EquilateralTriangleImage(TILE_SIZE / 3, OutlineMode.SOLID, POWERPLANT_COLOR),
        triangle1);
    WorldImage threeTriangles = new OverlayImage(triangle3, twoTriangles);
    result = new OverlayImage(threeTriangles, result);
    t.checkExpect(t1.draw(), result);
    t1.powerStation = false;
    t1.right = true;
    result = new OverlayImage(new RectangleImage(TILE_SIZE / 2, TILE_SIZE / 8,
        OutlineMode.SOLID, WIRE_COLOR).movePinhole(TILE_SIZE / -4, 0), 
        new OverlayImage(new RectangleImage(TILE_SIZE / 8, TILE_SIZE / 8,
            OutlineMode.SOLID, WIRE_COLOR), new EmptyImage()));
    t.checkExpect(t1.draw(), result);
    t1.left = true;
    result = new OverlayImage(new RectangleImage(TILE_SIZE / 2, TILE_SIZE / 8,
        OutlineMode.SOLID, WIRE_COLOR).movePinhole(TILE_SIZE / 4, 0), 
        result);
    t.checkExpect(t1.draw(), result);

  }

  // Tests rotate()
  public void testRotate(Tester t) {
    this.initData();
    t1.rotate();
    t.checkExpect(t1, t1);
    t1.right = true;
    t1.rotate();
    t.checkExpect(t1.right, false);
    t.checkExpect(t1.bottom, true);
    t.checkExpect(t1.left, false);
    t.checkExpect(t1.top, false);
    t1.rotate();
    t.checkExpect(t1.left, true);
    t.checkExpect(t1.bottom, false);
    t.checkExpect(t1.right, false);
    t.checkExpect(t1.top, false);
    t1.rotate();
    t.checkExpect(t1.left, false);
    t.checkExpect(t1.bottom, false);
    t.checkExpect(t1.right, false);
    t.checkExpect(t1.top, true);
    t1.rotate();
    t.checkExpect(t1.right, true);
    t.checkExpect(t1.bottom, false);
    t.checkExpect(t1.left, false);
    t.checkExpect(t1.top, false);
  }

  // Tests radius
  public void testRadius(Tester t) {
    this.initData();
    t.checkExpect(game1.radius, 10);
    t.checkExpect(game2.radius, 12);
  }

  //Tests reconstructFromEdge(ArrayList<Edge>,GamePiece, GamePiece)
  public void testReconstructFromEdge(Tester t) {
    this.initData();
    ArrayList<Edge> edges1 = new ArrayList<Edge>(Arrays.asList(e1, e2, e3, e4));
    t.checkExpect(game1.reconstructFromEdge(edges1, t4, t1).size(), 4);
    edges1 = new ArrayList<Edge>(Arrays.asList(e1, e2, e3));
    t.checkExpect(game1.reconstructFromEdge(edges1, t4, t1).size(), 4);
    edges1 = new ArrayList<Edge>(Arrays.asList(e1, e2));
    t.checkExpect(game1.reconstructFromEdge(edges1, t3, t1).size(), 3);
    edges1 = new ArrayList<Edge>(Arrays.asList(e1, e2, e3, e4));
    t.checkExpect(game1.reconstructFromEdge(edges1, t3, t1).size(), 3);
  }
  
  // Tests bfs(GamePiece, GamePiece)
  public void testBFS(Tester t) {
    this.initData();
    t.checkExpect(game1.bfs(game1.nodes.get(2), game1.nodes.get(2)).size(), 0);
    t.checkExpect(game1.bfs(game1.nodes.get(0), 
        game1.nodes.get(game1.nodes.size() - 1)).size(), 0);
    t.checkExpect(game1.bfs(game1.nodes.get(8), game1.nodes.get(2)).size(), 0);
  }
  
  // Tests find(HashMap<Integer, Integer>, GamePiece)
  public void testFind(Tester t) {
    this.initData();
    HashMap<Integer, Integer> testMap = new HashMap<Integer, Integer>();
    ArrayList<GamePiece> testList = new ArrayList<GamePiece>();
    testList.add(t1);
    testList.add(t2);
    testList.add(t3);
    testList.add(t4);
    testMap.put(0, 0);
    testMap.put(1, 0);
    testMap.put(2, 1);
    testMap.put(3, 3);
    game1.nodes = testList;
    t.checkExpect(game1.find(testMap, t1), t1);
    t.checkExpect(game1.find(testMap, t2), t1);
    t.checkExpect(game1.find(testMap, t3), t1);
    t.checkExpect(game1.find(testMap, t4), t4);
  }
  
  // Tests union(HashMap<Integer, Integer>, GamePiece, GamePiece)
  public void testUnion(Tester t) {
    this.initData();
    HashMap<Integer, Integer> testMap = new HashMap<Integer, Integer>();
    ArrayList<GamePiece> testList = new ArrayList<GamePiece>();
    testList.add(t1);
    testList.add(t2);
    testList.add(t3);
    testList.add(t4);
    testMap.put(0, 0);
    testMap.put(1, 0);
    testMap.put(2, 1);
    testMap.put(3, 3);
    game1.nodes = testList;
    game1.union(testMap, t1, t1);
    game1.union(testMap, t4, t1);
    game1.union(testMap, t1, t4);
    game1.union(testMap, t2, t4);
    game1.union(testMap, t3, t1);
    t.checkExpect(testMap.get(0), 2);
    t.checkExpect(testMap.get(1), 0);
    t.checkExpect(testMap.get(2), 1);
    t.checkExpect(testMap.get(3), 1);
  }
  
  // Tests initWires(ArrayList<Edge>)
  public void testInitWires(Tester t) {
    this.initData();
    ArrayList<Edge> edges = new ArrayList<Edge>(Arrays.asList(e1, e2, e3, e4));
    game1.initWires(edges);
    t.checkExpect(t1.right, false);
    t.checkExpect(t1.left, false);
    t.checkExpect(t1.top, false);
    t.checkExpect(t1.right, false);
    t.checkExpect(t2.right, false);
    t.checkExpect(t2.left, false);
    t.checkExpect(t2.top, true);
    t.checkExpect(t2.right, false);
    t.checkExpect(t3.right, false);
    t.checkExpect(t3.left, false);
    t.checkExpect(t3.top, true);
    t.checkExpect(t3.right, false);
    t.checkExpect(t4.right, false);
    t.checkExpect(t4.left, false);
    t.checkExpect(t4.top, true);
    t.checkExpect(t4.right, false);
  }
  
  // Tests initEdges(ArrayList<ArrayList<GamePiece>>)
  public void testInitEdges(Tester t) {
    this.initData();
    t.checkExpect(game5.initEdges(game5.board).size(), 24);
    t.checkExpect(game5.initEdges(game4.board).size(), 24);
    t.checkExpect(game4.initEdges(game4.board).size(), 112);
    ArrayList<Edge> temp = game1.initEdges(game1.board);
    temp.sort(new SortEdgeWeight());
    t.checkExpect(temp.get(0).weight < temp.get(temp.size() - 1).weight, true);
  }
  
  // Tests the comparator SortEdgeWeight
  public void testSortEdgeWeight(Tester t) {
    this.initData();
    ArrayList<Edge> edges1 = new ArrayList<Edge>(Arrays.asList(e1, e2, e3, e4));
    ArrayList<Edge> edges2 = new ArrayList<Edge>(Arrays.asList(e3, e1, e2, e4));
    edges1.sort(new SortEdgeWeight());
    t.checkExpect(edges1, edges2);
    edges1 = new ArrayList<Edge>(Arrays.asList(e1, e1, e1, e4));
    edges1.sort(new SortEdgeWeight());
    t.checkExpect(edges1, edges1);
  }
  
  // Tests findFurthest(int, int)
  public void testFindFurthest(Tester t) {
    this.initData();
    t.checkExpect(game1.findFurthest(7, 7), game1.nodes.get(0));
    t.checkExpect(game1.findFurthest(0, 0), game1.nodes.get(1));
    t.checkExpect(game1.findFurthest(3, 2), game1.nodes.get(0));
  }
  
  // Tests rotateTiles()
  public void testRotateTiles(Tester t) {
    this.initData();
    int tempRotations = game1.rotations;
    game1.rotateTiles();
    t.checkExpect(tempRotations == game1.rotations, false);
  }
}
