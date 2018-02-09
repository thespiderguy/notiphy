package biyat.sample.flowable.notiphy;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@Qualifier("restTemplateFactory")
public class RestTemplateFactory implements FactoryBean<RestTemplate>, InitializingBean {
	private RestTemplate restTemplate;

	public RestTemplateFactory() {
		super();
	}

	protected String getData() {

		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap();
		map.add("file", new ClassPathResource("processes/TTS_Request_Workflow.bpmn20.xml"));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
				map, headers);

		ResponseEntity<String> str = restTemplate.exchange(
				"http://localhost:8080/flowable-rest/service/repository/deployments", HttpMethod.POST, requestEntity,
				String.class);

		System.out.println(str.getBody());

		ResponseEntity<String> str2 = restTemplate.exchange(
				"http://localhost:8080/flowable-task/process-api/runtime/process-instances", HttpMethod.GET, null,
				String.class);
		System.out.println(str2.getBody());

		return str.getBody();
	}

	@Override
	public RestTemplate getObject() {
		return restTemplate;
	}

	@Override
	public Class<RestTemplate> getObjectType() {
		return RestTemplate.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() {
		HttpHost host = new HttpHost("localhost", 8080, "http");
		final ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactoryBasicAuth(host);
		restTemplate = new RestTemplate(requestFactory);
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("kermit", "kermit"));
	}

}