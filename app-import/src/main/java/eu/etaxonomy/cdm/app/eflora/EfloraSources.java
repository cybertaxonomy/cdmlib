// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.app.eflora;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @date 09.06.2010
 *
 */
public class EfloraSources {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EfloraSources.class);

	//Ericaceae
	public static String ericacea_local(){
		String sourceUrl = "file:C:/localCopy/Data/eflora/africa/ericaceae_v2.xml";
		return sourceUrl;
	}
	
	//Sapindaceae
	public static String fm_sapindaceae_local(){
		String sourceUrl = "file:C:/localCopy/Data/eflora/floraMalesiana/sapindaceae-01v25.xml";
		return sourceUrl;
	}
	
	//Sapindaceae2
	public static String fm_sapindaceae2_local(){
		String sourceUrl = "file:C:/localCopy/Data/eflora/floraMalesiana/sapindaceae-02final2.xml";
//		URL url = new SDDSources().getClass().getResource("/taxonX/palm_tn_29336.xml");
//		String sourceUrl = url.toString();
		return sourceUrl;
	}
	
	//Flora Malesiana Vol 13-1
	public static String fm_13_1_local(){
		String sourceUrl = "file:C:/localCopy/Data/eflora/floraMalesiana/fm13_1_v8 final.xml";
//		URL url = new SDDSources().getClass().getResource("/taxonX/palm_tn_29336.xml");
//		String sourceUrl = url.toString();
		return sourceUrl;
	}

	//Flora Malesiana Vol 13-2
	public static String fm_13_2_local(){
		String sourceUrl = "file:C:/localCopy/Data/eflora/floraMalesiana/fm13_2_v8 final.xml";
		return sourceUrl;
	}


}
