package org.auscope.portal.ui;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test search layer panel by keyword in main dev portal.
 * 
 * @author Rini Angreani
 *
 */
public class SearchLayerTest {
    protected WebDriver driver = null;
    protected String portal_url = null;

    @Parameters({ "browser", "version", "port", "portal_url" })
    @BeforeClass
    public void setup(String browser, String version, String port, String portal)
            throws MalformedURLException {
        this.driver = SeleniumTestUtil.getWebDriver(browser, version, port);
        this.portal_url = portal;
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }

    @BeforeMethod
    public void openPage() {
        // open portal
        driver.get(portal_url);
    }

    @Test
    /**
     * Test typing keyword in the search box and pressing enter.
     */
    public void testSearchAndEnter() {
        // search box
        WebElement searchBox = driver.findElement(By.id("hh-searchfield-Featured-inputEl"));

        // type "tenement" in search
        searchBox.sendKeys("tenement");
        searchBox.sendKeys(Keys.ENTER);

        checkTenementLayer();

    }

    public void checkTenementLayer() {
        // tenement layer header
        List<WebElement> results = driver.findElements(By.xpath("//div[text() = 'Tenements (1 item)']"));
        
        // Verify that there is 1 match
        assertEquals(1, results.size());

        WebElement layerGroup = results.get(0);  
        // Find that toggle button next to the panel header
        WebElement toggle = layerGroup.findElement(By.xpath("../following-sibling::div/img[@class='x-tool-img x-tool-expand-bottom']"));

        // expand the toggle
        toggle.click();

        // Work out the generated id i.e. 6530
        // First get the recordgrouppanel
        // <div class="x-box-target" id="recordgrouppanel-6530_header-targetEl" role="presentation" style="width: 350px;" data-ref="targetEl">
        String elementId = layerGroup.findElement(By.xpath("../..")).getAttribute("id");
        int start = elementId.indexOf("-") + 1;
        int end = elementId.indexOf("_");
        int generatedIdNo = Integer.valueOf(elementId.substring(start, end));
        
        // the layer should be inside a div with the next generated id increment
        // <div class="x-box-target" id="recordrowpanel-6531_header-targetEl" role="presentation" style="width: 358px;" data-ref="targetEl">
        // but it's too hard to test the layer count with extjs, so we just test the first result (there should only be 1 result in this case)
        WebElement layer = driver
                .findElement(By.id("recordrowpanel-" + ++generatedIdNo
                        + "_header-targetEl"));
        // it is the Mineral Tenements inside
        assertEquals("Mineral Tenements", layer.getAttribute("innerText").trim());
    }

    @Test
    /**
     * Test typing keyword in the search box and press search icon.
     */
    public void testSearchAndClickIcon() {
        // search box
        WebElement searchBox = driver.findElement(By.id("hh-searchfield-Featured-inputEl"));

        // type "tenement" in search
        searchBox.sendKeys("tenement");

        // Find the first (and only) search button on the page and click it
        WebElement searchIcon = new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOf(
                driver.findElement(By.id("hh-searchfield-Featured-trigger-search"))));

        searchIcon.click();

        checkTenementLayer();

    }

}
