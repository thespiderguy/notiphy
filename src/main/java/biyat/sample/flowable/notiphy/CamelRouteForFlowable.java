package biyat.sample.flowable.notiphy;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.flowable.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CamelRouteForFlowable extends RouteBuilder{
	@Autowired
	ProcessEngine processEngine;
	
	@Override
    public void configure() throws Exception {
		from("rabbitmq://localhost/biyat-mintek-sigma-tracking-exchange?username=guest&password=guest&queue=biyat-mintek-receiving-rabbit-camel-queue&routingKey=mintek-messages&autoDelete=false&autoAck=false")
		.removeHeaders("rabbitmq.*").throttle(100).timePeriodMillis(10000)/*.process(new Processor() {
			public void process(Exchange exchange) throws Exception {
				byte[] data = (byte[]) exchange.getIn().getBody();
				ObjectMapper objectMapper = new ObjectMapper();
				Notification notification = objectMapper.readValue(data, Notification.class);				
				exchange.getOut().setBody(notification.getResult());
			}
		})*/
		/*.convertBodyTo(String.class)
		.convertBodyTo(JSONObject.class)*/
		/**
		 * Make sure to call the process initiator. This is the starting point of the
		 * process execution. To make it possible
		 * <startEvent id="startEvent" flowable:initiator="initiator"></startEvent> must
		 * be mentioned in the process model.
		 */
		.to("flowable:Sigma-Notification-Landing-Process?copyCamelBodyToBodyAsString=true&copyVariablesFromProperties=true&copyVariablesFromHeader=true");
		
		from("rabbitmq://localhost/dispatch-ui-exchange?username=guest&password=guest&queue=dispatch-ui-queue&routingKey=dispatch&autoDelete=false&autoAck=false")
		.removeHeaders("rabbitmq.*").throttle(100).timePeriodMillis(10000).to("flowable:Sigma-Notification-Landing-Process?copyCamelBodyToBodyAsString=true&copyVariablesFromProperties=true&copyVariablesFromHeader=true");
		
		from("rabbitmq://localhost/biyat-tts-sigma-noti-exchange?username=guest&password=guest&queue=biyat-tts-receiving-rabbit-camel-queue&routingKey=tts-messages&autoDelete=false&autoAck=false")
		.removeHeaders("rabbitmq.*").throttle(100).timePeriodMillis(10000)
		.process(new Processor() {
			public void process(Exchange exchange) throws Exception {
				byte[] data = (byte[]) exchange.getIn().getBody();
				ObjectMapper objectMapper = new ObjectMapper();
				Notification notification = objectMapper.readValue(data, Notification.class);				
				exchange.getOut().setBody(notification.getResult());
			}
		})
		.to("flowable:Sigma-Notification-Landing-Process-2?copyVariablesFromProperties=true&copyVariablesFromHeader=true");
		
		
    }
}
