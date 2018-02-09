package biyat.sample.flowable.notiphy;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NotificationJSONDeserializer extends StdDeserializer<NotificationJSON> {
	public NotificationJSONDeserializer() { 
        this(null); 
    } 
 
    public NotificationJSONDeserializer(Class<?> vc) { 
        super(vc); 
    }
 
    @Override
    public NotificationJSON deserialize(JsonParser jp, DeserializationContext ctxt) 
      throws IOException, JsonProcessingException {
    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode returnMapProcess = objectMapper.createObjectNode();
    	ObjectNode returnMapdecision = objectMapper.createObjectNode();
        JsonNode node = jp.getCodec().readTree(jp);        
        /*String processDefinitionValue = node.get("processDefinitionKey").asText();
        returnMap.put("processDefinitionKey", processDefinitionValue);*/
        JsonNode variablesNode = node.get("variables");
        if(variablesNode.isArray()) {
        	ArrayNode variablesNodeArrForDecision = objectMapper.createArrayNode();
        	ArrayNode variablesNodeArrForProcess = objectMapper.createArrayNode();
        	for(JsonNode variableNode:variablesNode) {
        		String name = variableNode.get("name").asText();
        		String type = variableNode.get("type").asText();
        		JsonNode value = variableNode.get("value");
        		
        		ObjectNode variableItemNodeDecision = null;
        		ObjectNode variableItemNodeProcess = null;
        		variableItemNodeDecision = objectMapper.createObjectNode();
        		variableItemNodeProcess = objectMapper.createObjectNode();
        		variableItemNodeDecision.put("name", name);                        		
        		variableItemNodeProcess.put("name", name);

        		variableItemNodeDecision.put("type", type);
        		
        		if(value.isTextual()) {
        			variableItemNodeDecision.put("value", value.asText());
        			variableItemNodeProcess.put("value", value.asText());
        		} else {
        			variableItemNodeDecision.put("value", value);
        			variableItemNodeProcess.put("value", value);
        		}        		
        		if(!name.equals("PayLoad")) {            		
            		variablesNodeArrForDecision.add(variableItemNodeDecision);        		        			
        		} 
        		variablesNodeArrForProcess.add(variableItemNodeProcess);
        	}
        	returnMapdecision.set("inputVariables", variablesNodeArrForDecision);
        	returnMapProcess.set("variables", variablesNodeArrForProcess);
        }
        
        return new NotificationJSON(returnMapProcess, returnMapdecision);
    }
}
