package org.auscope.portal.server.vegl;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.util.structure.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * A reusuable utility class that accepts a abstractJobQueue and will poll for
 * jobs and attempt to run the jobs in the queue until the queue is empty.
 *
 *
 * @author tey006
 */
@Component
public class VGLPollingJobQueueManager extends QuartzJobBean{
    private static VGLTimePollQueue  jobQueue=null;



    public VGLPollingJobQueueManager(VGLTimePollQueue jobQueue) {
        VGLPollingJobQueueManager.jobQueue = jobQueue;

    }

    public VGLPollingJobQueueManager(){
        if(VGLPollingJobQueueManager.jobQueue==null){
            VGLPollingJobQueueManager.jobQueue = new VGLTimePollQueue();
        }

    }


    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {
        jobQueue.run();

    }

    public VGLTimePollQueue getQueue() {
        return VGLPollingJobQueueManager.jobQueue;
    }

    public void addJobToQueue(Job job){
        this.getQueue().addJob(job);
    }


    public class VGLTimePollQueue{

        private final Log logger = LogFactory.getLog(VGLTimePollQueue.class);

        //VT: unable to decide between a ConcurrentLinkedQueue vs a LinkedBlockingQueue.
        protected ConcurrentLinkedQueue<Job> queue;


        public VGLTimePollQueue(){
            queue= new ConcurrentLinkedQueue<Job>();

        }

        public VGLTimePollQueue(ConcurrentLinkedQueue<Job> queue){
            this.queue= queue;

        }


        public Job removeJobCleanUp(Exception e){
            Job j= this.removeJob();
            if(j instanceof VGLQueueJob){
                ((VGLQueueJob)j).updateErrorStatus(e);
            }
            return j;
        }




        public boolean hasJob(){
            return !queue.isEmpty();
        }




        public boolean runJob() throws PortalServiceException{
            return this.queue.peek().run();
        }



        public Job removeJob(){
            return this.queue.remove();
        }





        public void addJob(Job job){
            this.queue.add(job);
        }




        public void run(){
            this.manageJob();
        }



        public int size(){
            return this.queue.size();
        }



        public void clear(){
            this.queue.clear();
        }



        public boolean remove(Job o){
            return this.queue.remove(o);
        }



        public void manageJob() {
            try{
                while(this.hasJob()){
                    if(this.runJob()){
                        //Job has complete
                        this.removeJob();
                    }else{
                        //THIS LINE SHOULD NEVER BE REACH as runJob should either run fine and return true
                        //OR throw a portal exception
                        throw new PortalServiceException("Fatal error occurred,Job not completed");
                    }
                }

            }catch(PortalServiceException e){
                try{
                    if(!(e.getErrorCorrection()!= null && e.getErrorCorrection().contains("Quota exceeded"))){
                        //Something went wrong with this particular job, remove it
                        //Set status in error
                        Job job=this.removeJobCleanUp(e);
                        logger.error("Error with job:"+ job.toString() ,e);
                        this.manageJob();
                    }
                }catch(Exception ex){
                    //any exception thrown above will be silently absorb therefore its crucial to
                    //catch any here and notify the user.
                    logger.error(ex);
                }
            }catch(Exception e){
                //Fail safe but this code should be unreachable. If it does, its a fatal error.
                logger.error(e);
            }
        }


    }



}
