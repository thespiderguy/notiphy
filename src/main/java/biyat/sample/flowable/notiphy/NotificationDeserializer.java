package biyat.sample.flowable.notiphy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class NotificationDeserializer extends StdDeserializer<Notification> {
	public NotificationDeserializer() { 
        this(null); 
    } 
 
    public NotificationDeserializer(Class<?> vc) { 
        super(vc); 
    }
 
    @Override
    public Notification deserialize(JsonParser jp, DeserializationContext ctxt) 
      throws IOException, JsonProcessingException {
    	Map<String, Object> returnMap = new HashMap<String, Object>();
        JsonNode node = jp.getCodec().readTree(jp);        
        /*String processDefinitionValue = node.get("processDefinitionKey").asText();
        returnMap.put("processDefinitionKey", processDefinitionValue);*/
        JsonNode variablesNode = node.get("variables");
        if(variablesNode.isArray()) {
        	for(JsonNode variableNode:variablesNode) {
        		String name = variableNode.get("name").asText();
        		JsonNode value = variableNode.get("value");
        		if(value.isTextual()) {
        			returnMap.put(name, value.asText());
        		} else {
        			returnMap.put(name, value);
        		}
        		        		
        	}
        }
        
        return new Notification(returnMap);
    }
}
