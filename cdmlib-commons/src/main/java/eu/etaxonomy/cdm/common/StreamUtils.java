/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;

import org.apache.log4j.Logger;

/**
 * @author a.kohlbecker
 * @since 16.12.2010
 *
 */
public class StreamUtils {
	
	public static final Logger logger = Logger.getLogger(StreamUtils.class);
	
	/**
	 * Replaces each substring of this stream that matches the literal search sequence with the specified literal replace sequence. 
	 * The replacement proceeds from the beginning of the stream to the end, for example, replacing "aa" with "b" in the string "aaa" will result in "ba" rather than "ab".
	 * 
	 * @param stream 
	 * @param search The sequence of char values to be replaced
	 * @param replace The replacement sequence of char values
	 * @return
	 * @throws IOException
	 * 
	 */
	public static InputStream streamReplace(InputStream stream, String search,	String replace) throws IOException {
		InputStreamReader reader = new InputStreamReader(stream);
		StringBuilder strBuilder = new StringBuilder();
		
		char[] cbuf = new char[1024];
		int charsRead = -1;
		while ((charsRead = reader.read(cbuf)) > -1){
			strBuilder.append(cbuf, 0, charsRead);
		}
		String replacedContent = strBuilder.toString().replace(search, replace);	
		StringBufferInputStream replacedStream = new StringBufferInputStream(replacedContent); //TODO replace with StringReader
		logger.debug(replacedContent);
		return replacedStream;
	}
	
	public static InputStream streamReplaceAll(InputStream stream, String regex, String replace) throws IOException {
		InputStreamReader reader = new InputStreamReader(stream);
		StringBuilder strBuilder = new StringBuilder();
		
		char[] cbuf = new char[1024];
		int charsRead = -1;
		while ((charsRead = reader.read(cbuf)) > -1){
			strBuilder.append(cbuf, 0, charsRead);
		}
		String replacedContent = strBuilder.toString().replaceAll(regex, replace);	
		StringBufferInputStream replacedStream = new StringBufferInputStream(replacedContent); //TODO replace with StringReader
		logger.debug(replacedContent);
		return replacedStream;
	}
	
	public static String readToString(InputStream stream) throws IOException {
		InputStreamReader reader = new InputStreamReader(stream);
		StringBuilder strBuilder = new StringBuilder();
		
		char[] cbuf = new char[1024];
		int charsRead = -1;
		while ((charsRead = reader.read(cbuf)) > -1){
			strBuilder.append(cbuf, 0, charsRead);
		}
		return strBuilder.toString();
	}

}
