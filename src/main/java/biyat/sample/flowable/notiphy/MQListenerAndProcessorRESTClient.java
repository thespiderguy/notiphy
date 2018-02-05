package biyat.sample.flowable.notiphy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

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
public class MQListenerAndProcessorRESTClient {
	@Autowired
	ProcessEngine processEngine;

	@Autowired
	RestTemplateFactory restTemplateFactory;
	
	@Autowired
	protected RepositoryService repositoryService;
	private CountDownLatch latch = new CountDownLatch(1);

	public void receiveMessage(byte[] message) {
		System.out.println("Received <" + new String(message) + ">");

		//System.out.println(restTemplateFactory.getData());
		restTemplateFactory.getData();

		latch.countDown();
	}

	public CountDownLatch getLatch() {
		return latch;
	}

}
