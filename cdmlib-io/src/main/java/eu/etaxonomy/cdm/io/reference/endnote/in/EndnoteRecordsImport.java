/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

/**
 * This is EndNote import format
 * 
 * EndNote is a commercial reference management software package, 
 * used to manage bibliographies and references when writing essays and articles.
 */
package eu.etaxonomy.cdm.io.reference.endnote.in;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.reference.IPrintedUnitBase;
import eu.etaxonomy.cdm.model.reference.IPublicationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
/**
 * @author a.bukhman
 *
 */
@Component
public class EndnoteRecordsImport extends EndNoteImportBase implements ICdmIO<EndnoteImportState> {
	private static final Logger logger = Logger.getLogger(EndnoteRecordsImport.class);

	private static int modCount = 1000;
	ReferenceFactory refFactory = ReferenceFactory.newInstance();
	
	public EndnoteRecordsImport(){
		super();
	}
	
	@Override
	public boolean doCheck(EndnoteImportState state){
		boolean result = true;
		return result;
	}
		
	@Override
	public boolean doInvoke(EndnoteImportState state){
		logger.info("start make XML ...");
		boolean success = true;
		String childName;
		boolean obligatory;
		
		MapWrapper<Team> authorMap = (MapWrapper<Team>)state.getStore(ICdmIO.TEAM_STORE);
		
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		/*
		Map<String, ReferenceBase> map_article = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_book = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_book_section = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_journal = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_thesis = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_patent = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_proceedings = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_cdDvd = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_report = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_database = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_webPage = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_generic = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_printSeries = new HashMap<String, ReferenceBase>();
		Map<String, ReferenceBase> map_personalCommunication = new HashMap<String, ReferenceBase>();
		 */
		IReferenceService referenceService = getReferenceService();
		
		EndnoteImportConfigurator config = state.getConfig();
		Element elXml = getXmlElement(config);
		Namespace tcsNamespace = config.getEndnoteNamespace();
		
		logger.info("start make Records-Element ...");
		DoubleResult<Element, Boolean> doubleResult;	
		 
		childName = "records";
		obligatory = false;
		doubleResult = XmlHelp.getSingleChildElement(elXml, childName, tcsNamespace, obligatory);
		Element elRecords = doubleResult.getFirstResult();
		success &= doubleResult.getSecondResult();
		
		//TODO:was soll das??? kann raus?
		elRecords.getAttributes();

		logger.info("start make Record-Elementen ...");
	    String tcsElementName = "record";
		
		List<Element> elRecordList = (List<Element>)elRecords.getChildren(tcsElementName, tcsNamespace);
		ReferenceBase reference = null;	
		TeamOrPersonBase<?> author = null;
		
		
		
		int i = 0;
		// for each Record in Endnote
		for (Element elRecord : elRecordList){
			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}
			//create Record element
			CdmBase cdmBase = null;   
			
			logger.info("start make ref-type ...");
			childName = "ref-type";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elRef_type = doubleResult.getFirstResult();
			
			if (elRef_type != null) {	
				String strName_reftype = elRef_type.getAttributeValue("name");			
				if (strName_reftype.equals("Article")) {
					reference =  refFactory.newArticle();
				}else if (strName_reftype.equals("Book")){
					reference =  refFactory.newBook();
				}else if (strName_reftype.equals("Book Section")){
					reference =  refFactory.newBookSection();
				}else if (strName_reftype.equalsIgnoreCase("Patent")) {
					reference =  refFactory.newPatent();
				}else if (strName_reftype.equalsIgnoreCase("Personal Communication")){
					reference = refFactory.newPersonalCommunication();
				}else if (strName_reftype.equalsIgnoreCase("Journal")) {
					reference = refFactory.newJournal();
				}else if (strName_reftype.equalsIgnoreCase("CdDvd")) {
					reference = refFactory.newCdDvd();
				}else if (strName_reftype.equalsIgnoreCase("Database")) {
					reference = refFactory.newDatabase();
				}else if (strName_reftype.equalsIgnoreCase("WebPage")) {
					reference = refFactory.newWebPage();
				}else if (strName_reftype.equalsIgnoreCase("Report")) {
					reference = refFactory.newReport();
				}else if (strName_reftype.equalsIgnoreCase("Thesis")) {
					reference = refFactory.newThesis();
				}else if (strName_reftype.equalsIgnoreCase("Print Series")){
					reference = refFactory.newPrintSeries();
				}else if (strName_reftype.equals("Journal Article")){
					reference = refFactory.newArticle();
				}else if (strName_reftype.equalsIgnoreCase("Conference Proceedings")){
					reference = refFactory.newProceedings();
				}else if (strName_reftype.equalsIgnoreCase("Web Page")){
					reference = refFactory.newWebPage();
				}else {
					logger.warn("The type was not found...");
					reference = refFactory.newGeneric();
					success = false;
				}		 			
			}
			
		
			
			
			Team authorTeam = Team.NewInstance();		 
			
			logger.info("start make database ...");
			childName = "database";
			obligatory = false;
			// get the database...
			doubleResult = XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			
			success &= doubleResult.getSecondResult();
			//vorher war es: Element elDatabase = new Element("database");
			Element elDatabase = doubleResult.getFirstResult();
						
			
				
			logger.info("start make source-app ...");
			childName = "source-app";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elSource_app = doubleResult.getFirstResult();
			if (elSource_app != null) {
				
				String strName_app = elSource_app.getAttributeValue("name");
				String strVersion = elSource_app.getAttributeValue("version");
			}
	// --- reference number -----
			logger.info("start make rec-number ...");
			childName = "rec-number";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elRec_number = doubleResult.getFirstResult();
			String number = elRec_number.getTextNormalize(); 
			
		
			if (number != null) {  
				int num = Integer.parseInt(number);
				//warum wird die reference number in cdmBase geschrieben???
				reference.setId(num);
				cdmBase.setId(num);
			} else {
				
				// TODO: was hat der Typ mit cdmBase zu tun, warum wurde er nicht gefunden, wenn cdmBase = null???
				logger.warn("The type was not found...");
				success = false;
			}
		// ---- foreign keys ----	 
			// <key app="EN" db-id="v59px0rxzpx2sre9re8xrs9nwaw0dsfefwp9">10</key> ... 
			// the number is corresponding to the rec-number, probably because the data come out of the same db
			logger.info("start make foreign-keys ...");
			childName = "foreign-keys";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elForeing_keys = doubleResult.getFirstResult();
			if (elForeing_keys!= null) {
				childName = "key";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elForeing_keys, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elKey = doubleResult.getFirstResult();
				if (elKey != null) {
					String strApp = elKey.getAttributeValue("app");
					String strDb_Id = elKey.getAttributeValue("db-id");
				}
			}
			
			
			//--- Authors -----
			logger.info("start make contributors ...");
			childName = "contributors";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elContributors = doubleResult.getFirstResult();
			StringBuilder authorBilder = new StringBuilder();
			if (elContributors != null) {
				childName = "authors";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elContributors, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elAuthors = doubleResult.getFirstResult();
			
				if (elAuthors !=null){
					childName = "author";
					obligatory = false;
					
					doubleResult =  XmlHelp.getSingleChildElement(elAuthors, childName, tcsNamespace, obligatory);
					//maybe it would be better to use XmlHelp.getChildren(); because <Authors> only contains author tags and we want to have all authors, not only one.
					List<Element> authors = elAuthors.getChildren();
					//success &= doubleResult.getSecondResult();
					//Element elAuthor = doubleResult.getFirstResult();
					
					Person pAuthor;
					for (Element elAuthor : authors){
						String name = elAuthor.getValue();
						//first approach atomization only for names like lastname, firstname
						Element el_styleAuthor = elAuthor.getChild("style");
						name = el_styleAuthor.getValue();
						if (name.contains(",")){
							String lastName = name.substring(0,name.indexOf(","));
							String firstName = name.substring(name.indexOf(",")+1, name.length());
						} 
						pAuthor = Person.NewTitledInstance(name);
						authorTeam.addTeamMember(pAuthor);
					}
					
					reference.setAuthorTeam(authorTeam);
					
					
					/*
					if (elAuthor!=null) {
						//the attributes do not exist??? it looks like this:
						//<author>
						//   <style face="normal" font="default" size="100%">ACHILLI, J.</style>
						// </author>
						
						String strCorp_name = elAuthor.getAttributeValue("corp-name");			
						String strFirst_name = elAuthor.getAttributeValue("first-name");
						String strInitials = elAuthor.getAttributeValue("initials");
						String strLast_name = elAuthor.getAttributeValue("last-name");
						String strMiddle_initial = elAuthor.getAttributeValue("middle-initial");
						String strRole = elAuthor.getAttributeValue("role");
						String strSalutation = elAuthor.getAttributeValue("salutation");
						String strSuffix = elAuthor.getAttributeValue("suffix");
						String strTitle = elAuthor.getAttributeValue("title");
						
						childName = "style";
						obligatory = false;
						doubleResult =  XmlHelp.getSingleChildElement(elAuthor, childName, tcsNamespace, obligatory);
						success &= doubleResult.getSecondResult();
						Element elStyle = doubleResult.getFirstResult();
					
						if (elStyle != null) {
							String strColor = elStyle.getAttributeValue("color");
							String strFace = elStyle.getAttributeValue("face");
							String strFont = elStyle.getAttributeValue("font");
							String strSize = elStyle.getAttributeValue("size");
							String author_style =  elStyle.getTextNormalize();
							// authorTeam is empty... and why it is set, if elStyle!=null???
							reference.setAuthorTeam(authorTeam);
						    authorTeam.setNomenclaturalTitle(author_style);						  
					}
				}*/
			}	
				
				// --- secondary authors ---- there is no secondary author in cdm references... and there are no secondary authors in example file...
				/*
				logger.info("start make secondary-authors ...");
				childName = "secondary-authors";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elContributors, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elSecondary_Authors = doubleResult.getFirstResult();
				if (elSecondary_Authors != null) {
					childName = "author";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elSecondary_Authors, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elSecondary_Author = doubleResult.getFirstResult();
					if (elSecondary_Author != null) {
						String strsecondaryCorp_name = elSecondary_Author.getAttributeValue("corp-name");
						String strsecondaryFirst_name = elSecondary_Author.getAttributeValue("first-name");
						String strsecondaryInitials = elSecondary_Author.getAttributeValue("initials");
						String strsecondaryLast_name = elSecondary_Author.getAttributeValue("last-name");
						String strsecondaryMiddle_initial = elSecondary_Author.getAttributeValue("middle-initial");
						String strsecondaryRole = elSecondary_Author.getAttributeValue("role");
						String strsecondarySalutation = elSecondary_Author.getAttributeValue("salutation");
						String strsecondarySuffix = elSecondary_Author.getAttributeValue("suffix");
						String strsecondaryTitle = elSecondary_Author.getAttributeValue("title");
			
						childName = "style";
						obligatory = false;
						doubleResult =  XmlHelp.getSingleChildElement(elSecondary_Author, childName, tcsNamespace, obligatory);
						success &= doubleResult.getSecondResult();
						Element elStyle_secondary = doubleResult.getFirstResult();
						if (elStyle_secondary!= null) {
							String strColor_secondary = elStyle_secondary.getAttributeValue("color");
							String strFace_secondary = elStyle_secondary.getAttributeValue("face");
							String strFont_secondary = elStyle_secondary.getAttributeValue("font");
							String strSize_secondary = elStyle_secondary.getAttributeValue("size");
							String  secondary_author=  elStyle_secondary.getTextNormalize();
 					 
							reference.setAuthorTeam(authorTeam);
							authorTeam.setTitleCache(secondary_author);
						}
					}
				}
				*/
				/** It was not used in this Implementation
				logger.info("start make tertiary-authors ...");
				childName = "tertiary-authors";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elContributors, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elTertiary_Authors = doubleResult.getFirstResult();
				if (elTertiary_Authors != null) {
			
					childName = "author";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elTertiary_Authors, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elTertiary_Author = doubleResult.getFirstResult();
					if (elTertiary_Author != null) {
						String strtertiaryCorp_name = elTertiary_Author.getAttributeValue("corp-name");
						String strtertiaryFirst_name = elTertiary_Author.getAttributeValue("first-name");
						String strtertiaryInitials = elTertiary_Author.getAttributeValue("initials");
						String strtertiaryLast_name = elTertiary_Author.getAttributeValue("last-name");
						String strtertiaryMiddle_initial = elTertiary_Author.getAttributeValue("middle-initial");
						String strtertiaryRole = elTertiary_Author.getAttributeValue("role");
						String strtertiarySalutation = elTertiary_Author.getAttributeValue("salutation");
						String strtertiarySuffix = elTertiary_Author.getAttributeValue("suffix");
						String strtertiaryTitle = elTertiary_Author.getAttributeValue("title");
			
						childName = "style";
						obligatory = false;
						doubleResult =  XmlHelp.getSingleChildElement(elTertiary_Author, childName, tcsNamespace, obligatory);
						success &= doubleResult.getSecondResult();
						Element elStyle_tertiary = doubleResult.getFirstResult();
						if (elStyle_tertiary != null) { 
							String strColor_tertiary = elStyle_tertiary.getAttributeValue("color");
							String strFace_tertiary = elStyle_tertiary.getAttributeValue("face");
							String strFont_tertiary = elStyle_tertiary.getAttributeValue("font");
							String strSize_tertiary = elStyle_tertiary.getAttributeValue("size");
						}
					}
				}
				logger.info("start make subsidiary-authors ...");
				childName = "subsidiary-authors";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elContributors, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elSubsidiary_Authors = doubleResult.getFirstResult();
				if (elSubsidiary_Authors != null) {
					childName = "author";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elSubsidiary_Authors, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elSubsidiary_Author = doubleResult.getFirstResult();
					if (elSubsidiary_Author !=null){
						String strSubsidiaryCorp_name = elSubsidiary_Author.getAttributeValue("corp-name");
						String strSubsidiaryFirst_name = elSubsidiary_Author.getAttributeValue("first-name");
						String strSubsidiaryInitials = elSubsidiary_Author.getAttributeValue("initials");
						String strSubsidiaryLast_name = elSubsidiary_Author.getAttributeValue("last-name");
						String strSubsidiaryMiddle_initial = elSubsidiary_Author.getAttributeValue("middle-initial");
						String strSubsidiaryRole = elSubsidiary_Author.getAttributeValue("role");
						String strSubsidiarySalutation = elSubsidiary_Author.getAttributeValue("salutation");
						String strSubsidiarySuffix = elSubsidiary_Author.getAttributeValue("suffix");
						String strSubsidiaryTitle = elSubsidiary_Author.getAttributeValue("title");
			
						childName = "style";
						obligatory = false;
						doubleResult =  XmlHelp.getSingleChildElement(elSubsidiary_Author, childName, tcsNamespace, obligatory);
						success &= doubleResult.getSecondResult();
						Element elStyle_Subsidiary = doubleResult.getFirstResult();
						if (elStyle_Subsidiary != null) {
							String strColor_Subsidiary = elStyle_Subsidiary.getAttributeValue("color");
							String strFace_Subsidiary = elStyle_Subsidiary.getAttributeValue("face");
							String strFont_Subsidiary = elStyle_Subsidiary.getAttributeValue("font");
							String strSize_Subsidiary = elStyle_Subsidiary.getAttributeValue("size");
						}
					}
				}
				logger.info("start make translated-authors ...");
				childName = "translated-authors";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elContributors, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elTranslated_Authors = doubleResult.getFirstResult();
				if (elTranslated_Authors != null) {
					childName = "author";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elTranslated_Authors, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elTranslated_Author = doubleResult.getFirstResult();
					if (elTranslated_Author !=null){
						String strTranslatedCorp_name = elTranslated_Author.getAttributeValue("corp-name");
						String strTranslatedFirst_name = elTranslated_Author.getAttributeValue("first-name");
						String strTranslatedInitials = elTranslated_Author.getAttributeValue("initials");
						String strTranslatedLast_name = elTranslated_Author.getAttributeValue("last-name");
						String strTranslatedMiddle_initial = elTranslated_Author.getAttributeValue("middle-initial");
						String strTranslatedRole = elTranslated_Author.getAttributeValue("role");
						String strTranslatedSalutation = elTranslated_Author.getAttributeValue("salutation");
						String strTranslatedSuffix = elTranslated_Author.getAttributeValue("suffix");
						String strTranslatedTitle = elTranslated_Author.getAttributeValue("title");
			
						childName = "style";
						obligatory = false;
						doubleResult =  XmlHelp.getSingleChildElement(elTranslated_Author, childName, tcsNamespace, obligatory);
						success &= doubleResult.getSecondResult();
						Element elStyle_Translated = doubleResult.getFirstResult();
						if (elStyle_Translated!= null) {
							String strColor_Translated = elStyle_Translated.getAttributeValue("color");
							String strFace_Translated = elStyle_Translated.getAttributeValue("face");
							String strFont_Translated = elStyle_Translated.getAttributeValue("font");
							String strSize_Translated = elStyle_Translated.getAttributeValue("size");
						}
					}
				}
				*/
			}
			 
			// --- author address --- in endnote 7 it was "AUTHOR_ADDRESS", in Endnote 8 "auth_address" 
			
			logger.info("start make auth-address ...");
			childName = "auth-address";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elAuth_address = doubleResult.getFirstResult();
			
			if (elAuth_address != null){
			
				String address = elAuth_address.getValue();
				
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elAuth_address, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_address = doubleResult.getFirstResult();
				String strColor_address = elStyle_address.getAttributeValue("color");
				String strFace_address = elStyle_address.getAttributeValue("face");
				String strFont_address = elStyle_address.getAttributeValue("font");
				String strSize_address = elStyle_address.getAttributeValue("size");
				String address_style = elStyle_address.getTextNormalize();
				
				Contact contact =  new Contact();
				Address cdmAddress = Address.NewInstance();
				cdmAddress.setLocality(address);
				//reference.setAuthorTeam(authorTeam);
				authorTeam.setContact(contact);		 
				contact.addAddress(cdmAddress);
				
			}
			
			/*
			logger.info("start make auth-affilation ...");
			childName = "auth-affiliaton";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elAuth_affilation = doubleResult.getFirstResult();
			if (elAuth_affilation != null) {
				
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elAuth_affilation, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_affilation = doubleResult.getFirstResult();
				String strColor_affilation = elStyle_affilation.getAttributeValue("color");
				String strFace_affilation = elStyle_affilation.getAttributeValue("face");
				String strFont_affilation = elStyle_affilation.getAttributeValue("font");
				String strSize_affilation = elStyle_affilation.getAttributeValue("size");
				
				String affilation = elStyle_affilation.getTextNormalize();
				reference.addExtension(affilation, ExtensionType.AREA_OF_INTREREST());
			}
				*/
			
			// ---- titles ----
			logger.info("start make titles ...");
			childName = "titles";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elTitles = doubleResult.getFirstResult();
			if (elTitles != null) {
		
				childName = "title";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elTitles, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elTitle = doubleResult.getFirstResult();
				if (elTitle != null) {
					
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elTitle, childName, tcsNamespace, obligatory);
					List<Element> elList = XmlHelp.getMultipleChildElement(elTitle, childName, tcsNamespace, obligatory);
					StringBuilder title_new = new StringBuilder();
					for (int a = 0; a < elList.size(); a++){
						doubleResult.setFirstResult(elList.get(a));
						doubleResult.setSecondResult(true);
						success &= doubleResult.getSecondResult();
		
						Element el_Title = doubleResult.getFirstResult();
						String title = el_Title.getText();			 
						title_new.append(title+" ");
				
						if (el_Title != null) {
						
							String strColor_Title = el_Title.getAttributeValue("color");
							String strFace_Title = el_Title.getAttributeValue("face");
							String strFont_Title = el_Title.getAttributeValue("font");
							String strSize_Title = el_Title.getAttributeValue("size");
							String strName_reftype = elRef_type.getAttributeValue("name");
							//title_new.toString();
							reference.setTitle(title_new.toString());
							//if (strName_reftype.equals("Article")) {
								referenceMap.put(title_new.toString(), (ReferenceBase) reference);
								//ReferenceBase give_article = map_article.get(title_new.toString());
							/*	
								
							}else if (strName_reftype.equals("Book")) {
								referenceMap.put(title_new.toString(), reference);
								//ReferenceBase give_book = map_book.get(title_new.toString());
								
								
							}else if (strName_reftype.equals("Book Section")){
								referenceMap.put(title_new.toString(), reference);
								//ReferenceBase give_book_section = map_book_section.get(title_new.toString());
								
								
							}else if (strName_reftype.equalsIgnoreCase("Patent")) {
								referenceMap.put(title_new.toString(), reference);
								//ReferenceBase give_patent = map_patent.get(title_new.toString());
								
							}else if (strName_reftype.equalsIgnoreCase("Personal Communication")){
								referenceMap.put(title_new.toString(), reference);
								
							}else if (strName_reftype.equalsIgnoreCase("Journal")) {
								reference.put(title_new.toString(), reference);
								
							}else if (strName_reftype.equalsIgnoreCase("CdDvd")) {
								map_cdDvd.put(title_new.toString(), reference);
								
							}else if (strName_reftype.equalsIgnoreCase("Database")) {
								map_database.put(title_new.toString(), reference);
								
							}else if (strName_reftype.equalsIgnoreCase("WebPage")) {
								map_webPage.put(title_new.toString(), reference);
								
							}else if (strName_reftype.equalsIgnoreCase("Report")) {
								map_report.put(title_new.toString(), reference);
								
							}else if (strName_reftype.equalsIgnoreCase("Thesis")) {
								map_thesis.put(title_new.toString(), reference);
								
							}else if (strName_reftype.equalsIgnoreCase("Print Series")){
								map_printSeries.put(title_new.toString(), reference);
								
							}else if (strName_reftype.equals("Journal Article")){
								map_article.put(title_new.toString(), reference);
								
							}else if (strName_reftype.equalsIgnoreCase("Conference Proceedings")){
								map_proceedings.put(title_new.toString(), reference);
								
							}else {
								logger.warn("The type was not found...");
								map_generic.put(title_new.toString(), reference);
								
								success = false; 
							}*/		 
						}
					}
				}
			
				childName = "secondary-title";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elTitles, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elSecondary_title = doubleResult.getFirstResult();
				if (elSecondary_title!=null) {
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elSecondary_title, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_Secondary_title = doubleResult.getFirstResult();
					if (elStyle_Secondary_title != null) {
						String strColor_Secondary_title = elStyle_Secondary_title.getAttributeValue("color");
						String strFace_Secondary_title = elStyle_Secondary_title.getAttributeValue("face");
						String strFont_Secondary_title = elStyle_Secondary_title.getAttributeValue("font");
						String strSize_Secondary_title = elStyle_Secondary_title.getAttributeValue("size");
						
						
						String secondary_title = elRef_type.getValue();
						//String secondary_title = elStyle_Secondary_title.getTextNormalize();
						 
						if (reference.getType().equals(ReferenceType.Article)){
				    		if (secondary_title != null) {
				    			ReferenceBase journal = referenceMap.get(secondary_title);
				    			if (journal == null){
				    				journal = refFactory.newJournal();
				    				journal.setTitle(secondary_title);
				    				referenceMap.put(secondary_title, journal);
				    			}
				    			reference.setInJournal(journal);
				    		}
						}else if (reference.getType().equals(ReferenceType.BookSection)){
							if (secondary_title != null) {
				    			ReferenceBase book = referenceMap.get(secondary_title);
				    			if (book == null){				    				
				    				book = refFactory.newBook();
				    				book.setTitle(secondary_title);
				    				referenceMap.put(secondary_title, book);
				    			}
				    			reference.setInJournal(book);
				    		}
				    	}else if (reference.getType().equals(ReferenceType.InProceedings)){
				    		if (secondary_title != null){
				    			
				    			ReferenceBase proceedings = referenceMap.get(secondary_title);
				    			if (proceedings == null){
				    				proceedings = refFactory.newProceedings();
					    			proceedings.setTitle(secondary_title);
					    			referenceMap.put(secondary_title, proceedings);
				    			}
				    			reference.setInProceedings(proceedings);
				    			
				    		}
				    	}
						//TODO: maybe has to be continued...
									
					}
				}
				
				
			}
		 
		//------------ periodical -----------
			
			logger.info("start make periodical ...");
			childName = "periodical";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elPeriodical = doubleResult.getFirstResult();
			if (elPeriodical != null) {
				
				childName = "full-title";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elPeriodical, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elFull_title = doubleResult.getFirstResult();
			    if (elFull_title != null) {
			    	
			    	childName = "style";
			    	obligatory = false;
			    	doubleResult =  XmlHelp.getSingleChildElement(elFull_title, childName, tcsNamespace, obligatory);
			    	success &= doubleResult.getSecondResult();
			    	Element elStyle_Full_title = doubleResult.getFirstResult();
			    	
			    	
			    	//String strName_reftype = elRef_type.getAttributeValue("name");
			    	String periodical = elStyle_Full_title.getValue();
			    	
			    	if (reference.getType().equals("Article")){		    		
			    		
			    		if (periodical != null) {	    			 	
			    			ReferenceBase give_journal = referenceMap.get(periodical);
							if (give_journal!= null && reference.getInJournal() == null) {
								reference.setInJournal(give_journal);	
								give_journal.setTitle(periodical);
							} else {
								ReferenceBase journal = refFactory.newJournal();
								journal.setTitle(periodical);
								reference.setInJournal(journal);		 
								referenceMap.put(periodical, journal);
							}
							
			    		}
			    	} else if (reference.getType().equals(ReferenceType.InProceedings)){
			    		if (periodical != null){
			    			
			    			ReferenceBase proceedings = referenceMap.get(periodical);
			    			if (proceedings == null){
			    				proceedings = refFactory.newProceedings();
				    			proceedings.setTitle(periodical);
				    			referenceMap.put(periodical, proceedings);
			    			}
			    			reference.setInProceedings(proceedings);
			    			
			    		}
			    	}	 	
			    }
			     
			    
			}
			
			// -----------pages ---------------------
			logger.info("start make pages ...");
			childName = "pages";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elPages = doubleResult.getFirstResult();
			if (elPages != null) {
				String strEnd = elPages.getAttributeValue("end");
				String strStart = elPages.getAttributeValue("start");
			
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elPages, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Pages = doubleResult.getFirstResult();
				
			
				//String strName_reftype = elRef_type.getAttributeValue("name");
				String pages = elStyle_Pages.getValue();
				 
				reference.setPages(pages);
				 	
			}
		// ----volume ---------------
			logger.info("start make volume ...");
			childName = "volume";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elVolume = doubleResult.getFirstResult();
			if (elVolume != null) {
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elVolume, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Volume = doubleResult.getFirstResult();
				
				//String strName_reftype = elRef_type.getAttributeValue("name");
				String volume = elStyle_Volume.getValue();
				reference.setVolume(volume);
				
			}
		// --------- number/series ------------
			logger.info("start make number ...");
			childName = "number"; // In CDM it's "Series"
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elNumber = doubleResult.getFirstResult();
			if (elNumber != null) {
		
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elNumber, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Number = doubleResult.getFirstResult();
				
				String series = elStyle_Number.getText();
				
				reference.setSeries(series);
						
			}
			
		// --------------- edition ------------
			
			logger.info("start make edition ...");
			childName = "edition";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elEdition = doubleResult.getFirstResult();
			if (elEdition != null) {
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elEdition, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Edition = doubleResult.getFirstResult();
				String strColor_Edition = elStyle_Edition.getAttributeValue("color");
				String strFace_Edition = elStyle_Edition.getAttributeValue("face");
				String strFont_Edition = elStyle_Edition.getAttributeValue("font");
				String strSize_Edition = elStyle_Edition.getAttributeValue("size");
				
				//String strName_reftype = elRef_type.getAttributeValue("name");
				String edition = elStyle_Edition.getValue();
				
				reference.setEdition(edition);
				
			}
			
			
			// ------- dates ---------
			// <year>
            //		<style face="normal" font="default" size="100%">1993</style>
            // </year>
			childName = "dates";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elDates = doubleResult.getFirstResult();
			if (elDates != null) {
				
				childName = "year";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elDates, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elYear = doubleResult.getFirstResult();
				if (elYear != null) {
							
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elYear, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_Year = doubleResult.getFirstResult();
					String strColor_Year = elStyle_Year.getAttributeValue("color");
					String strFace_Year = elStyle_Year.getAttributeValue("face");
					String strFont_Year = elStyle_Year.getAttributeValue("font");
					String strSize_Year = elStyle_Year.getAttributeValue("size");
					
					String year = elStyle_Year.getValue();
					reference.setDatePublished(ImportHelper.getDatePublished(year));
					
				}
					
				logger.info("start make pub-dates ...");	 
				childName = "pub-dates";  //deadline - the name in Endnote Programm
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elDates, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elPub_dates = doubleResult.getFirstResult();
				if (elPub_dates != null) {
					
					childName = "date";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elPub_dates, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elDate = doubleResult.getFirstResult();
					if (elDate != null){
					
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elDate, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_Date = doubleResult.getFirstResult();
					String strColor_Date = elStyle_Date.getAttributeValue("color");
					String strFace_Date = elStyle_Date.getAttributeValue("face");
					String strFont_Date = elStyle_Date.getAttributeValue("font");
					String strSize_Date = elStyle_Date.getAttributeValue("size");
					
					String year = elStyle_Date.getText();
					reference.setDatePublished(ImportHelper.getDatePublished(year));
					}
				}
			}
			
			logger.info("start make pub-location ...");
		 	childName = "pub-location"; // activity location - the name in Endnote Programm
		 	obligatory = false;
		 	doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
		 	success &= doubleResult.getSecondResult();
		 	Element elPub_location = doubleResult.getFirstResult();
		 	if (elPub_location != null) {
		 	
		 		childName = "style";
		 		obligatory = false;
		 		doubleResult =  XmlHelp.getSingleChildElement(elPub_location, childName, tcsNamespace, obligatory);
		 		success &= doubleResult.getSecondResult();
		 		Element elStyle_Pub_location = doubleResult.getFirstResult();
		 		String strColor_Pub_location = elStyle_Pub_location.getAttributeValue("color");
		 		String strFace_Pub_location = elStyle_Pub_location.getAttributeValue("face");
		 		String strFont_Pub_location = elStyle_Pub_location.getAttributeValue("font");
		 		String strSize_Pub_location = elStyle_Pub_location.getAttributeValue("size");
		 		
		 		String place = elStyle_Pub_location.getTextNormalize();
		 		
		 		reference.setPlacePublished(place);
		 		
		 		
		 	}
		 	 
			logger.info("start make publisher ...");
			childName = "publisher";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elPublisher = doubleResult.getFirstResult();
			if (elPublisher != null) {
			
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elPublisher, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Publisher = doubleResult.getFirstResult();
				String strColor_Publisher = elStyle_Publisher.getAttributeValue("color");
				String strFace_Publisher = elStyle_Publisher.getAttributeValue("face");
				String strFont_Publisher = elStyle_Publisher.getAttributeValue("font");
				String strSize_Publisher = elStyle_Publisher.getAttributeValue("size");
				
				String publisher = elStyle_Publisher.getTextNormalize();
				reference.setPublisher(publisher);
				
			}
			
			
		
			logger.info("start make ISBN/ISNN ...");
			childName = "isbn";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elIsbn = doubleResult.getFirstResult();
			
			if (elIsbn != null) {
			
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elIsbn, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Isbn = doubleResult.getFirstResult();
				String strColor_Isbn = elStyle_Isbn.getAttributeValue("color");
				String strFace_Isbn = elStyle_Isbn.getAttributeValue("face");
				String strFont_Isbn = elStyle_Isbn.getAttributeValue("font");
				String strSize_Isbn = elStyle_Isbn.getAttributeValue("size");
				
				//String strName_reftype = elRef_type.getAttributeValue("name");
				String isbn = elStyle_Isbn.getTextNormalize();
				
				reference.setIsbn(isbn);
									 		
			}
			
			
			
			logger.info("start make electronic-resource-num ...");
			childName = "electronic-resource-num";  //DOI - the name in Endnote Programm
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elElectronic_resource_num = doubleResult.getFirstResult();
			if (elElectronic_resource_num != null) {
		
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elElectronic_resource_num, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Electronic_resource_num = doubleResult.getFirstResult();
				String strColor_Electronic_resource_num = elStyle_Electronic_resource_num.getAttributeValue("color");
				String strFace_Electronic_resource_num = elStyle_Electronic_resource_num.getAttributeValue("face");
				String strFont_Electronic_resource_num = elStyle_Electronic_resource_num.getAttributeValue("font");
				String strSize_Electronic_resource_num = elStyle_Electronic_resource_num.getAttributeValue("size");
				
				String dOI = elStyle_Electronic_resource_num.getTextNormalize();
				reference.addExtension(dOI, ExtensionType.DOI());
			}
			
			logger.info("start make abstract ...");
			childName = "abstract";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elAbstract = doubleResult.getFirstResult();
			if (elAbstract != null) {
			
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elAbstract, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Abstract = doubleResult.getFirstResult();
				String strColor_Abstract = elStyle_Abstract.getAttributeValue("color");
				String strFace_Abstract = elStyle_Abstract.getAttributeValue("face");
				String strFont_Abstract = elStyle_Abstract.getAttributeValue("font");
				String strSize_Abstract = elStyle_Abstract.getAttributeValue("size");
				
				String referenceAbstract = elStyle_Abstract.getTextNormalize();
				reference.setReferenceAbstract(referenceAbstract);
				
				
			}
			
			
			
			childName = "language";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elLanguage = doubleResult.getFirstResult();
			if (elLanguage != null) {
			
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elLanguage, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Language = doubleResult.getFirstResult();
				String strColor_Language = elStyle_Language.getAttributeValue("color");
				String strFace_Language = elStyle_Language.getAttributeValue("face");
				String strFont_Language = elStyle_Language.getAttributeValue("font");
				String strSize_Language = elStyle_Language.getAttributeValue("size");
				String label =  elStyle_Language.getTextNormalize();
				
				Language language =  Language.NewInstance();
				language.setLabel(label);
			}
			
			
			logger.info("start make urls ...");
			
			//<urls><related-urls><url><style face="normal" font="default" size="100%">www.dev.e-taxonomy.org</style></url></related-urls></urls>
			childName = "urls";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elUrls = doubleResult.getFirstResult();
			if (elUrls != null) {
				childName = "web-urls";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elUrls, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elWeb_urls = doubleResult.getFirstResult();
			
				childName = "url";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elWeb_urls, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elUrl = doubleResult.getFirstResult();
				if (elUrl != null) {
					String strHas_ut = elUrl.getAttributeValue("has-ut");
					String strPpv_app = elUrl.getAttributeValue("ppv-app");
					String strPpv_ref = elUrl.getAttributeValue("ppv-ref");
					String strPpv_ut = elUrl.getAttributeValue("ppv-ut");
						
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elUrl, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_Url = doubleResult.getFirstResult();
					String strColor_Url = elStyle_Url.getAttributeValue("color");
					String strFace_Url = elStyle_Url.getAttributeValue("face");
					String strFont_Url = elStyle_Url.getAttributeValue("font");
					String strSize_Url = elStyle_Url.getAttributeValue("size");
					reference.setUri(elStyle_Url.getTextNormalize());
				}
			
				childName = "pdf-urls";//for attached files
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elUrls, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elPdf_urls = doubleResult.getFirstResult();
			
				childName = "url";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elPdf_urls, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elPdfUrl = doubleResult.getFirstResult();
				if (elPdfUrl !=null) {
					String strHas_ut_pdf = elPdfUrl.getAttributeValue("has-ut");
					String strPpv_app_pdf = elPdfUrl.getAttributeValue("ppv-app");
					String strPpv_ref_pdf = elPdfUrl.getAttributeValue("ppv-ref");
					String strPpv_ut_pdf = elPdfUrl.getAttributeValue("ppv-ut");
			
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elPdfUrl, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_PdfUrl = doubleResult.getFirstResult();
					String strColor_PdfUrl = elStyle_PdfUrl.getAttributeValue("color");
					String strFace_PdfUrl = elStyle_PdfUrl.getAttributeValue("face");
					String strFont_PdfUrl = elStyle_PdfUrl.getAttributeValue("font");
					String strSize_PdfUrl = elStyle_PdfUrl.getAttributeValue("size");
					reference.setUri(elStyle_PdfUrl.getText());
				}
			
				childName = "text-urls";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elUrls, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elText_urls = doubleResult.getFirstResult();
			
				childName = "url";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elText_urls, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elTextUrl = doubleResult.getFirstResult();
				if (elTextUrl != null) {
					String strHas_ut_text = elTextUrl.getAttributeValue("has-ut");
					String strPpv_app_text = elTextUrl.getAttributeValue("ppv-app");
					String strPpv_ref_text = elTextUrl.getAttributeValue("ppv-ref");
					String strPpv_ut_text = elTextUrl.getAttributeValue("ppv-ut");
			
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elTextUrl, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_TextUrl = doubleResult.getFirstResult();
					String strColor_TextUrl = elStyle_TextUrl.getAttributeValue("color");
					String strFace_TextUrl = elStyle_TextUrl.getAttributeValue("face");
					String strFont_TextUrl = elStyle_TextUrl.getAttributeValue("font");
					String strSize_TextUrl = elStyle_TextUrl.getAttributeValue("size");
					reference.setUri(elStyle_TextUrl.getText());
				}
			
				childName = "related-urls";//for "normal" urls
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elUrls, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elRelated_urls = doubleResult.getFirstResult();
			
				childName = "url";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elRelated_urls, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elRelatedUrl = doubleResult.getFirstResult();
				if (elRelatedUrl != null) {
			
					String strHas_ut_related = elRelatedUrl.getAttributeValue("has-ut");
					String strPpv_app_related = elRelatedUrl.getAttributeValue("ppv-app");
					String strPpv_ref_related = elRelatedUrl.getAttributeValue("ppv-ref");
					String strPpv_ut_related = elRelatedUrl.getAttributeValue("ppv-ut");
			
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elRelatedUrl, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_RelatedUrl = doubleResult.getFirstResult();
					String strColor_RelatedUrl = elStyle_RelatedUrl.getAttributeValue("color");
					String strFace_RelatedUrl = elStyle_RelatedUrl.getAttributeValue("face");
					String strFont_RelatedUrl = elStyle_RelatedUrl.getAttributeValue("font");
					String strSize_RelatedUrl = elStyle_RelatedUrl.getAttributeValue("size");	
					reference.setUri(elStyle_RelatedUrl.getText());
				}
			
				childName = "image-urls";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elUrls, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elImage_urls = doubleResult.getFirstResult();
			
				childName = "url";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elImage_urls, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elImageUrl = doubleResult.getFirstResult();
				if (elImageUrl != null) {
					
					String strHas_ut_image = elImageUrl.getAttributeValue("has-ut");
					String strPpv_app_image = elImageUrl.getAttributeValue("ppv-app");
					String strPpv_ref_image = elImageUrl.getAttributeValue("ppv-ref");
					String strPpv_ut_image = elImageUrl.getAttributeValue("ppv-ut");
			
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elImageUrl, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_ImageUrl = doubleResult.getFirstResult();
					String strColor_ImageUrl = elStyle_ImageUrl.getAttributeValue("color");
					String strFace_ImageUrl = elStyle_ImageUrl.getAttributeValue("face");
					String strFont_ImageUrl = elStyle_ImageUrl.getAttributeValue("font");
					String strSize_ImageUrl = elStyle_ImageUrl.getAttributeValue("size");
					reference.setUri(elStyle_ImageUrl.getText());
				}
			}
			
			
			authorMap.put(elRec_number, (Team) author);
			referenceMap.put(elRec_number, reference);	 
		}	
		
		logger.info(i + " Records handled. Saving ...");
		referenceService.save(referenceMap.objects());
		logger.info("end make Records ...");
		return success;
	}	
			
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(EndnoteImportState state){
		EndnoteImportConfigurator tcsConfig = state.getConfig();
		return (! tcsConfig.isDoRecords());
	}
}
