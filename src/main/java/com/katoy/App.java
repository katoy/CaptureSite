
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
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TakesScreenshot;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
// import org.openqa.selenium.firefox.ChromeDriver;
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
    //String START_URL = "http://coffeescript.org/";
    //String PRE_NAME  = "http://coffeescript.org/";
    String START_URL = "http://www.sinatrarb.com/";
    String PRE_NAME  = "http://www.sinatrarb.com/";

    protected java.util.HashSet<String> alreadyListed;
    protected java.util.HashSet<URL> alreadyListedURL;
    protected java.util.Queue<String> queue;
    protected java.util.ArrayList<String[]> screens;
    protected int screenID = 0;
    protected WebDriverBackedSelenium selenium;
    protected WebDriver driver;

    public static void main(String[] args) {
	App app = new App();
        app.sub();
    }


    public void sub() {

	// See http://www.viaboxxsystems.de/start-your-webtests-with-selenium2-maven-testng-now
        //Proxy myProxy = new Proxy();
        //myProxy.setHttpProxy("proxy:80");

	//FirefoxProfile myProfile = new FirefoxProfile();
        //myProfile.setProxyPreferences(myProxy);

        // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface, 
        // not the implementation.
        // WebDriver driver = new FirefoxDriver(myProfile);  // new FirefoxDriver();

	// cd /Applications/Firefox.app/Contents/MacOS
	// ditto --arch i386 firefox-bin  firefox-bin-x
	System.setProperty("webdriver.firefox.bin",
			   "/Applications/Firefox.app/Contents/MacOS/firefox-bin-x");
        this.driver = new FirefoxDriver();
	this.driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);

        // System.setProperty("webdriver.chrome.driver", 
	//		   "src/main/resources/drivers/chrome/chromedriver-mac");
	// WebDriver driver = new ChromeDriver();
	
	this.selenium = new WebDriverBackedSelenium(this.driver, "");
	this.alreadyListed = new java.util.HashSet<String>();
	this.alreadyListedURL = new java.util.HashSet<URL>();
	this.queue = new java.util.LinkedList<String>();
	this.screens = new java.util.ArrayList<String[]>();

	this.selenium.setSpeed("2000");
	crawl(START_URL);

        //Close the browser
        driver.quit();
    }

    public void crawl(String startingUrl) {

	this.alreadyListed.add(startingUrl);
	this.queue.add(startingUrl);
	try {
	    this.alreadyListedURL.add(new URL(startingUrl));
	} catch (Exception e) {
	    System.out.println(e);
	}

	String newAddress;
	while ((newAddress = this.queue.poll())!=null) {
	    try {
		processPage(newAddress);
		//here you may add a code to do anything you wish with the page
		System.out.println("" + this.screenID + ":" + newAddress);
		save_page(newAddress);
	    } catch (Exception e) {
		System.out.println(e);
	    }
	}

	try {
	    FileWriter filewriter = new FileWriter("./screens/00-list.txt");
	    java.util.Iterator it = this.screens.iterator();
	    while (it.hasNext()) {
		String[] val = (String[])(it.next());
		filewriter.write(val[1] + ": " + val[0] + "\n");
	    }
	    filewriter.close();
	} catch(IOException e) {
	    System.out.println(e);
	}
    }

    
    protected void processPage(String urlStr) throws Exception {
	URL url = new URL(urlStr);
	URLConnection connection = url.openConnection();
	if (connection.getContentType().indexOf("text") < 0) {
	    return;
	}
	
	this.selenium.open(urlStr);
	
	int linkCount = this.driver.findElements(By.tagName("a")).size();
	// System.out.println(linkCount);
	for (int i = 0; i < linkCount - 1; i++) {
	    String href = this.selenium.getEval("document.links["+i+"].href");
	    URL javaURL = new URL(href);
	    if (isAddressValid(href, javaURL)) {
		if (!this.alreadyListed.contains(href)) {
		    this.queue.add(href);
		}
		this.alreadyListed.add(href);
		this.alreadyListedURL.add(javaURL);
	    }
	}
    }

    protected boolean isAddressValid(String href, URL javaURL) throws Exception {
	if ((href == null) || !href.startsWith(PRE_NAME) ) {
	    return false;
	}

	if (href.indexOf("/reference.html") >= 0) {
	    return false;
	}
	if (isSamed(javaURL)) {
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
    private void save_page(String url) {
	String name = String.format("./screens/test-%05d.png", screenID);
	String[] val = {url, name};
	screens.add(this.screenID, val);
	this.screenID += 1;

        try {
	  File srcFile = ((TakesScreenshot)this.driver).getScreenshotAs(OutputType.FILE);
	  FileUtils.copyFile(srcFile, new File(name));
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

