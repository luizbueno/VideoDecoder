package com.videodecoder.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import com.brightcove.zencoder.client.ZencoderClient;
import com.brightcove.zencoder.client.ZencoderClientException;
import com.brightcove.zencoder.client.model.ContainerFormat;
import com.brightcove.zencoder.client.request.ZencoderCreateJobRequest;
import com.brightcove.zencoder.client.request.ZencoderOutput;
import com.brightcove.zencoder.client.response.ZencoderCreateJobResponse;

@ManagedBean
public class UploadController implements Serializable{

	private static final long serialVersionUID = 7330198589296796485L;

	private UploadedFile file;

	private String urlArquivo = "";

	public void fileUploadAction(FileUploadEvent event) {  
		this.file = event.getFile();  
		String bucketName = "elasticbeanstalk-sa-east-1-585199353882";  
		AWSCredentials credentials = null;  
		try {  
			credentials = new ProfileCredentialsProvider().getCredentials();  
			System.out.println("Key: " + credentials.getAWSAccessKeyId() + ", Secret: " + credentials.getAWSSecretKey());  
		} catch (Exception e) {

			FacesMessage message1 = new FacesMessage("O Arquivo", event.getFile().getFileName() + " falhou ao realizar o Upload. " + 
					" Houve um erro na conexão com as credenciais da Amazon AWS.");
			FacesContext.getCurrentInstance().addMessage(null, message1);

			throw new AmazonClientException("Não foi possivel carregar as credenciais do arquivo de profiles de credenciais. "
					+ "Por favor verifique se seu arquivo de credenciais está no caminho correto "
					+ "(~/.aws/credentials) e se está em um formato válido.", e);
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

			setUrlArquivo("https://s3-sa-east-1.amazonaws.com/elasticbeanstalk-sa-east-1-585199353882/" + file.getFileName());

			realizaConversaoVideo();
			
			FacesMessage message2 = new FacesMessage("O Arquivo", event.getFile().getFileName() + " realizou o upload com sucesso! ");
			FacesContext.getCurrentInstance().addMessage(null, message2);

		} catch (AmazonServiceException ase) {  
			System.out.println("Foi detectado uma exceção do tipo AmazonServiceException, que siginifica que a requisição foi feita a Amazon S3, "  
					+ "mas foi rejeitada com uma resposta de erro por algum motivo. Segue os dados do erro:");  
			System.out.println("Error Message:  " + ase.getMessage());  
			System.out.println("HTTP Status Code: " + ase.getStatusCode());  
			System.out.println("AWS Error Code:  " + ase.getErrorCode());  
			System.out.println("Error Type:    " + ase.getErrorType());  
			System.out.println("Request ID:    " + ase.getRequestId());

			FacesMessage message3 = new FacesMessage("O Arquivo", event.getFile().getFileName() + " falhou ao realizar o Upload. " + 
					" Foi detectado uma exceção do tipo AmazonServiceException, que siginifica que a requisição foi feita a Amazon S3, "  
					+ "mas foi rejeitada com uma resposta de erro por algum motivo.");
			FacesContext.getCurrentInstance().addMessage(null, message3);

		} catch (AmazonClientException ace) {  
			System.out.println("Foi detectado uma exceção do tipo AmazonClientException, que siginifica que o cliente tentou se comunicar com a Amazon S3 "  
					+ "mas não foi possível acessar a rede.");

			FacesMessage message4 = new FacesMessage("O Arquivo", event.getFile().getFileName() + " falhou ao realizar o Upload. " + 
					"Foi detectado uma exceção do tipo AmazonClientException, que siginifica que o cliente tentou se comunicar com a Amazon S3 "  
					+ "mas não foi possível acessar a rede.");
			FacesContext.getCurrentInstance().addMessage(null, message4);

		} catch (IOException e) {
			e.printStackTrace();
		}  
	}
	
	public void realizaConversaoVideo() {
		
		ZencoderClient client = new ZencoderClient("bebd51c3884d45e0538d88a80a9fe102");
		
		ZencoderCreateJobRequest job = new ZencoderCreateJobRequest();
		job.setInput(getUrlArquivo());
		List<ZencoderOutput> outputs = new ArrayList<ZencoderOutput>();

		ZencoderOutput output1 = new ZencoderOutput();
		output1.setFormat(ContainerFormat.MP4);
		outputs.add(output1);

		job.setOutputs(outputs);
		try {
			ZencoderCreateJobResponse response = client.createZencoderJob(job);
			setUrlArquivo(response.getOutputs().get(0).getUrl());
		} catch (ZencoderClientException e) {
			FacesMessage message5 = new FacesMessage("O Arquivo", file.getFileName() + " falhou ao realizar o Upload." + 
					"Foi detectado uma exceção do tipo ZencoderClientException, que siginifica que ocorreu algum problema durante a conversão do vídeo.");
			FacesContext.getCurrentInstance().addMessage(null, message5);
			e.printStackTrace();
		}
	}

	public String getUrlArquivo() {
		return urlArquivo;
	}

	public void setUrlArquivo(String urlArquivo) {
		this.urlArquivo = urlArquivo;
	}

}
