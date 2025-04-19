package com.demoqa.test;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

public class BookStoreTest {
	
	private static final String APPLICATION_URL = "https://demoqa.com/login";
	private static final String USERNAME = "shanew";
	private static final String PASSWORD = "Shane@123";
	private static final String BOOK_TITLE_TEXT = "Learning JavaScript Design Patterns";
		
	//all the locators
	private static final By USERNAME_INPUT = By.id("userName");
	private static final By PASSWORD_INPUT = By.id("password");
	private static final By LOGIN_BUTTON = By.id("login");
	private static final By LOGOUT_BUTTON = By.xpath("//button[text()= 'Log out']");
	private static final By USERNAME_LABEL = By.id("userName-value");
	private static final By GOTO_STORE_BUTTON = By.id("gotoStore");
	private static final By SEARCH_BOX = By.id("searchBox");
	private static final By SEARCH_BUTTON = By.id("basic-addon2");
	private static final By BOOK_TITLE_LINK = By.xpath("//a[text()='" + BOOK_TITLE_TEXT + "']");
	private static final By AUTHOR_TEXT = By.xpath("//div[contains(text(), 'Addy Osmani')]");
	private static final By PUBLISHER_TEXT = By.xpath("//div[contains(text(), \"O'Reilly Media\")]");
	
	
	
	public static void main(String[] args) throws InterruptedException {
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		String timestamp = LocalDateTime.now().format(formatter);
		
		String filePath = "target/book_details_" + timestamp + ".txt";

		WebDriver driver = new ChromeDriver();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		JavascriptExecutor js = (JavascriptExecutor) driver;

		// Step-1 Login using the newly created user.
		driver.get(APPLICATION_URL);
		driver.manage().window().maximize();
		driver.findElement(USERNAME_INPUT).sendKeys(USERNAME);
		driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
		driver.findElement(LOGIN_BUTTON).click();
		Thread.sleep(1000);

		// step-2 Upon successful login, Validate username and logout button.
		try {
			WebElement usernameElement = wait
					.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_LABEL));
			String validate_username = usernameElement.getText();
			Assert.assertEquals("shanew", validate_username);

			WebElement logout_btn = wait
					.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON));
			Assert.assertTrue(logout_btn.isEnabled(), "Logout button should be enabled");
			System.out.println("Login successful. Logout button is enabled.");

			
			// step-3 - Click on bookstore button
			try {
				WebElement goToStoreButton = wait
						.until(ExpectedConditions.elementToBeClickable(GOTO_STORE_BUTTON));
				goToStoreButton.click();
			} catch (org.openqa.selenium.ElementClickInterceptedException e) {
				System.err.println("Go To Store button click intercepted by iframe. Attempting to handle...");

				// Remove ad iframe
				try {
					WebElement adIframe = driver
							.findElement(By.id("google_ads_iframe_/21849154601,22343295815/Ad.Plus-Anchor_0"));
					js.executeScript("arguments[0].remove();", adIframe);
					System.out.println("Ad iframe removed.");
				} catch (NoSuchElementException ignore) {
					System.out.println("Ad iframe not found.");
				}

				// Remove ad container div
				try {
					WebElement adContainer = driver
							.findElement(By.cssSelector("div[id^='google_ads_iframe_'][id$='__container__']"));
					js.executeScript("arguments[0].remove();", adContainer);
					System.out.println("Ad container div removed.");
				} catch (NoSuchElementException ignore) {
					System.out.println("Ad container not found.");
				}

				// Retry click after removing ad overlays
				WebElement goToStoreButtonRetry = wait
						.until(ExpectedConditions.elementToBeClickable(GOTO_STORE_BUTTON));
				js.executeScript("arguments[0].scrollIntoView(true);", goToStoreButtonRetry);
				Thread.sleep(500);
				goToStoreButtonRetry.click();

			} catch (Exception ex) {
				System.err.println("Error handling ad overlay: " + ex.getMessage());
			}

			
			// step- 4 - Search "Learning JavaScript Design Patterns"
			WebElement searchbox = wait
					.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_BOX));
			js.executeScript("arguments[0].scrollIntoView(true);", searchbox);
			Thread.sleep(1000);
			searchbox.sendKeys("Learning JavaScript Design Patterns");

			
			// step- 5 - Validate the search result to contain this book.
			driver.findElement(SEARCH_BUTTON).click();
			Thread.sleep(1000);

			WebElement searchResultTitle = driver
					.findElement(By.xpath("//a[text()='Learning JavaScript Design Patterns']"));
			Assert.assertTrue(searchResultTitle.isDisplayed(),
					"Search result should contain 'Learning JavaScript Design Patterns'");
			System.out.println(" Learning JavaScript Design Patterns book is displayed in search box");

			
			// step 6 - Print Title, Author and Publisher into a file.
			String bookTitle = driver.findElement(BOOK_TITLE_LINK)
					.getText();
			System.out.println("Title of the book: " + bookTitle);
			String author = driver.findElement(AUTHOR_TEXT).getText();
			System.out.println("author of the book: " + author);
			String publisher = driver.findElement(PUBLISHER_TEXT).getText();
			System.out.println("publisher of the book: " + publisher);

			
			try (FileWriter fileWriter = new FileWriter(filePath)) {
				fileWriter.write("Title: " + bookTitle + "\n");
				fileWriter.write("Author: " + author + "\n");
				fileWriter.write("Publisher: " + publisher + "\n");
				System.out.println("Book details written to book_details.txt");
			} catch (IOException e) {
				System.err.println("Error writing to file: " + e.getMessage());
			}

			// step 7 - Click on log out
			WebElement logoutButtonFinal = wait
					.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON));
			logoutButtonFinal.click();

		} catch (org.openqa.selenium.TimeoutException e) {
			System.err.println("Timeout occurred: " + e.getMessage());

		} finally {
			driver.quit();
		}
	}

}
