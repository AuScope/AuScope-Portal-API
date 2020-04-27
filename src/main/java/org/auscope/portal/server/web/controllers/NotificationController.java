package org.auscope.portal.server.web.controllers;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.server.web.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Provides a controller interface into some basic administration functionality/tests
 * e.g. pulling simple notifications from Twitter
 *
 * @author Josh Vote
 *
 */
@Controller
public class NotificationController extends BasePortalController {

    private final Log log = LogFactory.getLog(getClass());

    private NotificationService notificationService;

    /**
     * Creates a new instance of this class
     */
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Gets the set of (cached) notifications for this service
     *
     * @return
     * @throws URISyntaxException
     */
    @RequestMapping("/getNotifications.do")
    public ModelAndView getNotifications() throws URISyntaxException {

        try {
            List<NotificationService.Notification> ns = notificationService.getRecentNotifications();

            return generateJSONResponseMAV(true, ns, "");
        } catch (Exception ex) {
            log.debug("Unable to get notifications: " + ex.getMessage());
            log.error("Error:", ex);
            return generateJSONResponseMAV(false);
        }
    }
}
