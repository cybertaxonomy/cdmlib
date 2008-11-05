/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.sdd;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * @author h.fradin
 * @created 24.10.2008
 * @version 1.0
 */
public class SDDSources {
	private static final Logger logger = Logger.getLogger(SDDSources.class);
	
	public static String arecaceae(){
		//	Monocots rdf
		String sourceUrl = "http://dev.e-taxonomy.eu/trac/attachment/wiki/SampleDataConversion/Monocotyledonae/arecaceae.rdf?format=raw";
		return sourceUrl;
		
	}
	
	public static String taxonX_local(){
		//		Monocots rdf
		String sourceUrl = "file:C:/localCopy/eclipse/cdmlib/app-import/src/main/resources/palm_tn_29336.xml";
		return sourceUrl;
	}

	public static File taxonX_localDir(){
		//		Monocots rdf
		File sourceDir = new File("C:/localCopy/eclipse/cdmlib/app-import/src/main/resources/taxonX/");
		return sourceDir;
	}
	
	public static String viola_local(){
		//		SDD xml examples from the SDD v1.1 package
		String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/SDD-Test-Simple.xml";
		return sourceUrl;
		
	}

	public static String arecaceae_pub(){
		//		Monocots rdf
		String sourceUrl = "file:C:/localCopy/eclipse/cdmlib/app-import/src/main/resources/arecaceae_pub.rdf";
		return sourceUrl;
	}
	
	public static String arecaceae_short(){
		//		Monocots rdf
		String sourceUrl = "file:C:/localCopy/eclipse/cdmlib/app-import/src/main/resources/arecaceae_short.rdf";
		return sourceUrl;
	}
	
}
