package org.springframework.web.servlet.view.json.error;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.json.JsonErrorHandler;

public class ModelFlagError implements JsonErrorHandler {
	
	private String name = "failure";
	private String value = "true";
	
	@Override
	public void triggerError(Map model, RequestContext rc, BindingResult br,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		model.put(name, value);
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
