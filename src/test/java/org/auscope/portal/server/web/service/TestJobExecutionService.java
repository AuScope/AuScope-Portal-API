package org.auscope.portal.server.web.service;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Assert;

import org.auscope.portal.server.vegl.VEGLJob;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

/**
 * Units tests for JobExecutionService
 * @author Josh Vote
 *
 */
public class TestJobExecutionService extends JobExecutionService {
    
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private AmazonEC2 mockAmazonEC2 = context.mock(AmazonEC2.class);
    private VEGLJob job;
    
    public TestJobExecutionService() {
        super((AWSCredentials) null);
    }
    
    /**
     * This is how we inject our mock EC2 instance into the JobExecutionService
     */
    @Override
    protected AmazonEC2 getAmazonEC2Instance() {
        return mockAmazonEC2;
    }
    
    @Before
    public void initJobObject() {
        job = new VEGLJob(new Integer(13), "jobName", "jobDesc", "user",
                "user@email.com", null, null, "ec2InstanceId",
                "http://ec2.endpoint", "ec2Ami", "s3AccessKey", "s3SecretKey",
                "s3Bucket", "s3BaseKey", null, new Integer(45),
                "file-storage-id", "vm-subset-filepath");
    }
    
    /**
     * Tests that job execution correctly calls and parses a response from AmazonEC2
     */
    @Test
    public void testExecuteJob() {
        final String userDataString = "user-data-string";
        final String expectedInstanceId = "instance-id";
        
        final RunInstancesResult runInstanceResult = new RunInstancesResult();
        final Reservation reservation = new Reservation();
        final Instance instance = new Instance();

        instance.setInstanceId(expectedInstanceId);
        reservation.setInstances(Arrays.asList(instance));
        runInstanceResult.setReservation(reservation);
        
        context.checking(new Expectations() {{
            oneOf(mockAmazonEC2).setEndpoint(job.getEc2Endpoint());
            oneOf(mockAmazonEC2).runInstances(with(any(RunInstancesRequest.class)));will(returnValue(runInstanceResult));
        }});
        
        String actualInstanceId = this.executeJob(job, userDataString);
        
        Assert.assertEquals(expectedInstanceId, actualInstanceId);
    }
    
    /**
     * Tests that job execution correctly calls and parses a response from AmazonEC2
     * when EC2 reports failure by returning 0 running instances.
     */
    @Test
    public void testExecuteJobFailure() {
        final String userDataString = "user-data-string";
        final String expectedInstanceId = null;
        
        final RunInstancesResult runInstanceResult = new RunInstancesResult();
        final Reservation reservation = new Reservation();

        reservation.setInstances(new ArrayList<Instance>());
        runInstanceResult.setReservation(reservation);
        
        context.checking(new Expectations() {{
            oneOf(mockAmazonEC2).setEndpoint(job.getEc2Endpoint());
            oneOf(mockAmazonEC2).runInstances(with(any(RunInstancesRequest.class)));will(returnValue(runInstanceResult));
        }});
        
        String actualInstanceId = this.executeJob(job, userDataString);
        
        Assert.assertEquals(expectedInstanceId, actualInstanceId);
    }
    
    /**
     * Tests that job execution correctly calls and parses a response from AmazonEC2
     * when EC2 reports failure by throwing an exception
     */
    @Test(expected=AmazonServiceException.class)
    public void testExecuteJobException() {
        final String userDataString = "user-data-string";
        
        final RunInstancesResult runInstanceResult = new RunInstancesResult();
        final Reservation reservation = new Reservation();

        reservation.setInstances(new ArrayList<Instance>());
        runInstanceResult.setReservation(reservation);
        
        context.checking(new Expectations() {{
            oneOf(mockAmazonEC2).setEndpoint(job.getEc2Endpoint());
            oneOf(mockAmazonEC2).runInstances(with(any(RunInstancesRequest.class)));will(throwException(new AmazonServiceException("")));
        }});
        
        this.executeJob(job, userDataString);
        Assert.fail("Exception should've been thrown");
    }
    
    /**
     * Tests that job terminate correctly calls AmazonEC2
     */
    @Test
    public void testTerminateJob() {
        final TerminateInstancesResult terminateInstanceResult = new TerminateInstancesResult();
        
        context.checking(new Expectations() {{
            oneOf(mockAmazonEC2).setEndpoint(job.getEc2Endpoint());
            oneOf(mockAmazonEC2).terminateInstances(with(any(TerminateInstancesRequest.class)));will(returnValue(terminateInstanceResult));
        }});
        
        this.terminateJob(job);
    }
}
