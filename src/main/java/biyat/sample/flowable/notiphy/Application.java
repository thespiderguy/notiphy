package biyat.sample.flowable.notiphy;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.commons.lang3.StringUtils;
import org.flowable.dmn.api.DmnEngineConfigurationApi;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.api.DmnRuleService;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.engine.configurator.DmnEngineConfigurator;
import org.flowable.dmn.spring.SpringDmnEngineConfiguration;
import org.flowable.dmn.spring.configurator.SpringDmnEngineConfigurator;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.util.EngineServiceUtil;
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
	private Environment environment;

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
	
	@Bean(name = "dmnEngineConfiguration")
    public DmnEngineConfigurationApi dmnEngineConfiguration() {
        ProcessEngineConfiguration processEngineConfiguration = processEngine().getProcessEngineConfiguration();
        return EngineServiceUtil.getDmnEngineConfiguration(processEngineConfiguration);
    }
	
	@Bean(name = "ruleService")
    public DmnRuleService ruleService() {
        return dmnEngineConfiguration().getDmnRuleService();
    }
	
	@Bean
    public DmnRepositoryService dmnRepositoryService() {
        return dmnEngineConfiguration().getDmnRepositoryService();
    }
	
	 
	/*@Bean(name = "dmnEngineConfigure")
	public SpringDmnEngineConfiguration dmnEngineConfigure() {
		SpringDmnEngineConfiguration sdec = new SpringDmnEngineConfiguration();
		sdec.setDataSource(dataSource);
		sdec.setTransactionManager(transactionManager);
		sdec.setDatabaseSchemaUpdate("true");
		sdec.setDeploymentMode("single-resource");
		return sdec;
	}
	
	@Bean(name = "dmnEngineConfigurator")
	public DmnEngineConfigurator dmnEngineConfigurator() {
		DmnEngineConfigurator dec = new DmnEngineConfigurator();
		dec.setDmnEngineConfiguration(dmnEngineConfigure());
		return dec;
	
	
	@Bean(name = "dmnEngine")
	public DmnEngine getDmnEngine(SpringDmnEngineConfiguration sdec) {
		return sdec.buildDmnEngine();
	}}*/
	
	/*@Bean(name = "ruleService")
	public DmnRuleService getDmnRuleService(SpringDmnEngineConfiguration sdec) {
		return sdec.buildDmnEngine().getDmnRuleService();
	}*/
	

	@Bean(name = "processEngineFactoryBean")
    public ProcessEngineFactoryBean processEngineFactoryBean() {
        ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(processEngineConfiguration());
        return factoryBean;
    }
	
	/*@Bean(name = "processEngineFactoryBean")
	public ProcessEngineFactoryBean processEngineFactoryBean() {
		ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
		factoryBean.setProcessEngineConfiguration(springProcessEngineConfiguration);
		return factoryBean;
	}*/

	@Bean(name = "processEngine")
	public ProcessEngine processEngine() {
		try {
			return processEngineFactoryBean().getObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	 @Bean(name = "processEngineConfiguration")
	    public ProcessEngineConfigurationImpl processEngineConfiguration() {
	        SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
	        processEngineConfiguration.setDataSource(dataSource);
	        processEngineConfiguration.setDatabaseSchemaUpdate(environment.getProperty("engine.process.schema.update", "true"));
	        processEngineConfiguration.setTransactionManager(transactionManager);
	        processEngineConfiguration.setAsyncExecutorActivate(Boolean.valueOf(environment.getProperty("engine.process.asyncexecutor.activate", "true")));
	        processEngineConfiguration.setHistory(environment.getProperty("engine.process.history.level", "full"));
	        
	        String emailHost = environment.getProperty("email.host");
	        if (StringUtils.isNotEmpty(emailHost)) {
	            processEngineConfiguration.setMailServerHost(emailHost);
	            processEngineConfiguration.setMailServerPort(environment.getRequiredProperty("email.port", Integer.class));

	            Boolean useCredentials = environment.getProperty("email.useCredentials", Boolean.class);
	            if (Boolean.TRUE.equals(useCredentials)) {
	                processEngineConfiguration.setMailServerUsername(environment.getProperty("email.username"));
	                processEngineConfiguration.setMailServerPassword(environment.getProperty("email.password"));
	            }
	            
	            Boolean useSSL = environment.getProperty("email.useSSL", Boolean.class);
	            if (Boolean.TRUE.equals(useSSL)) {
	                processEngineConfiguration.setMailServerUseSSL(true);
	            }
	            
	            Boolean useTLS = environment.getProperty("email.useTLS", Boolean.class);
	            if (Boolean.TRUE.equals(useTLS)) {
	                processEngineConfiguration.setMailServerUseTLS(useTLS);
	            }
	        }

	        // Limit process definition cache
	        processEngineConfiguration.setProcessDefinitionCacheLimit(environment.getProperty("flowable.process-definitions.cache.max", Integer.class, 128));

	        // Enable safe XML. See http://www.flowable.org/docs/userguide/index.html#advanced.safe.bpmn.xml
	        processEngineConfiguration.setEnableSafeBpmnXml(true);

	        //processEngineConfiguration.addConfigurator(new SpringFormEngineConfigurator());
	        
	        SpringDmnEngineConfiguration dmnEngineConfiguration = new SpringDmnEngineConfiguration();
	        dmnEngineConfiguration.setHistoryEnabled(true);
	        SpringDmnEngineConfigurator dmnEngineConfigurator = new SpringDmnEngineConfigurator();
	        dmnEngineConfigurator.setDmnEngineConfiguration(dmnEngineConfiguration);
	        processEngineConfiguration.addConfigurator(dmnEngineConfigurator);
	        
	        /*
	        SpringContentEngineConfiguration contentEngineConfiguration = new SpringContentEngineConfiguration();
	        String contentRootFolder = environment.getProperty(PROP_FS_ROOT);
	        if (contentRootFolder != null) {
	            contentEngineConfiguration.setContentRootFolder(contentRootFolder);
	        }

	        Boolean createRootFolder = environment.getProperty(PROP_FS_CREATE_ROOT, Boolean.class);
	        if (createRootFolder != null) {
	            contentEngineConfiguration.setCreateContentRootFolder(createRootFolder);
	        }

	        SpringContentEngineConfigurator springContentEngineConfigurator = new SpringContentEngineConfigurator();
	        springContentEngineConfigurator.setContentEngineConfiguration(contentEngineConfiguration);

	        processEngineConfiguration.addConfigurator(springContentEngineConfigurator);
	        */

	        return processEngineConfiguration;
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