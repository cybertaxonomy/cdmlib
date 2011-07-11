// $Id$
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

	
	public static String getHomeDir() throws IOException{
		String homeDirString = System.getenv("USERPROFILE") != null ? System.getenv("USERPROFILE") : System.getProperty("user.home");
		
		if( ! new File(homeDirString).canWrite()){
			throw new IOException("Can not write to home directory. Assumed path is: " + homeDirString);
		}
		
		return homeDirString;
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
		String message = "This is a test";
		System.out.println(message);
		return message;
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
	

	/**
	 * Returns the trimmed value string if value is not <code>null</code>. 
	 * Returns the empty string if value is <code>null</code>.
	 * @param value
	 * @return
	 */
	static public String NzTrim(String value){
		return (value == null ? "" : value);
	}

	
	/**
	 * Returns value if value is not <code>null</code>. Returns empty string if value is <code>null</code>.
	 * @param value
	 * @return
	 */
	static public String Nz(String value){
		return (value == null ? "" : value);
	}

	/**
	 * Returns value if value is not <code>null</code>. Returns defaultValue if value is <code>null</code>.
	 * @param value
	 * @return
	 */
	static public String Nz(String value, String defaultValue){
		return (value == null ? defaultValue : value);
	}
	
	/**
	 * Returns value if value is not <code>null</code>. Returns 0 if value is <code>null</code>.
	 * @param value
	 * @return
	 */
	static public Integer Nz(Integer value){
		return (value == null ? 0 : value);
	}

	/**
	 * Returns value if value is not <code>null</code>. Returns 0 if value is <code>null</code>.
	 * @param value
	 * @return
	 */
	static public Long Nz(Long value){
		return (value == null ? 0 : value);
	}
		
	/**
	 * Concatenates an array of strings using the defined seperator.<BR>
	 * <code>Null</code> values are interpreted as empty strings.<BR>
	 * If all strings are <code>null</code> then <code>null</code> is returned.
	 * @param strings
	 * @param seperator
	 * @return String 
	 */
	static public String concat(CharSequence separator, String[] strings){
		String result = "";
		boolean allNull = true;
		for (String string : strings){
			if (string != null){
				if (result.length() > 0 && string.length() > 0){
					result += separator;
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

	/**
	 * Concatenates two strings, using the defined seperator.<BR>
	 * <code>Null</code> values are interpreted as empty Strings.<BR>
	 * If both strings are <code>null</code> then <code>null</code> is returned.
	 * @see #concat(CharSequence, String[]) 
	 * @param seperator
	 * @param string1
	 * @param string2
	 * @return String 
	 */
	static public String concat(CharSequence separator, String string1, String string2){
		String[] strings = {string1, string2};
		return concat(separator, strings);
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
	
	/**
	 * Returns true if the passed string starts with an upper case letter.
	 * @param string
	 * @return
	 */
	static public boolean isCapital(String string){
		if (isEmpty(string)){
			return false;
		}else{
			Character firstChar = string.charAt(0);
			if (firstChar.equals(Character.toUpperCase(firstChar))){
				return true;
			}else{
				return false;
			}
		}
		
	}

	/**
	 * Returns true if string is null, "" or string.trim() is ""
	 * @see isNotEmpty(String string)
	 * @param string
	 * @return
	 */
	static public boolean isEmpty(String string){
		if (string == null){
			return true;
		}
		if ("".equals(string.trim())){
			return true;
		}
		return false;
	}
	
	/**
	 * Tests if two objects are equal or both null. Otherwise returns false
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean nullSafeEqual(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null){
			return true;
		}
		if (obj1 == null && obj2 != null){
			return false;
		}
		return (obj1.equals(obj2));
	}
	
	/**
	 * Returns false if string is null, "" or string.trim() is ""
	 * Else true.
	 * @see isEmpty(String string)
	 * @param string
	 * @return
	 */
	static public boolean isNotEmpty(String string){
		return !isEmpty(string);
	}
	

	/**
	 * Computes all fields recursively
	 * @param clazz
	 * @return
	 */
	public static Map<String, Field> getAllFields(Class clazz, Class highestClass, boolean includeStatic, boolean includeTransient, boolean makeAccessible, boolean includeHighestClass) {
		Map<String, Field> result = new HashMap<String, Field>();
		if ( highestClass.isAssignableFrom(clazz) && (clazz != highestClass || includeHighestClass)){
			//exclude static
			for (Field field: clazz.getDeclaredFields()){
				if (includeStatic || ! Modifier.isStatic(field.getModifiers())){
					if (includeTransient || ! isTransient(field)){
						field.setAccessible(makeAccessible);
						result.put(field.getName(), field);
					}
				}
			}
			
			//include superclass fields
			Class superclass = clazz.getSuperclass();
			if (superclass != null){
				result.putAll(getAllFields(superclass, highestClass, includeStatic, includeTransient, makeAccessible, includeHighestClass));
			}
		}
		return result;
	}
	

	/**
	 * Returns true, if field has an annotation of type javax.persistence.Annotation
	 * @param field
	 * @return
	 */
	protected static boolean isTransient(Field field) {
		for (Annotation annotation : field.getAnnotations()){
			//if (Transient.class.isAssignableFrom(annotation.annotationType())){
			if (annotation.annotationType().getSimpleName().equals("Transient")){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Trims the string and if the string ends with a dot removes it.
	 * @param string
	 * @return
	 */
	public static String removeTrailingDot(String string){
		if (string == null){
			return null;
		}
		if (string.trim().endsWith(".")){
			return string.substring(0, string.length() -1);
		}
		return string;
	}
}