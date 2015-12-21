package eu.etaxonomy.cdm.print.out.mediawiki;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Ignore;

public class Cdm2MediawikiExporterTest {

	// ############## parameter###############

	// *****************portals and their nodes ********************

	// palmweb
	String portalUrl = "http://www.palmweb.org";
	String webServiceUrl = "http://dev.e-taxonomy.eu/cdmserver/palmae/";
	String taxonName = "Aphandra";// Acrocomia";//"Actinorhytis";
	String wikiPrefix = null;
	String classificationName = null;

	// flora of c a
	 //String webServiceUrl = "http://dev.e-taxonomy.eu/cdmserver/flora_central_africa/";
	 //String taxonName="Agarista";
	 //String taxonName="Restionaceae";
	 //String wikiPrefix="Internal";
	 //String classificationName= null;
	// String classificationName=null;

	// //cichoriae
	//private static final String webServiceUrl = "http://dev.e-taxonomy.eu/cdmserver/cichorieae/";
	//String taxonName = "Askellia alaica";
	//String classificationName ="standard view";
	//String wikiPrefix = null;


	// ******************other parameters **********************

	// ..................mediawiki...........................

	String wikiUrl = "http://biowikifarm.net/testwiki";
	String loginName = "Lorna Morris";//Sybille Bürs";
	// String password = CdmUtils.readInputLine("Password: ");
	String password = "dolfin_69";//ssetakil3?";

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

	@Ignore
	public void testExport() throws MalformedURLException {
		// do complete export
		if (portalUrl == null){
			portalUrl = webServiceUrl;//use the webserviceUrl if there isn't a different url for the portal
		}
		exporter.export(portalUrl, webServiceUrl, taxonName, classificationName, wikiUrl,
				loginName, password, wikiPrefix, false, false, false);

	}

	// @Test
	// public void testExportEntireDatabase() throws MalformedURLException {
	// // do complete export
	// exporter.export(webServiceUrl, wikiUrl, loginName, password,
	// wikiPrefix, true, false, true);
	//
	// }

	@Ignore
	public void testExportFromXmlFile() throws MalformedURLException {
		// TODO
		cdmExported = "/home/sybille/.cdmLibrary/mediawiki_tmp/20131022-1440-cdm_output.xml";

		exporter.exportFromXmlFile(portalUrl, cdmExported, webServiceUrl, wikiUrl,
				loginName, password, wikiPrefix, false, false, false);

	}

	@Ignore
	public void testUploadToMediawiki() {
		// do only wiki import
		// TODO
		wikiFile = "/home/sybille/.cdmLibrary/mediawiki_tmp/20131022-1533-output.xml";
		 exporter.uploadToMediawiki(wikiFile, wikiUrl, loginName, password);

	}

}
