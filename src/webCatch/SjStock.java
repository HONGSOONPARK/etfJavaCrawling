package webCatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	

	BufferedOutputStream bos = null;
	BufferedOutputStream bosResult = null;
	
	
	
	BufferedInputStream bis = null;
	
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {

		log.setLevel(Level.INFO);

		long startDate = System.currentTimeMillis();
		log.info(" Start Date : " + getCurrentData());

		
		SjStock selTest = new SjStock();
		
		
		//selTest.crawling();
		
		selTest.crawlingDetail();
		
		
//		if(){
//			selTest.getDetail();
//			log.info("********** ETF 목록 불러오기 성공 ");
//			
//		}else{
//			log.info("********** ETF 목록 불러오기 실패 ");
//		}
//	


		log.info(" End Date : " + getCurrentData());
		long endDate = System.currentTimeMillis();
		log.info( "실행 시간 : " + (endDate - startDate )/1000.0 +"초");
		

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

		base_url = "https://www.etf.com/";
	}
	
 
	public void crawling() {
		
		// 크롤링으로 찾아갈 URL 주소값 가져옴
		String getEtfHref = "";
		
		// 주소값을 모두 저장할 곳
		String resultText = "";
		
		// 크롤링으로 찾은 url개수
		int cnt = 0;
		
		// ETF RESULTS(예상 결과)
		int etfResult = 0;
				
		// 클릭 이벤트시 사용 
		// Actions actions = new Actions(driver);
		
		ArrayList<String> urlList = new ArrayList<>();

		JavascriptExecutor js = (JavascriptExecutor) driver;
		try {
			
			// webElemnet init;
			webElement = null;

			driver.get(base_url+"etfanalytics/etf-finder");

			WebDriverWait myWaitVar = new WebDriverWait(driver, 30);
			
			
			// 크롤링 결과 저장할 텍스트 파일 생성
			bos = new BufferedOutputStream(new FileOutputStream(System.getProperty("user.dir") + "/ETF-RESULT.txt"));
			

			// 크롤링으로 찾을 테이블 id : results
			myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.id("results")));

		
			// ETF RESULTS(예상 결과값을 가져온다)
			webElement = driver.findElement(By.xpath("//*[@id='etfResults']/span"));
			
			etfResult = Integer.parseInt(webElement.getText().substring(0, webElement.getText().indexOf(" ")).replace(",",""));
			
			log.info("********** ETF RESULTS(expect) : "+etfResult);
			
			// 하단 페이지당 100개 보여주기 클릭
			webElement = driver.findElement(By.xpath("//*[@id='results']/div[2]/section[2]"));
			
			js.executeScript("arguments[0].scrollIntoView();", webElement);
		
			webElement = driver.findElement(By.xpath("//*[@id='results_display']/div/div[4]/button/label/span"));
	
			js.executeScript("arguments[0].click();", webElement);
			
			// 모든 페이지 수 파싱 (현재 기준 23)
			webElement = driver.findElement(By.id("totalPages"));
			
			String totalPages[] = webElement.getText().split(" ");

			// 페이지 수
			int pageNum = Integer.parseInt(totalPages[1]);
			
			log.info("********** All Page : "+pageNum);
		
			for (int i = 0; i < pageNum; i++) {

				weList = driver.findElements(By.xpath(".//*[@id='finderTable']/tbody/tr"));
				
				for (int l = 1; l <= weList.size(); l++) {
					webElement = driver.findElement(By.xpath(".//*[@id='finderTable']/tbody/tr[" + l + "]/td[1]"));
					getEtfHref = webElement.getText();

					cnt++;

					resultText += getEtfHref + "\t";
					
//					System.out.println(getEtfHref);
					urlList.add(getEtfHref);

				}
				Thread.sleep(5);
				resultText += "\n";
				
				log.info("********** 파싱중 ....");

				webElement = driver.findElement(By.xpath(".//*[@id='finderTable']/tbody/tr[1]"));

				js.executeScript("arguments[0].scrollIntoView();", webElement);

				webElement = driver.findElement(By.xpath("//*[@id='nextPage']"));

				js.executeScript("arguments[0].click();", webElement);
				
				//actions.moveToElement(webElement).click().build().perform();

			}
			
			
			bos.write(resultText.getBytes()); // Byte형으로만 넣을 수 있음
			bos.flush();
			
			log.info("\n 예상 : "+etfResult +" | 검색 결과 : "+cnt+" | ");
			if(etfResult == cnt){
				log.info("\n 성공 ");
			}else{
				log.info("\n 실패 ");
			}
			
			

			Thread.sleep(3000);
			
	
			log.info("********** 다시 파싱하기 ....");
			

			
			
		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			bos = null;
			webElement = null;
			weList = null;
			
			driver.close();
			
		}

	}
	
	public void crawlingDetail() {
		

		
		ArrayList<String> urlListTest = new ArrayList<>(Arrays.asList("SPY","IVV","VTI","VOO","QQQ","VEA","AGG","IEFA","VWO","EFA"));
		
		ArrayList<String[]> resultList = new ArrayList<>();

		JavascriptExecutor js = (JavascriptExecutor) driver;
		WebDriverWait myWaitVar = null;
		String resultDetail = "";
		String title = "";
		

		
		
		
		try {
			// webElemnet init;
			webElement = null;
			
		
			bos = new BufferedOutputStream(new FileOutputStream(System.getProperty("user.dir") + "/RESULT.txt"));

			for(int i = 0; i < urlListTest.size(); i++){
				driver.get(base_url+""+urlListTest.get(i));	
			
				myWaitVar = new WebDriverWait(driver, 30);
				
				// 크롤링으로 찾을 테이블 id : results
				myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='form-reports-header']/div[1]/section[3]")));
	
				webElement = driver.findElement(By.xpath("//*[@id='form-reports-header']/div[1]/section[1]/section[1]/h1"));
				
				title = webElement.getText();
				
				resultDetail += title+"\t";
				
//				System.out.print(title+"\t");
				
				webElement = driver.findElement(By.xpath("//*[@id='form-reports-header']/div[1]/section[3]/div[1]/div[2]"));
				
				js.executeScript("arguments[0].scrollIntoView();", webElement);

				weList = driver.findElements(By.xpath("//*[@id='form-reports-header']/div[1]/section[3]/div[1]/div[2]/a"));
				
				for (int l = 1; l <= weList.size(); l++) {
					webElement = driver.findElement(By.xpath("//*[@id='form-reports-header']/div[1]/section[3]/div[1]/div[2]/a["+l+"]"));

					//resultList.add()
					
					if(l!=weList.size()){
						resultDetail += webElement.getText()+", ";	
					}else{
						resultDetail += webElement.getText();
					}
					
							
//					System.out.print(webElement.getText()+"\t");
				}
				resultDetail += "\n";
//				System.out.println();

			}
			bos.write(resultDetail.getBytes()); // Byte형으로만 넣을 수 있음
			bos.flush();
			
			System.out.println(resultDetail);
			
		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			bos = null;
			webElement = null;
			weList = null;

			driver.close();
			
		}
	}
	

	public static String getCurrentData() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		return sdf.format(new Date());

	}

}



