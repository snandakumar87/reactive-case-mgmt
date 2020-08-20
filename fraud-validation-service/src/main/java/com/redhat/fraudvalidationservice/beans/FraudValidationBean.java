package com.redhat.fraudvalidationservice.beans;

import com.google.gson.Gson;
import com.myspace.offermanagement.customerModel.CustomerModel;
import com.myspace.offermanagement.transactionmodel.Transaction;
import com.myspace.offermanagement.validation.FraudValidationPayload;
import com.myspace.offermanagement.validation.RiskValidationPayload;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.time.SessionClock;
import org.kie.api.time.SessionPseudoClock;
import org.kie.dmn.api.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.WebTarget;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class FraudValidationBean {

	private static final Logger LOG = Logger.getLogger(FraudValidationBean.class.getName());

	private static final String KSESSION = "";

	private final KieContainer kieContainer;

	private List<Transaction> referenceCustomerData;


	@Autowired
	public FraudValidationBean(KieContainer kieContainer) {
		LOG.info("Initializing a new session.");
		this.kieContainer = kieContainer;
		referenceCustomerData = new ArrayList<>();
		Transaction transaction = new Transaction();
		transaction.setTxnTs(new Date(new Date().getTime() - 300));
		transaction.setTxnAmount(200.0);
		transaction.setTxnCountry("IN");
		transaction.setMerchantType("MERCH1");
		referenceCustomerData.add(transaction);
		Transaction transaction2 = new Transaction();
		transaction2.setTxnTs(new Date(new Date().getTime() - 350));
		transaction2.setTxnCountry("HK");
		transaction2.setMerchantType("MERCH3");
		referenceCustomerData.add(transaction2);

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
			transaction.setTxnTs(new Date(((Double)val.get("txnTs")).intValue()));

			String caseId = (String)val.get("caseId");


			System.out.println("txn" + transaction.getCustId());

			KieSession kieSession = kieContainer.newKieSession();

			for (Transaction nextTransaction : referenceCustomerData) {
				insert(kieSession, "Reference", nextTransaction);
			}

			insert(kieSession,"Reference",transaction);
			kieSession.fireAllRules();

			Collection<?> fraudResponse = kieSession.getObjects();
			FraudValidationPayload potentialFraudFact = new FraudValidationPayload();
			for(Object object: fraudResponse) {
				String jsonString = new Gson().toJson(object);
				triggerAdhocTask(caseId, new Gson().toJson("{\"fraudValidation\":"+jsonString+"}"));
			}
			if(fraudResponse.isEmpty()) {
				triggerAdhocTask(caseId,"{\"fraudValidation\":"+"\"valid\""+"}");
			}

			kieSession.dispose();

		}catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}

	private static FactHandle insert(KieSession kieSession, String stream, Transaction cct) {
		SessionClock clock = kieSession.getSessionClock();
		if (!(clock instanceof SessionPseudoClock)) {
			String errorMessage = "This fact inserter can only be used with KieSessions that use a SessionPseudoClock";
			System.out.println(errorMessage);
			throw new IllegalStateException(errorMessage);
		}
		SessionPseudoClock pseudoClock = (SessionPseudoClock) clock;
		EntryPoint ep = kieSession.getEntryPoint(stream);

		// First insert the event
		FactHandle factHandle = ep.insert(cct);
		// And then advance the clock.

		long advanceTime = cct.getTxnTs().getTime() - pseudoClock.getCurrentTime();
		if (advanceTime > 0) {
			System.out.println("Advancing the PseudoClock with " + advanceTime + " milliseconds.");
			pseudoClock.advanceTime(advanceTime, TimeUnit.MILLISECONDS);
		} else {
			// Print a warning when we don't need to advance the clock. This usually means that the events are entering the system in the
			// incorrect order.
			System.out.println("Not advancing time. CreditCardTransaction timestamp is '" + cct.getTxnTs() + "', PseudoClock timestamp is '"
					+ pseudoClock.getCurrentTime() + "'.");
		}
		return factHandle;
	}


	// Test data for reference example
	private CustomerModel customerDataLookup(String custId, String cntry) {

		String[] classVal = new String[] {"SILVER","GOLD","PLATINUM"};

		CustomerModel customerModel = new CustomerModel();
		customerModel.setCustId(custId);
		customerModel.setIncome((double) 123455);
		customerModel.setAge((double) 34);
		customerModel.setCustomerClass(classVal[new Random().nextInt(classVal.length)]);
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
