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

	
	//Sapindaceae
	public static String sapindaceae_local(){
		String sourceUrl = "file:C:/localCopy/Data/eflora/sapindaceae/sapindaceae-01.xml";
		return sourceUrl;
	}
	
	//Sapindaceae2
	public static String sapindaceae2_local(){
		String sourceUrl = "file:C:/localCopy/Data/eflora/sapindaceae/sapindaceae-02final.xml";
//		URL url = new SDDSources().getClass().getResource("/taxonX/palm_tn_29336.xml");
//		String sourceUrl = url.toString();
		return sourceUrl;
	}
}
