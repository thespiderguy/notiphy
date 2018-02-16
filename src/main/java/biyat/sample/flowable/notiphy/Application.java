package biyat.sample.flowable.notiphy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.engine.configurator.DmnEngineConfigurator;
import org.flowable.dmn.engine.impl.persistence.entity.DecisionTableEntity;
import org.flowable.dmn.spring.SpringDmnEngineConfiguration;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.common.EngineConfigurator;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.flowable.engine.ManagementService;
import org.flowable.engine.common.impl.interceptor.Command;
import org.flowable.engine.common.impl.interceptor.CommandContext;
import org.flowable.engine.impl.cmd.AbstractCustomSqlExecution;
import org.flowable.engine.impl.util.CommandContextUtil;

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
	
	@Autowired
	protected SpringDmnEngineConfiguration springDmnEngineConfiguration;

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
	
	@Bean
    CommandLineRunner customMybatisXmlMapper(final ManagementService managementService) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                DecisionTableEntity processDefinitionDeploymentId = managementService.executeCommand(new Command<DecisionTableEntity>() {
                    @Override
                    public DecisionTableEntity execute(CommandContext commandContext) {
                    	Map<String, Object> params = new HashMap<String, Object>();
                        params.put("decisionTableKey", "NotificationSystemInitializer");
                        params.put("parentDeploymentId", "1");
                        return (DecisionTableEntity) CommandContextUtil.getDbSqlSession()
                                .selectOne("selectLatestDecisionTableByKeyAndParentDeploymentId", params);
                        
                        /*return (String) CommandContextUtil.getDbSqlSession()
                                .selectOne("selectProcessDefinitionDeploymentIdByKey", "Sigma-Notification-Landing-Process-2");*/
                        
                    }
                });

                System.out.println("Process definition deployment id = "+processDefinitionDeploymentId);
            }
        };
    }
	

	@Bean(name = "processEngineFactoryBean")
	public ProcessEngineFactoryBean processEngineFactoryBean() {
		ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
		List<EngineConfigurator> configurators = new ArrayList<EngineConfigurator>();
		configurators.add(dmnEngineConfigurator());
		springProcessEngineConfiguration.setConfigurators(configurators);
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
	
	public final static String CAMEL_SOURCE_EXCHANGE = "biyat-tts-sigma-noti-exchange";
	public final static String CAMEL_SOURCE_QUEUE = "biyat-tts-receiving-rabbit-camel-queue";
	public final static String CAMEL_SOURCE_Exg_Qu_ROUTING_KEY = "tts-messages-1";
	

	@Bean
	Queue camelSourceQueue() {
		return new Queue(CAMEL_SOURCE_QUEUE, true);
	}

	@Bean
	DirectExchange camelSourceExchange() {
		return new DirectExchange(CAMEL_SOURCE_EXCHANGE);
	}

	@Bean
	Binding camelBinding(Queue camelSourceQueue, DirectExchange camelSourceExchange) {
		return BindingBuilder.bind(camelSourceQueue).to(camelSourceExchange).with(CAMEL_SOURCE_Exg_Qu_ROUTING_KEY);
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
		return BindingBuilder.bind(queue).to(exchange).with("tts-notiphy-messages-1");
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