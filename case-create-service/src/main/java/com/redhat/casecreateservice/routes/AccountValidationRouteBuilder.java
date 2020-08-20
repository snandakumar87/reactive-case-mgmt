package com.redhat.casecreateservice.routes;

import com.redhat.casecreateservice.beans.AccountValidationBean;
import com.redhat.casecreateservice.beans.CaseCreateService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;

import java.util.logging.Logger;

public class AccountValidationRouteBuilder extends RouteBuilder {

	private static final Logger LOG = Logger.getLogger(AccountValidationRouteBuilder.class.getName());

	private String kafkaBootstrap = "localhost:9092";
	private String kafkaCreditTransferCreditorTopic = "event-input-stream";
	private String consumerMaxPollRecords ="500";
	private String consumerCount = "3";
	private String consumerSeekTo = "beginning";
	private String consumerGroup = "casecreateservice";



	@Override
	public void configure() throws Exception {
		System.out.println("starting account validation service");



		KafkaComponent kafka = new KafkaComponent();
		kafka.setBrokers(kafkaBootstrap);
		this.getContext().addComponent("kafka", kafka);
		CaseCreateService caseCreateService = new CaseCreateService();


		from("kafka:" + kafkaCreditTransferCreditorTopic + "?brokers=" + kafkaBootstrap + "&maxPollRecords="
				+ consumerMaxPollRecords + "&seekTo=" + consumerSeekTo
				+ "&groupId=" + consumerGroup).routeId("From Kafka")
						.bean(CaseCreateService.class, "caseDataTransform")
						.id("Transform case Data")
						.process(exchange -> {
							exchange.getIn().setHeader("txn",exchange.getIn().getBody());
						})
						.setHeader(Exchange.HTTP_METHOD, constant("POST"))
						.setHeader("Authorization",constant("Basic cGFtQWRtaW46cmVkaGF0cGFtMSE="))
						.setHeader("content-type",constant("application/json"))
						.id("REST invocation to Case Management Layer")
						.to("http://localhost:8080/kie-server/services/rest/server/containers/TransactionAnalytics_1.0.0-SNAPSHOT/cases/TransactionAnalytics.TransactionInvestigativeAnalysis/instances")
						.id("Fetch Case Id")
						.bean(CaseCreateService.class,"returnBody")
						.id("Format Case Id")
						.process(exchange -> {
							exchange.getIn().setHeader("caseId",exchange.getIn().getBody());
						})
						.bean(CaseCreateService.class,"addCaseToTxnObj")
						.id("Add case id to the Transaction Object")
						.to("kafka:"+"case-topic"+ "?brokers=" + kafkaBootstrap)
						.id("To Case Map Topic");





	}

}
