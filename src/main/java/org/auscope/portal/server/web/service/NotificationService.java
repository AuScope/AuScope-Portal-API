package org.auscope.portal.server.web.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.auscope.portal.core.services.PortalServiceException;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Service class for pulling simple notifications from Twitter
 * @author Josh Vote (CSIRO)
 *
 */
public class NotificationService {
    private Twitter twitter;
    private String notificationAccount;
    private List<Notification> cache;
    private long cachePopulated;
    private long cacheMaxAge = 15 * 60 * 1000; // 15 minutes
    private int maxTweetAgeDays = 14; //How many days in the past to look back for notifications
    private boolean enableTwitter = false;

    public NotificationService(boolean enableTwitter, String notificationAccount, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
    	this.enableTwitter = enableTwitter;
        if (!this.enableTwitter) {
            return;
        }
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
            .setOAuthConsumerKey(consumerKey)
            .setOAuthConsumerSecret(consumerSecret)
            .setOAuthAccessToken(accessToken)
            .setOAuthAccessTokenSecret(accessTokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
        this.notificationAccount = notificationAccount;
        this.cache = null;
        this.cachePopulated = 0L;
    }

    /**
     * How long the cache remains valid in milliseconds
     * @return
     */
    public long getCacheMaxAge() {
        return cacheMaxAge;
    }

    /**
     * How long the cache remains valid in milliseconds
     * @param cacheMaxAge
     */
    public synchronized void setCacheMaxAge(long cacheMaxAge) {
        this.cacheMaxAge = cacheMaxAge;
    }

    /**
     * How old a tweet can be in days before it's ignored as a notification
     * @return
     */
    public int getMaxTweetAgeDays() {
        return maxTweetAgeDays;
    }

    /**
     * How old a tweet can be in days before it's ignored as a notification
     * @param maxTweetAgeDays
     */
    public synchronized void setMaxTweetAgeDays(int maxTweetAgeDays) {
        this.maxTweetAgeDays = maxTweetAgeDays;
    }

    /**
     * Gets the cached set of notifications (if valid) otherwise makes a synchronous
     * request to Twitter to update the cache and then returns that value
     *
     * Only grabs notifications from the past maxTweetAgeDays days
     *
     * @return
     */
    public synchronized List<Notification> getRecentNotifications() throws PortalServiceException {

        if (!this.enableTwitter) {
            return null;
        }
        //If cache is valid, use that
        Calendar calendar = Calendar.getInstance();
        if (cache != null &&
            (calendar.getTimeInMillis() - cachePopulated) < cacheMaxAge) {
            return cache;
        }

        try {
            //Fire a query for tweets from the account made in the last X days
            Query query = new Query("from:" + notificationAccount);

            DateTimeFormatter format = DateTimeFormatter.ofPattern("YYYY-MM-dd");
            query.since(LocalDate.now().minusDays(maxTweetAgeDays).format(format));

            QueryResult result = twitter.search(query);
            cache = result.getTweets()
                .stream()
                .map(status -> new Notification(status.getText(), status.getUser().getScreenName(), status.getCreatedAt()))
                .collect(Collectors.toList());
            cachePopulated = Calendar.getInstance().getTimeInMillis();
            return cache;
        } catch (TwitterException ex) {
            throw new PortalServiceException("Exception accessing notifications for " + notificationAccount + " : " + ex.getMessage(), ex);
        }catch (NullPointerException ex) {
            return null;
        }

    }

    /**
     * Simple representation of a notification
     * @author Josh Vote (CSIRO)
     *
     */
    public static class Notification implements Serializable {
        private static final long serialVersionUID = -787217258187312614L;
        private String content;
        private String author;
        private Date time;

        public Notification() {
            super();
        }
        public Notification(String content, String author, Date time) {
            super();
            this.content = content;
            this.author = author;
            this.time = time;
        }
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
        public String getAuthor() {
            return author;
        }
        public void setAuthor(String author) {
            this.author = author;
        }
        public Date getTime() {
            return time;
        }
        public void setTime(Date time) {
            this.time = time;
        }
    }
}
