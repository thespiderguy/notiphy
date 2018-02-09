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

@Component("notificationSystemInitializerServiceTask")
public class NotificationSystemInitializerServiceTask implements JavaDelegate, ApplicationContextAware{
	
	ApplicationContext applicationContext = null;
	@Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        System.out.println("setting context");
        this.applicationContext = applicationContext;
    }
	
	private static final Logger LOG = Logger.getLogger(NotificationSystemInitializerServiceTask.class.getName());
	
	public void execute(DelegateExecution execution) {
		LOG.info("Entering NotificationSystemInitializerServiceTask:execute()");
		try {
			Map<String, Object> variables = execution.getVariables();
			LOG.info(variables.toString());
			/**
			 * Decide which process to execute.
			 */
			ObjectMapper objectMapper = new ObjectMapper();
			NotificationJSON notification = null;
			notification = objectMapper.readValue(variables.get("camelBody").toString(), NotificationJSON.class);
			ObjectNode decisionVariables = notification.getDecisionVariables();
			decisionVariables.put("decisionKey", "NotificationSystemInitializer");
			RestTemplate restTemplate = (RestTemplate) applicationContext.getBean("restTemplateFactory");
			HttpHost host = new HttpHost("localhost", 8080, "http");
			final ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactoryBasicAuth(host);
			restTemplate = new RestTemplate(requestFactory);
			restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("kermit", "kermit"));
			ResponseEntity<String> decisionResponse = restTemplate.exchange(
					"http://localhost:8080/flowable-rest/dmn-api/dmn-rule/execute", HttpMethod.POST,
					new HttpEntity<ObjectNode>(decisionVariables), String.class);
			LOG.info(decisionResponse.getBody());
			/**
			 * Instantiate the next process to be executed.
			 */
			ObjectNode processVariables = notification.getProcessVariables();			
			JsonNode root = objectMapper.readTree(decisionResponse.getBody());
			JsonNode arrNode = root.get("resultVariables");
			if (arrNode.isArray()) {
				for (final JsonNode arrNode2 : arrNode) {
					if (arrNode2.isArray()) {
						for (final JsonNode objNode : arrNode2) {
							processVariables.put("processDefinitionKey", objNode.get("value"));
						}
					}
				}
			}
			LOG.info("Result=" + root.get("resultVariables"));
			ResponseEntity<String> startProcessInstance = restTemplate.exchange(
					"http://localhost:8080/flowable-rest/service/runtime/process-instances", HttpMethod.POST,
					new HttpEntity<ObjectNode>(processVariables), String.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOG.info("Exiting NotificationSystemInitializerServiceTask:execute()");
	  }
}
