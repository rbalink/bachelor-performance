import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

public class Performance {
	final Logger log = Logger.getLogger("Performance");
	final String url = "jdbc:postgresql://35.234.103.130:5432/performance";
	final String user = "postgres";
	final String password = "geheim";
	HashMap<String, String> lscpu;
	PerformanceData pd;

	public enum Component {
		CPU, RAM, HDD
	}

	public Performance() {
		this.lscpu = new HashMap<>();
	}

	public static void main(String[] args) {
		Performance performance = new Performance();
		// performance.getHardwareInformation();

		performance.dummyCPU();
		performance.scrapingDatabase();
		
		System.out.println(performance.pd.toString());
		
		performance.externalDatabase();
	}

	// dummy for testing
	public void dummyCPU() {
		log.info("Creating dummy CPU instance");
		pd = new PerformanceData("Intel(R) Core(TM) i5-4460 CPU @ 3.20GHz", "i5-4460", 2, 3.20F, 2, "25", "150", "350", "500");
	}

	public void getHardwareInformation() {
		log.info("Reading Hardware Data from Computer");
		try {
			if (!(System.getProperty("os.name").equals("Linux"))) {
				System.err.println("The operating system for this program should be Linux!");
				throw new Exception();
			}

			readerCPU();

			pd = new PerformanceData(lscpu.get("Modellname"), lscpu.get("Modell"),
					Integer.parseInt(lscpu.get("Stepping")), Float.parseFloat(lscpu.get("CPU MHz")),
					Integer.parseInt(lscpu.get("Prozessorfamilie")), "Test", "", "", "");

			// TODO:
			// readerRAM();
			// readerDisk();
			System.out.println("break");

		} catch (Exception e) {
			System.err.println("Hardware Information Problems");
			e.printStackTrace();
		}
	}

	public void readerCPU() {
		log.info("Reading CPU data");
		try {
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

			process.destroy();

		} catch (Exception e) {
			System.err.println("Couldn't read from Terminal - lscpu");
			e.printStackTrace();
		}
	}

	// TODO: check with MacOS
	public void readerRAM() {
		log.info("Reading RAM data");
		try {
			ProcessBuilder builder = new ProcessBuilder();
			String text;

			// list ram info
			builder.command("bash", "-c", "sudo lshw -c memory");

			// read from terminal
			Process process = builder.start();
			BufferedReader read = new BufferedReader(new InputStreamReader(process.getInputStream()));

			while ((text = read.readLine()) != null) {
				System.out.println(text);
				// TODO:
			}

			process.destroy();

		} catch (Exception e) {
			System.err.println("Couldn't read from Terminal - lshw");
			e.printStackTrace();
		}
	}

	// TODO: Disk
	public void readerDisk() {
		log.info("Reading Disk data");
		try {
			ProcessBuilder builder = new ProcessBuilder();
			String text;

			// list ram info
			builder.command("bash", "-c", "sudo lshw -c memory");

			// read from terminal
			Process process = builder.start();
			BufferedReader read = new BufferedReader(new InputStreamReader(process.getInputStream()));

			while ((text = read.readLine()) != null) {
				System.out.println(text);
				// TODO:
			}

			process.destroy();

		} catch (Exception e) {
			System.err.println("Couldn't read from Terminal - DISK");
			e.printStackTrace();
		}
	}

	public void externalDatabase() {

		log.info("Connecting to external database");
		try {
			System.out.println("Loading the Driver");
			Class.forName("org.postgresql.Driver");
			System.out.println("Connecting to external PostgreSQL Server");
			Connection con = DriverManager.getConnection(this.url, this.user, this.password);

			System.out.println("Successful connection");

			Statement statement = con.createStatement();
			statement.execute("INSERT INTO postgres (cpu_model_name, cpu_family, stepping, cpu_mhz)"
					+ "VALUES ('" + this.pd.getModelname() + "'," + this.pd.getCpufamily() + "," + this.pd.getStepping() + "," + this.pd.getCpumhz() + ");");

			System.out.println("Inserting successful");
		} catch (Exception e) {
			System.err.println("PostgresQL Error");
			e.printStackTrace();
		}
	}

	public void scrapingDatabase() {
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

		boincDatabase();
		passmarkDatabase();
		userbenchmarkDatabase();
		geekbenchDatabase();
		// specBenchmark();

		// todo openbenchmarking

	}

	// Boinc Bakerlab Rosetta GFLOPS
	// TODO: bei mehreren Eintraegen
	private void boincDatabase() {
		log.info("Connecting boinc batabase (CPU)");

		try {
			final WebClient webClient = new WebClient();
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setJavaScriptEnabled(false);
			final HtmlPage page = webClient.getPage("https://boinc.bakerlab.org/rosetta/cpu_list.php");
			DomNodeList<DomElement> versuch = page.getElementsByTagName("table");
			HtmlTable table = (HtmlTable) versuch.get(0);
			
			for (int i = 0; i < table.getRowCount(); i++) {
				String cpuName = table.getRow(i).getCell(0).asNormalizedText();
				if (cpuName.contains(StringUtils.normalizeSpace(pd.getModelname()))) {
					String gflopsCore = table.getRow(i).getCell(3).asNormalizedText();
					String gflopsComputer = table.getRow(i).getCell(4).asNormalizedText();
					this.pd.setGflopsCore(Float.parseFloat(gflopsCore));

					this.pd.setGflopsComputer(Float.parseFloat(gflopsComputer));
					break;
				}
			}
			webClient.close();
		} catch (Exception e) {
			System.err.println("htmlunit Boinc Error");
			e.printStackTrace();
		}
	}

	// passmark Software (cpubenchmark)
	// passMark PerformanceTestv10 Score
	// TODO: Add Format CPU Name Kurz !
	// TODO: Add to Schema
	// TODO: Check ob der Name eindeutig ist !
	// TODO: Information: F�r CPU gibt es auch Multi CPU Systems
	private void passmarkDatabase() {
		log.info("Connecting passmark database (CPU, HDD, RAM)");
		pd.setPassMarkBenchmark(new PassMarkBenchmark());
		passmarkTableScraping(Component.CPU);
		passmarkTableScraping(Component.HDD);
		passmarkTableScraping(Component.RAM);
		log.info("passmark done");
	}

	private void passmarkTableScraping(Component com) {
		log.info("passmark db: " + com.toString());

		String passmarkUrlList = "";
		String passmarkUrlBeginning = "";
		// TODO: make generic @name and @name2

		String name = "";
		String name2 = "";
		int index = 0;
		if (com.equals(Component.CPU)) {
			passmarkUrlList = "https://www.cpubenchmark.net/cpu_list.php";
			passmarkUrlBeginning = "https://www.cpubenchmark.net/cpu.php";
			name = "i5-4460 "; // i5-4460 //i5-7300U
			name2 = "3.20"; // 3.20 // 2.60
			index = 1;
		} else if (com.equals(Component.HDD)) {
			passmarkUrlList = "https://www.harddrivebenchmark.net/hdd_list.php";
			passmarkUrlBeginning = "https://www.harddrivebenchmark.net/hdd.php";
			name = "Samsung SSD 750 EVO 500GB";
			name2 = "Samsung SSD 750 EVO 500GB";
			index = 0;
		} else if (com.equals(Component.RAM)) {
			// TODO: DDR2,3 und 4 unterschied!
			// https://www.memorybenchmark.net/ram_list.php
			// https://www.memorybenchmark.net/ram_list-ddr3.php
			// https://www.memorybenchmark.net/ram_list-ddr2.php
			// TODO: Partnumber and Manufacturer for information !
			passmarkUrlList = "https://www.memorybenchmark.net/ram_list-ddr3.php";
			passmarkUrlBeginning = "https://www.memorybenchmark.net/ram.php";
			name = "Kingston 99U5403-067.A00LF 4GB";
			name2 = "Kingston 99U5403-067.A00LF 4GB";
			index = 0;
		}
		try {
			final WebClient webClient = new WebClient();
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			final HtmlPage result = webClient.getPage(passmarkUrlList);
			DomNodeList<DomElement> domNode = result.getElementsByTagName("table");
			HtmlTable table = (HtmlTable) domNode.get(index);

			for (int i = 0; i < table.getRowCount(); i++) {
				if (table.getRow(i).getCell(0).asNormalizedText().contains(name)
						&& table.getRow(i).getCell(0).asNormalizedText().contains(name2)) {
					HtmlAnchor html = (HtmlAnchor) table.getRow(i).getCell(0).getFirstChild();
					if (com.equals(Component.RAM)) {
						pd.getPassMarkBenchmark().createRAM(table.getRow(i).getCell(1).asNormalizedText(),
								table.getRow(i).getCell(2).asNormalizedText(),
								table.getRow(i).getCell(3).asNormalizedText());
					}
					passmarkRedirecting(passmarkUrlBeginning + html.getHrefAttribute().split(".php")[1], com);
					break;
				}
			}
			webClient.close();

		} catch (Exception e) {
			System.err.println("htmlunit PassMark Error");
			e.printStackTrace();
		}
	}

	private void passmarkRedirecting(String url, Component com) {
		log.info("found " + com.toString() + " - reading data");
		String passmarkBench;

		try {
			final WebClient webClient = new WebClient();
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			final HtmlPage result = webClient.getPage(url);
			List<Object> ls = result.getByXPath("//div[contains(@class, 'right-desc')]/span[1]");
			HtmlSpan span = (HtmlSpan) ls.get(0);
			passmarkBench = span.asNormalizedText();

			if (com.equals(Component.CPU)) {
				List<Object> stats = result.getByXPath("//div[contains(@id, 'history')]/table");
				HtmlTable table = (HtmlTable) stats.get(0);
				pd.getPassMarkBenchmark().createCPU(passmarkBench, table.getRow(0).getCell(1).asNormalizedText(),
						table.getRow(1).getCell(1).asNormalizedText(), table.getRow(2).getCell(1).asNormalizedText(),
						table.getRow(3).getCell(1).asNormalizedText(), table.getRow(4).getCell(1).asNormalizedText(),
						table.getRow(5).getCell(1).asNormalizedText(), table.getRow(6).getCell(1).asNormalizedText(),
						table.getRow(7).getCell(1).asNormalizedText(), table.getRow(8).getCell(1).asNormalizedText());

			} else if (com.equals(Component.HDD)) {
				List<Object> stats = result.getByXPath("//div[contains(@id, 'history')]/table");
				HtmlTable table = (HtmlTable) stats.get(0);
				pd.getPassMarkBenchmark().createHDD(passmarkBench, table.getRow(0).getCell(1).asNormalizedText(),
						table.getRow(1).getCell(1).asNormalizedText(), table.getRow(2).getCell(1).asNormalizedText(),
						table.getRow(3).getCell(1).asNormalizedText());
			} else if (com.equals(Component.RAM)) {
				pd.getPassMarkBenchmark().getRAM().setPmScore(passmarkBench);
			}

			webClient.close();

		} catch (Exception e) {
			System.err.println("htmlunit PassMark CPU Error");
			e.printStackTrace();
		}
	}

	// userbenchmark
	private void userbenchmarkDatabase() {
		log.info("Reading data from userbenchmark db");
		userbenchmarkRedirect(Component.CPU);
		userbenchmarkRedirect(Component.RAM);
		userbenchmarkRedirect(Component.HDD);
		log.info("userbenchmark done");
	}

	private void userbenchmarkRedirect(Component com) {
		log.info("found " + com.toString() + " - reading data");
		String urlName = "";
		if (com.equals(Component.CPU)) {
			urlName = "i5-4460";
		} else if (com.equals(Component.RAM)) {
			urlName = "Kingston 99U5403-067.A00LF 4GB";
		} else if (com.equals(Component.HDD)) {
			urlName = "Samsung SSD 750 EVO 500GB";
		}

		try {
			final WebClient webClient = new WebClient();
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			final HtmlPage result = webClient.getPage("https://cpu.userbenchmark.com/Search?searchTerm=" + urlName);
			List<DomAttr> liste = result.getByXPath("//div/div/a[contains(@class, \"tl-tag\")][1]/@href");
			final HtmlPage cpupage = webClient.getPage(liste.get(0).getValue());
			if (com.equals(Component.CPU)) {
				DomText percentage = (DomText) cpupage.getByXPath("//thead/tr[1]/td[2]/div/a/text()").get(0);
				DomText cpuMemory = (DomText) cpupage
						.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[1]/td[2]/span/text()").get(0);
				DomText cpuOneCore = (DomText) cpupage
						.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[2]/td[2]/span/text()").get(0);
				DomText cpuTwoCore = (DomText) cpupage
						.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[3]/td[2]/span/text()").get(0);
				DomText cpuQuadCore = (DomText) cpupage
						.getByXPath("//thead/tr[1]/td[4]/table/tbody/tr[1]/td[2]/span/text()").get(0);
				DomText cpuOctaCore = (DomText) cpupage
						.getByXPath("//thead/tr[1]/td[4]/table/tbody/tr[2]/td[2]/span/text()").get(0);

				pd.getUb().createCPU(percentage.asNormalizedText(), cpuMemory.asNormalizedText(),
						cpuOneCore.asNormalizedText(), cpuTwoCore.asNormalizedText(), cpuQuadCore.asNormalizedText(),
						cpuOctaCore.asNormalizedText());
			} else if (com.equals(Component.RAM)) {
				DomText percentage = (DomText) cpupage.getByXPath("//thead/tr[1]/td[2]/div/a/text()").get(0);
				DomText read = (DomText) cpupage.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[1]/td[2]/span/text()")
						.get(0);
				DomText write = (DomText) cpupage.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[2]/td[2]/span/text()")
						.get(0);
				DomText mixed = (DomText) cpupage.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[3]/td[2]/span/text()")
						.get(0);
				DomText latenz = (DomText) cpupage
						.getByXPath("//thead/tr[1]/td[5]/table/tbody/tr[2]/td[1]/span[2]/text()").get(0);

				pd.getUb().createRAM(percentage.asNormalizedText(), read.asNormalizedText(), write.asNormalizedText(),
						mixed.asNormalizedText(), latenz.asNormalizedText());

			} else if (com.equals(Component.HDD)) {
				DomText percentage = (DomText) cpupage.getByXPath("//thead/tr[1]/td[2]/div/a/text()").get(0);
				DomText read = (DomText) cpupage.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[1]/td[2]/span/text()")
						.get(0);
				DomText write = (DomText) cpupage.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[2]/td[2]/span/text()")
						.get(0);
				DomText mixed = (DomText) cpupage.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[3]/td[2]/span/text()")
						.get(0);

				pd.getUb().createHDD(percentage.asNormalizedText(), read.asNormalizedText(), write.asNormalizedText(),
						mixed.asNormalizedText());
			}

			webClient.close();

		} catch (Exception e) {
			System.err.println("htmlunit userbenchmark Error");
			e.printStackTrace();
		}

	}

	// TODO: Bug multiCoreValue
	private void geekbenchDatabase() {
		log.info("Reading data from geekbench db (CPU)");
		String cpuNameKurz = "i5-4460";
		String gHZ = "3.2";

		try {
			final WebClient webClient = new WebClient();
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			final HtmlPage result = webClient.getPage("https://browser.geekbench.com/processor-benchmarks");
			DomNodeList<DomElement> domNode = result.getElementsByTagName("table");
			String singleCoreValue = geekbenchLoop((HtmlTable) domNode.get(0), cpuNameKurz, gHZ);
			String multiCoreValue = geekbenchLoop((HtmlTable) domNode.get(1), cpuNameKurz, gHZ);
			System.out.println("GeekbenchDB CPU: Single Core Value: " + singleCoreValue + " --- Multi Core Value: "
					+ multiCoreValue);

			webClient.close();
		} catch (Exception e) {
			System.err.println("htmlunit Geekbench Error");
			e.printStackTrace();
		}
	}

	private String geekbenchLoop(HtmlTable table, String cpuNameKurz, String gHz) {
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.getRow(i).getCell(0).asNormalizedText().contains(cpuNameKurz)
					&& table.getRow(i).getCell(0).asNormalizedText().contains(gHz)) {
				return table.getRow(i).getCell(1).asNormalizedText();
			}
		}
		return "";
	}

	private void specBenchmark() {
		log.info("spec Benchmark");
		String cpuNameKurz = "i5-4460";

		try {
			final WebClient webClient = new WebClient();
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			final HtmlPage result = webClient.getPage("https://browser.geekbench.com/processor-benchmarks");

			webClient.close();
		} catch (Exception e) {
			System.err.println("htmlunit SPEC Error");
			e.printStackTrace();
		}
	}

}

class PerformanceData {
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
	private String ramname;
	private String ramddr;
	private PassMarkBenchmark pm;
	private UserBenchmark ub;

	public PerformanceData(String modelname, String model, int stepping, float cpumhz, int cpufamily, String l1dcache,
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
		this.pm = new PassMarkBenchmark();
		this.ub = new UserBenchmark();
	}

	public PassMarkBenchmark getPassMarkBenchmark() {
		return this.pm;
	}

	public void setPassMarkBenchmark(PassMarkBenchmark pm) {
		this.pm = pm;
	}

	public UserBenchmark getUb() {
		return ub;
	}

	public void setUb(UserBenchmark ub) {
		this.ub = ub;
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

	public String getRamname() {
		return ramname;
	}

	public void setRamname(String ramname) {
		this.ramname = ramname;
	}

	public String getRamddr() {
		return ramddr;
	}

	public void setRamddr(String ramddr) {
		this.ramddr = ramddr;
	}

	public String toString() {
		return "Alle Werte: \nGFLOPS: " + gflopsComputer + " GLFOPS/Core: " + gflopsCore + " \nPassmark:\n"
				+ pm.getCPU().toString() + " \n" + pm.getRAM().toString() + " \n" + pm.getHDD().toString()
				+ " \nUserBenchmark\n" + ub.getCPU().toString() + " \n" + ub.getRAM().toString() + " \n"
				+ ub.getHDD().toString();
	}

}

class PassMarkBenchmark {
	public pmCPU cpu;
	public pmRAM ram;
	public pmHDD hdd;

	public PassMarkBenchmark() {

	}

	public void createCPU(String pmScore, String integerMath, String floatingPointMath, String findPrimeNumbers,
			String randomStringSorting, String dataEncryption, String dataCompression, String physics,
			String extendedInstructions, String singleThread) {
		this.cpu = new pmCPU(pmScore, integerMath, floatingPointMath, findPrimeNumbers, randomStringSorting,
				dataEncryption, dataCompression, physics, extendedInstructions, singleThread);
	}

	public pmCPU getCPU() {
		return this.cpu;
	}

	public void createRAM(String latency, String readGBs, String writeGBs) {
		this.ram = new pmRAM(latency, readGBs, writeGBs);
	}

	public pmRAM getRAM() {
		return this.ram;
	}

	public void createHDD(String pmScore, String sequentialreadMBs, String sequentialwriteMBs,
			String randomseekreadwriteMBs, String iposMBs) {
		this.hdd = new pmHDD(pmScore, sequentialreadMBs, sequentialwriteMBs, randomseekreadwriteMBs, iposMBs);
	}

	public pmHDD getHDD() {
		return this.hdd;
	}

}

class pmCPU {
	private final String pmScore;
	private final String integerMath;
	private final String floatingPointMath;
	private final String findPrimeNumbers;
	private final String randomStringSorting;
	private final String dataEncryption;
	private final String dataCompression;
	private final String physics;
	private final String extendedInstructions;
	private final String singleThread;

	public pmCPU(String pmScore, String integerMath, String floatingPointMath, String findPrimeNumbers,
			String randomStringSorting, String dataEncryption, String dataCompression, String physics,
			String extendedInstructions, String singleThread) {
		this.pmScore = pmScore;
		this.integerMath = integerMath;
		this.floatingPointMath = floatingPointMath;
		this.findPrimeNumbers = findPrimeNumbers;
		this.randomStringSorting = randomStringSorting;
		this.dataEncryption = dataEncryption;
		this.dataCompression = dataCompression;
		this.physics = physics;
		this.extendedInstructions = extendedInstructions;
		this.singleThread = singleThread;
	}

	public String toString() {
		return "Passmark CPU Score:" + pmScore + " - integerMath:" + integerMath + " - floatingPointMath:"
				+ floatingPointMath + " - " + "FindPrimeNumbers:" + findPrimeNumbers + " - randomStringSorting:"
				+ randomStringSorting + " - DataEncryption:" + dataEncryption + "" + " - DataCompression:"
				+ dataCompression + " - Physics:" + physics + " - ExtendedInstructions:" + extendedInstructions
				+ " - SingleThread:" + singleThread;
	}

	public String getPmScore() {
		return pmScore;
	}

	public String getIntegerMath() {
		return integerMath;
	}

	public String getFloatingPointMath() {
		return floatingPointMath;
	}

	public String getRandomStringSorting() {
		return randomStringSorting;
	}

	public String getDataEncryption() {
		return dataEncryption;
	}

	public String getDataCompression() {
		return dataCompression;
	}

	public String getPhysics() {
		return physics;
	}

	public String getExtendedInstructions() {
		return extendedInstructions;
	}

	public String getSingleThread() {
		return singleThread;
	}

	public String getfindPrimeNumbers() {
		return findPrimeNumbers;
	}
}

class pmRAM {
	private String pmScore;
	private final String latency;
	private final String readGBs;
	private final String writeGBs;

	public pmRAM(String latency, String readGBs, String writeGBs) {
		this.pmScore = "";
		this.latency = latency;
		this.readGBs = readGBs;
		this.writeGBs = writeGBs;
	}

	public String toString() {
		return "Passmark RAM Score:" + pmScore + " - latency:" + latency + " - readGBs:" + readGBs + " - " + "writeGBs:"
				+ writeGBs;
	}

	public void setPmScore(String pmScore) {
		this.pmScore = pmScore;
	}

	public String getPmScore() {
		return pmScore;
	}

	public String getLatency() {
		return latency;
	}

	public String getReadGBs() {
		return readGBs;
	}

	public String getWriteGBs() {
		return writeGBs;
	}
}

class pmHDD {
	private final String pmScore;
	private final String sequentialreadMBs;
	private final String sequentialwriteMBs;
	private final String randomseekreadwriteMBs;
	private final String iposMBs;

	public pmHDD(String pmScore, String sequentialreadMBs, String sequentialwriteMBs, String randomseekreadwriteMBs,
			String iposMBs) {
		this.pmScore = pmScore;
		this.sequentialreadMBs = sequentialreadMBs;
		this.sequentialwriteMBs = sequentialwriteMBs;
		this.randomseekreadwriteMBs = randomseekreadwriteMBs;
		this.iposMBs = iposMBs;
	}

	public String getPmScore() {
		return pmScore;
	}

	public String getSequentialwriteMBs() {
		return sequentialwriteMBs;
	}

	public String getRandomseekreadwriteMBs() {
		return randomseekreadwriteMBs;
	}

	public String getIposMBs() {
		return iposMBs;
	}

	public String getSequentialreadMBs() {
		return sequentialreadMBs;
	}

	public String toString() {
		return "Passmark HDD Score:" + pmScore + " - sequentialreadMBs:" + sequentialreadMBs + " - sequentialwriteMBs:"
				+ sequentialwriteMBs + " - " + "randomseekreadwriteMBs:" + randomseekreadwriteMBs + " - iposMBs:"
				+ iposMBs;
	}
}

class UserBenchmark {
	public ubCPU cpu;
	public ubRAM ram;
	public ubHDD hdd;

	public UserBenchmark() {

	}

	public void createCPU(String score, String memory, String core, String core2, String core4, String core8) {
		this.cpu = new ubCPU(score, memory, core, core2, core4, core8);
	}

	public ubCPU getCPU() {
		return this.cpu;
	}

	public void createRAM(String score, String read, String write, String mixed, String latenz) {
		this.ram = new ubRAM(score, read, write, mixed, latenz);
	}

	public ubRAM getRAM() {
		return this.ram;
	}

	public void createHDD(String score, String read, String write, String mixed) {
		this.hdd = new ubHDD(score, read, write, mixed);
	}

	public ubHDD getHDD() {
		return this.hdd;
	}

}

class ubCPU {
	private final String score;
	private final String memory;
	private final String core;
	private final String core2;
	private final String core4;
	private final String core8;

	public ubCPU(String score, String memory, String core, String core2, String core4, String core8) {
		this.score = score;
		this.memory = memory;
		this.core = core;
		this.core2 = core2;
		this.core4 = core4;
		this.core8 = core8;
	}

	public String getScore() {
		return score;
	}

	public String getMemory() {
		return memory;
	}

	public String getCore() {
		return core;
	}

	public String getCore2() {
		return core2;
	}

	public String getCore4() {
		return core4;
	}

	public String getCore8() {
		return core8;
	}

	public String toString() {
		return "Userbenchmark CPU: Score: " + score + "% --- Speicher: " + memory + " --- 1 Kern: " + core
				+ " --- 2 Kern: " + core2 + " --- 4 Kern: " + core4 + " --- 8 Kern: " + core8;
	}

}

class ubRAM {
	private String score;
	private String read;
	private String write;
	private String mixed;
	private String latenz;

	public ubRAM(String score, String read, String write, String mixed, String latenz) {
		this.score = score;
		this.read = read;
		this.write = write;
		this.mixed = mixed;
		this.latenz = latenz;
	}

	public String getScore() {
		return score;
	}

	public String getRead() {
		return read;
	}

	public String getWrite() {
		return write;
	}

	public String getMixed() {
		return mixed;
	}

	public String getLatenz() {
		return latenz;
	}

	public String toString() {
		return "Userbenchmark RAM: Score: " + this.score + "% --- Read GB/s: " + this.read + " --- Write GB/s: "
				+ this.write + " --- Mixed GB/s: " + this.mixed + " --- Latenz (ns): " + this.latenz;
	}

}

class ubHDD {
	private String score;
	private String read;
	private String write;
	private String mixed;

	public ubHDD(String score, String read, String write, String mixed) {
		this.score = score;
		this.read = read;
		this.write = write;
		this.mixed = mixed;
	}

	public String getScore() {
		return score;
	}

	public String getRead() {
		return read;
	}

	public String getWrite() {
		return write;
	}

	public String getMixed() {
		return mixed;
	}

	public String toString() {
		return "Userbenchmark HDD/SSD: Score: " + this.score + "% --- Read MB/s: " + this.read + " --- Write MB/s: "
				+ this.write + " --- Mixed MB/s: " + this.mixed;
	}

}