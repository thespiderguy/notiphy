package biyat.sample.flowable.notiphy;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class IncomingMessageReceiveTask implements JavaDelegate{

	public void execute(DelegateExecution execution) {
	    System.out.println("Finished IncomingMessageReceiveTask...");
	  }
}
