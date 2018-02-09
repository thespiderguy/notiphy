package biyat.sample.flowable.notiphy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.flowable.dmn.api.DmnRuleService;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Biyatpragyan Mohanty
 *
 */
@Component
public class MQListenerAndDecisionExecutorAPI {
	
	@Autowired
	DmnEngine dmnEngine;

	@Autowired
	protected RepositoryService repositoryService;
	private CountDownLatch latch = new CountDownLatch(1);
	private static final Logger LOG = Logger.getLogger(MQListenerAndDecisionExecutorAPI.class.getName());

	public void receiveMessage(byte[] message) {
		LOG.info("Received <" + new String(message) + ">");

		try {
			//JSONObject jsonMessage = new JSONObject(new String(message));

			DmnRuleService dmnRuleService = dmnEngine.getDmnRuleService();

			LOG.info("Executing landing rule...");
			/**
			 * Trying to decide which decision table is needed.
			 */
			List<Map<String, Object>> result1 = dmnRuleService.createExecuteDecisionBuilder()
					.decisionKey("NotificationSystemInitializer")
					.variable("notificationSystem", "TTS")
							.variable("notificationInterface", "Ticket Notification").execute(); //We can use jsonMessage.get("notificationSystem")
			String intermDecisionTable = result1.get(0).get("decisionTableReference").toString();
			LOG.info("Execute Intermediate Decision Table Rules for " + intermDecisionTable);
			/**
			 * Executing Decision Table 2
			 */
			List<Map<String, Object>> result2 = dmnRuleService.createExecuteDecisionBuilder()
					.decisionKey(intermDecisionTable)
					.variable("ticketType", "CR")
					.variable("monitoringLevel", "Monitoring")
					.variable("ticketAction", "Update")
					.variable("solutionCode", "6491")
					.variable("priority", "1")
					.variable("customerFacingManualNotesEntry", "Adding resolution code")
					.variable("status", "2")
					.variable("provisioningStatus", "In-Service")
					.variable("problemCode", "P60178")
					.execute();
			LOG.info("And the Decision is " + result2);

			latch.countDown();
		} catch (Exception e) {
			e.printStackTrace();
			LOG.severe("Wrong Message Format");
		}
	    
		latch.countDown();
	}

	public CountDownLatch getLatch() {
		return latch;
	}

}
