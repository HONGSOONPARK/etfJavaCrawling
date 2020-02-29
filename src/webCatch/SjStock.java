package webCatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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






/**
 * @author hspark
 *
 */

public class SjStock {


	public static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
	public static final String WEB_DRIVER_PATH = System.getProperty("user.dir") + "/src/webCatch/chromedriver_80v.exe";
	public static final Logger logger = Logger.getGlobal();

	// WebDriver
	private WebDriver driver;

	// WebElement
	private WebElement webElement;

	// WebElements
	private List<WebElement> weList;

	// 파싱한 url 목록
	private static ArrayList<String> urlList;
	
	private String base_url;
	BufferedOutputStream bos = null;
	
	
	BufferedInputStream bis = null;
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {

		logger.setLevel(Level.INFO);

		long startDate = System.currentTimeMillis();
		logger.log(logger.getLevel(), " Start Date : " + getCurrentData());

		
		SjStock selTest = new SjStock();
		selTest.crawling();
		selTest.crawlingDetail(urlList);

		logger.log(logger.getLevel(), " End Date : " + getCurrentData());
		
		long endDate = System.currentTimeMillis();
		
		logger.log(logger.getLevel(),  "실행 시간 : " + (endDate - startDate )/1000.0 +"초");
		

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
	
 
	

	public void crawling() throws IOException {
		
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
		
		urlList = new ArrayList<>();

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
			
			// 하단 페이지당 100개 보여주기 클릭
			webElement = driver.findElement(By.xpath("//*[@id='results']/div[2]/section[2]"));
			
			js.executeScript("arguments[0].scrollIntoView();", webElement);
		
			webElement = driver.findElement(By.xpath("//*[@id='results_display']/div/div[4]/button/label/span"));
	
			js.executeScript("arguments[0].click();", webElement);
			
			// 모든 페이지 수 파싱 (현재 기준 23개 나옴)
			webElement = driver.findElement(By.id("totalPages"));
			
			String totalPages[] = webElement.getText().split(" ");

			// 페이지 수
			int pageNum = Integer.parseInt(totalPages[1]);
			
			logger.log(logger.getLevel(), "********** All Page : "+pageNum);
		
			
			// 페이지를 돌며 tr 개수만큼 첫번째 td 값을 가져옴
			for (int i = 0; i < pageNum; i++) {

				weList = driver.findElements(By.xpath(".//*[@id='finderTable']/tbody/tr"));
				
				for (int l = 1; l <= weList.size(); l++) {
					webElement = driver.findElement(By.xpath(".//*[@id='finderTable']/tbody/tr[" + l + "]/td[1]"));
					getEtfHref = webElement.getText();
					cnt++;
					resultText += getEtfHref + "\t";
					urlList.add(getEtfHref);
				}
				Thread.sleep(5);
				resultText += "\n";
				
				webElement = driver.findElement(By.xpath(".//*[@id='finderTable']/tbody/tr[1]"));

				js.executeScript("arguments[0].scrollIntoView();", webElement);

				webElement = driver.findElement(By.xpath("//*[@id='nextPage']"));

				js.executeScript("arguments[0].click();", webElement);
				
				//actions.moveToElement(webElement).click().build().perform();
			}
			
			resultText += "\n//Create Time :"+getCurrentData();
			resultText += "\n//"+base_url;
			

			
			logger.log(logger.getLevel(), "\n 예상 : "+etfResult +" | 검색 결과 : "+cnt+" | ");
			if(etfResult == cnt){
				logger.log(logger.getLevel(), "\n 성공 ");
			}else{
				logger.log(logger.getLevel(), "\n 실패 ");
			}

			Thread.sleep(3000);
			
			
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			
			// 결과값 텍스트 파일에 저장, Byte형으로만 넣을 수 있음
			bos.write(resultText.getBytes()); 
			bos.flush();
			
			bos = null;
			webElement = null;
			weList = null;
			
			
		}
	}
	
	public void crawlingDetail(ArrayList<String> list) throws IOException {
	
		//list = new ArrayList<>(Arrays.asList("SPY","IVV","VTI","VOO","QQQ","VEA","AGG","IEFA","VWO","EFA"));
		
		list = new ArrayList<>(Arrays.asList("SPY","IVV"));
		
		
		ArrayList<String> urlListTest = new ArrayList<>();
		
		JavascriptExecutor js = (JavascriptExecutor) driver;
		WebDriverWait myWaitVar = null;
		String resultDetail = "";
		String name = "";
		String detailName = "";
		String closingPrice = "";
		String change = "";
		String time = "";
		String competingETSs = "";	
		
		int saveCount = 0;
		
		
		try {
			urlListTest = list;
			
			if(urlListTest != null){

				// webElemnet init;
				webElement = null;
				bos = new BufferedOutputStream(new FileOutputStream(System.getProperty("user.dir") + "/RESULT.txt"));
			
				// 말머리 만든다~~
				resultDetail += "Name\tDetailName\tClosing Price\tChange\tTime\tCompeting ETFs\n";
				//resultDetail += "Name\tDetailName\tCompeting ETFs\n";
				
				for(int i = 0; i < urlListTest.size(); i++){
					driver.get(base_url+""+urlListTest.get(i));	
				
					myWaitVar = new WebDriverWait(driver, 20);

					// name
					webElement = driver.findElement(By.xpath("//*[@id='form-reports-header']/div[1]/section[1]/section[1]/h1"));
					name = webElement.getText();
					resultDetail += name+"\t";
					
					// detailName
					webElement = driver.findElement(By.xpath("//*[@id='form-reports-header']/div[1]/section[1]/section[1]/span"));
					detailName = webElement.getText();
					resultDetail += detailName+"\t";
					
	
					
					try {
						// colsingPrice, change, time 값은 로딩 완료 후 확인가능하다. 100초 기다리고 안되면 걍 넘어가~~
						myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='closing-prices-header']/div[1]/div[1]/span[2]")));
//							
//						// closingPrice
						webElement = driver.findElement(By.xpath("//*[@id='closing-prices-header']/div[1]/div[1]/span[2]"));
						closingPrice = webElement.getText();
						resultDetail += closingPrice+"\t";
		
						// change
						webElement = driver.findElement(By.xpath("//*[@id='closing-prices-header']/div[1]/div[2]/span[2]"));
						change = webElement.getText();
						resultDetail += change+"\t";
						
						// time
						webElement = driver.findElement(By.xpath("//*[@id='closing-legend-header']"));
						time = webElement.getText();
						resultDetail += time+"\t";
					} catch (Exception e) {

						resultDetail += "connection error\t";
						resultDetail += "connection error\t";
						resultDetail += "connection error\t";
					
					}
					
								
					// 밑으로 잠깐 이동하려고 만듬
					webElement = driver.findElement(By.xpath("//*[@id='form-reports-header']/div[1]/section[3]/div[1]/div[2]"));
					js.executeScript("arguments[0].scrollIntoView();", webElement);
					
					weList = driver.findElements(By.xpath("//*[@id='form-reports-header']/div[1]/section[3]/div[1]/div[2]/a"));
					for (int l = 1; l <= weList.size(); l++) {	
						
						// competingETSs
						webElement = driver.findElement(By.xpath("//*[@id='form-reports-header']/div[1]/section[3]/div[1]/div[2]/a["+l+"]"));
						if(l != weList.size()){
							competingETSs += webElement.getText()+", ";	
						}else{
							competingETSs += webElement.getText();
						}
						resultDetail += competingETSs;
						competingETSs ="";
					}
					
					resultDetail += "\n";
						
					logger.log(logger.getLevel(), "파싱 진행중 {"+i+"] : "+ resultDetail);
					
					saveCount++;
					
					
					
					if(saveCount == 100){
						
						logger.log(logger.getLevel(), "데이터 중간저장ㅎㅎ");
						
						bos.write(resultDetail.getBytes());
						bos.flush();
						saveCount = 0;
						resultDetail ="";
					}
		
				}		
			}
			
		} catch (Exception e) {

		} finally {
			bos.write(resultDetail.getBytes()); // Byte형으로만 넣을 수 있음
			bos.flush();
			
			bos = null;
			webElement = null;
			weList = null;

		}
	}
	

	public static String getCurrentData() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		return sdf.format(new Date());

	}

}



