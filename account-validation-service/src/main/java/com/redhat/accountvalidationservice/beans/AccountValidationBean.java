package com.redhat.accountvalidationservice.beans;

import com.google.gson.Gson;
import com.myspace.offermanagement.customerModel.CustomerModel;
import com.myspace.offermanagement.transactionmodel.Transaction;
import com.myspace.offermanagement.validation.AccountValidationPayload;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.WebTarget;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

@Service
public class AccountValidationBean {

	private static final Logger LOG = Logger.getLogger(AccountValidationBean.class.getName());

	private static final String KSESSION = "";

	private final KieContainer kieContainer;


	@Autowired
	public AccountValidationBean(KieContainer kieContainer) {
		LOG.info("Initializing a new session.");
		this.kieContainer = kieContainer;
	}

	public String validateTxn(String body) {
		String resultJson=  "NO_DATA";
		try {



			LinkedHashMap<String, Object> mapVal = new Gson().fromJson(body, LinkedHashMap.class);

			Map<String,Object> val = (Map<String,Object>)mapVal.get("case-data");




			Transaction transaction = new Transaction();
			transaction.setCustId((String)val.get("custId"));
			transaction.setMerchantType((String)val.get("merchantType"));
			transaction.setTxnId((String)val.get("txnId"));
			transaction.setTxnCountry((String)val.get("txnCountry"));
			transaction.setTxnAmount((Double)val.get("txnAmount"));

			String caseId = (String)val.get("caseId");


			System.out.println("txn" + transaction.getCustId());


			KieSession kSession = kieContainer.newKieSession();

			CustomerModel customerModel = customerDataLookup(transaction.getCustId(),transaction.getTxnCountry());
			kSession.insert(customerModel);
			kSession.insert(transaction);
			kSession.fireAllRules();

			Collection<?> events = kSession.getObjects(new ClassObjectFilter(AccountValidationPayload.class));

			AccountValidationPayload eventAnalysis = null;
			for (Object evnt : events) {

				eventAnalysis = (AccountValidationPayload) evnt;
				System.out.println("res fired" +eventAnalysis.getTxnId());
				resultJson = new Gson().toJson(eventAnalysis);
				triggerAdhocTask(caseId,"{\"accountValidation\":"+"\""+eventAnalysis.getValidationPayload()+"\""+"}");
			}

			if(events.isEmpty()) {
				triggerAdhocTask(caseId,"{\"accountValidation\":"+"\"valid\""+"}");
			}

		}catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}

	// Test data for reference example
	private CustomerModel customerDataLookup(String custId, String cntry) {

		String[] classVal = new String[] {"SILVER","GOLD","PLATINUM"};



		CustomerModel customerModel = new CustomerModel();
		customerModel.setCustId(custId);
		customerModel.setIncome((double) 123455);
		customerModel.setAge((double) 34);
		if(custId.equals("6538764975321765")) {
			customerModel.setCustomerClass("SILVER");
		} else {
			customerModel.setCustomerClass(classVal[new Random().nextInt(classVal.length)]);
		}
		customerModel.setHomeCountry(cntry);

		return customerModel;
	}

	public KieContainer getKieContainer() {
		return kieContainer;
	}

	private void triggerAdhocTask(String caseId, String value) {

		ResteasyClient client = new ResteasyClientBuilder().httpEngine(new URLConnectionEngine()).build();
		WebTarget target = client.target("http://localhost:8080/kie-server");
		ResteasyWebTarget tgt = (ResteasyWebTarget)target;
		tgt.register(new BasicAuthentication("pamAdmin","redhatpam1!"));
		BusinessCentralTaskInterface customerProxy = tgt.proxy(BusinessCentralTaskInterface.class);
		customerProxy.triggerAdhocTask(caseId,value);
	}


}
