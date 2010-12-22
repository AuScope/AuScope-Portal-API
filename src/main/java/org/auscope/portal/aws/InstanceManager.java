package org.auscope.portal.aws;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.aws.JobSubmissionStub.SubmitJob;
import org.auscope.portal.aws.JobSubmissionStub.SubmitJobResponse;
import org.auscope.portal.server.gridjob.GeodesyJob;
import org.auscope.portal.server.gridjob.GeodesyJobManager;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

/**
 * Handles the launching, process execution and termination of Amazon machine instances.
 * 
 * @author bai167
 */
public class InstanceManager implements Runnable{
	
	private final Log logger = LogFactory.getLog(getClass());
	
	public static final int INSTANCE_STATE_CODE_PENDING = 0;
	public static final int INSTANCE_STATE_CODE_RUNNING = 16;
	public static final int INSTANCE_STATE_CODE_SHUTTING_DOWN = 32;
	public static final int INSTANCE_STATE_CODE_TERMINATED = 48;
	
	private AmazonEC2 ec2;
	private boolean instanceStarted = false;
	private List<GeodesyJob> jobList = new ArrayList<GeodesyJob>();
	private String imageId;
	private String instanceType;
	private Instance instance;
    private GeodesyJobManager jobManager;

	public void setEC2(AmazonEC2 ec2) {
		this.ec2 = ec2;
	}
	
	public AmazonEC2 getEC2() {
		return ec2;
	}
	
	public void addJobToList(GeodesyJob job) {
		jobList.add(job);
	}

	public boolean isInstanceStarted() {
		return instanceStarted;
	}

	public void setInstanceStarted(boolean instanceStarted) {
		this.instanceStarted = instanceStarted;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public GeodesyJobManager getJobManager() {
		return jobManager;
	}

	public void setJobManager(GeodesyJobManager jobManager) {
		this.jobManager = jobManager;
	}

	/**
	 * Launch a new Amazon Machine Instance (AMI).  
	 * 
	 * @throws InterruptedException
	 */
	private void launchInstance() throws InterruptedException {

		// launch the instance
		logger.info("Launching instance of image: " + imageId);
		RunInstancesRequest request = new RunInstancesRequest(imageId, 1, 1);
		request.setInstanceType(instanceType);
		List<Instance> instances = ec2.runInstances(request).getReservation().getInstances();
		instance = instances.get(0);
				
		// create a DescribeInstancesRequest for describing the instance we just launched		
		DescribeInstancesRequest descInstRequest = new DescribeInstancesRequest(); 
		String instanceId = instance.getInstanceId();
		List<String> ids = new ArrayList<String>();
		ids.add(instanceId);
		descInstRequest.setInstanceIds(ids);
		
		// Instance will take a little bit to start up . Check the instance state every 10 seconds 
		// until it is running.
		DescribeInstancesResult descInstResult = new DescribeInstancesResult(); 
		boolean instanceRunning = false;
		
		while(!instanceRunning) {
				
			// wait for instance to be running
			descInstResult = ec2.describeInstances(descInstRequest);
			instances = descInstResult.getReservations().get(0).getInstances();
			instance = instances.get(0);
			instance.getState().getName();
			if (instance.getState().getCode() != INSTANCE_STATE_CODE_PENDING) {
				instanceRunning = true;
			}
			else {
				
				// wait 10 seconds to check again
				Thread.sleep(10000);
			}
		}
		
		// wait 30 seconds to allow services (ie tomcat) to start up.
		logger.info("Instance launched. Waiting for services to start...");
		Thread.sleep(30000);
		
		logger.info("Instance running");
	}

	/**
	 * Gets a job form the job list and calls the JobSubmission web service on the AMI. Once processing is complete
	 * the job is removed from the job list. This process will repeat until there are no more jobs in the list.
	 */
	private void processJob() {
		
		while (!jobList.isEmpty()) {
			
			GeodesyJob job = jobList.get(0);
			logger.info("Processing jobid: " + job.getId());
			String dnsName = instance.getPublicDnsName();
			String targetEndpoint = "http://"+ dnsName +":8080/VEGL-Service/services/JobSubmission.JobSubmissionHttpSoap12Endpoint/";
			
			try {
				JobSubmissionStub stub = new JobSubmissionStub(targetEndpoint);
				SubmitJob subJob = new SubmitJob();
				subJob.setS3KeyPrefix(job.getOutputDir());
				SubmitJobResponse resp = stub.submitJob(subJob);
				job.setStatus(resp.get_return());
	            logger.debug("response: " + resp.get_return());
	        } catch (AxisFault e) {
	            logger.error("Failed to create web service stub using endpoint: " + targetEndpoint);
	            job.setStatus("Failed");
	            e.printStackTrace();
	        } catch (RemoteException e) {
	        	logger.error("Failed to call web service method");
	        	job.setStatus("Failed");
	            e.printStackTrace();
	        } 
			
	        jobManager.saveJob(job);
			jobList.remove(job);
			logger.debug("removed job");
		}
	}
	
	/**
	 * Terminate a running AMI.
	 * 
	 * @param instanceId The ID of the instance to terminate
	 */
	private void terminateInstance(String instanceId) {
		
		instanceStarted = false;
		TerminateInstancesRequest tir = new TerminateInstancesRequest();
		List<String> ids = new ArrayList<String>();
		ids.add(instanceId);
		tir.setInstanceIds(ids);
		ec2.terminateInstances(tir);
		logger.info("Terminated instance id: " + instanceId);
	}
	
	@Override
	public synchronized void run() {
		// Launch an AMI, process the jobs in the job list and terminate the AMI when done.
		try {
			launchInstance();
			processJob();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (instance != null) {
				terminateInstance(instance.getInstanceId());
			}
		}
	}
}
