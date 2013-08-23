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
    private static AbstractJobQueue  jobQueue;
    private static VGLPollingJobQueueManager queueManager = null;


    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private VGLPollingJobQueueManager(AbstractJobQueue jobQueue) {
        VGLPollingJobQueueManager.jobQueue = jobQueue;
        this.initialize();
    }

    private VGLPollingJobQueueManager(){
        VGLTimePollQueue queue= new VGLTimePollQueue();
        VGLPollingJobQueueManager.queueManager=new VGLPollingJobQueueManager(queue);
    }

    private void initialize() {
        scheduler.scheduleAtFixedRate(jobQueue, 10, 10, TimeUnit.MINUTES);

    }

    public AbstractJobQueue getQueue() {
        return VGLPollingJobQueueManager.jobQueue;
    }

    public void addJobToQueue(Job job){
        this.getQueue().addJob(job);
    }

    public static VGLPollingJobQueueManager getInstance(){
        if(VGLPollingJobQueueManager.queueManager==null){
            return new VGLPollingJobQueueManager();
        }else{
            return VGLPollingJobQueueManager.queueManager;
        }
    }
}
