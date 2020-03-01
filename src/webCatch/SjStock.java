package webCatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * 
 * 
 * @author 박홍순
 * @version Test Version 0.2
 * @since 2020.03.01
 * 
 * 개발 : Java 1.8, Selenium-java-3.141.59, Chromedriver-81v
 * 
 * 
 * 	프로그램 목적
 * 1. https://www.etf.com/etfanalytics/etf-finder 에 있는 ETF 상세 데이터 크롤링 (상준스탁 요청)
 *  
 *  프로그램 시나리오
 * 
 * 1. https://www.etf.com/etfanalytics/etf-finder 접근
 * 2. ETF RESULTS 값 파싱 -> 예상 결과 값을 확인하기 위해서
 * 3. Fund Basics 탭에 있는 Ticker 값 파싱 (2020.03.01 기준, 234페이지, 2333개의 데이터) 
 * 4. 하단 Display 100 클릭하여 한 페이지 100개 데이터 출력 (이렇게 하는게 속도면에서 좀 빠름)
 * 5. Ticker 값 ArrayList 에 저장, 그리고 끝나면 ETF-RESULT.txt에 저장
 * 6. ArrayList 의 개수 만큼 URL 반복 접근( URL은 https://www.etf.com + Ticker 값임, Ex)https://www.etf.com/SPY )
 * 7. URL 접근 후 TITLE 값 파싱 : 페이지 접근여부, 접근 안되면 에러 로그 남기고 다음 페이지 접근
 * 8. TITLE 값이 파싱 됨 -> URL 정상 접근이라 판단하고 Competing ETFs 값을 파싱
 * 9. ArrayList 의 개수 만큼 반복하여 데이터 파싱, 100번 반복시 RESULT.txt에 저장
 * 10. 모든 페이지 접근 후 데이터는 RESULT.txt에 저장, 프로그램 종료 
 * 11. 예외처리 발생시 로그만 남기고 걍 다음단계로 넘어감
 * 12. 메모장 열어서 결과값 확인 -> 복사 후 엑셀 붙여넣기 가능
 * 13. 끝
 * 
 * 
 *	보완해야 할 부분
 * 1. 멀티쓰레드 적용하면 시간 단축 가능, 현재 단일 프로세스 하나만 적용(3시간 넘게 걸림 ㅋㅋㅋ)
 * 2. 예외 처리가 좀 불완전함, 현재 페이지 접근 안되면 다음 순서로 넘어가는걸로 했지만, 여러번 반복 시도하여 정확도를 높일 수도 있음
 * 3. 중간에 취소하면 첨부터 해야함(ㅋㅋ)
 * 4. 로그내용 txt로 저장 안함
 * 5. 파이썬으로 개발하면 좀더 빠를듯함
 * 
 */
public class SjStock {

	// Chrome Driver ID
	public static final String WEB_DRIVER_ID = "webdriver.chrome.driver";

	// Chrome Driver PATH
	public static final String WEB_DRIVER_PATH = System.getProperty("user.dir") + "/src/webCatch/chromedriver_80v.exe";
	
	// Logger
	public static final Logger logger = Logger.getGlobal();

	// WebDriver
	private WebDriver driver;

	// WebElement
	private WebElement webElement;

	// WebElements
	private List<WebElement> weList;

	// 파싱한 url 목록
	private static ArrayList<String> urlList;
	
	// 접속할 기본 url 
	private String base_url;
	
	// 파일 읽기,쓰기
	BufferedOutputStream bos = null;
	BufferedInputStream bis = null;
	
	
	public static void main(String[] args) throws IOException {

		logger.setLevel(Level.INFO);

		// 프로그램 시작 시간
		long startDate = System.currentTimeMillis();
		logger.log(logger.getLevel(), " Start Date : " + getCurrentData());

		
		SjStock selTest = new SjStock();
		
		selTest.etfNameCrawling();
		selTest.etfDetailCrawling(urlList);

		
		// 프로그램 종료시간
		logger.log(logger.getLevel(), " End Date : " + getCurrentData());
 
		long endDate = System.currentTimeMillis();
		
		logger.log(logger.getLevel(),  "실행 시간 : " + (endDate - startDate )/1000.0 +"초");
		

	}
    
	public SjStock() {
		super();

		// System Property SetUp
		System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

		// Driver SetUp, Add Options
		ChromeOptions options = new ChromeOptions();
		options.setCapability("ignoreProtectedModeSettings", true);
		options.addArguments("start-maximized");
		options.addArguments("enable-automation");
		options.addArguments("no-sandbox");
		options.addArguments("disable-infobars");
		options.addArguments("disable-dev-shm-usage");
		options.addArguments("disable-browser-side-navigation"); 
		options.addArguments("headless");
		options.addArguments("window-size=1920x1080");
		options.addArguments("disable-gpu");
		

		driver = new ChromeDriver(options);

		// 접속할 주소
		base_url = "https://www.etf.com/";
	}
	
 
	

	/**
	 * 
	 * @author 박홍순	
	 * 
	 * ETF 이름 파싱하기
	 * 
	 */
	public void etfNameCrawling() throws IOException {
		
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
					resultText += getEtfHref + "\n";
					urlList.add(getEtfHref);
				}
				Thread.sleep(5);
				//resultText += "\n";
				
				webElement = driver.findElement(By.xpath(".//*[@id='finderTable']/tbody/tr[1]"));

				js.executeScript("arguments[0].scrollIntoView();", webElement);

				
				// 다음페이지 클릭
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
	
	
	
	/**
	 * 
	 * @author 박홍순	
	 * 
	 * ETF Detail 파싱
	 * 
	 */
	
	public void etfDetailCrawling(ArrayList<String> list) throws IOException {
	
		// Test Data
		// list = new ArrayList<>(Arrays.asList("SPY","IVV","VTI","VOO","QQQ","VEA","AGG","IEFA","VWO","EFA"));

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
		
		logger.log(logger.getLevel(), "Init - resultDetail["+resultDetail+"] "
				+ "| name["+name+"] "
				+ "| detailName["+detailName+"] "
				+ "| closingPrice["+closingPrice+"] "
				+ "| change["+change+"] "
				+ "| time["+time+"] | " 
				+ "| competingETSs["+competingETSs+"] | " );
		
				
		int saveCount = 0;
		
		
		try {
			urlListTest = list;
			
			if(urlListTest != null){

				// webElemnet init;
				webElement = null;
				bos = new BufferedOutputStream(new FileOutputStream(System.getProperty("user.dir") + "/RESULT.txt"));
			
				// 말머리 만든다~~
//				resultDetail += "Name\tDetailName\tClosing Price\tChange\tTime\tCompeting ETFs\n";
				resultDetail += "Name\tDetailName\tCompeting ETFs\n";
				
				for(int i = 0; i < urlListTest.size(); i++){
					driver.get(base_url+""+urlListTest.get(i));	
				
					myWaitVar = new WebDriverWait(driver,20);
					
					

					try {
						// URL 접근시 이름 체크, 로딩 실패시 다음으로 넘어간다.
						myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='form-reports-header']/div[1]/section[3]/div[1]/div[2]")));

						// name
						webElement = driver.findElement(By.xpath("//*[@id='form-reports-header']/div[1]/section[1]/section[1]/h1"));
						name = webElement.getText();
						resultDetail += name+"\t";
						
						// detailName
						webElement = driver.findElement(By.xpath("//*[@id='form-reports-header']/div[1]/section[1]/section[1]/span"));
						detailName = webElement.getText();
						resultDetail += detailName+"\t";
						
		
						
						// colsingPrice, change, time 값은 로딩 안되서 걍 막아놈;;; 왜 안됨
//						try {
//							// colsingPrice, change, time 값은 로딩 완료 후 확인가능하다. 100초 기다리고 안되면 걍 넘어가~~
//							myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='closing-prices-header']/div[1]/div[1]/span[2]")));
//								
//							// closingPrice
//							webElement = driver.findElement(By.xpath("//*[@id='closing-prices-header']/div[1]/div[1]/span[2]"));
//							closingPrice = webElement.getText();
//							resultDetail += closingPrice+"\t";
//			
//							// change
//							webElement = driver.findElement(By.xpath("//*[@id='closing-prices-header']/div[1]/div[2]/span[2]"));
//							change = webElement.getText();
//							resultDetail += change+"\t";
//							
//							// time
//							webElement = driver.findElement(By.xpath("//*[@id='closing-legend-header']"));
//							time = webElement.getText();
//							resultDetail += time+"\t";
//						} catch (Exception e) {
//							resultDetail += urlListTest.get(i)+"\t closingPrice Connection Error \n";
//							resultDetail += urlListTest.get(i)+"\t change Connection Error \n";
//							resultDetail += urlListTest.get(i)+"\t time Connection Error \n";
//							logger.log(logger.getLevel(), resultDetail);
//						
//						}
						
									
						// 밑으로 잠깐 이동하려고 만듬
						webElement = driver.findElement(By.xpath("//*[@id='form-reports-header']/div[1]/section[3]/div[1]/div[2]"));
						js.executeScript("arguments[0].scrollIntoView();", webElement);
						
						
						
						// competingETSs를 가져온다
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
						logger.log(logger.getLevel(), "파싱 진행중 {"+i+"} : "+ urlListTest.get(i));
						saveCount++;
						
						
						// 100개마다 txt파일에 저장
						if(saveCount == 100){
							logger.log(logger.getLevel(), "데이터 중간저장");
							logger.log(logger.getLevel(), resultDetail);
							
							bos.write(resultDetail.getBytes());
							bos.flush();
							saveCount = 0;
							resultDetail ="";
						}
					
					} catch (Exception e) {
						resultDetail += urlListTest.get(i)+"\tConnection Error \n";
						logger.log(logger.getLevel(), resultDetail);
						myWaitVar = null;
					}
		
				}	
			}
			
		} catch (Exception e) {
			logger.log(logger.getLevel(), "Exception .. !");

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



