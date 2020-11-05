import java.util.*;

public class Reducer {
  public static void main(String[] args){
  	
  	//String s = "64.242.88.10 - - [07/Mar/2004:16:10:02 -0800] \"GET /mailman/listinfo/hsdivision HTTP/1.1\" 200 6291";

    Map<String, Integer> map = new HashMap<String, Integer>();

  	Scanner scanner = new Scanner(System.in);
  	while (scanner.hasNext()) {

	  	String data[] = scanner.nextLine().split("\t");

      // System.out.println("debug: " +  data[0] + ":" + data[1]);

      if( map.get(data[0]) != null ){
        map.put(data[0], map.get(data[0]) + 1);  
      }else{
        map.put(data[0], 1);
      }
	}// End of while

  for (String key : map.keySet()) {
    // use the key here
    System.out.println(key+ ":" + map.get(key));

  }

  }//End of main
}//End of FirstJavaProgram Class