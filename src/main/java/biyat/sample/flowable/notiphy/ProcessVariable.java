package biyat.sample.flowable.notiphy;

import java.io.Serializable;

public class ProcessVariable implements Serializable {
	private static final long serialVersionUID = 1L;
	
	String name = "";
	String value = "";
	
    
    public ProcessVariable() {
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
