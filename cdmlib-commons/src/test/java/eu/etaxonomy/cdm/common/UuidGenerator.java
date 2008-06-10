/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * 
 * @author a.mueller
 */
public class UuidGenerator {
	private static final Logger logger = Logger.getLogger(UuidGenerator.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (int i = 0; i < 0; i++){
			System.out.println(UUID.randomUUID());
		}
		boolean result;
		String str =  "file://C:/tmp";
		try {
			str =  "\\\\Bgbm11\\Edit-WP6";
			File file = null;
			file = new File(str);
			if (file.exists()){
				result = true;
			}else{
				result = false;
			}
			logger.warn(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
