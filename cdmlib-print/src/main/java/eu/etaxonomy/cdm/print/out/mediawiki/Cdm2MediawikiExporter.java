package eu.etaxonomy.cdm.print.out.mediawiki;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import eu.etaxonomy.cdm.print.IXMLEntityFactory;
import eu.etaxonomy.cdm.print.PublishConfigurator;
import eu.etaxonomy.cdm.print.Publisher;
import eu.etaxonomy.cdm.print.out.IPublishOutputModule;
import eu.etaxonomy.cdm.print.out.PublishOutputModuleBase;

/**
 * fill in all parameters and you get a complete export from a cdm database to a
 * mediawiki
 * 
 * TODO would we move this class somewhere else and/or rename it?
 * 
 * @author s.buers
 * 
 */
public class Cdm2MediawikiExporter {

	private static final String CDM_EXPORTED_XML = "cdm_output.xml";

	private static final String PAGE_SUMMARY = "automatic import from CDM";

	private static final String TMP_OUTPUTFOLDER = "src/main/resources/tmp/";

	private static final Logger logger = Logger
			.getLogger(Cdm2MediawikiExporter.class);

	private static PublishConfigurator configurator;

	private static IXMLEntityFactory factory;

	// where the mediawiki xml code is stored
	private static String mediawikiFilePath = null;

	// where the cdm exported xml can be stored
	private static String cdm_output_file =null;
	
	// TODO delete these constants
	// palmweb
	// private final String
	// webServiceUrl="http://dev.e-taxonomy.eu/cdmserver/palmae/";

	// flora of c a
//	private static final String webServiceUrl = "http://test.e-taxonomy.eu/cdmserver/flora_central_africa/";
	
	// private static final String rootNode =
	// "0044aae4-721b-4726-85ff-752a89cff748";
	// private static final String featureTree =
	// "051d35ee-22f1-42d8-be07-9e9bfec5bcf7";

	// //cichoriae
	// private static final String
	// webServiceUrl="http://dev.e-taxonomy.eu/cdmserver/cichorieae/";
	// // Ericaceae:
//	private static final String featureTree = "051d35ee-22f1-42d8-be07-9e9bfec5bcf7";
//	private static final String rootNode = "a605e87e-113e-4ebd-ad97-f086b734b4da";
//	
	//palmae
	private static final String webServiceUrl = "http://dev.e-taxonomy.eu/cdmserver/palmae/";
	//Acrocomia
	private static final String rootNode = "f8f8a7ba-4bd7-4fdc-871c-99494439143d";
	private static final String featureTree = "72ccce05-7cc8-4dab-8e47-bf3f5fd848a0";
	
	// these parameter have a senseful default parameter, you only use
	private Document inputDocument = null;
	private Document externalDocument = null;
	
	private MediawikiOutputModule wikiOutputModule;

	/**
	 * TODO delete this method.
	 * 
	 * @param args
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws MalformedURLException {

		Cdm2MediawikiExporter exporter = new Cdm2MediawikiExporter();

		// do complete export
		 exporter.export(webServiceUrl, "Restionaceae",
		 "http://biowikifarm.net/testwiki", "Sybille Bürs", "ssetakil3?",
		 "Internal", true, false, true);

		// do only wiki import
		// exporter.uploadToMediawiki("/home/sybille/workspaces/workspace_b/wiki_statistics/cdmlib-print/src/main/resources/tmp/20130926-2347-output.xml",
		// "http://biowikifarm.net/testwiki", "Lorna Morris", "dolfin_69",
		// false);

		// do export from file:
//		parameters: String serviceUrl, String taxonName, String wikiUrl,
//				String wikiLoginUid, String passwd, String wikiPageNamespace,
//				boolean import2Mediawiki, boolean deleteOutputFiles,
//				boolean importImages
		
		
		/*
		 * String filename, String serviceUrl,
			String taxonName, String wikiUrl, String wikiLoginUid,
			String passwd, String wikiPageNamespace, boolean import2Mediawiki,
			boolean deleteOutputFiles, boolean importImages
		 */
//		exporter.exportFromXmlFile(
//				"/home/sybille/workspaces/workspace_b/wiki_statistics/cdmlib-print/src/main/resources/tmp/document1.xml",
//				webServiceUrl, "Restionaceae",
//				"http://biowikifarm.net/testwiki", "Lorna Morris", "dolfin_69",
//				"Internal", true, false, false);

	}

	/**
	 * does the whole export process: runs cdm export to mediawiki xml-file and
	 * wiki import of this file
	 * 
	 * @param serviceUrl
	 * @param taxonName
	 * @param wikiUrl
	 *            - url of the destination wiki
	 * @param wikiLoginUid
	 *            - uid of wiki admin
	 * @param passwd
	 *            - password of the above wiki admin
	 * @param wikiPageNamespace
	 *            - prefix that, if not null, will be added to all pages
	 * @throws MalformedURLException
	 * 
	 *             TODO: make passwd "unplain" MAYDO: pass more parameters e.g.:
	 *             alternative stylesheet layout parameters (that may force the
	 *             use of different stylesheet) export folder - we use a
	 *             temporary so far boolean for telling if we want to keep the
	 *             mediawiki xml file ...
	 */
	public void export(String serviceUrl, String taxonName, String wikiUrl,
			String wikiLoginUid, String passwd, String wikiPageNamespace,
			boolean import2Mediawiki, boolean deleteOutputFiles,
			boolean importImages) throws MalformedURLException {

		export(serviceUrl, taxonName, wikiUrl, wikiLoginUid, passwd,
				wikiPageNamespace, import2Mediawiki, deleteOutputFiles,
				importImages, true);
	}

	public void exportFromXmlFile(String filename, String serviceUrl,
			String taxonName, String wikiUrl, String wikiLoginUid,
			String passwd, String wikiPageNamespace, boolean import2Mediawiki,
			boolean deleteOutputFiles, boolean importImages)
			throws MalformedURLException {

		externalDocument = getDocument(filename);
		export(serviceUrl, taxonName, wikiUrl, wikiLoginUid, passwd,
				wikiPageNamespace, import2Mediawiki, deleteOutputFiles,
				importImages, false);

	}

	/*
	 * TODO: make passwd "unplain" MAYDO: pass more parameters e.g.: alternative
	 * stylesheet layout parameters (that may force the use of different
	 * stylesheet) export folder - we use a temporary so far boolean for telling
	 * if we want to keep the mediawiki xml file ...
	 */
	private void export(String serviceUrl, String taxonName, String wikiUrl,
			String wikiLoginUid, String passwd, String wikiPageNamespace,
			boolean import2Mediawiki, boolean deleteOutputFiles,
			boolean importImages, boolean usePublisher)
			throws MalformedURLException {

		// setup configurator
		configurator = PublishConfigurator.NewRemoteInstance();
		configurator.setWebserviceUrl(serviceUrl);
		factory = configurator.getFactory();

		// TODO get uuid from taxon name
		UUID taxonNodeUuid = UUID.fromString(rootNode);// restionaceae
		getUuidFromTaxonName(taxonName);

		Element taxonNodeElement = factory.getTaxonNode(taxonNodeUuid);
		configurator.addSelectedTaxonNodeElements(taxonNodeElement);

		// TODO get feature tree from taxon name/taxon node
		configurator.setFeatureTree(UUID.fromString(featureTree));

		File exportFolder = new File(TMP_OUTPUTFOLDER);
		configurator.setExportFolder(exportFolder);

//		MediawikiOutputModule wikiOutputModule;
		if (wikiPageNamespace == null
				|| wikiPageNamespace.replaceAll(" ", "").equals("")) {
			wikiOutputModule = new MediawikiOutputModule();
		} else {
			wikiOutputModule = new MediawikiOutputModule(wikiPageNamespace);
		}
		((MediawikiOutputModule) wikiOutputModule).setUsername(wikiLoginUid);

		
		if (usePublisher) {
			List<IPublishOutputModule> modules = new ArrayList<IPublishOutputModule>();
			modules.add(wikiOutputModule);
			configurator.setOutputModules(modules);

			// do export from cdm to mediawiki xml file
			Publisher.publish(configurator);
			if (importImages) {
				// the cdm out put where we want to fetch the urls of the images:
				inputDocument = ((MediawikiOutputModule) wikiOutputModule)
						.getInputDocument();
			}
		} else {
			wikiOutputModule.output(externalDocument,
					configurator.getExportFolder(),
					configurator.getProgressMonitor());
		}
		mediawikiFilePath = ((MediawikiOutputModule) wikiOutputModule).getFilePath();

		
		if (usePublisher && !deleteOutputFiles) {
			saveCdmXmlExportedDocument(exportFolder, inputDocument);
		}
		// import into mediawiki
		if (import2Mediawiki) {
			uploadToMediawiki(wikiUrl, wikiLoginUid, passwd, deleteOutputFiles);
		}

	}

	private void getUuidFromTaxonName(String rootnode2) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param exportFolder
	 * @param inputDocument
	 */
	private void saveCdmXmlExportedDocument(File exportFolder,
			Document inputDocument) {
		XMLOutputter xmlOutput = new XMLOutputter();

		cdm_output_file=exportFolder
				+ File.separator + wikiOutputModule.generateFilenameWithDate(CDM_EXPORTED_XML);
		
		// display nice nice
		xmlOutput.setFormat(Format.getPrettyFormat());
		try {
			xmlOutput.output(inputDocument, new FileWriter(cdm_output_file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void deleteOutputFiles() {
		File file = new File(mediawikiFilePath);
		file.delete();
	}

	/**
	 * uploads a given mediawiki xml file to a mediawiki
	 * 
	 * @param inputFilePath
	 * @param wikiUrl
	 * @param wikiUser
	 * @param passwd
	 * @param deleteOutputFile
	 */
	public void uploadToMediawiki(String inputFilePath, String wikiUrl,
			String wikiUser, String passwd, boolean deleteOutputFile) {
		mediawikiFilePath = inputFilePath;
		uploadToMediawiki(wikiUrl, wikiUser, passwd, deleteOutputFile);
	}

	/*
	 * @author l.morris
	 */
	private void uploadToMediawiki(String wikiUrl, String wikiUser,
			String passwd, boolean deleteOutputFile) {

		WikiBot myBot = new WikiBot(wikiUrl, wikiUser, passwd);

		// login to mediawiki
		try {
			if (!myBot.login()) {
				System.out.println("Login failed");
				return;
			}
			logger.info("logged in to mediawiki " + wikiUrl + ".");

			// parse wiki xml file and import pages one by one
			// to mediawiki
			// TODO import whole file, with ... from mediawiki API

			// get published output file
			org.jdom.Document document = getDocument(mediawikiFilePath);
			// get page nodes
			Element rootElement = document.getRootElement();
			// export pages
			List pages = rootElement.getChildren("page");
			Iterator itr = pages.iterator();
			int length = pages.size();
			int i = 1;
			while (itr.hasNext()) {
				Element page = (Element) itr.next();
				String title = page.getChild("title").getText();
				String text = page.getChild("revision").getChild("text")
						.getText();
				myBot.edit(title, text, PAGE_SUMMARY);
				logger.info("exported page " + i + "/" + length + " " + title
						+ " to " + wikiUrl + ".");
				i++;
			}

			// fetch images from input document and save them tmp

			// TODO get image nodes and fetch image urls to a list
			// download images

			// TODO log out?

			// delete xml-output
			// MAYDO do this in a saver way: only clean the actual xml file
			// TODO do we want to delete output file?
			if (deleteOutputFile) {
				deleteOutputFiles();
				logger.info("deleted temporary file(s)");
			}

			/*
			 * URL url = new URL(
			 * "http://media.e-taxonomy.eu/palmae/photos/palm_tc_14566_1.jpg");
			 */

			// File file = new
			// File("C:\\Users\\l.morris\\Documents\\prin_pub_test2\\palm_tc_14568_4.jpg");//palm_tc_14494_1.jpg
			// //mediwiki8.xml");

			/*
			 * File file2 = new File(
			 * "C:\\Users\\l.morris\\Documents\\prin_pub_test2\\Mediwiki7.xml");
			 */

			/*
			 * // this uploadAFile works to upload an image // you need to have
			 * the image e.g. palm_tc_14566_1.jpg // so
			 * http://media.e-taxonomy.eu/palmae/photos/palm_tc_14566_1.jpg //
			 * doesn't work File file = new File(
			 * "http://media.e-taxonomy.eu/palmae/photos/palm_tc_14566_1.jpg");
			 * // File file = new File(url.toURI()); /*myBot.uploadAFile(file,
			 * "palm_tc_14566_1.jpg", "my text", "my comment");
			 */
			// need to get a list of image paths. Xper2 use Base.getAllResources
			// to get these.
			// myBot.uploadAFile(file, filename, text, comment);
			// List<WikiPage>
			// create Wiki page - name, content, comments
			// e.g. new WikiPage('taxonName', wikitext, comments)

			/*
			 * for (int i = 0; i < listOfpages.size(); i++) {
			 * 
			 * if (myBot.importPage(listOfpages.get(i))) { pagesOK++; } else {
			 * pagesKO++; } }
			 */
			// */
			// -----------

			// logout
			myBot.logout();

		} catch (LoginException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			return;
		}

	}

	private Document getDocument(String filePath) {
		SAXBuilder saxBuilder = new SAXBuilder();
		File file = new File(filePath);
		Document document = null;
		// converted file to document object
		try {
			document = saxBuilder.build(file);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return document;
	}

}
