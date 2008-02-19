package org.springframework.web.servlet.view.json;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.support.RequestContext;

public interface JsonErrorHandler {
	public void triggerError(Map model, RequestContext rc, BindingResult br,  HttpServletRequest request,
			HttpServletResponse response) throws Exception;
	
}
