package eu.etaxonomy.cdm.print.out.mediawiki;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

public class Cdm2MediawikiExporterTest {

	//############## parameter###############
	
	// *****************portals and their nodes ********************

			// palmweb
			String webServiceUrl = "http://dev.e-taxonomy.eu/cdmserver/palmae/";
			String taxonName = "Acrocomia";
			String wikiPrefix = null;

			// flora of c a
			// String webServiceUrl =
			// "http://dev.e-taxonomy.eu/cdmserver/flora_central_africa/";
			// String taxonName="Agarista";
			// // String taxonName="Restionaceae";
			// String wikiPrefix="Internal";

			// //cichoriae
			// private static final String
			// webServiceUrl="http://dev.e-taxonomy.eu/cdmserver/cichorieae/";

			// ******************other parameters **********************

			// ..................mediawiki...........................

			String wikiUrl = "http://biowikifarm.net/testwiki";
			String loginName = "Lorna Morris";
			// String password = CdmUtils.readInputLine("Password: ");
			String password = "dolfin_69";
			
			
			// inputfiles:
			// TODO put a cdm exported file in here:
			private String cdmExported = null;
			// TODO put a mediawiki xml file in here:
			 String wikiFile = null;

			
			//#############################################
			
			Cdm2MediawikiExporter exporter;
			

	
	@Before
	public void setUp() throws Exception {
		exporter = new Cdm2MediawikiExporter();
		
	}

	@Test
	public void testExport() throws MalformedURLException {
		// do complete export
				exporter.export(webServiceUrl, taxonName, wikiUrl, loginName, password,
						wikiPrefix, true, false, true);

	}
	
	@Test
	public void testExportFromXmlFile() throws MalformedURLException {
		exporter.exportFromXmlFile(
				  cdmExported ,
				   webServiceUrl, taxonName, wikiUrl,
				  loginName, password, wikiPrefix, true, false, false);
	}
	
	@Test
	public void testUploadToMediawiki(){
		 //do only wiki import
		exporter.uploadToMediawiki(wikiFile,
		 wikiUrl, loginName, password);
		 
	}

}
