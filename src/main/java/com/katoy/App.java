
/** 
 * mvn clean test
 * 
 * See http://qaftw.wordpress.com/category/selenium-2/
 */

package com.katoy;

import org.apache.commons.io.FileUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.JavascriptExecutor;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import org.openqa.selenium.chrome.ChromeDriver;

// import org.openqa.selenium.safari.SafariDriver;

import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class App {
    // EDIT POINT
    //=========================================================
    final boolean USE_PROXY = false;
    final int MAX_CACPURE = 500;
    final boolean IGNORE_FRAGMENT = true; 
    // final boolean IGNORE_FRAGMENT = false;  // when rafhaep, documentation 
    
    // String START_URL = "http://raphaeljs.com/";
    String START_URL = "http://coffeescript.org/";
    // String START_URL = "http://www.sinatrarb.com/";
    // String START_URL = "http://www.google.co.jp/";
    // String START_URL = "https://github.com/katoy/CaptureSite";
    // String START_URL = "http://www.youtube.com/results?search_query=%E8%A5%BF%E6%9D%91%E7%94%B1%E8%B5%B7%E6%B1%9F"; // 西村由起江
    
    String PRE_NAME  = START_URL;
    
    protected java.util.HashSet<String> alreadyListed;
    protected java.util.HashSet<URL> alreadyListedURL;
    protected java.util.Queue<String> queue;
    protected java.util.ArrayList<String[]> screens;
    protected int screenID = 0;
    protected WebDriver driver;
    
    public static void main(String[] args) {
	App app = new App();
	app.capture();
    }
    
    public void capture() {	
	// See http://www.viaboxxsystems.de/start-your-webtests-with-selenium2-maven-testng-now
	if (USE_PROXY) {
	    Proxy myProxy = new Proxy();
	    myProxy.setHttpProxy("proxy:80");
	    FirefoxProfile myProfile = new FirefoxProfile();
	    myProfile.setProxyPreferences(myProxy);	
	    
	    // Create a new instance of the Firefox driver
	    // Notice that the remainder of the code relies on the interface, 
	    // not the implementation.
	    this.driver = new FirefoxDriver(myProfile);  // new FirefoxDriver();
	} else {	    
	    // ======== firefox
	    // cd /Applications/Firefox.app/Contents/MacOS
	    // ditto --arch i386 firefox-bin  firefox-bin-x
	    System.setProperty("webdriver.firefox.bin",
	    		       "/Applications/Firefox.app/Contents/MacOS/firefox-bin-x");
	    this.driver = new FirefoxDriver();
	}
	
	// ========= chreome
	//System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, 
	//		   "/Users/youichikato/github/CaptureSites/driver/chromedriver");
	//this.driver = new ChromeDriver();
	
	// ========= safari
	// this.driver = new SafariDriver();
	
	try {
	    this.driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
	    
	    this.alreadyListed = new java.util.HashSet<String>();
	    this.alreadyListedURL = new java.util.HashSet<URL>();
	    this.queue = new java.util.LinkedList<String>();
	    this.screens = new java.util.ArrayList<String[]>();
	    
	    crawl(START_URL);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println(e);	    
	} finally {
	    if (this.driver != null) {		
		//Close the browser
		this.driver.quit();
	    }
	}
    }
    
    public void crawl(String startingUrl) throws Exception {
	
	this.alreadyListed.add(startingUrl);
	this.queue.add(startingUrl);
	this.alreadyListedURL.add(new URL(startingUrl));
	
	String newAddress;
	while ((newAddress = this.queue.poll())!=null) {
	    try {
		processPage(newAddress);
		//here you may add a code to do anything you wish with the page
		System.out.println("" + this.screenID + ":" + newAddress);
		save_page(newAddress);
	    } catch (Exception e) {
		e.printStackTrace();
		System.out.println(e);
	    }

	    if (this.screenID >= MAX_CACPURE) {
		break;
	    }
	}

	FileWriter filewriter = null;
	try {
	    filewriter = new FileWriter("./screens/00-list.txt");
	    java.util.Iterator it = this.screens.iterator();
	    while (it.hasNext()) {
		String[] val = (String[])(it.next());
		filewriter.write(val[1] + ": " + val[0] + "\n");
	    }
	} catch(IOException e) {
	    throw e;	    
	} finally {
	    if (filewriter != null) {
		filewriter.close();
	    }	    
	}
    }

    protected void processPage(String urlStr) throws Exception {
	URL url = new URL(urlStr);
	URLConnection connection = url.openConnection();
	String contentType = connection.getContentType();
	if (contentType != null && contentType.indexOf("text") < 0) {
	    return;
	}

	JavascriptExecutor js = (JavascriptExecutor)(this.driver);
	
	// this.selenium.open(urlStr);
	this.driver.get(urlStr);
	
	// int linkCount = this.driver.findElements(By.tagName("a")).size();
	// System.out.println(linkCount);
	int linkCount = 0;
	try {
	    Long  c = (Long)(js.executeScript("return document.links.length"));
	    linkCount = c.intValue();
	} catch (Exception e) {
	    System.out.println(e);
	}

	for (int i = 0; i < linkCount - 1; i++) {
	    String href = null;
	    try {
		// href = this.selenium.getEval("document.links["+i+"].href");
		href = (String)(js.executeScript("return document.links["+i+"].href"));
	    } catch (Exception e) {
		System.out.println(e);
	    }

	    if ((href != null) && isAddressValid(href)) {
		if (!this.alreadyListed.contains(href)) {
		    this.queue.add(href);
		}
		this.alreadyListed.add(href);
		this.alreadyListedURL.add(new URL(href));
	    }
	}
    }

    protected boolean isAddressValid(String href) throws Exception {
	if ((href == null) || !href.startsWith(PRE_NAME) ) {
	    return false;
	}

	// google
	if (href.startsWith("http://www.google.co.jp/news/")) {
	    return false;
	}
	if (href.startsWith("http://www.google.co.jp/products/")) {
	    return false;
	}

	// github
	if (href.startsWith("https://github.com/katoy/CaptureSite/issues")) {
	    return false;
	}

	// rapahel
	if (IGNORE_FRAGMENT && isSamed(new URL(href))) {
	    return false;
	}

	// youtube
	if (href.indexOf("&search_filter=") > 0) {
	    return false;
	}

	return true;
    }

    // #name のフラグメントを無視して比較判定する。
    protected boolean isSamed(URL url) throws Exception {
        java.util.Iterator it = this.alreadyListedURL.iterator();
        while (it.hasNext()) {
	    if (url.sameFile((URL)(it.next()))) {
		return true;
	    }
        }
	return false;
    }

    // スクリーンショットを保存する。
    private void save_page(String url)  throws Exception {
	String name = String.format("./screens/test-%05d.png", screenID);
	String[] val = {url, name};
	screens.add(this.screenID, val);
	this.screenID += 1;
	
	File srcFile = ((TakesScreenshot)this.driver).getScreenshotAs(OutputType.FILE);
	FileUtils.copyFile(srcFile, new File(name));
    }
}
