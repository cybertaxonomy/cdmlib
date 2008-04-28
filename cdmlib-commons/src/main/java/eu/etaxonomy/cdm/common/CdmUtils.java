/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 *
 */
public class CdmUtils {
	private static final Logger logger = Logger.getLogger(CdmUtils.class);
	
	
	static final String MUST_EXIST_FILE = "MUST-EXIST.txt";
	
	//folder seperator
	static String folderSeperator;


	/**
	 * Returns the an InputStream for a read-only source
	 * @param resourceFileName the resources path within the classpath(!)
	 * @return
	 * @throws IOException
	 */
	public static InputStream getReadableResourceStream(String resourceFileName) 
			throws IOException{
		InputStream urlStream = CdmUtils.class.getResourceAsStream("/"+ resourceFileName);
		return urlStream;
	}

	
	/**
	 * @return
	 */
	static public String getFolderSeperator(){
		if (folderSeperator == null){
			URL url = CdmUtils.class.getResource("/"+ MUST_EXIST_FILE);
			if ( url != null && ! urlIsJarOrBundle(url) ){
				folderSeperator =  File.separator;
			}else{
				folderSeperator = "/";
			}
		}
		return folderSeperator;
	}
	
	
	/**
	 * @param url
	 * @return
	 */
	static private boolean urlIsJarOrBundle(URL url){
		return url.getProtocol().startsWith("jar") || url.getProtocol().startsWith("bundleresource");
	}
	
	/**
	 * Returns the file name for the file in which 'clazz' is to be found (helps finding according libraries)
	 * @param clazz
	 * @return
	 */
	static public String findLibrary(Class clazz){
		String result = null;
		if (clazz != null){
			String fullPackageName = clazz.getCanonicalName();
			fullPackageName = fullPackageName.replace(".", "/");
			URL url = CdmUtils.class.getResource("/" + fullPackageName + ".class" );
			if (url != null){
				result = url.getFile();
			}else{
				result = "";
			}
			logger.debug("LibraryURL for " + clazz.getCanonicalName() + " : " + result);
		}
		return result;
	}
	
	static public String testMe(){
		System.out.println("Shit");
		return "Oje";
	}
	
	static public String readInputLine(String inputQuestion){
		try {
			
			System.out.print(inputQuestion);
			BufferedReader in = new BufferedReader( new java.io.InputStreamReader( System.in )); 
			String input; 
			input = in.readLine(); 
			return input;
		} catch (IOException e) {
			logger.warn("IOExeption");
			return null;
		}
	}
	
	static public String Nz(String value){
		return (value == null ? "" : value);
	}

	
	static public Integer Nz(Integer value){
		return (value == null ? 0 : value);
	}
	
	/**
	 * Concatenates the an Array of Strings, using the defined seperator.
	 * Null values are interpreted as empty Strings
	 * If all Strings are null a null is returned 
	 * @param strings
	 * @param seperator
	 * @return String 
	 */
	static public String concat(CharSequence seperator, String[] strings){
		String result = "";
		boolean allNull = true;
		for (String string : strings){
			if (string != null){
				if (result.length() > 0 && string.length() > 0){
					result += seperator;
				}
				result += string;
				allNull = false;
			}
		}
		//if all strings are null result should be null, not ""
		if (allNull){
			return null;
		}else {
			return result;
		}
	}

	static public String concat(CharSequence seperator, String string1, String string2){
		String[] strings = {string1, string2};
		return concat(seperator, strings);
	}
	
}