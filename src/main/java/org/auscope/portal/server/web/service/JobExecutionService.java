package org.auscope.portal.server.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.vegl.VEGLJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.core.codec.Base64;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

/**
 * A service class for providing allowing a Job object to be executed remotely at some form
 * of high performance compute facility.
 *
 * @author Josh Vote
 */
@Service
public class JobExecutionService {

    private final Log logger = LogFactory.getLog(getClass());

    private AWSCredentials credentials;

    /**
     * Creates a new instance with the specified credentials
     * @param credentials
     */
    public JobExecutionService(AWSCredentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Creates a new instance loading its credentials from the configured credentials file in hostConfig
     * @param hostConfig Must have the key 'HOST.aws.credentials.file' specified
     * @throws IOException if the specified credentials file cannot be read
     */
    @Autowired
    public JobExecutionService(PortalPropertyPlaceholderConfigurer hostConfig) throws IOException {
        String awsCredFileLocation = hostConfig.resolvePlaceholder("HOST.aws.credentials.file");
        logger.info(String.format("Attempting to load AWS Credentials from '%1$s'", awsCredFileLocation));
        FileSystemResource res = new FileSystemResource(awsCredFileLocation);

        this.credentials = new PropertiesCredentials(res.getInputStream());
    }

    /**
     * Gets an instance of an AmazonEC2 for use in submitting/terminating jobs
     * @return
     */
    protected AmazonEC2 getAmazonEC2Instance() {
        return new AmazonEC2Client(credentials);
    }

    /**
     * Begins execution of the specified job and returns the ID of the started instance.
     *
     * This function will create a VM to run the job which will be responsible for decoding
     * the userDataString and downloading any input files from the JobStorageService
     *
     * @param job The job to execute
     * @param userDataString A string that is made available to the job when it starts execution (this will be Base64 encoded before being sent to the VM)
     * @return null if execution fails or the instance ID of the running VM
     */
    public String executeJob(VEGLJob job, String userDataString) throws AmazonServiceException, AmazonClientException {
        AmazonEC2 ec2 = getAmazonEC2Instance();
        ec2.setEndpoint(job.getEc2Endpoint());
        RunInstancesRequest instanceRequest = new RunInstancesRequest(job.getEc2AMI(), 1, 1);

        String base64EncodedUserData = new String(Base64.encode(userDataString.toString().getBytes()));
        instanceRequest.setUserData(base64EncodedUserData);
        instanceRequest.setInstanceType("m1.large");
        instanceRequest.setKeyName("vegl-test-key"); //TODO - bacon - DELETE THIS CODE - it's for testing
        RunInstancesResult result = ec2.runInstances(instanceRequest);
        List<Instance> instances = result.getReservation().getInstances();

        //We should get a single item on success
        if (instances.size() == 0) {
            return null;
        }
        Instance instance = instances.get(0);
        return instance.getInstanceId();
    }

    /**
     * Makes a request that the VM started by job be terminated
     * @param job The job whose execution should be terminated
     */
    public void terminateJob(VEGLJob job) {
        AmazonEC2 ec2 = getAmazonEC2Instance();

        TerminateInstancesRequest termReq = new TerminateInstancesRequest();
        ArrayList<String> instanceIdList = new ArrayList<String>();
        instanceIdList.add(job.getEc2InstanceId());
        termReq.setInstanceIds(instanceIdList);
        ec2.terminateInstances(termReq);
    }
}
