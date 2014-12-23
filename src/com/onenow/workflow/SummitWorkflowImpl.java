package com.onenow.workflow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.enremmeta.onenow.swf.CloudPriceListerClient;
import com.enremmeta.onenow.swf.CloudPriceListerClientImpl;
import com.enremmeta.onenow.swf.SummitWorkflowSelfClient;
import com.enremmeta.onenow.swf.SummitWorkflowSelfClientImpl;
import com.onenow.salesforce.SalesforceClient;
import com.onenow.salesforce.SalesforceClientImpl;
import com.sforce.soap.enterprise.sobject.Cloud__c;

public class SummitWorkflowImpl implements SummitWorkflow {

	private CloudPriceListerClient cloudPriceLister = new CloudPriceListerClientImpl();
	private SalesforceClient sforce = new SalesforceClientImpl();
	private SummitWorkflowSelfClient selfClient = new SummitWorkflowSelfClientImpl();

	private int counter = 0;

	@Asynchronous
	void processCloudList(Promise<List<Cloud__c>> cloudPromise) {
		List<Cloud__c> clouds = cloudPromise.get();
		Set<String> providers = new HashSet<String>();
		for (Cloud__c cloud : clouds) {
			String provider = cloud.getProvider__c();
			System.out.println(provider);
			providers.add(provider);
		}
		for (String provider : providers) {
			Promise<List<String>> instTypes = cloudPriceLister
					.getInstanceTypes(provider);
			processInstanceTypes(provider, instTypes);

		}
	}

	@Asynchronous
	void processInstanceTypes(String provider,
			Promise<List<String>> instTypesPromise) {
		List<String> instTypes = instTypesPromise.get();
		System.out.println("Provider " + provider
				+ " has these instance types: " + instTypes);
	}

	@Override
	public void mainFlow() {
		Promise<List<Cloud__c>> clouds = sforce.getClouds();
		processCloudList(clouds);

	}

}