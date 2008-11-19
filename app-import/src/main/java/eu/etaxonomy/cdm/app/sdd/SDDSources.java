/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.sdd;

import org.apache.log4j.Logger;

/**
 * @author h.fradin
 * @created 24.10.2008
 * @version 1.0
 */
public class SDDSources {
	private static final Logger logger = Logger.getLogger(SDDSources.class);
	
/*	public static String arecaceae(){
		//	Monocots rdf
		String sourceUrl = "http://dev.e-taxonomy.eu/trac/attachment/wiki/SampleDataConversion/Monocotyledonae/arecaceae.rdf?format=raw";
		return sourceUrl;
		
	}
*/
	
	public static String viola_local(){
		//		SDD XML example from the SDD v1.1 package
		String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/SDD-Test-Simple.xml";
		return sourceUrl;
		
	}
	
	public static String LIAS_local(){
		//		SDD XML example from http://wiki.tdwg.org/twiki/bin/view/SDD/RealWorldExamples_SDD1dot1
		String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/LIAS_Main.sdd11/LIAS_Main.sdd11.xml";
		return sourceUrl;
		
	}
	
	public static String Erythroneura_local(){
		//		SDD XML example from http://wiki.tdwg.org/twiki/bin/view/SDD/RealWorldExamples_SDD1dot1
		String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/Erythroneura.sdd11/Erythroneura.sdd11.xml";
		return sourceUrl;
		
	}
	
	public static String Cicad_local(){
		//		SDD XML example from http://wiki.tdwg.org/twiki/bin/view/SDD/RealWorldExamples_SDD1dot1
		String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/Cicad.sdd11/Cicad.sdd11.xml";
		return sourceUrl;
		
	}
	
	public static String ValRosandraFRIDAKey_local(){
		//		SDD XML example from http://wiki.tdwg.org/twiki/bin/view/SDD/RealWorldExamples_SDD1dot1
		String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/Val-Rosandra-FRIDA-Key.sdd11/Val-Rosandra-FRIDA-Key.sdd11.xml";
		return sourceUrl;
		
	}
	
	public static String FreshwaterAquaticInsects_local(){
		//		SDD export from an Xper² application
		String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/Freshwater aquatic insects/export_SDD_freshwater_aquatic_insects_fr - 20080430.sdd.xml";
		return sourceUrl;
	}
	
	public static String viola_local_andreas(){
		//		SDD xml examples from the SDD v1.1 package
		String sourceUrl = "file:C:/localCopy/eclipse/cdmlib/app-import/src/main/resources/SDD-Test-Simple.xml";
		return sourceUrl;
		
	}

	public static String arecaceae_pub(){
		//		Monocots rdf
		String sourceUrl = "file:C:/localCopy/eclipse/cdmlib/app-import/src/main/resources/arecaceae_pub.rdf";
		return sourceUrl;
	}

}
