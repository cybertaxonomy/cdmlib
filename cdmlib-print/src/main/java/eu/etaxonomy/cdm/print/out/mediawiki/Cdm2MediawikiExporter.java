package eu.etaxonomy.cdm.print.out.mediawiki;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.print.IXMLEntityFactory;
import eu.etaxonomy.cdm.print.PublishConfigurator;
import eu.etaxonomy.cdm.print.Publisher;
import eu.etaxonomy.cdm.print.out.IPublishOutputModule;
import org.apache.commons.io.FileUtils;

/**
 * fill in all parameters and you get a complete export from a cdm database to a
 * mediawiki
 * 
 * TODO would we move this class somewhere else and/or rename it?
 * 
 * @author s.buers, l.morris
 * 
 */
public class Cdm2MediawikiExporter {

	private static final String MEDIAWIKI_CDM_SUB_DIR = "mediawiki_tmp";

	private static final String IMAGE_DIR = MEDIAWIKI_CDM_SUB_DIR
			+ File.separator + "images";

	private static final String CDM_EXPORT_FILE_NAME = "cdm_output";

	private static final String PAGE_SUMMARY = "automatic import from CDM";

	private static final Logger logger = Logger
			.getLogger(Cdm2MediawikiExporter.class);

	private static PublishConfigurator configurator;

	private static IXMLEntityFactory factory;

	// where the mediawiki xml code is stored
	private static String mediawikiFileWithPath = null;

	// where the cdm exported xml can be stored
	private static String cdm_output_file = null;

	// TODO delete these constants

	// these parameter have a senseful default parameter, you only use
	private Document cdmOutputDocument = null;
	private Document externalDocument = null;

	private MediawikiOutputModule wikiOutputModule;

	private static File temporaryExportFolder = null;

	private static File temporaryImageExportFolder;

	/**
	 * TODO delete this method. - and static modifier
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		// *****************portals and nodes ********************

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

		Cdm2MediawikiExporter exporter = new Cdm2MediawikiExporter();

		// String password = CdmUtils.readInputLine("Password: ");
		String password = "dolfin_69";

		// do complete export
		exporter.export(webServiceUrl, taxonName, wikiUrl, loginName, password,
				wikiPrefix, true, false, true);

		// do only wiki import
		// exporter.uploadToMediawiki("/home/sybille/workspaces/workspace_b/wiki_statistics/cdmlib-print/src/main/resources/tmp/20130926-2347-output.xml",
		// "http://biowikifarm.net/testwiki", "Lorna Morris", "dolfin_69",
		// false);

		// do export from file
		/*
		 * exporter.exportFromXmlFile(
		 * "/home/sybille/workspaces/workspace_b/wiki_statistics/cdmlib-print/src/main/resources/tmp/document1.xml"
		 * , webServiceUrl, "Restionaceae", "http://biowikifarm.net/testwiki",
		 * "Lorna Morris", "dolfin_69", "Internal", true, false, false);
		 */

		// ********************************************
		// String
		// url="http://image.br.fgov.be/gallery3/var/albums/Ericaceae/Fig1_AgaristaSalicifolia.jpg";
		// temporaryImageExportFolder = CdmUtils.getCdmSubDir(IMAGE_DIR);
		//
		// downloadImage(url);
		//
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
	 *            - prefix that, will be added to all pages null or "" will make
	 *            no prefix
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
/**
 * if you already have a cdm xml export in some file you put it in here 
 * the mediawiki xml is created and imported to an mediawiki
 * does step 2 and 3 out of all 3 export steps
 * 
 * @param filename
 * @param serviceUrl
 * @param taxonName
 * @param wikiUrl
 * @param wikiLoginUid
 * @param passwd
 * @param wikiPageNamespace
 * @param import2Mediawiki
 * @param deleteOutputFiles
 * @param importImages
 * @throws MalformedURLException
 */
	public void exportFromXmlFile(String filename, String serviceUrl,
			String taxonName, String wikiUrl, String wikiLoginUid,
			String passwd, String wikiPageNamespace, boolean import2Mediawiki,
			boolean deleteOutputFiles, boolean importImages)
			throws MalformedURLException {

		externalDocument = getDocument(filename);
		export(serviceUrl, taxonName, wikiUrl, wikiLoginUid, passwd,
				wikiPageNamespace, import2Mediawiki, deleteOutputFiles,
				importImages, importImages);

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

		// create folder for (tmp) output files
		temporaryExportFolder = CdmUtils.getCdmSubDir(MEDIAWIKI_CDM_SUB_DIR);
		if (temporaryExportFolder != null) {
			logger.info("using " + temporaryExportFolder.getAbsolutePath()
					+ " as temporary directory.");
		} else {
			logger.error("could not create directory"
					+ temporaryExportFolder.getAbsolutePath());
			return;
		}

		// setup configurator
		configurator = PublishConfigurator.NewRemoteInstance();
		configurator.setWebserviceUrl(serviceUrl);
		factory = configurator.getFactory();

		// get taxon node uuid from taxon name and pass it to the configurator:
		// TODO get classification name from export() - add a parameter
		// and use it to choose the right taxon
		Element taxonNodeElement = factory.getTaxonNodesByName(taxonName);
		configurator.addSelectedTaxonNodeElements(taxonNodeElement);

		// get feature tree from taxon name/taxon node and pass it to the
		// configurator:
		List<Element> featureTrees = factory.getFeatureTrees();
		for (Element featureTreeElement : featureTrees) {
			featureTreeElement.getChild("uuid");
		}
		String featureTree = featureTrees.get(0).getChild("uuid").getValue();
		configurator.setFeatureTree(UUID.fromString(featureTree));

		// pass cdm exportfolder to configurator:
		configurator.setExportFolder(temporaryExportFolder);

		// create MediawikiOutputModule with or without mediawiki pages
		// namespace:
		if (wikiPageNamespace == null
				|| wikiPageNamespace.replaceAll(" ", "").equals("")) {
			wikiOutputModule = new MediawikiOutputModule();
		} else {
			wikiOutputModule = new MediawikiOutputModule(wikiPageNamespace);
		}

		// set username to wikiOutModule for having it in the history of the
		// page
		// then it will be the same as the username that is used for the actual
		// mediawiki import.
		((MediawikiOutputModule) wikiOutputModule).setUsername(wikiLoginUid);

		// if we actually export from the cdm and not from a file we run the
		// Publisher
		// with the wikiOutputModule
		// else we run the wikiOutputModule with an input document (cdm exportes
		// xml) from file
		if (usePublisher) {
			List<IPublishOutputModule> modules = new ArrayList<IPublishOutputModule>();
			modules.add(wikiOutputModule);
			configurator.setOutputModules(modules);

			// do export from cdm to mediawiki xml file
			Publisher.publish(configurator);

		} else {
			wikiOutputModule.output(externalDocument,
					configurator.getExportFolder(),
					configurator.getProgressMonitor());
		}

		// we get the whole filename that the wikiOutputModule created
		mediawikiFileWithPath = ((MediawikiOutputModule) wikiOutputModule)
				.getFilePath();

		// if we want to upload images or save the cdm exported document,
		// we put it to a field
		if ((usePublisher && !deleteOutputFiles) || importImages) {
			// the cdm output where we want to fetch the urls of the
			// images:
			cdmOutputDocument = ((MediawikiOutputModule) wikiOutputModule)
					.getInputDocument();
		}

		// if we just created the cdm exported xml and want to
		// keep all the output, we save the cdm exported document in a file
		if (usePublisher && !deleteOutputFiles) {
			saveCdmXmlExportedDocument(temporaryExportFolder, cdmOutputDocument);
		}
		// import into mediawiki
		if (import2Mediawiki) {
			uploadToMediawiki(wikiUrl, wikiLoginUid, passwd);
		}

		if (importImages) {
			downloadAllImages();
		}

		if (deleteOutputFiles) {
			deleteOutputFiles();
			logger.info("deleted temporary file(s)");
		}
	}

	/**
	 * @param exportFolder
	 * @param cdmOutputDocument
	 */
	private void saveCdmXmlExportedDocument(File exportFolder,
			Document cdmOutputDocument) {
		XMLOutputter xmlOutput = new XMLOutputter();

		cdm_output_file = exportFolder
				+ File.separator
				+ wikiOutputModule
						.generateFilenameWithDate(CDM_EXPORT_FILE_NAME);

		// display nice nice
		xmlOutput.setFormat(Format.getPrettyFormat());
		try {
			xmlOutput
					.output(cdmOutputDocument, new FileWriter(cdm_output_file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("created CDM output file: " + cdm_output_file + ".");
	}

	private void deleteOutputFiles() {
		File file = new File(mediawikiFileWithPath);
		file.delete();
	}

	/**
	 * uploads a given mediawiki xml file to a mediawiki - does only third (last) step
	 * of the whole export process
	 * 
	 * @param inputFilePath
	 * @param wikiUrl
	 * @param wikiUser
	 * @param passwd
	 * @param deleteOutputFile
	 */
	public void uploadToMediawiki(String inputFilePath, String wikiUrl,
			String wikiUser, String passwd) {
		mediawikiFileWithPath = inputFilePath;
		uploadToMediawiki(wikiUrl, wikiUser, passwd);
	}

	/*
	 * @author l.morris
	 */
	private void uploadToMediawiki(String wikiUrl, String wikiUser,
			String passwd) {

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
			org.jdom.Document document = getDocument(mediawikiFileWithPath);
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

	private void downloadAllImages() {
		//TODO
	}

	/**
	 * @param url
	 * @throws MalformedURLException
	 * @throws IOException
	 * TODO give a unique id to each image name
	 * 			but this has to be done also in the wikioutput then
	 */
	private static void downloadImage(String url) throws MalformedURLException,
			IOException {
		URL imageUrl = new URL(url);
		String[] arr = url.split(File.separator);
		String filename = arr[arr.length - 1];
		System.out.println(filename);
		String filePath = temporaryImageExportFolder.getAbsolutePath()
				+ File.separator + filename;
		File imageFile = new File(filePath);
		System.out.println(imageFile.getAbsolutePath());
		FileUtils.copyURLToFile(imageUrl, new File(filePath));
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
