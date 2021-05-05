import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Performance {

	public static void main(String[] args) {
		System.out.println("Hello VM-Machine");
		
		try {
			
			//TODO: check if Linux --- else exit
			
			ProcessBuilder builder = new ProcessBuilder();
			StringBuilder out = new StringBuilder();
			String text;
		
			//test command
			builder.command("bash", "-c", "lscpu");
		
			//start process
			Process process = builder.start();
			
			//read terminal
			BufferedReader read = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			while((text = read.readLine()) != null){
				out.append(text + "\n");
				System.out.println(text);
			}
			
			//finish process
			//TODO: else cases
			int exit = process.waitFor();
			System.out.println("\nFinished : " + exit);
			
			
		} catch (Exception e){
			e.printStackTrace();
		}
		//TODO: more Exceptions
	}

}
