package biyat.sample.flowable.notiphy;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

@JsonDeserialize(using = NotificationJSONDeserializer.class)
public class NotificationJSON implements Serializable {
	private static final long serialVersionUID = 1L;
	
	ObjectNode processVariables;
	ObjectNode decisionVariables;
    
    public NotificationJSON(ObjectNode processVariables, ObjectNode decisionVariables) {
    	this.processVariables = processVariables;
    	this.decisionVariables = decisionVariables;
    }

	public ObjectNode getProcessVariables() {
		return processVariables;
	}

	public ObjectNode getDecisionVariables() {
		return decisionVariables;
	}

    
	
    
    
    
}
