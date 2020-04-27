package org.auscope.portal.ui.mobile;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.util.List;

import org.auscope.portal.ui.SearchLayerTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test search layer panel by keyword in mobile portal.
 * 
 * @author Rini Angreani
 *
 */
public class SearchLayerMobileTest extends SearchLayerTest {

    @Parameters({ "browser", "version", "port", "portal_url" })
    @BeforeClass
    public void setup(String browser, String version, String port, String portal)
            throws MalformedURLException {
        super.setup(browser, version, port, portal);
    }

    @AfterClass
    public void tearDown() {
        super.tearDown();
    }

    @BeforeMethod
    public void openPageAndClickMenuToggle() {
        // open portal
        super.openPage();
        // bring up menu toggle
        WebElement menuToggle = new WebDriverWait(driver, 30).until(ExpectedConditions.elementToBeClickable(
                driver.findElement(By.cssSelector(".menu-icon-toggle"))));
        menuToggle.click();
        // bring up search box
        ((JavascriptExecutor)driver).executeScript("document.querySelectorAll('.search-icon-border-right')[0].click()");        
    }

    @Test
    /**
     * Test typing keyword in the search box and pressing enter.
     */
    public void testSearchAndEnter() {
        // give 60s to load up search box 
        WebElement searchBox = new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(
                driver.findElement(By.id("main-search"))));

        // type "tenement" in search
        searchBox.sendKeys("tenement");
        searchBox.sendKeys(Keys.ENTER);

        checkTenementLayer();
    }
    
    @Test
    /**
     * Test typing keyword in the search box and press search icon.
     */
    public void testSearchAndClickIcon() {
        // give 60s to load up search box 
        WebElement searchBox = new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOf(
                driver.findElement(By.id("main-search"))));

        // type "tenement" in search
        searchBox.sendKeys("tenement");

        // Find the first (and only) search button on the page and click it
        WebElement searchIcon = driver
                .findElement(By.cssSelector("form input#main-search + span"));

        searchIcon.click();

        checkTenementLayer();
    }

    public void checkTenementLayer() {
        // panel header
        List<WebElement> results = driver
                .findElements(By.cssSelector(".panel-title"));

        // Verify that there is 2 match
        assertEquals(2, results.size());

        // result should be tenement layer
        WebElement layerGroup = results.get(0);
        assertEquals("Tenements", layerGroup.getText());

        // expand the layer group
        WebElement toggle = driver
                .findElement(By.className("accordion-toggle"));

        toggle.click();

        // there should only be 2 layer in the group
        List<WebElement> rows = driver.findElements(By.className("layer-row"));
        assertEquals(2, rows.size());

        WebElement layer = rows.get(0);
        assertEquals("Mineral Tenements", layer.getAttribute("innerText").trim());
    }

}
