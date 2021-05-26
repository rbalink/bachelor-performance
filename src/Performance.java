import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Performance {

	public static void main(String[] args) {
		final String url = "jdbc:postgresql://35.234.107.124:5432/";
		final String user = "postgres";
		final String password = "geheim";
		
		try {
			
			if(!(System.getProperty("os.name").equals("Linux"))){
				System.err.println("The operating system for this program should be Linux!");
			}
			
			
			ProcessBuilder builder = new ProcessBuilder();
			StringBuilder out = new StringBuilder();
			String text;
			
			//list cpu info
			builder.command("bash", "-c", "lscpu");
		
			//start process
			Process process = builder.start();
			
			//read terminal
			BufferedReader read = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			while((text = read.readLine()) != null){
				out.append(text + "\n");
				//System.out.println(text);
			}
			
			//finish process
			//TODO: else cases
			int exit = process.waitFor();
			//System.out.println("\nFinished : " + exit);
			//System.out.println(out.toString());
			
			
		} catch (Exception e){
			e.printStackTrace();
		}
		//TODO: more Exceptions
		
		//TODO: RAM, GPU, more... from bash Terminal
		
		//TODO: Daten verarbeiten
		
		//TODO: Verbinden mit Online-Datenbank
		
		// Connecting to external PostgreSQL

		try {
			System.out.println("Loading the Driver");
			Class.forName("org.postgresql.Driver");
			System.out.println("Connecting to external PostgreSQL Server");
			Connection con = DriverManager.getConnection(url,user,password);
			System.out.println("Successful connection");
			
			Statement statement = con.createStatement();
			statement.execute("INSERT INTO datatbl (cpu_model_name, cpu_family)"+ "VALUES ('JavaTestAgain',22);");
			
			System.out.println("Inserting successful");
		} catch ( Exception e){
			e.printStackTrace();
		}
	}

}
