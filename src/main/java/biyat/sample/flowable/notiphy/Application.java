package biyat.sample.flowable.notiphy;

import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.engine.configurator.DmnEngineConfigurator;
import org.flowable.dmn.spring.SpringDmnEngineConfiguration;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.task.api.Task;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author Biyatpragyan Mohanty
 * System configuration and bean definitions.
 *
 */
@RestController
@EnableAutoConfiguration
@ComponentScan("biyat.sample.flowable.notiphy")
public class Application {

	@RequestMapping("/")
	String home() {
		return "Hello World!";
	}

	/**
	 * No need to define Camel Context, since this is auto configured based on included library. 
	 * If we want to use it, we just need to autowire.
	 */
	@Autowired
	CamelContext camelContext;

	/**
	 * Spring boot main code execution.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	@Resource
	private Environment env;

	@Autowired
	protected PlatformTransactionManager transactionManager;

	@Autowired
	protected DataSource dataSource;

	@Autowired
	protected SpringProcessEngineConfiguration springProcessEngineConfiguration;

	//@Autowired
	//protected DmnEngineConfigurator dmnEngineConfigurator;
	
	//@Autowired
    //private ProcessEngineConfiguration processEngineConfiguration;
	
	@Bean
	public RepositoryService repositoryService() {
		return processEngine().getRepositoryService();
	}
	
	@Bean
	public SpringDmnEngineConfiguration dmnEngineConfigure() {
		SpringDmnEngineConfiguration sdec = new SpringDmnEngineConfiguration();
		sdec.setDataSource(dataSource);
		sdec.setTransactionManager(transactionManager);
		sdec.setDatabaseSchemaUpdate("true");
		sdec.setDeploymentMode("single-resource");
		return sdec;
	}
	
	@Bean
	public DmnEngineConfigurator dmnEngineConfigurator() {
		DmnEngineConfigurator dec = new DmnEngineConfigurator();
		dec.setDmnEngineConfiguration(dmnEngineConfigure());
		return dec;
	}
	
	@Bean
	public DmnEngine getDmnEngine(SpringDmnEngineConfiguration sdec) {
		return sdec.buildDmnEngine();
	}
	

	@Bean(name = "processEngineFactoryBean")
	public ProcessEngineFactoryBean processEngineFactoryBean() {
		ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
		factoryBean.setProcessEngineConfiguration(springProcessEngineConfiguration);
		return factoryBean;
	}

	@Bean(name = "processEngine")
	public ProcessEngine processEngine() {
		try {
			return processEngineFactoryBean().getObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * When configuration are initialized we want to let Camel know what do i.e all rules 
	 * must be configured so that Camel can use them to act on the input events.
	 * @return
	 */
	@Bean
	RoutesBuilder camelRouter() {
		return new RouteBuilder() {

			@Autowired
			ProcessEngine processEngine;

			@Autowired
			protected RepositoryService repositoryService;

			@Override
			public void configure() throws Exception {

				/**
				 * Deploy process definition in Flowable. Provide Rules Engine Configuration.
				 * Pass the BPMN diagram to Flowable.
				 */
				/*Deployment deployment = repositoryService.createDeployment()
						.addClasspathResource("processes/TTS_Request_Workflow.bpmn20.xml").deploy();
				ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
						.deploymentId(deployment.getId()).singleResult();
				System.out.println("Found process Id : " + processDefinition.getId());*/

				/**
				 * Retrieve particular process from the Flowable Deployment that we need to
				 * invoke. Prepare the input parameters for the first task. Invoke the process
				 * by passing the input parameter to that task and getting Rutime handle and
				 * invoking.
				 */
				from("rabbitmq://localhost/biyat-tts-sigma-noti-exchange?username=guest&password=guest&queue=biyat-tts-receiving-rabbit-camel-queue&routingKey=tts-messages&autoDelete=false&autoAck=false")
						.removeHeaders("rabbitmq.*").throttle(100).timePeriodMillis(10000).process(new Processor() {
							public void process(Exchange exchange) throws Exception {
								/**
								 * Any parameters needed for the process task could be passed here.
								 */
								exchange.getOut().getHeaders().put("source", "TTS");
								exchange.getOut().getHeaders().put("sender", "biyat-tts");
								exchange.getOut().getHeaders().put("ticketId", "CR23234");
								exchange.getOut().getHeaders().put("description", "Service Down");
								exchange.getOut().setBody(exchange.getIn().getBody());
							}
						})
						/**
						 * Make sure to call the process initiator. This is the starting point of the
						 * process execution. To make it possible
						 * <startEvent id="startEvent" flowable:initiator="initiator"></startEvent> must
						 * be mentioned in the process model.
						 */
						.to("flowable:InitiatorCamelCallProcess")
						/**
						 * Below code is optional. It will be used for further processing after the
						 * route is complete.
						 */
						.process(new Processor() {
							public void process(Exchange exchange) throws Exception {
								TaskService taskService = processEngine.getTaskService();
								List<Task> tasks = taskService.createTaskQuery().list();
								System.out.println("You have " + tasks.size() + " tasks:");
								System.out.println("Task List");
								System.out.println("=========");
								String taskId = "";
								for (int i = 0; i < tasks.size(); i++) {
									System.out.println((i + 1) + ") " + tasks.get(i).getName());
								}
								// taskService.complete(tasks.get(0).getId());
							}
						});

			}

			/*@Override
			public void configure() throws Exception {

				from("rabbitmq://messageserver.com/process-exchange?"
						+ "queue=order-queue"
						+ "&routingKey=medical-supplies"
						+ "&autoDelete=false&autoAck=true")
						.removeHeaders("rabbitmq.*").throttle(100).timePeriodMillis(10000).process(new Processor() {
							public void process(Exchange exchange) throws Exception {
								exchange.getOut().getHeaders().put("rabbitmq.EXCHANGE_NAME", "test-outter-exchange");
								exchange.getOut().getHeaders().put("rabbitmq.ROUTING_KEY", "outter-messages");
								exchange.getOut().setBody(exchange.getIn().getBody());

							}
						})
						.to("rabbitmq://messageserver.com/emergency-exchange?"
								+ "&routingKey=prime-orders&autoDelete=false");
								
			}*/

			/**
			 * Use Camel route to get data from one Rabbit MQ Queue to another i.e. create a
			 * bridge.
			 * 
			 * @throws Exception
			 */
			/*
			 * @Override public void configure1() throws Exception {
			 * 
			 * from(
			 * "rabbitmq://localhost/biyat-tts-sigma-noti-exchange?username=guest&password=guest&queue=biyat-tts-receiving-rabbit-camel-queue&routingKey=tts-messages&autoDelete=false&autoAck=true")
			 * .removeHeaders("rabbitmq.*").throttle(100).timePeriodMillis(10000).process(
			 * new Processor() { public void process(Exchange exchange) throws Exception {
			 * exchange.getOut().getHeaders().put("rabbitmq.EXCHANGE_NAME",
			 * "test-outter-exchange");
			 * exchange.getOut().getHeaders().put("rabbitmq.ROUTING_KEY",
			 * "outter-messages"); exchange.getOut().setBody(exchange.getIn().getBody());
			 * 
			 * } }) .to(
			 * "rabbitmq://localhost/test-outter-exchange?username=guest&password=guest&routingKey=outter-messages&autoDelete=false"
			 * ); }
			 */
		};
	}

	public final static String SFG_MESSAGE_QUEUE = "biyat-tts-receiving-rabbit-queue";

	@Bean
	Queue queue() {
		return new Queue(SFG_MESSAGE_QUEUE, true);
	}

	@Bean
	DirectExchange exchange() {
		return new DirectExchange("biyat-tts-sigma-notiphy-exchange");
	}

	@Bean
	Binding binding(Queue queue, DirectExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(SFG_MESSAGE_QUEUE);
	}

	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(SFG_MESSAGE_QUEUE);
		container.setMessageListener(listenerAdapter);
		return container;
	}

	/**
	 * Change the program we want to use depending on scenario
	 * @param receiver
	 * @return
	 */	
	  @Bean 
	  MessageListenerAdapter listenerAdapter(MQListenerAndDecisionExecutorAPI receiver) { 
		  return new MessageListenerAdapter(receiver, "receiveMessage"); 
	  }
	
	  /*@Bean 
	  MessageListenerAdapter listenerAdapter(MQListenerAndProcessorExecutorAPI receiver) { 
		  return new MessageListenerAdapter(receiver, "receiveMessage"); 
	  }*/
	 

	/*@Bean
	MessageListenerAdapter listenerAdapter(MQListenerAndProcessorRESTClient receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}*/
}