package com.project.webBuilder.common.util;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Screenshot {

    public static void takeScreenshot(String url, String savePath) throws IOException {
        // 크롬 드라이버 위치 설정
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        // Headless 옵션 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);
        try {
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.get(url);

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destination = new File(savePath);
            ImageIO.write(ImageIO.read(screenshot), "png", destination);

        } finally {
            driver.quit();
        }
    }
}