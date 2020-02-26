package webCatch;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class craw {


  
  public static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
  public static final String WEB_DRIVER_PATH = "D:/Data/Dev/workspace/neon-workspace/webCatch/src/webCatch/chromedriver_80v.exe";
  
  
  public static void main(String[] args) throws ClientProtocolException, IOException {


		System.out.println(" Start Date : " + getCurrentData());

		craw selTest = new craw();
        selTest.crawl();


        

	    System.out.println(" End Date : " + getCurrentData());

  }
  
  
  //WebDriver
  private WebDriver driver;
  
  private WebElement webElement;
  
  private List<WebElement> weList;
  
  ArrayList<String> urlList;
  
  private String base_url;
  
  public craw() {
      super();

      //System Property SetUp
      System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
      
              
      //Driver SetUp
       ChromeOptions options = new ChromeOptions();
       options.setCapability("ignoreProtectedModeSettings", true);
       driver = new ChromeDriver(options);
      
       base_url = "https://www.etf.com/etfanalytics/etf-finder";
      
      
      
  }
 
    public void crawl() {
 
        try {
            //get page (= 브라우저에서 url을 주소창에 넣은 후 request 한 것과 같다)
            driver.get(base_url);
             
//            //iframe으로 구성된 곳은 해당 프레임으로 전환시킨다.
//            driver.switchTo().frame(driver.findElement(By.id("loginForm")));
//            
//            //iframe 내부에서 id 필드 탐색
//            webElement = driver.findElement(By.id("id"));
//            String daum_id ="your id";
//            webElement.sendKeys(daum_id);
//            
//            //iframe 내부에서 pw 필드 탐색
//            webElement = driver.findElement(By.id("inputPwd"));
//            String daum_pw ="your pw";
//            webElement.sendKeys(daum_pw);
//            
// 
//            //로그인 버튼 클릭
//            webElement = driver.findElement(By.id("loginSubmit"));
//            webElement.submit();
            
//            webElement = driver.findElement(By.id("results"));
            
            WebDriverWait myWaitVar = new WebDriverWait(driver, 30);
            
            myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.id("results")));
            Actions actions = new Actions(driver); 
            
//            webElement = driver.findElement(By.xpath("//*[@id='results_display']/div/div[4]/button"));
          
    
            //actions.moveToElement(webElement).click().build().perform();
            
              
            
            
            webElement = driver.findElement(By.id("totalPages"));
            
            String totalPages[] = webElement.getText().split(" ");
            
            int pageNum = Integer.parseInt(totalPages[1]);
            
            System.out.println(pageNum);
            
            
            String href = "";
            
            
            urlList = new ArrayList<>();
            
            Thread.sleep(5000);
            
            for(int i = 0; i < pageNum; i++){
            	
     
            	  weList = driver.findElements(By.xpath(".//*[@id='finderTable']/tbody/tr"));
            	  Thread.sleep(10);
            	  
                for(int l = 1; l <= weList.size(); l++){
                	webElement = driver.findElement(By.xpath(".//*[@id='finderTable']/tbody/tr["+l+"]/td[1]/a"));
                	//webElement = driver.findElement(By.xpath(".//*[@id='finderTable']/tbody/tr["+l+"]/td[1]"));
                	
                	href = webElement.getAttribute("href");
                	
            
                	urlList.add(href);
                	
                	System.out.println(href);
//                	System.out.println(webElement.getText());
                	
                }
                
              
                
                webElement = driver.findElement(By.xpath("//*[@id='nextPage']"));
                actions.moveToElement(webElement).click().build().perform();
               
            }
            
           
            System.out.println(urlList);
            
            Thread.sleep(10000);
    
        } catch (Exception e) {
            
            e.printStackTrace();
        
        } finally {
            driver.close();
        }
 
    }



	public static String getCurrentData(){
	
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	
	    return sdf.format(new Date());
	
	}

}



