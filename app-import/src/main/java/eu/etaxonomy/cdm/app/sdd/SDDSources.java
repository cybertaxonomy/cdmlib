/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.sdd;

import java.net.URL;

import org.apache.log4j.Logger;

/**
 * @author h.fradin
 * @created 24.10.2008
 * @version 1.0
 */
public class SDDSources {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SDDSources.class);
	
	public static String viola_local(){
		//		SDD XML example from the SDD v1.1 package
		URL url = new SDDSources().getClass().getResource("/sdd/SDD-Test-Simple.xml");
		String sourceUrl = url.toString();
		return sourceUrl;
		
	}
	
	public static String SDDImport_local(String filePath){
		//		any SDD XML file
		String sourceUrl = "file:" + filePath;
		return sourceUrl;
		
	}
	
	public static String LIAS_local(){
		//		SDD XML example from http://wiki.tdwg.org/twiki/bin/view/SDD/RealWorldExamples_SDD1dot1
		//String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/LIAS_Main.sdd11/LIAS_Main.sdd11.xml";
		URL url = new SDDSources().getClass().getResource("/sdd/LIAS_Main.sdd11.xml");
		String sourceUrl = url.toString();
		return sourceUrl;
		
	}
	
	public static String Erythroneura_local(){
		//		SDD XML example from http://wiki.tdwg.org/twiki/bin/view/SDD/RealWorldExamples_SDD1dot1
		//String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/Erythroneura.sdd11/Erythroneura.sdd11.xml";
		URL url = new SDDSources().getClass().getResource("/sdd/Erythroneura.sdd11.xml");
		String sourceUrl = url.toString();
		return sourceUrl;
		
	}
	
	public static String Cicad_local(){
		//		SDD XML example from http://wiki.tdwg.org/twiki/bin/view/SDD/RealWorldExamples_SDD1dot1
		//String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/Cicad.sdd11/Cicad.sdd11.xml";
		URL url = new SDDSources().getClass().getResource("/sdd/Cicad.sdd11.xml");
		String sourceUrl = url.toString();
		return sourceUrl;
		
	}
	
	public static String ValRosandraFRIDAKey_local(){
		//		SDD XML example from http://wiki.tdwg.org/twiki/bin/view/SDD/RealWorldExamples_SDD1dot1
		//String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/Val-Rosandra-FRIDA-Key.sdd11/Val-Rosandra-FRIDA-Key.sdd11.xml";
		URL url = new SDDSources().getClass().getResource("/sdd/Val-Rosandra-FRIDA-Key.sdd11.xml");
		String sourceUrl = url.toString();
		return sourceUrl;
		
	}
	
	public static String FreshwaterAquaticInsects_local(){
		//		SDD export from an Xper2 application
		String sourceUrl = "file:C:/Documents and Settings/lis/Mes documents/SDD/SDD bis/SDD1.1/SDD1.1/examples/Freshwater aquatic insects/test v2.sdd.xml";
		return sourceUrl;
	}
	
	public static String cichorieae_Xper2_local(){
		URL url = new SDDSources().getClass().getResource("/sdd/cichorieae-Xper2.xml");
		String sourceUrl = url.toString();
		return sourceUrl;	
	}
		
	public static String Cichorieae_DA_export_sdd(){
		URL url = new SDDSources().getClass().getResource("/sdd/Cichorieae-DA-export-sdd.xml");
		String sourceUrl = url.toString();
		return sourceUrl;	
	}
	
}
