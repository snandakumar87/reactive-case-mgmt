package com.redhat.accountvalidationservice.routes;

import com.redhat.accountvalidationservice.beans.AccountValidationBean;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;

import java.util.logging.Logger;

public class AccountValidationRouteBuilder extends RouteBuilder {

	private static final Logger LOG = Logger.getLogger(AccountValidationRouteBuilder.class.getName());

	private String kafkaBootstrap = "localhost:9092";
	private String kafkaCreditTransferCreditorTopic = "case-topic";
	private String consumerMaxPollRecords ="500";
	private String consumerCount = "1";
	private String consumerSeekTo = "beginning";
	private String consumerGroup = "accountvalidationservice";



	@Override
	public void configure() throws Exception {
		try {
			System.out.println("starting account validation service");


			KafkaComponent kafka = new KafkaComponent();
			kafka.setBrokers(kafkaBootstrap);
			this.getContext().addComponent("kafka", kafka);


			from("kafka:" + kafkaCreditTransferCreditorTopic + "?brokers=" + kafkaBootstrap + "&maxPollRecords="
					+ consumerMaxPollRecords + "&seekTo=" + consumerSeekTo
					+ "&groupId=" + consumerGroup).routeId("FromKafka")
					.bean(AccountValidationBean.class, "validateTxn")
					.filter(simple("${body} != \"NO_DATA\""))
					.to("kafka:" + "account-validate" + "?brokers=" + kafkaBootstrap);

		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
