/**
 * 
 */
package eu.etaxonomy.cdm.common;

import static eu.etaxonomy.cdm.common.XmlHelp.getRoot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.jdom.output.Format;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;

/**
 * @author a.mueller
 *
 */
public class CdmUtils {
	private static final Logger logger = Logger.getLogger(CdmUtils.class);
	
	//directory of the resources (configfiles etc.)
	static File fileResourceDir;
	static final String MUST_EXIST_FILE = "applicationContext.xml";

	/**
	 * Returns the an InputStream for a read-only source
	 * @param resourceFileName the resources path within the classpath(!)
	 * @return
	 * @throws IOException
	 */
	public static InputStream getReadableResourceStream(String resourceFileName) 
			throws IOException{
		InputStream urlStream = CdmApplicationController.class.getResourceAsStream("/"+ resourceFileName);
		return urlStream;
	}
	
	
	/**
	 * Returns the directory path to the writable resources. (Resources must be copied to this directory, this is automatically done for
	 * the cdm.datasource.xml, sessionfactory.xml and applicationContext.xml
	 * @return 
	 */
	public static File getWritableResourceDir(){
		//compute only once
		if (fileResourceDir == null){
			//workaround to find out in which environment the library is executed
			URL url = CdmApplicationController.class.getResource("/"+ MUST_EXIST_FILE);
			if (url != null){
				File file = new File(url.getPath()); 
				if (file.exists()){
					fileResourceDir= file.getParentFile();
				}else{
					String subPath = File.separator + "cdmResources" ;
					file = new File(System.getProperty("user.home") + File.separator + ".cdmLibrary" + File.separator + "writableResources" );
					//file = new File(System.getProperty("user.dir") + subPath );  //does not work in plugin-environmen (uses eclipse installation directory)
					
					file.mkdirs();
					copyResource(file, CdmDataSource.DATASOURCE_FILE_NAME);
					copyResource(file, CdmDataSource.SESSION_FACTORY_FILE);
					copyResource(file, CdmDataSource.APPLICATION_CONTEXT_FILE_NAME);
					fileResourceDir = file;
				}
			}
			logger.debug("Resource directory: " + (fileResourceDir == null?"null":fileResourceDir.getAbsolutePath()));		
		}
		return fileResourceDir;
	}
	
	/**
	 * Copies a file from the classpath resource (e.g. jar-File) to the resources directory in the file system (get
	 * @param directory
	 * @param resourceFileName
	 */
	static private void copyResource(File directory, String resourceFileName){
		try {
			File fileToCopy = new File(directory + File.separator + resourceFileName);
			if (fileToCopy.createNewFile()){
				InputStream isDataSource = CdmApplicationController.class.getResourceAsStream("/"+ resourceFileName);
				XmlHelp.saveToXml(getRoot(isDataSource).getDocument(), new FileOutputStream(fileToCopy), Format.getPrettyFormat());
			}
		} catch (IOException e) {
			logger.error("File "  + resourceFileName + " + could not be created");
		}
	}
	
	/**
	 * Returns the string to the applicationContext.xml to be used.
	 * @return
	 */
	public static String getApplicationContextString(){
		String result = "file:" + CdmUtils.getWritableResourceDir() + File.separator + CdmDataSource.APPLICATION_CONTEXT_FILE_NAME;
		return result;
	}
	
	
	/**
	 * Returns the file name for the file in whick clazz is to be found (helps finding according libraries)
	 * @param clazz
	 * @return
	 */
	static public String findLibrary(Class clazz){
		String result = null;
		if (clazz != null){
			String fullPackageName = clazz.getCanonicalName();
			fullPackageName = fullPackageName.replace(".", "/");
			URL url = CdmApplicationController.class.getResource("/" + fullPackageName + ".class" );
			if (url != null){
				result = url.getFile();
			}else{
				result = "";
			}
			logger.info("LibraryURL for " + clazz.getCanonicalName() + " : " + result);
		}
		return result;
	}
	
}
