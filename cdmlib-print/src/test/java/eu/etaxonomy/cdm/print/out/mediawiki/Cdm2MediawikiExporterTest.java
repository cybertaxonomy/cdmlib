package eu.etaxonomy.cdm.print.out.mediawiki;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class Cdm2MediawikiExporterTest {

	// ############## parameter###############

	// *****************portals and their nodes ********************

	// palmweb
	String webServiceUrl = "http://dev.e-taxonomy.eu/cdmserver/palmae/";
	String taxonName = "Aphandra";// Acrocomia";//"Actinorhytis";
	String wikiPrefix = null;
	String classificationName = null;

	// flora of c a
	// String webServiceUrl =
	// "http://dev.e-taxonomy.eu/cdmserver/flora_central_africa/";
	// String taxonName="Agarista";
	// // // String taxonName="Restionaceae";
	// String wikiPrefix="Internal";
	// String classificationName=null;

	// //cichoriae
	// private static final String webServiceUrl =
	// "http://dev.e-taxonomy.eu/cdmserver/cichorieae/";
	// String taxonName = "Askellia alaica";
	// String classificationName ="standard view";
	// String wikiPrefix = null;

	// ******************other parameters **********************

	// ..................mediawiki...........................

	String wikiUrl = "http://biowikifarm.net/testwiki";
	String loginName = "Sybille Bürs";
	// String password = CdmUtils.readInputLine("Password: ");
	String password = "ssetakil3?";

	// inputfiles:
	// TODO put a cdm exported file in here:
	private String cdmExported = null;
	// TODO put a mediawiki xml file in here:
	String wikiFile = null;

	// #############################################

	Cdm2MediawikiExporter exporter;

	@Before
	public void setUp() throws Exception {
		exporter = new Cdm2MediawikiExporter();

	}

	@Test
	public void testExport() throws MalformedURLException {
		// do complete export
		exporter.export(webServiceUrl, taxonName, classificationName, wikiUrl,
				loginName, password, wikiPrefix, false, false, false);

	}

	// @Test
	// public void testExportEntireDatabase() throws MalformedURLException {
	// // do complete export
	// exporter.export(webServiceUrl, wikiUrl, loginName, password,
	// wikiPrefix, true, false, true);
	//
	// }

	@Test
	public void testExportFromXmlFile() throws MalformedURLException {
		// TODO
		cdmExported = "/home/sybille/.cdmLibrary/mediawiki_tmp/20131022-1440-cdm_output.xml";

		exporter.exportFromXmlFile(cdmExported, webServiceUrl, wikiUrl,
				loginName, password, wikiPrefix, false, false, false);
	}

	@Test
	public void testUploadToMediawiki() {
		// do only wiki import
		// TODO
		wikiFile = "/home/sybille/.cdmLibrary/mediawiki_tmp/20131022-1533-output.xml";
		 exporter.uploadToMediawiki(wikiFile, wikiUrl, loginName, password);

	}

}
