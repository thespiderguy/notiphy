package biyat.sample.flowable.notiphy;

import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component("incomingMessageDeserializerServiceTask")
public class IncomingMessageDeserializerServiceTask implements JavaDelegate{
	
	private static final Logger LOG = Logger.getLogger(IncomingMessageDeserializerServiceTask.class.getName());
	
	public void execute(DelegateExecution execution) {
		LOG.info("Entering IncomingMessageDeserializerServiceTask:execute()");
		try {
			Map<String, Object> variables = execution.getVariables();
			LOG.info(variables.toString());
			ObjectMapper objectMapper = new ObjectMapper();
			Notification notification = null;
			notification = objectMapper.readValue(variables.get("camelBody").toString(), Notification.class);
			
			variables.putAll(notification.getResult());
			execution.setVariables(variables);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOG.info("Exiting IncomingMessageDeserializerServiceTask:execute()");
	  }
}
