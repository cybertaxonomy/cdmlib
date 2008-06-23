/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.tcs;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public class TcsSources {
	private static final Logger logger = Logger.getLogger(TcsSources.class);
	
	public static String arecaceae(){
		//	Monocots rdf
		String sourceUrl = "http://dev.e-taxonomy.eu/trac/attachment/wiki/SampleDataConversion/Monocotyledonae/arecaceae.rdf?format=raw";
		return sourceUrl;
		
	}
	
	public static String arecaceae_local(){
		//		Monocots rdf
		String sourceUrl = "file:C:/localCopy/eclipse/cdmlib/app-import/src/main/resources/arecaceae.rdf";
		return sourceUrl;
		
	}
	
	public static String arecaceae_short(){
		//		Monocots rdf
		String sourceUrl = "file:C:/localCopy/eclipse/cdmlib/app-import/src/main/resources/arecaceae_short.rdf";
		return sourceUrl;
		
	}
}
