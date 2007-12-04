/**
 * 
 */
package eu.etaxonomy.cdm.common;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 *
 */
public class CdmUtils {
	private static final Logger logger = Logger.getLogger(CdmUtils.class);
	
	//directory of the resources (configfiles etc.)
	static File fileResourceDir;
	static final String MUST_EXIST_FILE = "applicationContext.xml";
	
	
	public static File getResourceDir(){
		//compute only once
		if (fileResourceDir == null){
			//workaround to find out in which environment the library is executed
			String strUserDir = System.getProperty("user.dir"); //OLD:  
			File fileJar = new File(strUserDir + File.separator + MUST_EXIST_FILE);
			File fileEclipse = new File(strUserDir + File.separator + "target" +  File.separator + "classes" + File.separator + MUST_EXIST_FILE);
			
			if (fileJar.exists()){
				// jar environment
				fileResourceDir = fileJar.getParentFile();
			}else if (fileEclipse.exists()){
				// eclipse project environment
				fileResourceDir = fileEclipse.getParentFile();
			}else{
				// unknown environment
				fileResourceDir = null;
			}
			logger.debug("Resource directory: " + fileResourceDir==null?"null":fileResourceDir.getAbsolutePath());		
		}
		return fileResourceDir;
	}
	
}
