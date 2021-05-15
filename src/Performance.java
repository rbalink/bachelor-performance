import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Performance {

	public static void main(String[] args) {
		System.out.println("Hello VM-Machine");
		
		try {
			
			//TODO: check if Linux --- else exit
			
			ProcessBuilder builder = new ProcessBuilder();
			StringBuilder out = new StringBuilder();
			String text;
			
			//add google credentials
			String currentPath = "export GOOGLE_APPLICATION_CREDENTIALS=\""+System.getProperty("user.dir")+"/majestic-layout-311618-323b9d7633bf.json\"";
			System.out.println(currentPath);
			builder.command("bash", "-c", currentPath);
			System.out.println("Credentials eingetragen");
			
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
		
		//TODO: Daten in externer Datenbank speichern
		
		String url = "jdbc:postgresql:///benchmarkdb?cloudSqlInstance=majestic-layout-311618:europe-west3:benchmarkdb&socketFactory=com.google.cloud.sql.postgres.SocketFactory&user=postgres&password=DancingQueen666";
		
		try {
			System.out.println("Connect to Driver");
			Class.forName("org.postgresql.Driver");
			System.out.println("Connecting to PostgreSQL CloudSQL");
			Connection con = DriverManager.getConnection(url);
			System.out.println("Successful connection");
			Statement statement = con.createStatement();
			statement.execute("INSERT INTO cpu (cpuname)"+ "VALUES ('Performance');");
			System.out.println("Inserting successful");
		} catch ( Exception e){
			e.printStackTrace();
		}
	}

}
