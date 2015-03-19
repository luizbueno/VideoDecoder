package com.videodecoder.controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

@ManagedBean
public class UploadController {

	private UploadedFile file;

	public void handleFileUpload(FileUploadEvent event) {

		//		ZencoderClient client = new ZencoderClient("bebd51c3884d45e0538d88a80a9fe102");
		//		
		//		ZencoderCreateJobRequest job = new ZencoderCreateJobRequest();
		//		job.setInput("s3://zencodertesting/test.mov");
		//		List<ZencoderOutput> outputs = new ArrayList<ZencoderOutput>();
		//
		//		ZencoderOutput output1 = new ZencoderOutput();
		//		output1.setFormat(ContainerFormat.MP4);
		//		outputs.add(output1);
		//
		//		ZencoderOutput output2 = new ZencoderOutput();
		//		output2.setFormat(ContainerFormat.WEBM);
		//		outputs.add(output2);
		//
		//		job.setOutputs(outputs);
		//		ZencoderCreateJobResponse response = client.createZencoderJob(job);

		try {
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext()  
	                .getRealPath("");  
	  
	        byte[] conteudo = event.getFile().getContents();  
	        FileOutputStream fos = new FileOutputStream("C://java//");  
	        fos.write(conteudo);  
	        fos.close();

			FacesMessage message1 = new FacesMessage("O Arquivo", event.getFile().getFileName() + " realizou upload com sucesso!");
			FacesContext.getCurrentInstance().addMessage(null, message1);

		} catch (IOException e) {
			FacesMessage message2 = new FacesMessage("O Arquivo", event.getFile().getFileName() + " falhou ao realizar o upload!");
			FacesContext.getCurrentInstance().addMessage(null, message2);
		}

	}	

	public void fileUploadAction(FileUploadEvent event) {  
		this.file = event.getFile();  
		String bucketName = "yourBucket";  
		AWSCredentials credentials = null;  
		try {  
			credentials = new ProfileCredentialsProvider().getCredentials();  
			System.out.println("Key: " + credentials.getAWSAccessKeyId() + ", Secret: " + credentials.getAWSSecretKey());  
		} catch (Exception e) {  
			throw new AmazonClientException(  
					"Cannot load the credentials from the credential profiles file. " +  
							"Please make sure that your credentials file is at the correct " +  
							"location (~/.aws/credentials), and is in valid format.",  
							e);  
		}  
		AmazonS3 s3 = new AmazonS3Client(credentials);  
		AccessControlList acl = new AccessControlList();  
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);  
		try {  
			String keyName = file.getFileName();  
			S3Object s3Object = new S3Object();  
			ObjectMetadata omd = new ObjectMetadata();  
			omd.setContentType(file.getContentType());  
			omd.setContentLength(file.getSize());  
			omd.setHeader("filename", file.getFileName());  
			InputStream input = file.getInputstream();
			s3Object.setObjectContent(input);  
			s3.putObject(new PutObjectRequest(bucketName, keyName, input, omd).withAccessControlList(acl));  
			s3Object.close();  
			System.out.println("Uploaded successfully!");  
		} catch (AmazonServiceException ase) {  
			System.out.println("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was "  
					+ "rejected with an error response for some reason.");  
			System.out.println("Error Message:  " + ase.getMessage());  
			System.out.println("HTTP Status Code: " + ase.getStatusCode());  
			System.out.println("AWS Error Code:  " + ase.getErrorCode());  
			System.out.println("Error Type:    " + ase.getErrorType());  
			System.out.println("Request ID:    " + ase.getRequestId());  
		} catch (AmazonClientException ace) {  
			System.out.println("Caught an AmazonClientException, which means the client encountered an internal error while "  
					+ "trying to communicate with S3, such as not being able to access the network.");  
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}  

}
