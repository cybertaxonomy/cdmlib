/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.tcs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.sdd.SDDSources;

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
		logger.debug("TcsSource " +  sourceUrl);
		return sourceUrl;
		
	}
	
	public static String taxonX_local(){
		//		Monocots rdf
		//String sourceUrl = "file:C:/localCopy/eclipse/cdmlib/app-import/src/main/resources/palm_tn_29336.xml";
		URL url = new SDDSources().getClass().getResource("/taxonX/palm_tn_29336.xml");
		String sourceUrl = url.toString();
		return sourceUrl;
	}

	public static File taxonX_localDir(){
		//		Monocots rdf
		File sourceDir = new File("target/classes/taxonX/"); //palm_tc_14495.xml
		return sourceDir;
	}
	
	public static String arecaceae_local(){
		//		Monocots rdf
		//String sourceUrl = "file:C:/localCopy/eclipse/cdmlib/app-import/src/main/resources/arecaceae.rdf";
		URL url = new SDDSources().getClass().getResource("/arecaceae.rdf");
		String sourceUrl = url.toString();
		return sourceUrl;
		
	}

	
	public static String arecaceae_short(){
		//		Monocots rdf
		URL url = new SDDSources().getClass().getResource("/arecaceae_short.rdf");
		String sourceUrl = url.toString();
		return sourceUrl;
	}

	public static String tcsXmlTest_local(){
		//		tcsXmlTest.xml
		URL url = new TcsSources().getClass().getResource("/tcsXmlTest.xml");
		String sourceUrl = url.toString();
		return sourceUrl;
		
	}	

	public static String tcsXml_localPath(){
		File file = new File("C:\\localCopy\\Data\\tdwg\\Cichorium_tcs.xml");
		String sourceUrl;
		try{
			sourceUrl = file.toURI().toURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return sourceUrl;
		
	}	
	
	public static String tcsXmlTest_local2(){
		//		tcsXmlTest.xml
		URL url = new TcsSources().getClass().getResource("/TcsXmlImportConfiguratorTest-input.xml");
		String sourceUrl = url.toString();
		return sourceUrl;	
	}
	
	public static String tcsRdf_globis(){
		//		globis.rdf.xml
		//String sourceUrl = "file:C:/Dokumente und Einstellungen/a.kohlbecker.BGBM/Desktop/globis.rdf.xml";
		String sourceUrl = "/globis_valid.rdf.xml";
		URL resourceUrl = new TcsSources().getClass().getResource(sourceUrl);
		logger.debug("TcsRdfSource " +  resourceUrl.toString());
		return resourceUrl.toString();
		
	}	
	

	
}
