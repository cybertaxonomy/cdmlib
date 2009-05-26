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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	
	public static String getHomeDir(){
		return System.getProperty("user.home");
	}

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
	 * Returns the an InputStream for a read-only source
	 * @param resourceFileName the resources path within the classpath(!)
	 * @return
	 * @throws IOException
	 */
	public static InputStreamReader getUtf8ResourceReader(String resourceFileName) 
			throws IOException{
		InputStream urlStream = CdmUtils.class.getResourceAsStream("/"+ resourceFileName);
		InputStreamReader inputStreamReader = new InputStreamReader(urlStream, "UTF8");
		return inputStreamReader;
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
	static public String findLibrary(Class<?> clazz){
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
	
	static public Long Nz(Long value){
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

	
    /** Returns a version of the input where all contiguous
     * whitespace characters are replaced with a single
     * space. Line terminators are treated like whitespace.
     * 
     * @param inputStr
     * @return
     */
    public static CharSequence removeDuplicateWhitespace(CharSequence inputStr) {
    	
        String patternStr = "\\s+";
        String replaceStr = " ";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.replaceAll(replaceStr);
    }
    

    /** Builds a list of strings by splitting an input string
     * with delimiters whitespace, comma, or semicolon
     * @param value
     * @return
     */
    public static ArrayList<String> buildList(String value) {

    	ArrayList<String> resultList = new ArrayList<String>();
    	for (String tag : value.split("[\\s,;]+")) {
    		resultList.add(tag);
    	}
        return resultList;
    }
    

	static public boolean urlExists(String strUrl, boolean withWarning){
		try {
		     HttpURLConnection.setFollowRedirects(false);
		      // note : you may also need
		      //        HttpURLConnection.setInstanceFollowRedirects(false)
		      HttpURLConnection con =
		         (HttpURLConnection) new URL(strUrl).openConnection();
		      con.setRequestMethod("HEAD");
		      return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (MalformedURLException e) {
			if (withWarning) {
				logger.warn(e);
			}
		} catch (IOException e) {
			//
		};
		return false;
	}
	
	static public URI string2Uri(String string) {
        URI uri = null;
		try {
			uri = new URI(string);
			logger.debug("uri: " + uri.toString());
		} catch (URISyntaxException ex) {
			logger.error("Problem converting string " + string + " to URI " + uri);
			return null;
		}
		return uri;
	}
    	
	static public boolean isNumeric(String string){
		if (string == null){
			return false;
		}
		try {
			Double.valueOf(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
		
	}
	
}