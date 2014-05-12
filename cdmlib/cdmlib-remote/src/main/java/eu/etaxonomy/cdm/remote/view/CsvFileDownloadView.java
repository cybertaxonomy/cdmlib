package eu.etaxonomy.cdm.remote.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.servlet.View;


/**
 * This class is a generic approach to generate a csv output from POJOs.
 * Yet there will be a problem with multidimensional structures. It is 
 * recommended to flatten these out before handing your data over to this
 * view.
 * <p>
 *<b>This is a experimental class, can be changed in the future</b>
 * 
 * 
 * @author a.oppermann
 *
 */
public class CsvFileDownloadView implements View{

	private File file;

	Logger logger = Logger.getLogger(CsvFileDownloadView.class);

	/**
	 * 
	 * @param file
	 */
	public CsvFileDownloadView(File file){
		this.file = file;
	}

	
	
	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
        
        ArrayList<?> list = (ArrayList<?>) model.get("csv");
        JSONObject obj;
        JSONObject result = new JSONObject();
        for(Object o : list){
        	obj = new JSONObject(o);
        	result.append("records", obj);
        }
        JSONArray ja = result.getJSONArray("records");
        String csv = CDL.toString(ja);

        File file = getFile();
		FileUtils.write(file, csv);
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
