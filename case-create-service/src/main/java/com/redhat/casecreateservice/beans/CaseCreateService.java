package com.redhat.casecreateservice.beans;

import com.google.gson.Gson;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class CaseCreateService {

	private static final Logger LOG = Logger.getLogger(CaseCreateService.class.getName());

	public String caseDataTransform(String body) {



		String caseDataString  = "{\"case-data\":"+body+"}";
		System.out.println(caseDataString);
		return caseDataString;




	}

	public String addCaseToTxnObj(Exchange exchange){

		String caseData = String.valueOf(exchange.getIn().getHeader("txn"));
		String caseId = String.valueOf(exchange.getIn().getHeader("caseId"));
		String[] argMap = caseData.split("}");
		String retVal = argMap[0]+",\"caseId\":"+"\""+caseId+"\"}}";
		return retVal;

	}

	public String returnBody(String body) {
		return new Gson().fromJson(body,String.class);
	}

}
