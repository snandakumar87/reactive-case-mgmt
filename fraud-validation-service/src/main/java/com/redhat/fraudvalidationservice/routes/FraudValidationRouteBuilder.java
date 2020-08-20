package com.redhat.fraudvalidationservice.routes;

import com.redhat.fraudvalidationservice.beans.FraudValidationBean;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;

import java.util.logging.Logger;

public class FraudValidationRouteBuilder extends RouteBuilder {

	private static final Logger LOG = Logger.getLogger(FraudValidationRouteBuilder.class.getName());

	private String kafkaBootstrap = "localhost:9092";
	private String kafkaCreditTransferCreditorTopic = "case-topic";
	private String consumerMaxPollRecords ="500";
	private String consumerCount = "1";
	private String consumerSeekTo = "beginning";
	private String consumerGroup = "fraudvalidationservice";



	@Override
	public void configure() throws Exception {
		try {
			System.out.println("starting account validation service");


			KafkaComponent kafka = new KafkaComponent();
			kafka.setBrokers(kafkaBootstrap);
			this.getContext().addComponent("kafka", kafka);

            from("timer://simpleTimer?period=1000").log("hello");

			from("kafka:" + kafkaCreditTransferCreditorTopic + "?brokers=" + kafkaBootstrap + "&maxPollRecords="
					+ consumerMaxPollRecords + "&seekTo=" + consumerSeekTo
					+ "&groupId=" + consumerGroup).routeId("FromKafka")
					.log("inside here");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
