import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

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
		System.out.println(performance.prozessor.toString());

		// external postgres database
		// performance.externalDatabase();
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
			System.err.println("Couldn't read from Terminal");
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
					+ "VALUES ('" + this.prozessor.getModelname() + "'," + this.prozessor.getCpufamily() + ","
					+ this.prozessor.getModel() + "," + this.prozessor.getStepping() + "," + this.prozessor.getCpumhz()
					+ ");");

			System.out.println("Inserting successful");
		} catch (Exception e) {
			System.err.println("PostgresQL Error");
			e.printStackTrace();
		}
	}

	public void scrapingDatabase() {
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

		String testCPU = "Intel(R) Core(TM) i5-4460 CPU @ 3.20GHz";
		String testCPU2 = "Intel(R) Core(TM) i7-4790 CPU @ 3.60GHz";
		String testCPU3 = "Intel(R) Core(TM) i5-8350U CPU @ 1.70GHz";

		// Boinc Bakerlab Rosetta MFLOPS
		// TODO: bei mehreren Eintraegen
		try {
			final WebClient webClient = new WebClient();
			final HtmlPage page = webClient.getPage("https://boinc.bakerlab.org/rosetta/cpu_list.php");
			DomNodeList<DomElement> versuch = page.getElementsByTagName("table");
			HtmlTable table = (HtmlTable) versuch.get(0);

			for (int i = 0; i < table.getRowCount(); i++) {
				String cpuName = table.getRow(i).getCell(0).asNormalizedText();
				if (cpuName.contains(testCPU)) {
					String gflopsCore = table.getRow(i).getCell(3).asNormalizedText();
					String gflopsComputer = table.getRow(i).getCell(4).asNormalizedText();
					this.prozessor.setGflopsCore(Float.parseFloat(gflopsCore));
					
					this.prozessor.setGflopsComputer(Float.parseFloat(gflopsComputer));
					break;
				}
			}
		} catch (Exception e) {
			System.err.println("htmlunit Boinc Error");
			e.printStackTrace();
		}
		
		// passmark Software
		//
		String cpuNameKurz = "i5-4460";
		
		try {
			final WebClient webClient = new WebClient();
			String passmarkSearchHttps = "https://www.passmark.com/search/zoomsearch.php?zoom_query=" + cpuNameKurz;
			System.out.println(passmarkSearchHttps);
			final HtmlPage page = webClient.getPage(passmarkSearchHttps);
			DomNodeList<DomElement> versuch = page.getElementsByTagName("table");
			HtmlTable table = (HtmlTable) versuch.get(0);

			for (int i = 0; i < table.getRowCount(); i++) {
				String cpuName = table.getRow(i).getCell(0).asNormalizedText();
				if (cpuName.contains(testCPU)) {
					String gflopsCore = table.getRow(i).getCell(3).asNormalizedText();
					String gflopsComputer = table.getRow(i).getCell(4).asNormalizedText();
					this.prozessor.setGflopsCore(Float.parseFloat(gflopsCore));
					
					this.prozessor.setGflopsComputer(Float.parseFloat(gflopsComputer));
					break;
				}
			}
		} catch (Exception e) {
			System.err.println("htmlunit Boinc Error");
			e.printStackTrace();
		}
	}

}

class CPU {
	private String modelname;
	private String model;
	private int stepping;
	private float cpumhz;
	private int cpufamily;
	private String l1dcache;
	private String l1icache;
	private String l2cache;
	private String l3cache;
	private float gflopsCore;
	private float gflopsComputer;

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

	public String toString() {
		return "This CPU :" + this.modelname + " -- GFlops pro Computer:" + this.gflopsComputer;
	}

	public String getModelname() {
		return modelname;
	}

	public void setModelname(String modelname) {
		this.modelname = modelname;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getStepping() {
		return stepping;
	}

	public void setStepping(int stepping) {
		this.stepping = stepping;
	}

	public float getCpumhz() {
		return cpumhz;
	}

	public void setCpumhz(float cpumhz) {
		this.cpumhz = cpumhz;
	}

	public int getCpufamily() {
		return cpufamily;
	}

	public void setCpufamily(int cpufamily) {
		this.cpufamily = cpufamily;
	}

	public String getL1dcache() {
		return l1dcache;
	}

	public void setL1dcache(String l1dcache) {
		this.l1dcache = l1dcache;
	}

	public String getL1icache() {
		return l1icache;
	}

	public void setL1icache(String l1icache) {
		this.l1icache = l1icache;
	}

	public String getL2cache() {
		return l2cache;
	}

	public void setL2cache(String l2cache) {
		this.l2cache = l2cache;
	}

	public String getL3cache() {
		return l3cache;
	}

	public void setL3cache(String l3cache) {
		this.l3cache = l3cache;
	}

	public float getGflopsCore() {
		return gflopsCore;
	}

	public void setGflopsCore(float gflopsCore) {
		this.gflopsCore = gflopsCore;
	}

	public float getGflopsComputer() {
		return gflopsComputer;
	}

	public void setGflopsComputer(float gflopsComputer) {
		this.gflopsComputer = gflopsComputer;
	}
}