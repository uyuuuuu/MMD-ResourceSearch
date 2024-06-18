package com.uyuu.mmd_resource_search;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Scrap {
	private String url;
	public Scrap(String url) {
		this.url = url;
	}
	public String getBowlDL() {
		try {
			final String PATH = "C:/PROGRAMS/Eclipse/workspace/data_engineering/bin/chromedriver.exe";
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless=new");
			System.setProperty("webdriver.chrome.driver", PATH);
	        WebDriver driver = new ChromeDriver(options);
	        //GET
	        driver.get(url);
			Thread.sleep(800);
			WebElement dlCount = driver.findElement(By.xpath("/html/body/main/div/div/div[1]/div[1]/article/div[3]/div[1]"));
			String dl = dlCount.getText();
			driver.quit();
			return dl;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch(NoSuchElementException e){
			return "非公開ページの可能性があります";
		}
	}
}
