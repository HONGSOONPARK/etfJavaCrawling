package webCatch;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;






public class SjStock {


  
	public static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
	public static final String WEB_DRIVER_PATH = System.getProperty("user.dir") + "/src/webCatch/chromedriver_80v.exe";
	public static final Logger log = Logger.getGlobal();

	// WebDriver
	private WebDriver driver;

	// WebElement
	private WebElement webElement;

	// WebElements
	private List<WebElement> weList;

	
	ArrayList<String> urlList;

	private String base_url;

	BufferedOutputStream bs = null;

	public static void main(String[] args) throws ClientProtocolException, IOException {

		log.setLevel(Level.INFO);

		log.info(" Start Date : " + getCurrentData());

		SjStock selTest = new SjStock();
		selTest.crawling();

		log.info(" End Date : " + getCurrentData());

	}
  
  

  
	public SjStock() {
		super();

		// System Property SetUp
		System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

		// Driver SetUp
		ChromeOptions options = new ChromeOptions();
		options.setCapability("ignoreProtectedModeSettings", true);
		options.addArguments("headless");
		options.addArguments("window-size=1920x1080");
		options.addArguments("disable-gpu");

		driver = new ChromeDriver(options);

		base_url = "https://www.etf.com/etfanalytics/etf-finder";
	}
 
	public void crawling() {
		
		// URL 주소값 가져옴
		String getEtfHref = "";
		
		
		// 모든 URL 주소값 저장할 곳
		String resultText = "";
		
		// 크롤링으로 찾은 url개수
		int cnt = 0;
		
		// ETF RESULTS
		int etfResult = 0;
				
		// 클릭 이벤트시 사용 
		Actions actions = new Actions(driver);
				

		JavascriptExecutor js = (JavascriptExecutor) driver;
		
		
		try {
			
			// webElemnet init;
			
			webElement = null;

			driver.get(base_url);

			WebDriverWait myWaitVar = new WebDriverWait(driver, 30);

			// 크롤링으로 찾을 테이블 id : results
			myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.id("results")));

		

			webElement = driver.findElement(By.xpath("//*[@id='etfResults']/span"));
			
			etfResult = Integer.parseInt(webElement.getText().substring(0, webElement.getText().indexOf(" ")).replace(",",""));
			
			webElement = driver.findElement(By.xpath("//*[@id='results_display']/div/div[4]/button/label/span"));
			
			js.executeScript("arguments[0].scrollIntoView();", webElement);
			
			actions.moveToElement(webElement).click().build().perform();
					
			
			// 모든 페이지 수 (현재 기준 233)
			webElement = driver.findElement(By.id("totalPages"));
			
			String totalPages[] = webElement.getText().split(" ");

			int pageNum = Integer.parseInt(totalPages[1]);
			
			System.out.println(pageNum);
		
			for (int i = 0; i < pageNum; i++) {

				weList = driver.findElements(By.xpath(".//*[@id='finderTable']/tbody/tr"));
				
				System.out.println(weList.size()+" ================================");

				for (int l = 1; l <= weList.size(); l++) {
					webElement = driver.findElement(By.xpath(".//*[@id='finderTable']/tbody/tr[" + l + "]/td[1]"));
					getEtfHref = webElement.getText();

					cnt++;

					resultText += getEtfHref + "\t";
					
					System.out.println(getEtfHref);

				}

				resultText += "\n";

				webElement = driver.findElement(By.xpath(".//*[@id='finderTable']/tbody/tr[1]"));

				js.executeScript("arguments[0].scrollIntoView();", webElement);

				webElement = driver.findElement(By.xpath("//*[@id='nextPage']"));

				actions.moveToElement(webElement).click().build().perform();

			}
			
			resultText += "\n"+cnt;

			bs = new BufferedOutputStream(new FileOutputStream(System.getProperty("user.dir") + "/ETF-RESULT.txt"));

			bs.write(resultText.getBytes()); // Byte형으로만 넣을 수 있음


			Thread.sleep(50);

		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			driver.close();
		}

	}



	public static String getCurrentData() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

		return sdf.format(new Date());

	}

}



