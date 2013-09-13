// $Id$
package eu.etaxonomy.cdm.remote.view;

import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

public class HtmlView implements View{


	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.View#getContentType()
	 */
	@Override
    public String getContentType() {
		return "text/html";
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.View#render(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
    @SuppressWarnings("unchecked")
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
