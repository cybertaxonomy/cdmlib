package eu.etaxonomy.cdm.remote.view;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.View;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class JsonView extends BaseView implements View{
	Log log = LogFactory.getLog(JsonView.class);

	public String getContentType() {
		return "application/json";
	}

	public void render(Map model, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		// Retrieve data from model
		Object dto = getResponseData(model);
		// prepare writer
		Writer out = new BufferedWriter(new OutputStreamWriter(resp.getOutputStream()));
		// create JSON Object
		if (Collection.class.isAssignableFrom(dto.getClass())){
			JSONArray jObj = JSONArray.fromObject(dto); 
			out.append(jObj.toString());
		}else{
			JSONObject jObj = JSONObject.fromObject(dto);
			out.append(jObj.toString());
		}
	}
}
