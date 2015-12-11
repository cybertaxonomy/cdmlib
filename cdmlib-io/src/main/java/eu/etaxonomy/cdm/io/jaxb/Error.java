package eu.etaxonomy.cdm.io.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Error", propOrder = {
	    "code",
	    "message",
		"resource",
		"cause",
		"stackTrace"
})
@XmlRootElement(name = "Error")
public class Error {
	@XmlAttribute
	private Integer status;

	@XmlElement(name = "Message")
	private String message;
	
	@XmlElement(name = "Resource")
	private String resource;
	
	@XmlElement(name = "Cause")
	private String cause;
	
	@XmlElement(name = "Code")
	private String code;

	@XmlElementWrapper(name = "StackTrace")
	@XmlElement(name = "StackTraceElement")
	private List<String> stackTrace;

	public Error() {
		
	}
	
	public Error(Throwable throwable) {
		if(throwable.getCause() != null) {
		    this.cause = throwable.getCause().getClass().getName();
		}
		this.message = throwable.getLocalizedMessage();
		this.stackTrace = new ArrayList<String>();
		for(StackTraceElement ste : throwable.getStackTrace()) {
			this.stackTrace.add(ste.toString());
		}
	}
	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<String> getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(List<String> stackTrace) {
		this.stackTrace = stackTrace;
	} 
}
