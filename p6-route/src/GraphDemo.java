import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*; 

/**
 * Demonstrates the calculation of shortest paths in the US Highway
 * network, showing the functionality of GraphProcessor and using
 * Visualize
 * To do: Add your name(s) as authors
 */
public class GraphDemo {
    public static void main(String[] args) throws Exception {
        
        //INITALIZE
        
        GraphProcessor obj = new GraphProcessor();
        FileInputStream input = new FileInputStream("data/usa.graph");
        obj.initialize(input);

        //SCANNER

        System.out.println("Where are you coming from?");
        System.out.println("Enter a city and state abbreviation (e.g., Durham, NC):");

        Scanner scanFrom = new Scanner(System.in);
        String cityFrom = scanFrom.nextLine();

        String cityFromSaved = cityFrom.split(",")[0];

        System.out.println("Where are you going to?");
        System.out.println("Enter a city and state abbreviation (e.g., Durham, NC):");

        Scanner scanTo = new Scanner(System.in);
        String cityTo = scanTo.nextLine();

        String cityToSaved = cityTo.split(",")[0];
        //System.out.println("Line 42:" +cityToSaved);
        
        //Start timing

        long startTime = System.nanoTime();

        //SEARCH

        Point cityStartCoord = searchCSV(cityFromSaved);
        Point cityEndCoord = searchCSV(cityToSaved);
        //System.out.println("Line 52: " +cityEndCoord);

        Point startNear = obj.nearestPoint(cityStartCoord);
        Point endNear = obj.nearestPoint(cityEndCoord);

        List<Point> outRoute = obj.route(startNear, endNear);

        Double outRouteDist = obj.routeDistance(outRoute);
        
        //End timing

        long elapseNanos = System.nanoTime() - startTime;

        //Printing

        System.out.println("Nearest point to " + cityFrom + " is: " + cityStartCoord.toString() + ".");
        System.out.println("Nearest point to " + cityTo + " is: " + cityEndCoord.toString() + ".");
        System.out.println("Route between " + cityStartCoord.toString() + "and " + cityEndCoord.toString() + "is " + outRouteDist + " miles.");
        System.out.println("Total time to get nearest points, route, and get distance: " + elapseNanos*0.000001 + "ms." );

        Visualize objVis = new Visualize("data/usa.vis", "images/usa.png");

        //objVis.drawPoint(cityStartCoord);
        //objVis.drawPoint(cityEndCoord);

        objVis.drawRoute(outRoute);
        
        

    }
    //PULL DATA FROM .CSV
    public static Point searchCSV(String cityFromSaved) throws IOException{
        String splitBy = ",";
        BufferedReader br = new BufferedReader(new FileReader("data/uscities.csv"));
        String line;
        Double cityLat;
        Double cityLon;

        while((line = br.readLine()) != null){
            String[] values = line.split(splitBy);
            //System.out.println(values[0]);
            for (int i = 0; i < values.length; i++) {
                //System.out.println(values[i]);
                if(values[i].equals(cityFromSaved)){
                    //System.out.println("HIT: " + values[i].toString());
                   cityLat = Double.parseDouble(values[i+2]);
                   cityLon = Double.parseDouble(values[i+3]);
                   Point startCity = new Point(cityLat, cityLon);
                   //System.out.println("IN HELPER: "+ startCity.toString());
                   return startCity;
                }
            }
            
        }
        return null;

    }
    


}