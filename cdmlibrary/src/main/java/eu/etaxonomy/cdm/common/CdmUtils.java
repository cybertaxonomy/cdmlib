/**
 * 
 */
package eu.etaxonomy.cdm.common;

import java.io.File;

/**
 * @author a.mueller
 *
 */
public class CdmUtils {
	
	public static File getResourceDir(){
		String strPath = System.getProperty("user.dir") + File.separator + "target" +  File.separator + "classes";
		return new File(strPath);
	}
	
}
