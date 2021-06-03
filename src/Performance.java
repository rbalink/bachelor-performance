import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;

public class Performance {

	public static void main(String[] args) {
		final String url = "jdbc:postgresql://35.234.107.124:5432/";
		final String user = "postgres";
		final String password = "geheim";
		HashMap<String, String> lscpu = new HashMap<>();
		
		try {
			
			if(!(System.getProperty("os.name").equals("Linux"))){
				System.err.println("The operating system for this program should be Linux!");
			}
			
			
			ProcessBuilder builder = new ProcessBuilder();
			String text;
			
			//list cpu info
			builder.command("bash", "-c", "lscpu");
		
			//start process
			Process process = builder.start();
			
			//read terminal
			BufferedReader read = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			while((text = read.readLine()) != null){
				String[] parts = text.split(":", 2);
				parts[1] = parts[1].trim();
				lscpu.put(parts[0], parts[1]);
			}
			int exit = process.waitFor();
			
			//TODO: what if terminal language is english?
			String modelname = lscpu.get("Modellname");
			String model = lscpu.get("Modell");
			int stepping = Integer.parseInt(lscpu.get("Stepping"));
			float cpumhz = Float.parseFloat(lscpu.get("CPU MHz"));
			int cpufamily = Integer.parseInt(lscpu.get("Prozessorfamilie"));
			String l1dcache = "";
			String l1icache = "";
			String l2cache = "";
			String l3cache = "";
			
			//printout hashmap
			//lscpu.entrySet().forEach(entry -> {System.out.println(entry.getKey() + " "+ entry.getValue());
			//});
		
			//TODO: Connection to Online-CPU-Database (cpu-world.com)
		
			// Connection to remote PostgreSQL database
			System.out.println("Loading the Driver");
			Class.forName("org.postgresql.Driver");
			System.out.println("Connecting to external PostgreSQL Server");
			Connection con = DriverManager.getConnection(url,user,password);
			System.out.println("Successful connection");
			
			Statement statement = con.createStatement();
			statement.execute("INSERT INTO datatbl (cpu_model_name, cpu_family, cpu_model, stepping, cpu_mhz)"+ "VALUES ('"+modelname+"',"+ cpufamily+","+ model+","+ stepping+","+cpumhz+");");
			
			System.out.println("Inserting successful");
		} catch ( Exception e){
			e.printStackTrace();
		}
	}

}
