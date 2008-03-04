package eu.etaxonomy.cdm.api.application;

import static eu.etaxonomy.cdm.common.XmlHelp.getRoot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.output.Format;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.CdmDataSource;

public class CdmApplicationUtils {
	private static final Logger logger = Logger.getLogger(CdmApplicationUtils.class);
	
	//directory of the resources (configfiles etc.)
	static File fileResourceDir;
	static final String MUST_EXIST_FILE = "applicationContext.xml";

	/**
	 * Returns the directory path to the writable resources. (Resources must be copied to this directory, this is automatically done for
	 * the cdm.datasource.xml, sessionfactory.xml and applicationContext.xml
	 * @return 
	 */
	public static File getWritableResourceDir(){
		//compute only once
		if (fileResourceDir == null){
			//workaround to find out in which environment the library is executed
			URL url = CdmUtils.class.getResource("/eu/etaxonomy/cdm/"+ MUST_EXIST_FILE);
			if (url != null){
				File file = new File(url.getPath()); 
				if (file.exists()){
					fileResourceDir= file.getParentFile();
				}else{
					String subPath = File.separator + "cdmResources" ;
					file = new File(System.getProperty("user.home") + File.separator + ".cdmLibrary" + File.separator + "writableResources" );
					//file = new File(System.getProperty("user.dir") + subPath );  //does not work in plugin-environmen (uses eclipse installation directory)
					
					file.mkdirs();
					copyResources(file);
					fileResourceDir = file;
				}
			}
			logger.debug("Resource directory: " + (fileResourceDir == null?"null":fileResourceDir.getAbsolutePath()));		
		}
		return fileResourceDir;
	}
	
	static private void copyResources(File directory){
		copyResource(directory, CdmDataSource.DATASOURCE_FILE_NAME);
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
				InputStream isDataSource = CdmUtils.class.getResourceAsStream("/"+ resourceFileName);
				XmlHelp.saveToXml(getRoot(isDataSource).getDocument(), new FileOutputStream(fileToCopy), Format.getPrettyFormat());
			}
		} catch (IOException e) {
			logger.error("File "  + resourceFileName + " + could not be created");
		}
	}
	

}
