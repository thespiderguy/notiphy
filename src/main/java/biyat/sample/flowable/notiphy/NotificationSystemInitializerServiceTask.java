package biyat.sample.flowable.notiphy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.flowable.dmn.api.DmnEngineConfigurationApi;
import org.flowable.dmn.api.DmnRuleService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("notificationSystemInitializerServiceTask")
public class NotificationSystemInitializerServiceTask implements JavaDelegate, ApplicationContextAware{
	
	ApplicationContext applicationContext = null;
	@Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        System.out.println("setting context");
        this.applicationContext = applicationContext;
    }
	
	/*@Autowired
	DmnEngine dmnEngine;
	@Autowired
	ProcessEngine processEngine;*/

	private static final Logger LOG = Logger.getLogger(NotificationSystemInitializerServiceTask.class.getName());
	
	public void execute(DelegateExecution execution) {
		LOG.info("Entering NotificationSystemInitializerServiceTask:execute()");
		Map<String, Object> variables = execution.getVariables();
		LOG.info(variables.toString());
		
		if (applicationContext != null && applicationContext.containsBean("ruleService")){
			/*DmnEngine dmnEngine = (DmnEngine) applicationContext.getBean("dmnEngine");
            DmnRuleService dmnRuleService = dmnEngine.getDmnRuleService();*/
			
			DmnRuleService ruleService = (DmnRuleService) applicationContext.getBean("ruleService");
			
			DmnEngineConfigurationApi dmnEngineConfiguration = (DmnEngineConfigurationApi) applicationContext.getBean("dmnEngineConfiguration");
			
			ProcessEngine processEngine = (ProcessEngine) applicationContext.getBean("processEngine");
    		ProcessInstance processInstance = processEngine.getRuntimeService()
    				.startProcessInstanceByKey("TTSRequestHandlerProcess", variables);
    		
			try {
				List<Map<String, Object>> result1 = ruleService.createExecuteDecisionBuilder()
	    				.decisionKey("NotificationSystemInitializer")
	    				.variable("notificationSystem", "TTS")
						.variable("notificationInterface", "Ticket Notification")
	    				//.variables(variables)
	    				.execute(); //We can use jsonMessage.get("notificationSystem")
	    		
	    		/*Map<String, Object> inputVariables = new HashMap<String, Object>();
	    	    inputVariables.put("notificationSystem", "TTS");
	    	    inputVariables.put("notificationInterface", "Ticket Notification");
	    		List<Map<String, Object>> result = dmnRuleService.executeDecisionByKey("NotificationSystemInitializer", inputVariables);*/
	    		
	    		String str;
	    		System.out.println("Biyat1="+result1);
			} catch(Exception e) {
				//do nothing
			}
    		
    		
    		Map<String, Object> inputVariables = new HashMap<String, Object>();
    	    inputVariables.put("notificationSystem", "TTS");
    	    inputVariables.put("notificationInterface", "Ticket Notification");
    		List<Map<String, Object>> result = ruleService.executeDecisionByKey("NotificationSystemInitializer", inputVariables);
    		System.out.println("Biyat2="+result);
    		
    		
    		
    		
    		//String intermDecisionTable = result1.get(0).get("decisionTableReference").toString();
        }
		
		
	    LOG.info("Exiting NotificationSystemInitializerServiceTask:execute()");
	  }
}
