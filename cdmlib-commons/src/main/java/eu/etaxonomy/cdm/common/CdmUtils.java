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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @author a.kohlbecker
 */
public class CdmUtils {

    private static final Logger logger = Logger.getLogger(CdmUtils.class);

    /**
     * The per user cdm folder name: ".cdmLibrary"
     */
    private static final String cdmFolderName = ".cdmLibrary";

    static final String userHome = System.getProperty("user.home");

    /**
     * The per user cdm folder "~/.cdmLibrary"
     */
    public final static File perUserCdmFolder = new File(userHome + File.separator + cdmFolderName );

    static final String MUST_EXIST_FILE = "MUST-EXIST.txt";

    //folder seperator
    static String folderSeperator;

    public static File getCdmHomeDir() {
        return new File(perUserCdmFolder + File.separator);
    }

	public static File getCdmSubDir(String dirName) {

		File folder = new File(getCdmHomeDir(), dirName);
		// if the directory does not exist, create it
		if (!folder.exists()) {
			if (!folder.mkdir()) {
				// TODO throw some Exception
				return null;
			}
		}
		return folder;
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
     * Returns str if str is not the empty String (''). Returns null if str is empty.
     * @param str
     * @return
     */
    static public String Ne(String str){
        return ("".equals(str)? null : str);
    }

    /**
     * Returns str if str.trim() is not empty. Returns null otherwise.
     * @param str
     * @return
     */
    static public String Nb(String str){
        return (str == null || str.trim().equals("")? null : str);
    }


    /**
     * Concatenates an array of strings using the defined separator.<BR>
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


	/**
	 * Returns <code>preferred</code> if not blank, else returns <code>alternative</code>.
	 * If reverse is <code>true</code> computation is
	 * the other way round (<code>alternative</code> if not blank, otherwise <code>preferred</code>).
	 * @param preferred first string
	 * @param alternative second string
	 * @param reverse reverse flag
	 * @param nzTrim if <code>true</code> the result is trimmed and <code>null</code> values are replaced by empty string.
	 * @return the preferred string
	 */
	static public String getPreferredNonEmptyString(String preferred, String alternative, boolean reverse, boolean nzTrim){
		String result;
		if (! reverse){
			result = StringUtils.isBlank(preferred) ? alternative : preferred;
		}else{
			result = StringUtils.isBlank(alternative) ? preferred : alternative;
		}
		if (nzTrim){
			result = Nz(result).trim();
		}
		return result;
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
     * Returns <code>true</code> if the passed string starts with an upper case letter.
     * <code>false</code> otherwise. The later includes <code>null</code> and empty strings.
     * @param string
     * @return
     */
    static public boolean isCapital(String string){
        if (isBlank(string)){
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
    static public boolean isBlank(String string){
        if (string == null){
            return true;
        }
        if ("".equals(string.trim())){
            return true;
        }
        return false;
    }

    /**
     * Returns <code>false</code> if string is null, "" or string.trim() is ""
     * @see isNotEmpty(String string)
     * @param string
     * @return
     */
    static public boolean isNotBlank(String string){
        return ! isBlank(string);
    }

    /**
     * @see #isBlank(String)
     * @deprecated use {@link #isBlank(String)} instead
     * @param string
     * @return
     */
    @Deprecated
    static public boolean isEmpty(String string){
        return isBlank(string);
    }

    static public boolean areBlank(String ... strings){
        for (String string : strings){
            if (! isBlank(string)){
                return false;
            }
        }
        return true;
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
     * Compares 2 strings with defined values for <code>null</code>
     * @param str1
     * @param str2
     * @return
     */
    public static int nullSafeCompareTo(String str1, String str2) {
        if (str1 == null){
            return str2 == null ? 0 : -1;
        }else if (str2 == null){
            return 1;
        }else{
            return (str1.compareTo(str2));
        }
    }

    /**
     * Returns false if string is null, "" or string.trim() is ""
     * Else true.
     * @see isBlank(String string)
     * @see #isNotBlank(String)
     * @deprecated use {@link #isNotBlank(String)} instead
     * @param string
     * @return
     */
    @Deprecated
    static public boolean isNotEmpty(String string){
        return isNotBlank(string);
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

    /**
     * Returns surrounding brackets "(",")". Trim the string if necessary.
     * @param text
     * @return
     */
    public static String removeBrackets(String text) {
        if (text == null){
            return null;
        }
        text = text.trim();
        if (text.matches("^\\(.*\\)$")){
            text = text.substring(1, text.length() -1);
        }
        return text;
    }

}
