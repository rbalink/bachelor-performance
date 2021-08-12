import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Performance {
	final String url = "jdbc:postgresql://35.234.64.171:5432/performance";
	final String user = "postgres";
	final String password = "geheim";
	HashMap<String, String> lscpu;
	CPU prozessor;

	public Performance() {
		this.lscpu = new HashMap<>();
	}

	public static void main(String[] args) {
		Performance performance = new Performance();
		performance.getHardwareInformation();

		// TODO: what if terminal language is english?
		
		// online CPU Database (boinc rosetta GFLOPS)
		performance.scrapingDatabase();

		// external postgres database
		performance.externalDatabase();
	}

	public void getHardwareInformation() {
		try {

			if (!(System.getProperty("os.name").equals("Linux"))) {
				System.err.println("The operating system for this program should be Linux!");
				throw new Exception();
			}

			ProcessBuilder builder = new ProcessBuilder();
			String text;

			// list cpu info
			builder.command("bash", "-c", "lscpu");

			// read from terminal
			Process process = builder.start();
			BufferedReader read = new BufferedReader(new InputStreamReader(process.getInputStream()));

			while ((text = read.readLine()) != null) {
				String[] parts = text.split(":", 2);
				parts[1] = parts[1].trim();
				this.lscpu.put(parts[0], parts[1]);
			}
			
			int exit = process.waitFor();

		} catch (Exception e) {
			System.err.println("Hardware Error");
			e.printStackTrace();
		}

		prozessor = new CPU(lscpu.get("Modellname"), lscpu.get("Modell"), Integer.parseInt(lscpu.get("Stepping")),
				Float.parseFloat(lscpu.get("CPU MHz")), Integer.parseInt(lscpu.get("Prozessorfamilie")), "Test", "", "",
				"");
	}

	public void externalDatabase() {
		try {
			System.out.println("Loading the Driver");
			Class.forName("org.postgresql.Driver");
			System.out.println("Connecting to external PostgreSQL Server");
			Connection con = DriverManager.getConnection(this.url, this.user, this.password);
			System.out.println(con.getSchema());

			System.out.println("Successful connection");

			Statement statement = con.createStatement();
			statement.execute("INSERT INTO postgres (cpu_model_name, cpu_family, cpu_model, stepping, cpu_mhz)"
					+ "VALUES ('" + this.prozessor.modelname + "'," + this.prozessor.cpufamily + ","
					+ this.prozessor.model + "," + this.prozessor.stepping + "," + this.prozessor.cpumhz + ");");

			System.out.println("Inserting successful");
		} catch (Exception e) {
			System.err.println("PostgresQL Error");
			e.printStackTrace();
		}
	}

	public void scrapingDatabase() {
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

		try {
			final WebClient webClient = new WebClient();
			final HtmlPage page = webClient.getPage("https://boinc.bakerlab.org/rosetta/cpu_list.php");
			System.out.println("Website: " + page.getTitleText());
		} catch (Exception e) {
			System.err.println("htmlunit Error");
			e.printStackTrace();
		}
	}

}

class CPU {
	String modelname;
	String model;
	int stepping;
	float cpumhz;
	int cpufamily;
	String l1dcache;
	String l1icache;
	String l2cache;
	String l3cache;

	public CPU(String modelname, String model, int stepping, float cpumhz, int cpufamily, String l1dcache,
			String l1icache, String l2cache, String l3cache) {
		this.modelname = modelname;
		this.model = model;
		this.stepping = stepping;
		this.cpumhz = cpumhz;
		this.cpufamily = cpufamily;
		this.l1dcache = l1dcache;
		this.l1icache = l1icache;
		this.l2cache = l2cache;
		this.l3cache = l3cache;
	}
}
