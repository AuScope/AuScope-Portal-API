package org.auscope.portal.server.vegl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.auscope.portal.core.util.structure.AbstractJobQueue;
import org.auscope.portal.core.util.structure.Job;

/**
 * A reusuable utility class that accepts a abstractJobQueue and will poll for
 * jobs and attempt to run the jobs in the queue until the queue is empty.
 *
 *
 * @author tey006
 */

public class VGLPollingJobQueueManager {
    AbstractJobQueue jobQueue;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public VGLPollingJobQueueManager(AbstractJobQueue jobQueue) {
        this.jobQueue = jobQueue;
        this.initialize();
    }

    public void initialize() {
        scheduler.scheduleAtFixedRate(jobQueue, 30, 30, TimeUnit.MINUTES);

    }

    public AbstractJobQueue getQueue() {
        return jobQueue;
    }

    public void addJobToQueue(Job job){
        this.getQueue().addJob(job);
    }
}
