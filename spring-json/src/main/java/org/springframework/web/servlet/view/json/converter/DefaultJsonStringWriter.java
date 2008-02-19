package org.springframework.web.servlet.view.json.converter;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.view.json.converter.MapGraphWalker;
import net.sf.sojo.common.PathRecordWalkerInterceptor;
import net.sf.sojo.interchange.json.JsonWalkerInterceptor;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.json.JsonStringWriter;

public class DefaultJsonStringWriter implements JsonStringWriter {
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean convertAllMapValues;
	
	@Override
	public void convertAndWrite(Map model, Writer writer, BindingResult br) throws IOException{
		MapGraphWalker walker = new MapGraphWalker();
		walker.getObjectUtil().setWithSimpleKeyMapper(false);
				
		JsonViewWalkerInterceptor jsonInterceptor = new JsonViewWalkerInterceptor();
		jsonInterceptor.setWithNullValuesInMap(true);
		jsonInterceptor.setConvertAllMapValues(convertAllMapValues);
		if(br != null){
			jsonInterceptor.setPropertyEditorRegistry(br.getPropertyEditorRegistry());
			jsonInterceptor.setObjectName(br.getObjectName());
		}
		walker.addInterceptor(jsonInterceptor);
		walker.walk(model);
		
		if(logger.isDebugEnabled())
			logger.debug(jsonInterceptor.getJsonString());
		
		writer.write(jsonInterceptor.getJsonString());
	}

	public boolean isConvertAllMapValues() {
		return convertAllMapValues;
	}

	public void setConvertAllMapValues(boolean convertAllMapValues) {
		this.convertAllMapValues = convertAllMapValues;
	}
	
	

}
