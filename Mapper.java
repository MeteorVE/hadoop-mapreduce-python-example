import java.util.*;
import java.time.LocalDateTime; // Import the LocalDateTime class
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Mapper {
  public static void main(String[] args){
  	
  	//String s = "64.242.88.10 - - [07/Mar/2004:16:10:02 -0800] \"GET /mailman/listinfo/hsdivision HTTP/1.1\" 200 6291";

  	Scanner scanner = new Scanner(System.in);
  	while (scanner.hasNext()) {

	  	String t = scanner.nextLine().split("- - \\[")[1].split(" -")[0];
  		SimpleDateFormat accesslogDateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss", new Locale("es","ES"));
  		SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-mm-dd HH:00:00.000\t1");
  		// %Y-%m-%d %H:00:00.000

  		Date result = null;
  		String result_s = null;
  		try {
  				result = accesslogDateFormat.parse(t);
  				result_s = outputDateFormat.format(result);
  				System.out.println(result_s);

		}catch(ParseException e) {
				e.printStackTrace();
		}// End of Try Catch

	}// End of while

  }//End of main
}//End of FirstJavaProgram Class