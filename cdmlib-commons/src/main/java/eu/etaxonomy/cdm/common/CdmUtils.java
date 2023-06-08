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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @author a.kohlbecker
 */
public class CdmUtils {

    private static final Logger logger = LogManager.getLogger();

    static private boolean urlIsJarOrBundle(URL url){
        return url.getProtocol().startsWith("jar") || url.getProtocol().startsWith("bundleresource");
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
     * Returns an InputStream for a read-only source
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

    static public String readInputLine(String inputQuestion){

        try {
            System.out.print(inputQuestion);
            BufferedReader in = new BufferedReader( new InputStreamReader( System.in ));
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
     * <code>Null</code> values and empty strings are handled as if they
     * do not exist. So <BR><BR>
     *
     * concat(":", "a", "", null, "b") results in "a:b"<BR><BR>
     *
     * If all strings are <code>null</code> then <code>null</code> is returned.
     *
     * @see #concat(CharSequence, String, String)
     * @param strings the strings to concatenate
     * @param seperator the separator for concatenation
     * @return String the concatenation result
     */
    static public String concat(CharSequence separator, String... strings){
        StringBuilder result = new StringBuilder();
        boolean allNull = true;
        for (String string : strings){
            if (string != null){
                if (result.length() > 0 && string.length() > 0){
                    result.append(separator);
                }
                result.append(string);
                allNull = false;
            }
        }
        //if all strings are null result should be null, not ""
        if (allNull){
            return null;
        }else {
            return result.toString();
        }
    }


    /**
     * Concatenates two strings, using the defined separator.<BR>
     * <code>Null</code> values are interpreted as empty strings.<BR>
     * Empty strings are not included in concatenation so concat(":", "a", "")
     * results in "a", not "a:".<BR>
     *
     * If both strings are <code>null</code> then <code>null</code> is returned.
     *
     * @see #concat(CharSequence, String[])
     * @param sepearator the separator
     * @param string1 first string to concatenate
     * @param string2 second string to concatenate
     * @return String the concatenated string
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

    /**
     * Builds a list of strings by splitting an input string
     * with delimiters whitespace, comma, or semicolon
     * @param value
     * @return
     */
    public static List<String> buildList(String value) {

        List<String> resultList = new ArrayList<String>();
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
        }
        return false;
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

    /**
     * Checks if all of the given strings are blank.
     * @param strings Strings to test
     * @return <code>true</code> if all strings are blank, false otherwise
     */
    static public boolean areBlank(String ... strings){
        for (String string : strings){
            if (! isBlank(string)){
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if two objects are equal or both null. Otherwise returns <code>false</code>.
     */
    public static boolean nullSafeEqual(Object obj1, Object obj2) {
        if (obj1 == null){
            return obj2 == null;
        }
        return (obj1 == obj2) || (obj1.equals(obj2));
    }

    /**
     * Compares 2 instances of {@link Comparable} with defined values for <code>null</code>
     */
    public static <T extends Comparable<T>> int nullSafeCompareTo(T c1, T c2) {
        if (c1 == null){
            return c2 == null ? 0 : -1;
        }else if (c2 == null){
            return 1;
        }else{
            return (c1.compareTo(c2));
        }
    }

    public static int nullSafeCompareTo(String str1, String str2, boolean ignoreCase) {
        if (str1 == null){
            return str2 == null ? 0 : -1;
        }else if (str2 == null){
            return 1;
        }else{
            if (ignoreCase) {
                return str1.compareToIgnoreCase(str2);
            }else {
                return str1.compareTo(str2);
            }
        }
    }

    /**
     * Tests if two objects are equal or both null. Otherwise returns <code>false</code>.
     */
    public static String nullSafeTrim(String str) {
        if (str == null){
            return str;
        }
        return str.trim();
    }

    /**
     * Computes all fields recursively
     * @param clazz
     * @return
     */
    public static Map<String, Field> getAllFields(Class clazz, Class highestClass, boolean includeStatic, boolean includeTransient, boolean makeAccessible, boolean includeHighestClass) {
        Map<String, Field> result = new HashMap<>();
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
            Class<?> superclass = clazz.getSuperclass();
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
     * Trims the string and if the string ends with 1 or more dots removes it.
     */
    public static String removeTrailingDots(String string){
        while (string != null && string.trim().endsWith(".")){
            return string.substring(0, string.length() -1);
        }
        return string;
    }

    /**
     * Adds a trailing dot to the given String
     * if string is not blank and does not end with dot already.
     * Otherwise str is returned.
     */
    public static String addTrailingDotIfNotExists(String str){
        if (StringUtils.isNotBlank(str) && !str.endsWith(".")){
            str += ".";
         }
        return str;
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

    /**
     * Compares 2 strings. If they are not empty and equal returns <code>true</code>
     * otherwise false.
     *
     * @param str1
     * @param str2
     * @return compare result as boolean
     */
    public static boolean nonEmptyEquals(String str1, String str2) {
        return (isNotBlank(str1) && str1.equals(str2));
    }

    /**
     * Compares if str1 and str2 is equal when ignoring whitespaces.
     * Returns <code>true</code> if both or <code>null</code> or
     * whitespace ignore equal.
     * @param str1
     * @param str2
     * @return
     */
    public static boolean equalsIgnoreWS(String str1, String str2) {
        if (str1 == null){
            return str2 == null;
        }else if (str2 == null){
            return false;
        }else{
            return str1.replaceAll("\\s", "").equals(str2.replaceAll("\\s", ""));
        }
    }

    /**
     * Checks if all strings given provide are {@link #isBlank(String) blank}.
     * Returns <code>true</code> if strs is null or empty
     * @param strs
     * @return
     */
    public static boolean isBlank(String ... strs) {
        if (strs == null){
            return true;
        }
        for (String str : strs) {
            if (isNotBlank(str)){
                return false;
            }
        }
        return true;
    }

    /**
     * Transforms a search string which allows wildcard "*" into a
     * java regular expression such that all other characters are handled as normal text.
     * @param regEx
     * @return
     */
    public static String quoteRegExWithWildcard(String regEx){
        return Pattern.quote(regEx).replace("*", "\\E.*\\Q").replace("\\Q\\E", "");
    }

    public static int diffIndex(String str1, String str2) {
        if (str1 == null || str2 == null){
            return 0;
        }
        for (int i = 0; i<str1.length() && i<str2.length() ;i++) {
            if (str1.charAt(i)!= str2.charAt(i)){
                return i;
            }
        }
        if(str1.length()!=str2.length()){
            return Math.max(str1.length(), str2.length());
        }
        return -1;
    }

    public static String userFriendlyCamelCase(String camelCase){
        return String.join(" ", StringUtils.splitByCharacterTypeCamelCase(camelCase));
    }

    public static String userFriendlyClassName(Class<?> clazz){
        return userFriendlyCamelCase(clazz.getSimpleName());
    }

    /**
     * Returns <code>true</code> if the collection is <code>null</code> or empty.
     */
    public static boolean isNullSafeEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    public static int modifiedDamerauLevenshteinDistance(String str1, String str2) {
		if (str1 == str2) {
			return 0;
		} else if (str1.isEmpty()) {
			return str2.length();
		} else if (str2.isEmpty()) {
			return str1.length();
		} else if (str2.length() == 1 && str1.length() == 1 && str1 != str2) {
			return 1;
		} else {

			int[][] distanceMatrix = new int[str1.length() + 1][str2.length() + 1];

			for (int i = 0; i <= str1.length(); i++) {
				distanceMatrix[i][0] = i;
			}

			for (int j = 0; j <= str2.length(); j++) {
				distanceMatrix[0][j] = j;
			}

			for (int i = 1; i <= str1.length(); i++) {
				for (int j = 1; j <= str2.length(); j++) {
					int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
					distanceMatrix[i][j] = Math.min(
							Math.min(distanceMatrix[i - 1][j] + 1, distanceMatrix[i][j - 1] + 1),
							distanceMatrix[i - 1][j - 1] + cost);

					if (i > 1 && j > 1 && str1.charAt(i - 1) == str2.charAt(j - 2)
							&& str1.charAt(i - 2) == str2.charAt(j - 1)) {
						distanceMatrix[i][j] = Math.min(distanceMatrix[i][j], distanceMatrix[i - 2][j - 2] + cost);
					}
				}
			}
			return distanceMatrix[str1.length()][str2.length()];
		}
	}

}
