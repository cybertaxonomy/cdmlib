package org.springframework.web.servlet.view.json.error;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.json.JsonErrorHandler;

public class HttpStatusError implements JsonErrorHandler {
	public static final int DEFAULT_JSON_ERROR_STATUS = 311;
	private int errorCode = DEFAULT_JSON_ERROR_STATUS;
	
	@Override
	public void triggerError(Map model, RequestContext rc, BindingResult br,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setStatus(errorCode);
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	

}
