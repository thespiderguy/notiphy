package biyat.sample.flowable.notiphy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.flowable.dmn.api.DmnRuleService;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Biyatpragyan Mohanty
 *
 */
@Component
public class MQListenerAndProcessorExecutorAPI {
	/*@Autowired
	ProcessEngine processEngine;
	
	@Autowired
	DmnEngine dmnEngine;

	@Autowired
	protected RepositoryService repositoryService;*/
	private CountDownLatch latch = new CountDownLatch(1);

	public void receiveMessage(byte[] message) {
		System.out.println("Received <" + new String(message) + ">");

		/**
		 * Deploy process definition in Flowable. Provide Rules Engine Configuration.
		 * Pass the BPMN diagram to Flowable.
		 *//*
		Deployment deployment = repositoryService.createDeployment()
				.addClasspathResource("processes/TTS_Request_Workflow.bpmn20.xml").deploy();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.deploymentId(deployment.getId()).singleResult();
		*//**
		 * Prepare parameter to be passed into Flowable process/task
		 *//*
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("source", "TTS");
		variables.put("sender", "biyat-tts");
		variables.put("ticketId", "CR23234");
		variables.put("description", "Service Down");
		*//**
		 * Instantiate flowable process by passing the process Id and parameters
		 *//*
		ProcessInstance processInstance = processEngine.getRuntimeService()
				.startProcessInstanceByKey("TTSRequestHandlerProcess", variables);

		TaskService taskService = processEngine.getTaskService();
		List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
		System.out.println("You have " + tasks.size() + " tasks:");
		System.out.println("Task List");
		System.out.println("=========");
		for (int i = 0; i < tasks.size(); i++) {
			System.out.println((i + 1) + ") " + tasks.get(i).getName());
		}*/
		
		latch.countDown();
	}

	public CountDownLatch getLatch() {
		return latch;
	}

}
