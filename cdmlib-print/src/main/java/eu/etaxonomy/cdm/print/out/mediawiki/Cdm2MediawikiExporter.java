package eu.etaxonomy.cdm.print.out.mediawiki;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.print.IXMLEntityFactory;
import eu.etaxonomy.cdm.print.PublishConfigurator;
import eu.etaxonomy.cdm.print.Publisher;
import eu.etaxonomy.cdm.print.out.IPublishOutputModule;

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

	private static final String IMAGES_FOLDER = "images";

	private static final String FILESEPARATOR = File.separator;

	//constants
	private static final String MEDIAWIKI_CDM_SUB_DIR = "mediawiki_tmp";

	private static final String IMAGE_DIR = MEDIAWIKI_CDM_SUB_DIR
			+ File.separator + "images";

	private static final String CDM_EXPORT_FILE_NAME = "cdm_output";

	private static final String PAGE_SUMMARY = "automatic import from CDM";

	//-------------------

	private static final Logger logger = Logger
			.getLogger(Cdm2MediawikiExporter.class);

	private PublishConfigurator configurator = PublishConfigurator.NewRemoteInstance();
			
	private IXMLEntityFactory factory;

	// where the mediawiki xml code is stored
	private String mediawikiFileWithPath = null;

	// where the cdm exported xml can be stored
	private String cdm_output_file = null;

	private Document cdmOutputDocument = null;
	private Document externalDocument = null;

	private MediawikiOutputModule wikiOutputModule;

	private File temporaryExportFolder = null;

	private List<String> localImages;

	
	public void export(String serviceUrl, UUID taxonNodeUuid, UUID treeNodeUuid,  String wikiUrl,
			String wikiLoginUid, String passwd, String wikiPageNamespace,
			boolean import2Mediawiki, boolean deleteOutputFiles,
			boolean importImages) throws MalformedURLException {

		//TODO
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
	public void export(String portalUrl, String serviceUrl, String taxonName, String classificationName, String wikiUrl,
			String wikiLoginUid, String passwd, String wikiPageNamespace,
			boolean import2Mediawiki, boolean deleteOutputFiles,
			boolean importImages) throws MalformedURLException {

		// get taxon node uuid from taxon name and pass it to the configurator:
				// TODO get classification name from export() - add a parameter
				// and use it to choose the right taxon

				// setup configurator
		setupConfigurator(serviceUrl);
		configurator.addSelectedTaxonNodeElements(factory.getTaxonNodesByName(taxonName, classificationName));
		
		
		export(portalUrl, serviceUrl, wikiUrl, wikiLoginUid, passwd,
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
	 * @param classificationName
	 * @param wikiUrl
	 * @param wikiLoginUid
	 * @param passwd
	 * @param wikiPageNamespace
	 * @param import2Mediawiki
	 * @param deleteOutputFiles
	 * @param importImages
	 * @throws MalformedURLException
	 */
	public void exportFromXmlFile(String portalUrl, String filename, String serviceUrl,
			 String wikiUrl, String wikiLoginUid,
			String passwd, String wikiPageNamespace, boolean import2Mediawiki,
			boolean deleteOutputFiles, boolean importImages)
					throws MalformedURLException {

		//put the document to a field:
		externalDocument = getDocument(filename);
		
//		setupConfigurator(serviceUrl);
		
		// and run export with usePublisher=false:
		export(portalUrl, serviceUrl, wikiUrl, wikiLoginUid, passwd,
				wikiPageNamespace, import2Mediawiki, deleteOutputFiles,
				importImages, false);

	}

	/*
	 * TODO: make passwd "unplain" MAYDO: pass more parameters e.g.: alternative
	 * stylesheet layout parameters (that may force the use of different
	 * stylesheet) export folder - we use a temporary so far boolean for telling
	 * if we want to keep the mediawiki xml file ...
	 */
	private void export(String portalUrl, String serviceUrl, String wikiUrl,
			String wikiLoginUid, String passwd, String wikiPageNamespace,
			boolean import2Mediawiki, boolean deleteOutputFiles,
			boolean importImages, boolean usePublisher)
					throws MalformedURLException {

		// create MediawikiOutputModule with or without mediawiki pages
		// namespace:
		if (wikiPageNamespace == null
				|| wikiPageNamespace.replaceAll(" ", "").equals("")) {
			wikiOutputModule = new MediawikiOutputModule(portalUrl);
		} else {
			wikiOutputModule = new MediawikiOutputModule(wikiPageNamespace, portalUrl);
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
			logger.info("read data from local file.");
			createTemporaryExportFolder();
			wikiOutputModule.output(externalDocument,
					temporaryExportFolder,
					DefaultProgressMonitor.NewInstance());
		}

		// we get the whole filename that the wikiOutputModule created
		mediawikiFileWithPath = ((MediawikiOutputModule) wikiOutputModule)
				.getFilePath();

		logger.info("mediawiki xml file created and saved to"+mediawikiFileWithPath);
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

		if (importImages){
			downloadImages();
		}
		else{
			logger.info("did not get images!");
		}
		
		if (import2Mediawiki && importImages && !(localImages.isEmpty())) {
			uploadImagesToMediawiki(wikiUrl, wikiLoginUid, passwd);
		}
		
		if (deleteOutputFiles) {
			deleteOutputFiles();
			logger.info("deleted temporary file(s)");
		}
	}

	/**
	 * 
	 */
	private void createTemporaryExportFolder() {
		temporaryExportFolder = CdmUtils.getCdmHomeSubDir(MEDIAWIKI_CDM_SUB_DIR);
		if (temporaryExportFolder != null) {
			logger.info("using " + temporaryExportFolder.getAbsolutePath()
					+ " as temporary directory.");
		} else {
			logger.error("could not create directory"
					+ temporaryExportFolder.getAbsolutePath());
			return;
		}
	}

	/**
	 * @param serviceUrl
	 * @throws MalformedURLException
	 */
	private void setupConfigurator(String serviceUrl)
			throws MalformedURLException {
		
		createTemporaryExportFolder();
		
		configurator.setWebserviceUrl(serviceUrl);
		factory = configurator.getFactory();

		// get feature tree from taxon name/taxon node and pass it to the
		// configurator:
		// TODO, get a feature tree name or uuid as method parameters 
		String featureTree = getDefaultFeatureTree();
		configurator.setFeatureTree(UUID.fromString(featureTree));

		// pass cdm exportfolder to configurator:
		configurator.setExportFolder(temporaryExportFolder);
	}

	/**
	 * @return
	 */
	private String getDefaultFeatureTree() {
		List<Element> featureTrees = factory.getFeatureTrees();
		for (Element featureTreeElement : featureTrees) {
			featureTreeElement.getChild("uuid");
		}
		String featureTree = featureTrees.get(0).getChild("uuid").getValue();
		return featureTree;
	}

	/**
	 * @param exportFolder
	 * @param cdmOutputDocument
	 */
	private void saveCdmXmlExportedDocument(File exportFolder,
			Document cdmOutputDocument) {
		//XMLOutputter xmlOutput = new XMLOutputter();

		cdm_output_file = exportFolder
				+ File.separator
				+ wikiOutputModule
				.generateFilenameWithDate(CDM_EXPORT_FILE_NAME);

		// display nice nice
		Format format = Format.getPrettyFormat();

		//JDOMParseException Invalid byte 2 of 3-byte UTF-8 sequence which occurs for e.g.
		//with German umlauts and French accents on characters
		format.setEncoding("ISO-8859-1");//"UTF-8");
		XMLOutputter xmlOutput = new XMLOutputter(format);	
		xmlOutput.setFormat(format);		

		try {
			xmlOutput.output(cdmOutputDocument, new FileWriter(cdm_output_file));			
                    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("saved CDM output file to: " + cdm_output_file + ".");
	}

	private void deleteOutputFiles() {
		logger.info("delete local files: ");
		File file = new File(mediawikiFileWithPath);
		file.delete();
		logger.info("deleted "+mediawikiFileWithPath+".");
		for (String localImage : localImages) {
			file= new File(localImage);
			file.delete();
			logger.info("deleted image "+localImage+".");
		}
		//TODO delete tmp folders
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
	public void uploadToMediawiki(String inputFilePath, String wikiUrl, String wikiLoginUid, String passwd) {
		mediawikiFileWithPath = inputFilePath;
		logger.info("reading file "+mediawikiFileWithPath+".");
		uploadToMediawiki(wikiUrl, wikiLoginUid, passwd);
	}

	/*
	 * @author l.morris
	 */
	private void uploadToMediawiki(String wikiUrl, String wikiLoginUid, String passwd) {

		// login to mediawiki
		
		WikiBot myBot = getBotAndLogin(wikiUrl, wikiLoginUid, passwd);

		
		try {

			// parse wiki xml file and import pages one by one
			// to mediawiki
			// MAYDO import whole file, with functionality from mediawiki API

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
			myBot.logout();
			logger.info("all pages uploaded and mediawiki logout.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			return;
		}

	}
	/**
	 * @param wikiUrl
	 * @param wikiLoginUid
	 * @param passwd
	 * @return
	 */
	private WikiBot getBotAndLogin(String wikiUrl, String wikiLoginUid,
			String passwd) {
		WikiBot myBot = new WikiBot(wikiUrl, wikiLoginUid, passwd);

		// login to mediawiki
		try {
			myBot.login();
		} catch (Exception e) {
			logger.info("Cannot log into Mediwiki: "+wikiUrl);
			e.printStackTrace();
		}

		logger.info("logged in to mediawiki as " + wikiLoginUid + ".");
		return myBot;
	}

	private void downloadImages() {
		org.jdom.Document document = wikiOutputModule.getInputDocument();
		localImages = new ArrayList<String>();
		
		try {
			List<Element> media_uris = XPath.selectNodes(document, "//Taxon/media/e/representations/e/parts/e/uri");
		
			if(media_uris.isEmpty()){
				logger.info("there are no images in the data.");
				return;
			}
			
			for (Element urlEl : media_uris) {
				String url=urlEl.getValue();
				URL imageUrl = new URL(url);
				String[] arr = url.split("/");
				String filename = arr[arr.length - 1];
				//String filePath = temporaryImageExportFolder.getAbsolutePath()
					//	+ File.separator + filename;
				String filePath = temporaryExportFolder.getAbsolutePath()
						+FILESEPARATOR + IMAGES_FOLDER +FILESEPARATOR+ filename;
				logger.info("downloading image " + url+" to "+filePath);
				
				FileUtils.copyURLToFile(imageUrl, new File(filePath));
				localImages.add(filePath);
			}
		
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void uploadImagesToMediawiki(String wikiUrl, String wikiLoginUid, String passwd) {
		WikiBot myBot = getBotAndLogin(wikiUrl, wikiLoginUid, passwd); 
		// get published output file
		

			for(String localUri : localImages){						
				
				try {
					uploadImage(myBot, localUri);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// logout		
		myBot.logout();
		logger.info("all images uploaded to mediawiki "+wikiUrl+" and logged out.");
	}

	/**
	 * @param filePath
	 * @throws MalformedURLException
	 * @throws IOException
	 * TODO give a unique id to each image name
	 * 			but this has to be done also in the wikioutput then
	 */
	private void uploadImage(WikiBot myBot, String filePath) throws MalformedURLException,
	IOException {
//		URL imageUrl = new URL(url);
//		String[] arr = url.split("/");
//		String filename = arr[arr.length - 1];
////		System.out.println(filename);
//		//String filePath = temporaryImageExportFolder.getAbsolutePath()
//			//	+ File.separator + filename;
//		String filePath = temporaryExportFolder.getAbsolutePath()
//				+FILESEPARATOR + IMAGES_FOLDER +FILESEPARATOR+ filename;
////		System.out.println(filePath);
//		File imageFile = new File(filePath);
//		logger.info("downloading image " + url);
		
//		FileUtils.copyURLToFile(imageUrl, new File(filePath));
		
		
		File imageFile = new File(filePath);
		String[] arr = filePath.split("/");
		String filename = arr[arr.length - 1];
		try {
			//Upload image to Mediawiki
			//TODO: Change text to give a description of the image
			myBot.uploadAFile(imageFile, filename, "some text", "no comment");
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("uploaded image " + imageFile.getName()+" to mediawiki.");
	}

	private Document getDocument(String filePath) {
		SAXBuilder saxBuilder = new SAXBuilder();		
		
		File file = new File(filePath);
		Document document = null;
		FileInputStream fileis;

		// converted file to document object
		try {
			//document = saxBuilder.build(file);
			fileis = new FileInputStream(file);
			BufferedInputStream in = new BufferedInputStream(fileis); 
			document = saxBuilder.build(in);
			
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			logger.error(e.getCause().getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return document;
	}

}
