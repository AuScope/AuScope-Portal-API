package org.auscope.portal.server.vegl;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.util.structure.AbstractJobQueue;
import org.auscope.portal.core.util.structure.Job;

public class VGLTimePollQueue extends AbstractJobQueue{

    private static final Log logger = LogFactory.getLog(VGLTimePollQueue.class);


    public VGLTimePollQueue(){
       super();

    }

    public VGLTimePollQueue(ConcurrentLinkedQueue<Job> queue){
        super(queue);

    }


    public Job removeJobCleanUp(){
        Job j= this.removeJob();
        if(j instanceof VGLQueueJob){
            ((VGLQueueJob)j).updateErrorStatus();
        }
        return j;
    }

    @Override
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
                   Job job=this.removeJobCleanUp();
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
