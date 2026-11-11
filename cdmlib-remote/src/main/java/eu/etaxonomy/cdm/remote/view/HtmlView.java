/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.view;

import java.io.Writer;
import java.util.Map;

import org.springframework.web.servlet.View;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HtmlView implements View{

	@Override
    public String getContentType() {
		return "text/html";
	}

	@Override
	public void render(Map arg0, HttpServletRequest arg1, HttpServletResponse response) throws Exception {
		response.setContentType(getContentType());
		response.setCharacterEncoding("UTF-8");
		Writer out = response.getWriter();
		if(arg0.get("html") != null) {
		    out.append(arg0.get("html").toString());
		} else {
		    out.append("<html><head><title>").append(arg0.get("title").toString()).append("</title></head><body>");
		    out.append(arg0.get("body").toString());
		    out.append("<body></html>");
		}
		response.flushBuffer();
	}

}
