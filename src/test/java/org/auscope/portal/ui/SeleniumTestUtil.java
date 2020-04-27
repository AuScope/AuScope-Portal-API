package org.auscope.portal.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SeleniumTestUtil {
    
    public static WebDriver getWebDriver(String browser, String version, String port) throws MalformedURLException {
        WebDriver driver;       
        DesiredCapabilities capability = new DesiredCapabilities();
        if (version != null && !version.isEmpty()) {
            // set browser version if specified
            capability.setVersion(version);
        }
        capability.setBrowserName(browser);
        String hubHost;
        Object hub = System.getProperty("hub");        
        if (hub != null) {
            // Use specified hub
            hubHost = hub.toString();
        } else {
            // defaults to localhost
            hubHost = "localhost";
        }
        
        driver = new RemoteWebDriver(new URL("http://".concat(hubHost)
                .concat(":").concat(port).concat("/wd/hub")), capability);

        return driver;
    }
}
