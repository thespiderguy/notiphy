package biyat.sample.flowable.notiphy;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = NotificationDeserializer.class)
public class Notification implements Serializable {
	private static final long serialVersionUID = 1L;
	
	Map<String, Object> result;
    
    public Notification(Map<String, Object> result) {
    	this.result = result;
    }

	public Map<String, Object> getResult() {
		return result;
	}

	public void setResult(Map<String, Object> result) {
		this.result = result;
	}
    
    
    
}
