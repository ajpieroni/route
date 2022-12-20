import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ThreadFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.management.MemoryManagerMXBean;

import java.util.*; 

/**
 * Models a weighted graph of latitude-longitude points
 * and supports various distance and routing operations.
 * To do: Add your name(s) as additional authors
 * @author Brandon Fain
 * @author Alex Pieroni
 *
 */
public class GraphProcessor {

    //instance variables
    private Integer numVert;
    private Integer numEdge;

    private TreeMap<Point, HashSet<Point>> theGraph = new TreeMap<>();

    
    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * @param file a FileInputStream of the .graph file
     * @throws Exception if file not found or error reading
     */
    public void initialize(FileInputStream file) throws Exception {
        
        reader(file);
      
        }

    private void reader(FileInputStream file) throws FileNotFoundException {
        ArrayList<Point> nodes = new ArrayList<>();
        
            Scanner reader = new Scanner(file);
            String[] numVE = reader.nextLine().split(" ");
            numVert = Integer.parseInt(numVE[0]);
            numEdge = Integer.parseInt(numVE[1]);
    
            for(int i = 0; i < numVert; i++){ //10 times for simple
                String[] vertex = reader.nextLine().split(" ");
                
                String nodeName = vertex[0];
                Double nodeLat = Double.parseDouble(vertex[1]);
                Double nodeLon = Double.parseDouble(vertex[2]);

                Point node = new Point(nodeLat, nodeLon);
                nodes.add(node);
                theGraph.put(node, new HashSet<>());
                
            }

            for(int i = 0; i < numEdge; i++){
                String[] edges = reader.nextLine().split(" ");
                if(edges.length > 2){
                    String edgeName = edges[2];
                }
                Integer uIndex = Integer.parseInt(edges[0]);
                Integer vIndex = Integer.parseInt(edges[1]); 

                Point u = nodes.get(uIndex);
                Point v = nodes.get(vIndex);

                theGraph.get(u).add(v);
                theGraph.get(v).add(u);
            }

            reader.close();
    }
        
    /**
     * Searches for the point in the graph that is closest in
     * straight-line distance to the parameter point p
     * @param p A point, not necessarily in the graph
     * @return The closest point in the graph to p
     */
    public Point nearestPoint(Point p) { //passed test

        //returns vertex in the graph that is closest to P

        double lowestDist = 100000000;
        Point lowestDistVert = null;

        //loop through points 
        for (Point vert : theGraph.keySet()) {
            if(vert.distance(p)< lowestDist){
                lowestDist = vert.distance(p);
                lowestDistVert = vert;
            }
        }

        return lowestDistVert;
    }


    /**
     * Calculates the total distance along the route, summing
     * the distance between the first and the second Points, 
     * the second and the third, ..., the second to last and
     * the last. Distance returned in miles.
     * @param start Beginning point. May or may not be in the graph.
     * @param end Destination point May or may not be in the graph.
     * @return The distance to get from start to end
     */
    public double routeDistance(List<Point> route) {
        // TODO Implement routeDistance
        //calculate total distance along the path

        double distRoute = 0.0;
        if(route.size() < 1){
            return 0.0;
        }

        for (int i = 0; i < route.size()-1; i++) {
            distRoute += route.get(i).distance(route.get(i+1));
        }

        return distRoute;
    }
    

    /**
     * Checks if input points are part of a connected component
     * in the graph, that is, can one get from one to the other
     * only traversing edges in the graph
     * @param p1 one point
     * @param p2 another point
     * @return true if p2 is reachable from p1 (and vice versa)
     */
    public boolean connected(Point p1, Point p2) {
        //BFS for O(N+M) when using adjacency list and to more easily adapt for Dijkstra's in the next step 
        if (!theGraph.containsKey(p1) || !theGraph.containsKey(p2)){ //base case: if p1 or p2 are not themselves points in the graph 
            return false; 
        }
        Queue<Point> toExplore = new LinkedList<>(); //queue of points to explore 
        //use theGraph for the adjacency list 
        Set<Point> visited = new HashSet<>(); //initialize visited set 
        Map<Point, Point> previous = new HashMap<>(); //initialize map for previous points 
        Point current = p1;  //start from p1
        visited.add(current); //add current to visited set 
        toExplore.add(current); //add current to queue 
        while (!toExplore.isEmpty()){ //go through all the points
            current = toExplore.remove(); 
            for (Point neighbor: theGraph.get(current)){ //get the neighbors
                if (!visited.contains(neighbor)){ 
                    previous.put(neighbor, current);
                    visited.add(neighbor);
                    toExplore.add(neighbor);
                }
                if (visited.contains(p2)){ //we've already found a path to p2 
                    return true; 
                }
            }
        }
        return false;
    }


    /**
     * Returns the shortest path, traversing the graph, that begins at start
     * and terminates at end, including start and end as the first and last
     * points in the returned list. If there is no such route, either because
     * start is not connected to end or because start equals end, throws an
     * exception.
     * @param start Beginning point.
     * @param end Destination point.
     * @return The shortest path [start, ..., end].
     * @throws InvalidAlgorithmParameterException if there is no such route, 
     * either because start is not connected to end or because start equals end.
     */
    public List<Point> route(Point start, Point end) throws InvalidAlgorithmParameterException {
        if (connected(start, end)==false){
            throw new InvalidAlgorithmParameterException("No path between start and end"); //throw out the exception if the points dont exist or smth 
        }
        Map<Point, Double> distance = new HashMap<>(); //initialize hashmap for keeping track of distance 
        Comparator<Point> comp = (a, b) -> Double.compare(distance.get(a), distance.get(b));//in order of shortest distance to start 
        PriorityQueue<Point> toExplore = new PriorityQueue<>(comp); //initialize explore priority queue with comparator (sorted by distance)
        Map<Point, Point> previous = new HashMap<>(); //initialize previous hashmap 
        //^initialiations 
        Point current = start; 
        distance.put(current, 0.0);
        toExplore.add(current); 
        //add start point information 
        while (!toExplore.isEmpty()){  //go through all of the points 
            current=toExplore.remove(); 
            for (Point neighbor: theGraph.get(current)){ //go through all of the neighbors 
                double weight = current.distance(neighbor); //get the weight 
                if (!distance.containsKey(neighbor) || distance.get(neighbor)>distance.get(current)+ weight){ //if no path found yet or found a shorter path to neighbor by going through curent 
                    distance.put(neighbor, distance.get(current)+weight);
                    previous.put(neighbor, current); //record the new shortest path to neighbor
                    toExplore.add(neighbor); //add neighbor to explore later
                }
            
            }
        }

        //for return, implement an arraylist that includes checker (aka the last destination)
        
        List<Point> returnList = new ArrayList(previous.keySet().size());
        Point checker = end;
        returnList.add(end);

        //we need to make sure both start and end are there; 
        //while checker != start, iterate node in previous, 
        //add next to return list, and iterate checker

        Point node;
        while(checker != start){
            node = previous.get(checker);
            returnList.add(0, node);
            checker = node;
        }
        return returnList;
    }

    
}
