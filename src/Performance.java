import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
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

		// System.out.println(performance.pd.toString());
		performance.compare();
		// performance.externalDatabase();
	}

	// dummy for testing
	public void dummyCPU() {
		log.info("Creating dummy CPU instance");
		// Intel(R) Core(TM) i5-4460 CPU @ 3.20GHz
		// Intel(R) Core(TM) i7-4790 CPU @ 3.60GHz
		pd = new PerformanceData("Intel(R) Core(TM) i5-4460 CPU @ 3.20GHz", "i5-4460", 2, 3.2, 2, "25", "150", "350",
				"500");
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
					Integer.parseInt(lscpu.get("Stepping")), Double.parseDouble(lscpu.get("CPU MHz")),
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
			log.info("Loading the driver");
			Class.forName("org.postgresql.Driver");
			log.info("Connecting to external PostgreSQL server");
			Connection con = DriverManager.getConnection(this.url, this.user, this.password);
			log.info("Connecting to external PostgreSQL server");
			System.out.println("Successful connection");

			Statement statement = con.createStatement();
			statement.execute(
					"INSERT INTO postgres (cpu_model_name, cpu_family, stepping, cpu_mhz,boinc_cpu_comp, boinc_cpu_core, pm_cpu_score, pm_cpu_intmath, pm_cpu_floatingpm, pm_cpu_prime, pm_cpu_randstso, pm_cpu_encrypt, pm_cpu_datacom, pm_cpu_phy, pm_cpu_exins, pm_cpu_thread, pm_ram_score, pm_ram_latenz, pm_ram_read, pm_ram_write, pm_hdd_score, pm_hdd_sread, pm_hdd_swrite, pm_hdd_rsrw, pm_hdd_ipos, ub_cpu_score, ub_cpu_memory, ub_cpu_core, ub_cpu_core2, ub_cpu_core4, ub_cpu_core8, ub_ram_score, ub_ram_read, ub_ram_write, ub_ram_mixed, ub_ram_latenz, ub_hdd_score, ub_hdd_read, ub_hdd_write, ub_hdd_mixed, gb_cpu_singlecore, gb_cpu_multicore)"
							+ "VALUES ('" + this.pd.getModelname() + "'," + this.pd.getCpufamily() + ","
							+ this.pd.getStepping() + "," + this.pd.getCpumhz() + "," + this.pd.getGflopsComputer()
							+ "," + this.pd.getGflopsCore() + "," + this.pd.getPm().getCPU().getPmScore() + ","
							+ this.pd.getPm().getCPU().getIntegerMath() + ","
							+ this.pd.getPm().getCPU().getFloatingPointMath() + ","
							+ this.pd.getPm().getCPU().getfindPrimeNumbers() + ","
							+ this.pd.getPm().getCPU().getRandomStringSorting() + ","
							+ this.pd.getPm().getCPU().getDataEncryption() + ","
							+ this.pd.getPm().getCPU().getDataCompression() + ","
							+ this.pd.getPm().getCPU().getPhysics() + ","
							+ this.pd.getPm().getCPU().getExtendedInstructions() + ","
							+ this.pd.getPm().getCPU().getSingleThread() + "," + this.pd.getPm().getRAM().getPmScore()
							+ "," + this.pd.getPm().getRAM().getLatency() + "," + this.pd.getPm().getRAM().getReadGBs()
							+ "," + this.pd.getPm().getRAM().getWriteGBs() + "," + this.pd.getPm().getHDD().getPmScore()
							+ "," + this.pd.getPm().getHDD().getSequentialreadMBs() + ","
							+ this.pd.getPm().getHDD().getSequentialwriteMBs() + ","
							+ this.pd.getPm().getHDD().getRandomseekreadwriteMBs() + ","
							+ this.pd.getPm().getHDD().getIposMBs() + "," + this.pd.getUb().getCPU().getScore() + ","
							+ this.pd.getUb().getCPU().getMemory() + "," + this.pd.getUb().getCPU().getCore() + ","
							+ this.pd.getUb().getCPU().getCore2() + "," + this.pd.getUb().getCPU().getCore4() + ","
							+ this.pd.getUb().getCPU().getCore8() + "," + this.pd.getUb().getRAM().getScore() + ","
							+ this.pd.getUb().getRAM().getRead() + "," + this.pd.getUb().getRAM().getWrite() + ","
							+ this.pd.getUb().getRAM().getMixed() + "," + this.pd.getUb().getRAM().getLatenz() + ","
							+ this.pd.getUb().getHDD().getScore() + "," + this.pd.getUb().getHDD().getRead() + ","
							+ this.pd.getUb().getHDD().getWrite() + "," + this.pd.getUb().getHDD().getMixed() + ","
							+ this.pd.getGbsingle() + "," + this.pd.getGbmulti() + ");");

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
			name = shortCPUname(pd.getModelname()); // "i5-4460 "; // i5-4460
													// //i5-7300U
			name2 = pd.getCpumhz() + "0"; // "3.20"; // 3.20 // 2.60
			index = 1;
		} else if (com.equals(Component.HDD)) {
			passmarkUrlList = "https://www.harddrivebenchmark.net/hdd_list.php";
			passmarkUrlBeginning = "https://www.harddrivebenchmark.net/hdd.php";
			name = "Samsung SSD 840 Evo 500GB"; // Samsung SSD 840 Evo 500GB
												// //Samsung SSD 750 EVO 500GB
			name2 = "Samsung SSD 840 Evo 500GB"; // Samsung SSD 840 Evo 500GB
													// //Samsung SSD 750 EVO
													// 500GB
			index = 0;
		} else if (com.equals(Component.RAM)) {
			// TODO: DDR2,3 und 4 unterschied!
			// https://www.memorybenchmark.net/ram_list.php
			// https://www.memorybenchmark.net/ram_list-ddr3.php
			// https://www.memorybenchmark.net/ram_list-ddr2.php
			// TODO: Partnumber and Manufacturer for information !
			passmarkUrlList = "https://www.memorybenchmark.net/ram_list-ddr3.php";
			passmarkUrlBeginning = "https://www.memorybenchmark.net/ram.php";
			name = "G.SKILL F4 DDR4 3600 C16"; // G.SKILL F4 DDR4 3600 C16
												// //Kingston 99U5403-067.A00LF
												// 4GB
			name2 = "G.SKILL F4 DDR4 3600 C16"; // G.SKILL F4 DDR4 3600 C16
												// //Kingston 99U5403-067.A00LF
												// 4GB
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
						pd.getPm().createRAM(Double.parseDouble(table.getRow(i).getCell(1).asNormalizedText()),
								Double.parseDouble(table.getRow(i).getCell(2).asNormalizedText()),
								Double.parseDouble(table.getRow(i).getCell(3).asNormalizedText()));
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
				pd.getPm().createCPU(Double.parseDouble(passmarkBench),
						Double.parseDouble(
								table.getRow(0).getCell(1).asNormalizedText().split(" ")[0].replace(",", ".")),
						Double.parseDouble(
								table.getRow(1).getCell(1).asNormalizedText().split(" ")[0].replace(",", ".")),
						Double.parseDouble(table.getRow(2).getCell(1).asNormalizedText().split(" ")[0]),
						Double.parseDouble(table.getRow(3).getCell(1).asNormalizedText().split(" ")[0]),
						Double.parseDouble(
								table.getRow(4).getCell(1).asNormalizedText().split(" ")[0].replace(",", ".")),
						Double.parseDouble(table.getRow(5).getCell(1).asNormalizedText().split(" ")[0]),
						Double.parseDouble(table.getRow(6).getCell(1).asNormalizedText().split(" ")[0]),
						Double.parseDouble(
								table.getRow(7).getCell(1).asNormalizedText().split(" ")[0].replace(",", ".")),
						Double.parseDouble(
								table.getRow(8).getCell(1).asNormalizedText().split(" ")[0].replace(",", ".")));

			} else if (com.equals(Component.HDD)) {
				List<Object> stats = result.getByXPath("//div[contains(@id, 'history')]/table");
				HtmlTable table = (HtmlTable) stats.get(0);
				pd.getPm().createHDD(Double.parseDouble(passmarkBench),
						Double.parseDouble(table.getRow(0).getCell(1).asNormalizedText().split(" ")[0]),
						Double.parseDouble(table.getRow(1).getCell(1).asNormalizedText().split(" ")[0]),
						Double.parseDouble(table.getRow(2).getCell(1).asNormalizedText().split(" ")[0]),
						Double.parseDouble(table.getRow(3).getCell(1).asNormalizedText().split(" ")[0]));
			} else if (com.equals(Component.RAM)) {
				pd.getPm().getRAM().setPmScore(Double.parseDouble(passmarkBench));
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
			urlName = shortCPUname(pd.getModelname());// "i5-4460";
		} else if (com.equals(Component.RAM)) {
			urlName = "G.SKILL F4 DDR4 3600 C16"; // G.SKILL F4 DDR4 3600 C16
													// //Kingston
													// 99U5403-067.A00LF 4GB
		} else if (com.equals(Component.HDD)) {
			urlName = "Samsung SSD 840 Evo 500GB"; // Samsung SSD 750 EVO 500GB
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

				pd.getUb().createCPU(Double.parseDouble(percentage.asNormalizedText()),
						Double.parseDouble(cpuMemory.asNormalizedText()),
						Double.parseDouble(cpuOneCore.asNormalizedText()),
						Double.parseDouble(cpuTwoCore.asNormalizedText()),
						Double.parseDouble(cpuQuadCore.asNormalizedText()),
						Double.parseDouble(cpuOctaCore.asNormalizedText()));
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

				pd.getUb().createRAM(Double.parseDouble(percentage.asNormalizedText()),
						Double.parseDouble(read.asNormalizedText()), Double.parseDouble(write.asNormalizedText()),
						Double.parseDouble(mixed.asNormalizedText()), Double.parseDouble(latenz.asNormalizedText()));

			} else if (com.equals(Component.HDD)) {
				DomText percentage = (DomText) cpupage.getByXPath("//thead/tr[1]/td[2]/div/a/text()").get(0);
				DomText read = (DomText) cpupage.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[1]/td[2]/span/text()")
						.get(0);
				DomText write = (DomText) cpupage.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[2]/td[2]/span/text()")
						.get(0);
				DomText mixed = (DomText) cpupage.getByXPath("//thead/tr[1]/td[3]/table/tbody/tr[3]/td[2]/span/text()")
						.get(0);

				pd.getUb().createHDD(Double.parseDouble(percentage.asNormalizedText()),
						Double.parseDouble(read.asNormalizedText()), Double.parseDouble(write.asNormalizedText()),
						Double.parseDouble(mixed.asNormalizedText()));
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
		String cpuNameKurz = shortCPUname(pd.getModelname());
		String gHZ = Double.toString(pd.getCpumhz());

		try {
			final WebClient webClient = new WebClient();
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			final HtmlPage result = webClient.getPage("https://browser.geekbench.com/processor-benchmarks");
			DomNodeList<DomElement> domNode = result.getElementsByTagName("table");
			String singleCoreValue = geekbenchLoop((HtmlTable) domNode.get(0), cpuNameKurz, gHZ);
			String multiCoreValue = geekbenchLoop((HtmlTable) domNode.get(1), cpuNameKurz, gHZ);
			pd.setGbsingle(Double.parseDouble(singleCoreValue));
			pd.setGbmulti(Double.parseDouble(multiCoreValue));
			// System.out.println("GeekbenchDB CPU: Single Core Value: " +
			// singleCoreValue + " --- Multi Core Value: "
			// + multiCoreValue);

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

	// comparing latest database entry with tested hardware
	public void compare() {
		log.info("Connecting to external database");
		try {
			Class.forName("org.postgresql.Driver");
			Connection con = DriverManager.getConnection(this.url, this.user, this.password);
			System.out.println("Successful connection");

			Statement statement = con.createStatement();
			ResultSet rs1 = statement.executeQuery("SELECT count(*) from postgres;");
			rs1.next();
			String entries = rs1.getString(1);

			// Statement getStatement = con.createStatement();
			// ResultSet rs2 = getStatement.executeQuery("select * from postgres
			// where id = "+entries+";");
			// while(rs2.next()){
			// for(int i = 1 ; i<45;i++){
			// //System.out.println(rs2.getString(i));
			// }
			//
			// }

			ResultSet rs3 = statement.executeQuery("select pm_cpu_score from postgres where id = " + entries + ";");
			rs3.next();
			double d = Double.parseDouble(rs3.getString(1));
			double s = pd.getPm().getCPU().getPmScore() / d;

			// System.out.println("PMScore CPU Score "+s+"%");
			double x = (s - 1) * 100;
			System.out.println("Nach PMScore ist die CPU " + new DecimalFormat("#.##").format(x) + "% schneller");

			ResultSet rs4 = statement.executeQuery("select ub_cpu_score from postgres where id = " + entries + ";");
			rs4.next();
			d = Double.parseDouble(rs4.getString(1));
			s = pd.getUb().getCPU().getScore() / d;

			// System.out.println("UBscore CPU Score "+s+"%");
			x = (s - 1) * 100;
			System.out.println("Nach UB ist die CPU " + new DecimalFormat("#.##").format(x) + "% schneller");

			ResultSet rs5 = statement.executeQuery("select gb_cpu_multicore from postgres where id = " + entries + ";");
			rs5.next();
			d = Double.parseDouble(rs5.getString(1));
			s = pd.getGbmulti() / d;

			// System.out.println("GBscore CPU Multi Score "+s+"%");
			x = (s - 1) * 100;
			System.out.println("Nach GB ist die CPU " + new DecimalFormat("#.##").format(x) + "% schneller");

			ResultSet rs6 = statement.executeQuery("select boinc_cpu_comp from postgres where id = " + entries + ";");
			rs6.next();
			d = Double.parseDouble(rs6.getString(1));
			s = pd.getGflopsComputer();
			// s = pd.getGbmulti() / d;

			// System.out.println("GBscore CPU Multi Score "+s+"%");
			// x = (s-1)*100;
			System.out.println("Nach GFLOPS aktuell: " + new DecimalFormat("#.##").format(s) + ", letzter Eintrag: "
					+ new DecimalFormat("#.##").format(d));

			ResultSet rs7 = statement
					.executeQuery("select pm_cpu_floatingpm, pm_cpu_datacom from postgres where id = " + entries + ";");
			rs7.next();
			d = Double.parseDouble(rs7.getString(1));
			double e = Double.parseDouble(rs7.getString(2));
			double t = pd.getPm().getCPU().getFloatingPointMath();

			s = pd.getPm().getCPU().getDataCompression();
			double eigen = (((0.66 * (t / d)) + (0.33 * (s / e))) - 1) * 100;

			System.out
					.println("Nach eigener Berechnung CPU: " + new DecimalFormat("#.##").format(eigen) + "% schneller");

			System.out.println();
			ResultSet rs8 = statement.executeQuery("select ub_hdd_mixed from postgres where id = " + entries + ";");
			rs8.next();
			d = Double.parseDouble(rs8.getString(1));
			s = pd.getUb().getHDD().getMixed() / d;

			x = (s - 1) * 100;
			System.out.println(
					"HardDisk IO ReadWriteSpeed " + new DecimalFormat("#.##").format(x) + "% schneller (MB/s)");

			System.out.println();
			ResultSet rs9 = statement.executeQuery("select ub_ram_mixed from postgres where id = " + entries + ";");
			rs9.next();
			d = Double.parseDouble(rs9.getString(1));
			s = pd.getUb().getRAM().getMixed() / d;

			x = (s - 1) * 100;
			System.out.println("RAM ReadWriteSpeed " + new DecimalFormat("#.##").format(x) + "% schneller (MB/s)");

			ResultSet rs10 = statement.executeQuery("select ub_ram_latenz from postgres where id = " + entries + ";");
			rs10.next();
			d = Double.parseDouble(rs10.getString(1));
			s = pd.getUb().getRAM().getLatenz() / d;

			x = (s - 1) * 100;
			System.out.println("RAM Latenz " + new DecimalFormat("#.##").format(x) + "% schneller (ns)");

			ResultSet rs11 = statement.executeQuery("select ub_ram_score from postgres where id = " + entries + ";");
			rs11.next();
			d = Double.parseDouble(rs11.getString(1));
			s = pd.getUb().getRAM().getScore() / d;

			x = (s - 1) * 100;
			System.out.println("RAM UB Score " + new DecimalFormat("#.##").format(x) + "% schneller");

		} catch (Exception e) {
			System.err.println("PostgresQL Error");
			e.printStackTrace();
		}
	}

	private static String shortCPUname(String cpu) {
		if (cpu.contains("Intel")) {
			return cpu.split(" ")[2];
		}
		return "";
	}

}

class PerformanceData {
	private String modelname;
	private String model;
	private int stepping;
	private double cpumhz;
	private int cpufamily;
	private String l1dcache;
	private String l1icache;
	private String l2cache;
	private String l3cache;
	private double gflopsCore;
	private double gflopsComputer;
	private String ramname;
	private String ramddr;
	private PassMarkBenchmark pm;
	private UserBenchmark ub;
	private double gbsingle;
	private double gbmulti;

	public PerformanceData(String modelname, String model, int stepping, double cpumhz, int cpufamily, String l1dcache,
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

	public PassMarkBenchmark getPm() {
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

	public double getCpumhz() {
		return cpumhz;
	}

	public void setCpumhz(int cpumhz) {
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

	public double getGflopsCore() {
		return gflopsCore;
	}

	public void setGflopsCore(float gflopsCore) {
		this.gflopsCore = gflopsCore;
	}

	public double getGflopsComputer() {
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

	public double getGbsingle() {
		return gbsingle;
	}

	public void setGbsingle(double gbsingle) {
		this.gbsingle = gbsingle;
	}

	public double getGbmulti() {
		return gbmulti;
	}

	public void setGbmulti(double gbmulti) {
		this.gbmulti = gbmulti;
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

	public void createCPU(double pmScore, double integerMath, double floatingPointMath, double findPrimeNumbers,
			double randomStringSorting, double dataEncryption, double dataCompression, double physics,
			double extendedInstructions, double singleThread) {
		this.cpu = new pmCPU(pmScore, integerMath, floatingPointMath, findPrimeNumbers, randomStringSorting,
				dataEncryption, dataCompression, physics, extendedInstructions, singleThread);
	}

	public pmCPU getCPU() {
		return this.cpu;
	}

	public void createRAM(double latency, double readGBs, double writeGBs) {
		this.ram = new pmRAM(latency, readGBs, writeGBs);
	}

	public pmRAM getRAM() {
		return this.ram;
	}

	public void createHDD(double pmScore, double sequentialreadMBs, double sequentialwriteMBs,
			double randomseekreadwriteMBs, double iposMBs) {
		this.hdd = new pmHDD(pmScore, sequentialreadMBs, sequentialwriteMBs, randomseekreadwriteMBs, iposMBs);
	}

	public pmHDD getHDD() {
		return this.hdd;
	}

}

class pmCPU {
	private final double pmScore;
	private final double integerMath;
	private final double floatingPointMath;
	private final double findPrimeNumbers;
	private final double randomStringSorting;
	private final double dataEncryption;
	private final double dataCompression;
	private final double physics;
	private final double extendedInstructions;
	private final double singleThread;

	public pmCPU(double pmScore, double integerMath, double floatingPointMath, double findPrimeNumbers,
			double randomStringSorting, double dataEncryption, double dataCompression, double physics,
			double extendedInstructions, double singleThread) {
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

	public double getPmScore() {
		return pmScore;
	}

	public double getIntegerMath() {
		return integerMath;
	}

	public double getFloatingPointMath() {
		return floatingPointMath;
	}

	public double getRandomStringSorting() {
		return randomStringSorting;
	}

	public double getDataEncryption() {
		return dataEncryption;
	}

	public double getDataCompression() {
		return dataCompression;
	}

	public double getPhysics() {
		return physics;
	}

	public double getExtendedInstructions() {
		return extendedInstructions;
	}

	public double getSingleThread() {
		return singleThread;
	}

	public double getfindPrimeNumbers() {
		return findPrimeNumbers;
	}
}

class pmRAM {
	private double pmScore;
	private final double latency;
	private final double readGBs;
	private final double writeGBs;

	public pmRAM(double latency, double readGBs, double writeGBs) {
		this.pmScore = 0;
		this.latency = latency;
		this.readGBs = readGBs;
		this.writeGBs = writeGBs;
	}

	public String toString() {
		return "Passmark RAM Score:" + pmScore + " - latency:" + latency + " - readGBs:" + readGBs + " - " + "writeGBs:"
				+ writeGBs;
	}

	public void setPmScore(double pmScore) {
		this.pmScore = pmScore;
	}

	public double getPmScore() {
		return pmScore;
	}

	public double getLatency() {
		return latency;
	}

	public double getReadGBs() {
		return readGBs;
	}

	public double getWriteGBs() {
		return writeGBs;
	}
}

class pmHDD {
	private final double pmScore;
	private final double sequentialreadMBs;
	private final double sequentialwriteMBs;
	private final double randomseekreadwriteMBs;
	private final double iposMBs;

	public pmHDD(double pmScore, double sequentialreadMBs, double sequentialwriteMBs, double randomseekreadwriteMBs,
			double iposMBs) {
		this.pmScore = pmScore;
		this.sequentialreadMBs = sequentialreadMBs;
		this.sequentialwriteMBs = sequentialwriteMBs;
		this.randomseekreadwriteMBs = randomseekreadwriteMBs;
		this.iposMBs = iposMBs;
	}

	public double getPmScore() {
		return pmScore;
	}

	public double getSequentialwriteMBs() {
		return sequentialwriteMBs;
	}

	public double getRandomseekreadwriteMBs() {
		return randomseekreadwriteMBs;
	}

	public double getIposMBs() {
		return iposMBs;
	}

	public double getSequentialreadMBs() {
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

	public void createCPU(double score, double memory, double core, double core2, double core4, double core8) {
		this.cpu = new ubCPU(score, memory, core, core2, core4, core8);
	}

	public ubCPU getCPU() {
		return this.cpu;
	}

	public void createRAM(double score, double read, double write, double mixed, double latenz) {
		this.ram = new ubRAM(score, read, write, mixed, latenz);
	}

	public ubRAM getRAM() {
		return this.ram;
	}

	public void createHDD(double score, double read, double write, double mixed) {
		this.hdd = new ubHDD(score, read, write, mixed);
	}

	public ubHDD getHDD() {
		return this.hdd;
	}

}

class ubCPU {
	private final double score;
	private final double memory;
	private final double core;
	private final double core2;
	private final double core4;
	private final double core8;

	public ubCPU(double score, double memory, double core, double core2, double core4, double core8) {
		this.score = score;
		this.memory = memory;
		this.core = core;
		this.core2 = core2;
		this.core4 = core4;
		this.core8 = core8;
	}

	public double getScore() {
		return score;
	}

	public double getMemory() {
		return memory;
	}

	public double getCore() {
		return core;
	}

	public double getCore2() {
		return core2;
	}

	public double getCore4() {
		return core4;
	}

	public double getCore8() {
		return core8;
	}

	public String toString() {
		return "Userbenchmark CPU: Score: " + score + "% --- Speicher: " + memory + " --- 1 Kern: " + core
				+ " --- 2 Kern: " + core2 + " --- 4 Kern: " + core4 + " --- 8 Kern: " + core8;
	}

}

class ubRAM {
	private double score;
	private double read;
	private double write;
	private double mixed;
	private double latenz;

	public ubRAM(double score, double read, double write, double mixed, double latenz) {
		this.score = score;
		this.read = read;
		this.write = write;
		this.mixed = mixed;
		this.latenz = latenz;
	}

	public double getScore() {
		return score;
	}

	public double getRead() {
		return read;
	}

	public double getWrite() {
		return write;
	}

	public double getMixed() {
		return mixed;
	}

	public double getLatenz() {
		return latenz;
	}

	public String toString() {
		return "Userbenchmark RAM: Score: " + this.score + "% --- Read GB/s: " + this.read + " --- Write GB/s: "
				+ this.write + " --- Mixed GB/s: " + this.mixed + " --- Latenz (ns): " + this.latenz;
	}

}

class ubHDD {
	private double score;
	private double read;
	private double write;
	private double mixed;

	public ubHDD(double score, double read, double write, double mixed) {
		this.score = score;
		this.read = read;
		this.write = write;
		this.mixed = mixed;
	}

	public double getScore() {
		return score;
	}

	public double getRead() {
		return read;
	}

	public double getWrite() {
		return write;
	}

	public double getMixed() {
		return mixed;
	}

	public String toString() {
		return "Userbenchmark HDD/SSD: Score: " + this.score + "% --- Read MB/s: " + this.read + " --- Write MB/s: "
				+ this.write + " --- Mixed MB/s: " + this.mixed;
	}

}