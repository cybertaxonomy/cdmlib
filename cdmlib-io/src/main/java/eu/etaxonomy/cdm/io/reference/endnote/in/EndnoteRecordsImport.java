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

import java.net.URI;
import java.net.URISyntaxException;
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
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.reference.IPrintedUnitBase;
import eu.etaxonomy.cdm.model.reference.IPublicationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
/**
 * @author a.bukhman
 *
 */
@Component
public class EndnoteRecordsImport extends EndNoteImportBase implements ICdmIO<EndnoteImportState> {
	private static final Logger logger = Logger.getLogger(EndnoteRecordsImport.class);

	private static int modCount = 1000;

	public EndnoteRecordsImport(){
		super();
	}

	@Override
	public boolean doCheck(EndnoteImportState state){
		boolean result = true;
		return result;
	}

	@Override
	public void doInvoke(EndnoteImportState state){
		logger.info("start make XML ...");
		boolean success = true;
		String childName;
		boolean obligatory;

		MapWrapper<Team> authorMap = (MapWrapper<Team>)state.getStore(ICdmIO.TEAM_STORE);
		MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);

		Map<String, Reference> map_article = new HashMap<String, Reference>();
		Map<String, Reference> map_book = new HashMap<String, Reference>();
		Map<String, Reference> map_book_section = new HashMap<String, Reference>();
		Map<String, Reference> map_journal = new HashMap<String, Reference>();
		Map<String, Reference> map_thesis = new HashMap<String, Reference>();
		Map<String, Reference> map_patent = new HashMap<String, Reference>();
		Map<String, Reference> map_proceedings = new HashMap<String, Reference>();
		Map<String, Reference> map_cdDvd = new HashMap<String, Reference>();
		Map<String, Reference> map_report = new HashMap<String, Reference>();
		Map<String, Reference> map_database = new HashMap<String, Reference>();
		Map<String, Reference> map_webPage = new HashMap<String, Reference>();
		Map<String, Reference> map_generic = new HashMap<String, Reference>();
		Map<String, Reference> map_printSeries = new HashMap<String, Reference>();
		Map<String, Reference> map_personalCommunication = new HashMap<String, Reference>();

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
		elRecords.getAttributes();

		logger.info("start make Record-Elementen ...");
	    String tcsElementName = "record";
		String idNamespace = "record";
		List<Element> elRecordList = elRecords.getChildren(tcsElementName, tcsNamespace);
		Reference reference = null;
		TeamOrPersonBase<?> author = null;
		IPrintedUnitBase printedUnitBase = null;


		int i = 0;
		// for each Record in Endnote
		for (Element elRecord : elRecordList){
			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}
			List<String> elementList = new ArrayList<String>();
			//create Record element
			IPublicationBase publicationBase = null;
			CdmBase cdmBase = null;

			Reference article = ReferenceFactory.newArticle();
			Reference book = ReferenceFactory.newBook();
			Reference bookSection = ReferenceFactory.newBookSection();
			Reference thesis = ReferenceFactory.newThesis();
			Reference journal = ReferenceFactory.newJournal();
			Reference patent =  ReferenceFactory.newPatent();
			Reference generic = ReferenceFactory.newGeneric();
			Reference personalCommunication = ReferenceFactory.newPersonalCommunication();
			Reference proceedings  = ReferenceFactory.newProceedings();
			Reference printSeries = ReferenceFactory.newPrintSeries();
			Reference cdDvd = ReferenceFactory.newCdDvd();
			Reference database = ReferenceFactory.newDatabase();
			Reference report = ReferenceFactory.newReport();
			Reference webPage = ReferenceFactory.newWebPage();
			Institution school = Institution.NewInstance();
			Team authorship = Team.NewInstance();

			logger.info("start make database ...");
			childName = "database";
			obligatory = false;
			doubleResult = XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elDatabase = new Element ("database");

			if (elDatabase != null) {

				String strName = elDatabase.getAttributeValue("name");
				String strPath = elDatabase.getAttributeValue("path");
			}

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

			logger.info("start make rec-number ...");
			childName = "rec-number";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elRec_number = doubleResult.getFirstResult();
			String nummer = elRec_number.getTextNormalize();
			int num = Integer.parseInt(nummer);

			if (cdmBase != null) {
				reference.setId(num);
				cdmBase.setId(num);
			} else {
				logger.warn("The type was not found...");
				success = false;
			}

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

			logger.info("start make ref-type ...");
			childName = "ref-type";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elRef_type = doubleResult.getFirstResult();

			if (elRef_type != null) {
				String strName_reftype = elRef_type.getAttributeValue("name");
				if (strName_reftype.equals("Article")) {
					reference =  article;
				}else if (strName_reftype.equals("Book")){
					reference =  book;
				}else if (strName_reftype.equals("Book Section")){
					reference =  bookSection;
				}else if (strName_reftype.equalsIgnoreCase("Patent")) {
					reference =  patent;
				}else if (strName_reftype.equalsIgnoreCase("Personal Communication")){
					reference = personalCommunication;
				}else if (strName_reftype.equalsIgnoreCase("Journal")) {
					reference = journal;
				}else if (strName_reftype.equalsIgnoreCase("CdDvd")) {
					reference = cdDvd;
				}else if (strName_reftype.equalsIgnoreCase("Database")) {
					reference = database;
				}else if (strName_reftype.equalsIgnoreCase("WebPage")) {
					reference = webPage;
				}else if (strName_reftype.equalsIgnoreCase("Report")) {
					reference = report;
				}else if (strName_reftype.equalsIgnoreCase("Thesis")) {
					reference = thesis;
				}else if (strName_reftype.equalsIgnoreCase("Print Series")){
					reference = printSeries;
				}else if (strName_reftype.equals("Journal Article")){
					reference = article;
				}else if (strName_reftype.equalsIgnoreCase("Conference Proceedings")){
					reference = proceedings;
				}else if (strName_reftype.equalsIgnoreCase("Web Page")){
					reference = webPage;
				}else {
					logger.warn("The type was not found...");
					reference = generic;
					success = false;
				}
			}

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
					success &= doubleResult.getSecondResult();
					Element elAuthor = doubleResult.getFirstResult();
					if (elAuthor!=null) {
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

							reference.setAuthorship(authorship);
						    authorship.setNomenclaturalTitle(author_style);
					}
				}
			}
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

							reference.setAuthorship(authorship);
							authorship.setTitleCache(secondary_author, true);
						}
					}
				}
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

			logger.info("start make auth-address ...");
			childName = "auth-address";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elAuth_address = doubleResult.getFirstResult();

			if (elAuth_address != null){

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
				Address address = Address.NewInstance();
				reference.setAuthorship(authorship);
				authorship.setContact(contact);
				contact.addAddress(address);
				address.setLocality(address_style);
			}


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

						Element elStyle_Title = doubleResult.getFirstResult();
						String title = elStyle_Title.getText();
						title_new.append(title+" ");

						if (elStyle_Title != null) {

							String strColor_Title = elStyle_Title.getAttributeValue("color");
							String strFace_Title = elStyle_Title.getAttributeValue("face");
							String strFont_Title = elStyle_Title.getAttributeValue("font");
							String strSize_Title = elStyle_Title.getAttributeValue("size");
							String strName_reftype = elRef_type.getAttributeValue("name");
							title_new.toString();

							if (strName_reftype.equals("Article")) {
								map_article.put(title_new.toString(), article);
								Reference give_article = map_article.get(title_new.toString());
								give_article.setTitle(title_new.toString());
								reference=give_article;
							}else if (strName_reftype.equals("Book")) {
								map_book.put(title_new.toString(), book);
								Reference give_book = map_book.get(title_new.toString());
								give_book.setTitle(title_new.toString());
								reference=give_book;
							}else if (strName_reftype.equals("Book Section")){
								map_book_section.put(title_new.toString(), bookSection);
								Reference give_book_section = map_book_section.get(title_new.toString());
								give_book_section.setTitle(title_new.toString());
								reference=give_book_section;
							}else if (strName_reftype.equalsIgnoreCase("Patent")) {
								map_patent.put(title_new.toString(), patent);
								Reference give_patent = map_patent.get(title_new.toString());
								give_patent.setTitle(title_new.toString());
								reference=give_patent;
							}else if (strName_reftype.equalsIgnoreCase("Personal Communication")){
								personalCommunication.setTitle(title_new.toString());
								reference=personalCommunication;
							}else if (strName_reftype.equalsIgnoreCase("Journal")) {
								map_journal.put(title_new.toString(), journal);
								Reference give_journal = map_journal.get(title_new.toString());
								give_journal.setTitle(title_new.toString());
								reference=give_journal;
							}else if (strName_reftype.equalsIgnoreCase("CdDvd")) {
								map_cdDvd.put(title_new.toString(), cdDvd);
								Reference give_cdDvd = map_cdDvd.get(title_new.toString());
								give_cdDvd.setTitle(title_new.toString());
								reference=give_cdDvd;
							}else if (strName_reftype.equalsIgnoreCase("Database")) {
								map_database.put(title_new.toString(), database);
								Reference give_database = map_database.get(title_new.toString());
								give_database.setTitle(title_new.toString());
								reference=give_database;
							}else if (strName_reftype.equalsIgnoreCase("WebPage")) {
								map_webPage.put(title_new.toString(), webPage);
								Reference give_webPage = map_webPage.get(title_new.toString());
								give_webPage.setTitle(title_new.toString());
								reference=give_webPage;
							}else if (strName_reftype.equalsIgnoreCase("Report")) {
								map_report.put(title_new.toString(), report);
								Reference give_report = map_report.get(title_new.toString());
								give_report.setTitle(title_new.toString());
								reference=give_report;
							}else if (strName_reftype.equalsIgnoreCase("Thesis")) {
								map_thesis.put(title_new.toString(), thesis);
								Reference give_thesis = map_thesis.get(title_new.toString());
								give_thesis.setTitle(title_new.toString());
								reference=give_thesis;
							}else if (strName_reftype.equalsIgnoreCase("Print Series")){
								map_printSeries.put(title_new.toString(), printSeries);
								Reference give_printSeries = map_printSeries.get(title_new.toString());
								give_printSeries.setTitle(title_new.toString());
							}else if (strName_reftype.equals("Journal Article")){
								map_article.put(title_new.toString(), article);
								Reference give_article = map_article.get(title_new.toString());
								give_article.setTitle(title_new.toString());
								reference=give_article;
							}else if (strName_reftype.equalsIgnoreCase("Conference Proceedings")){
								map_proceedings.put(title_new.toString(), proceedings);
								Reference give_proceedings = map_proceedings.get(title_new.toString());
								give_proceedings.setTitle(title_new.toString());
								reference=give_proceedings;
							}else {
								logger.warn("The type was not found...");
								map_generic.put(title_new.toString(), generic);
								Reference give_generic = map_generic.get(title_new.toString());
								give_generic.setTitle(title_new.toString());
								reference=give_generic;
								success = false;
							}
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
						String strName_reftype = elRef_type.getAttributeValue("name");
						String secondary_title = elStyle_Secondary_title.getTextNormalize();

						if (strName_reftype.equals("Book Section")){
				    		if (secondary_title != null) {
				    			Reference give_book =map_book.get(secondary_title);
				    			if (give_book!= null) {
				    				bookSection.setInBook(give_book);
				    				give_book.setTitle(secondary_title);
				    			} else {
				    				bookSection.setInBook(book);
				    				map_book.put(secondary_title, book);
				    				book.setTitle(secondary_title);
				    			}
				    			reference=bookSection;
				    		}
				    	}else {
							logger.warn("The type was not found...");
							map_generic.put(secondary_title, generic);
							Reference give_generic = map_generic.get(secondary_title);
							give_generic.setTitle(secondary_title);
							reference=give_generic;
							success = false;
						}
					}
				}

				/** It was not used in this Implementation
				childName = "tertiary-title";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elTitles, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elTertiary_title = doubleResult.getFirstResult();
				if (elTertiary_title != null){
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elTertiary_title, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_Tertiary_title = doubleResult.getFirstResult();
					if (elStyle_Tertiary_title != null) {
						String strColor_Tertiary_title = elStyle_Tertiary_title.getAttributeValue("color");
						String strFace_Tertiary_title = elStyle_Tertiary_title.getAttributeValue("face");
						String strFont_Tertiary_title = elStyle_Tertiary_title.getAttributeValue("font");
						String strSize_Tertiary_title = elStyle_Tertiary_title.getAttributeValue("size");
					}
				}
				childName = "alt-title";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elTitles, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elAlt_title = doubleResult.getFirstResult();
				if (elAlt_title != null) {
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elAlt_title, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_Alt_title = doubleResult.getFirstResult();
					if (elStyle_Alt_title != null) {
						String strColor_Alt_title = elStyle_Alt_title.getAttributeValue("color");
						String strFace_Alt_title = elStyle_Alt_title.getAttributeValue("face");
						String strFont_Alt_title = elStyle_Alt_title.getAttributeValue("font");
						String strSize_Alt_title = elStyle_Alt_title.getAttributeValue("size");
					}
				}
				childName = "short-title";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elTitles, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elShort_title = doubleResult.getFirstResult();
				if (elShort_title != null) {

					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elShort_title, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_Short_title = doubleResult.getFirstResult();
					if (elStyle_Short_title != null) {
						String strColor_Short_title = elStyle_Short_title.getAttributeValue("color");
						String strFace_Short_title = elStyle_Short_title.getAttributeValue("face");
						String strFont_Short_title = elStyle_Short_title.getAttributeValue("font");
						String strSize_Short_title = elStyle_Short_title.getAttributeValue("size");
					}
				}

				childName = "translated-title";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elTitles, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elTranslated_title = doubleResult.getFirstResult();
				if (elTranslated_title != null) {

					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elTranslated_title, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_Translated_title = doubleResult.getFirstResult();
					if (elStyle_Translated_title != null) {
						String strColor_Translated_title = elStyle_Translated_title.getAttributeValue("color");
						String strFace_Translated_title = elStyle_Translated_title.getAttributeValue("face");
						String strFont_Translated_title = elStyle_Translated_title.getAttributeValue("font");
						String strSize_Translated_title = elStyle_Translated_title.getAttributeValue("size");
					}
				}
			*/
			}

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
			    	String strColor_Full_title = elStyle_Full_title.getAttributeValue("color");
			    	String strFace_Full_title = elStyle_Full_title.getAttributeValue("face");
			    	String strFont_Full_title = elStyle_Full_title.getAttributeValue("font");
			    	String strSize_Full_title = elStyle_Full_title.getAttributeValue("size");

			    	String strName_reftype = elRef_type.getAttributeValue("name");
			    	String periodical = elStyle_Full_title.getTextNormalize();

			    	if (strName_reftype.equals("Journal Article")){

			    		if (periodical != null) {
			    			Reference give_journal = map_journal.get(periodical);
							if (give_journal!= null) {
								article.setInJournal(give_journal);
								give_journal.setTitle(periodical);
							} else {
								article.setInJournal(journal);
								map_journal.put(periodical, journal);
								journal.setTitle(periodical);
							}
							reference=article;
			    		}
			    	} else {
						logger.warn("The type was not found...");
						success = false;
					}
			    }

			    /** It was not used in this Implementation
			    childName = "abbr-1";
			    obligatory = false;
			    doubleResult =  XmlHelp.getSingleChildElement(elPeriodical, childName, tcsNamespace, obligatory);
			    success &= doubleResult.getSecondResult();
			    Element elAbbr_1 = doubleResult.getFirstResult();
			    if (elAbbr_1 != null) {

			    	childName = "style";
			    	obligatory = false;
			    	doubleResult =  XmlHelp.getSingleChildElement(elAbbr_1, childName, tcsNamespace, obligatory);
			    	success &= doubleResult.getSecondResult();
			    	Element elStyle_Abbr_1 = doubleResult.getFirstResult();
			    	String strColor_Abbr_1 = elStyle_Abbr_1.getAttributeValue("color");
			    	String strFace_Abbr_1 = elStyle_Abbr_1.getAttributeValue("face");
			    	String strFont_Abbr_1 = elStyle_Abbr_1.getAttributeValue("font");
			    	String strSize_Abbr_1 = elStyle_Abbr_1.getAttributeValue("size");
			    }

			    childName = "abbr-2";
			    obligatory = false;
			    doubleResult =  XmlHelp.getSingleChildElement(elPeriodical, childName, tcsNamespace, obligatory);
			    success &= doubleResult.getSecondResult();
			    Element elAbbr_2 = doubleResult.getFirstResult();
			    if (elAbbr_2 != null) {

			    	childName = "style";
			    	obligatory = false;
			    	doubleResult =  XmlHelp.getSingleChildElement(elAbbr_2, childName, tcsNamespace, obligatory);
			    	success &= doubleResult.getSecondResult();
			    	Element elStyle_Abbr_2 = doubleResult.getFirstResult();
			    	String strColor_Abbr_2 = elStyle_Abbr_2.getAttributeValue("color");
			    	String strFace_Abbr_2 = elStyle_Abbr_2.getAttributeValue("face");
			    	String strFont_Abbr_2 = elStyle_Abbr_2.getAttributeValue("font");
			    	String strSize_Abbr_2 = elStyle_Abbr_2.getAttributeValue("size");
			    }

			    childName = "abbr_3";
			    obligatory = false;
			    doubleResult =  XmlHelp.getSingleChildElement(elPeriodical, childName, tcsNamespace, obligatory);
			    success &= doubleResult.getSecondResult();
			    Element elAbbr_3 = doubleResult.getFirstResult();
			    if (elAbbr_3 != null) {

			    	childName = "style";
			    	obligatory = false;
			    	doubleResult =  XmlHelp.getSingleChildElement(elAbbr_3, childName, tcsNamespace, obligatory);
			    	success &= doubleResult.getSecondResult();
			    	Element elStyle_Abbr_3 = doubleResult.getFirstResult();
			    	String strColor_Abbr_3 = elStyle_Abbr_3.getAttributeValue("color");
			    	String strFace_Abbr_3 = elStyle_Abbr_3.getAttributeValue("face");
			    	String strFont_Abbr_3 = elStyle_Abbr_3.getAttributeValue("font");
			    	String strSize_Abbr_3 = elStyle_Abbr_3.getAttributeValue("size");
			    }
			*/
			}

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
				String strColor_Pages = elStyle_Pages.getAttributeValue("color");
				String strFace_Pages = elStyle_Pages.getAttributeValue("face");
				String strFont_Pages = elStyle_Pages.getAttributeValue("font");
				String strSize_Pages = elStyle_Pages.getAttributeValue("size");

				String strName_reftype = elRef_type.getAttributeValue("name");
				String page = elStyle_Pages.getTextNormalize();

				if (strName_reftype.equals("Journal Article")) {
					map_article.put(page, article);
					Reference give_article = map_article.get(page);
					give_article.setPages(page);
					reference = give_article;
				}else if (strName_reftype.equals("Article")){
					map_article.put(page, article);
					Reference give_article = map_article.get(page);
					give_article.setPages(page);
					reference = give_article;
				}else if (strName_reftype.equals("Book")){
					map_book.put(page, book);
					Reference give_book = map_book.get(page);
					give_book.setPages(page);
					reference=give_book;
				}else if (strName_reftype.equals("Book Section")){
					map_book_section.put(page, bookSection);
					Reference give_book_section = map_book_section.get(page);
					give_book_section.setPages(page);
					reference=give_book_section;
				}else if (strName_reftype.equalsIgnoreCase("Conference Proceedings")){
					map_proceedings.put(page, proceedings);
					Reference give_proceedings = map_proceedings.get(page);
					give_proceedings.setPages(page);
					reference=give_proceedings;
				} else {
					logger.warn("The type was not found...");
					map_generic.put(page, generic);
					Reference give_generic  = map_generic.get(page);
					give_generic.setPages(page);
					reference =give_generic;
					success = false;
				}
			}

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
				String strColor_Volume = elStyle_Volume.getAttributeValue("color");
				String strFace_Volume = elStyle_Volume.getAttributeValue("face");
				String strFont_Volume = elStyle_Volume.getAttributeValue("font");
				String strSize_Volume = elStyle_Volume.getAttributeValue("size");

				String strName_reftype = elRef_type.getAttributeValue("name");
				String volume = elStyle_Volume.getTextNormalize();

				if (strName_reftype.equals("Journal Article")) {
					map_article.put(volume, article);
					Reference give_article = map_article.get(volume);
					give_article.setVolume(volume);
					reference = give_article;
				}else if (strName_reftype.equals("Article")){
					map_article.put(volume, article);
					Reference give_article = map_article.get(volume);
					give_article.setVolume(volume);
					reference = give_article;
				}else if (strName_reftype.equals("Book")){
					map_book.put(volume, book);
					Reference give_book = map_book.get(volume);
					give_book.setVolume(volume);
					reference=give_book;
				}else if (strName_reftype.equals("Book Section")){
					 if (volume != null) {
						 bookSection.setInBook(book);
						 book.setVolume(volume);
						 reference= bookSection;
					 }
				}else if (strName_reftype.equalsIgnoreCase("Conference Proceedings")){
					map_proceedings.put(volume, proceedings);
					Reference give_proceedings = map_proceedings.get(volume);
					give_proceedings.setVolume(volume);
					reference=give_proceedings;
				}else{
					logger.warn("The type was not found...");
					map_generic.put(volume, generic);
					Reference give_generic  = map_generic.get(volume);
					give_generic.setVolume(volume);
					reference =give_generic;
					success = true;
				}
			}

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
				String strColor_Number = elStyle_Number.getAttributeValue("color");
				String strFace_Number = elStyle_Number.getAttributeValue("face");
				String strFont_Number = elStyle_Number.getAttributeValue("font");
				String strSize_Number = elStyle_Number.getAttributeValue("size");

				String strName_reftype = elRef_type.getAttributeValue("name");
				String number = elStyle_Number.getTextNormalize();

				if (strName_reftype.equals("Journal Article")) {
					map_article.put(number, article);
					Reference give_article = map_article.get(number);
					give_article.setSeriesPart(number);
					reference = give_article;
				}else if (strName_reftype.equals("Article")){
					map_article.put(number, article);
					Reference give_article = map_article.get(number);
					give_article.setSeriesPart(number);
					reference = give_article;
				}else {
					logger.warn("The type was not found...");
					map_generic.put(number, generic);
					Reference give_generic  = map_generic.get(number);
					give_generic.setSeriesPart(number);
					reference =give_generic;
					success = false;
				}
			}

			/**
			// NOT USE IN THE IMPLEMENTATION
			childName = "issue"; // not use in CDM
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elIssue = doubleResult.getFirstResult();
			if (elIssue != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elIssue, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Issue = doubleResult.getFirstResult();
				String strColor_Issue = elStyle_Issue.getAttributeValue("color");
				String strFace_Issue = elStyle_Issue.getAttributeValue("face");
				String strFont_Issue = elStyle_Issue.getAttributeValue("font");
				String strSize_Issue = elStyle_Issue.getAttributeValue("size");
			}

			// LIKE NUMBER ELEMENT (the same content) use very selten
			//Amount Received - the name in Endnote Programm
			childName = "num-vols"; // not use in CDM
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elNum_vols = doubleResult.getFirstResult();
			if (elNum_vols != null) {
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elNum_vols, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Num_vols = doubleResult.getFirstResult();
				String strColor_Num_vols = elStyle_Num_vols.getAttributeValue("color");
				String strFace_Num_vols = elStyle_Num_vols.getAttributeValue("face");
				String strFont_Num_vols = elStyle_Num_vols.getAttributeValue("font");
				String strSize_Num_vols = elStyle_Num_vols.getAttributeValue("size");
			}
			*/

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

				String strName_reftype = elRef_type.getAttributeValue("name");
				String edition = elStyle_Edition.getTextNormalize();

				if (strName_reftype.equals("Book")) {
					map_book.put(edition, book);
					Reference give_book = map_book.get(edition);
					give_book.setEdition(edition);
					reference=give_book;
				}else if (strName_reftype.equals("Book Section")) {
					bookSection.setInBook(book);
					book.setEdition(edition);
					reference=bookSection;
				}else {
					logger.warn("The type was not found...");
					success = false;
				}
			}

			/**It was not used in this Implementation
			// LIKE NUMBER ELEMENT (the same content) use very selten
			childName = "section";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elSection = doubleResult.getFirstResult();
			if (elSection != null) {
				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elSection, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Section = doubleResult.getFirstResult();
				String strColor_Section = elStyle_Section.getAttributeValue("color");
				String strFace_Section = elStyle_Section.getAttributeValue("face");
				String strFont_Section = elStyle_Section.getAttributeValue("font");
				String strSize_Section = elStyle_Section.getAttributeValue("size");
			}

			// NOT USE IN THE IMPLEMENTATION
			childName = "reprint-edition";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elReprint_edition = doubleResult.getFirstResult();
			if (elReprint_edition != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elReprint_edition, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Reprint_edition = doubleResult.getFirstResult();
				String strColor_Reprint_edition = elStyle_Reprint_edition.getAttributeValue("color");
				String strFace_Reprint_edition = elStyle_Reprint_edition.getAttributeValue("face");
				String strFont_Reprint_edition = elStyle_Reprint_edition.getAttributeValue("font");
				String strSize_Reprint_edition = elStyle_Reprint_edition.getAttributeValue("size");
			}

			// use very selten keywords use multiple keyword elements
			childName = "keywords";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elKeywords = doubleResult.getFirstResult();
			if (elKeywords != null) {

				childName = "keyword";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elKeywords, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elKeyword = doubleResult.getFirstResult();
				if (elKeyword != null) {
					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elKeyword, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_Keyword = doubleResult.getFirstResult();
					String strColor_Keyword = elStyle_Keyword.getAttributeValue("color");
					String strFace_Keyword = elStyle_Keyword.getAttributeValue("face");
					String strFont_Keyword = elStyle_Keyword.getAttributeValue("font");
					String strSize_Keyword = elStyle_Keyword.getAttributeValue("size");
				}
			}
			*/

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
					String strDay = elYear.getAttributeValue("day");
					String strJulian = elYear.getAttributeValue("julian");
					String strMonth = elYear.getAttributeValue("month");
					String strYear = elYear.getAttributeValue("year");

					childName = "style";
					obligatory = false;
					doubleResult =  XmlHelp.getSingleChildElement(elYear, childName, tcsNamespace, obligatory);
					success &= doubleResult.getSecondResult();
					Element elStyle_Year = doubleResult.getFirstResult();
					String strColor_Year = elStyle_Year.getAttributeValue("color");
					String strFace_Year = elStyle_Year.getAttributeValue("face");
					String strFont_Year = elStyle_Year.getAttributeValue("font");
					String strSize_Year = elStyle_Year.getAttributeValue("size");

					String year = elStyle_Year.getText();
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
		 		String strName_reftype = elRef_type.getAttributeValue("name");

				if (strName_reftype.equals("Report")) {
					map_report.put(place, report);
					Reference give_report = map_report.get(place);
					give_report.setPlacePublished(place);
					reference=give_report;
				}else if (strName_reftype.equals("Book")){
					map_book.put(place, book);
					Reference give_book = map_book.get(place);
					give_book.setPlacePublished(place);
					reference=give_book;
				}else if (strName_reftype.equals("Thesis")){
					map_thesis.put(place, thesis);
					Reference give_thesis = map_thesis.get(place);
					give_thesis.setPlacePublished(place);
					reference=give_thesis;
				}else if (strName_reftype.equalsIgnoreCase("Conference Proceedings")){
					map_proceedings.put(place, proceedings);
					Reference give_proceedings = map_proceedings.get(place);
					give_proceedings.setPlacePublished(place);
					reference=give_proceedings;
				}else if (strName_reftype.equalsIgnoreCase("Database")){
					map_database.put(place, database);
					Reference give_database = map_database.get(place);
					give_database.setPlacePublished(place);
					reference=give_database;
				}else if (strName_reftype.equalsIgnoreCase("CdDvd")){
					map_cdDvd.put(place, cdDvd);
					Reference give_cdDvd = map_cdDvd.get(place);
					give_cdDvd.setPlacePublished(place);
					reference=give_cdDvd;
				}else if (strName_reftype.equalsIgnoreCase("Print Series")){
					map_printSeries.put(place, printSeries);
					Reference give_printSeries = map_printSeries.get(place);
					give_printSeries.setPlacePublished(place);
					reference=give_printSeries;
				}else if (strName_reftype.equalsIgnoreCase("Journal")){
					map_journal.put(place, journal);
					Reference give_journal = map_journal.get(place);
					give_journal.setPlacePublished(place);
					reference=give_journal;
				} else {
					logger.warn("The type was not found...");
					map_generic.put(place, generic);
					Reference give_generic = map_generic.get(place);
					give_generic.setPlacePublished(place);
					reference=give_generic;
					success = false;
				}
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
				String strName_reftype = elRef_type.getAttributeValue("name");

				if (strName_reftype.equals("Report")) {
					map_report.put(publisher, report);
					Reference give_report = map_report.get(publisher);
					give_report.setPublisher(publisher);
					reference=give_report;
				}else if (strName_reftype.equals("Book")){
					map_book.put(publisher, book);
					Reference give_book = map_book.get(publisher);
					give_book.setPublisher(publisher);
					reference=give_book;
				}else if (strName_reftype.equals("Book Section")){
					if (publisher != null) {
						bookSection.setInBook(book);
						book.setPublisher(publisher);
						reference= bookSection;
					}
				}else if (strName_reftype.equals("Thesis")){
					map_thesis.put(publisher, thesis);
					Reference give_thesis = map_thesis.get(publisher);
					give_thesis.setPublisher(publisher);
					reference=give_thesis;
				}else if (strName_reftype.equalsIgnoreCase("Conference Proceedings")){
					map_proceedings.put(publisher, proceedings);
					Reference give_proceedings = map_proceedings.get(publisher);
					give_proceedings.setPublisher(publisher);
					reference=give_proceedings;
				}else if (strName_reftype.equalsIgnoreCase("Database")){
					map_database.put(publisher, database);
					Reference give_database = map_database.get(publisher);
					give_database.setPublisher(publisher);
					reference=give_database;
				}else if (strName_reftype.equalsIgnoreCase("CdDvd")){
					map_cdDvd.put(publisher, cdDvd);
					Reference give_cdDvd = map_cdDvd.get(publisher);
					give_cdDvd.setPublisher(publisher);
					reference=give_cdDvd;
				}else if (strName_reftype.equalsIgnoreCase("Print Series")){
					map_printSeries.put(publisher, printSeries);
					Reference give_printSeries = map_printSeries.get(publisher);
					give_printSeries.setPublisher(publisher);
					reference=give_printSeries;
				}else if (strName_reftype.equalsIgnoreCase("Journal")){
					map_journal.put(publisher, journal);
					Reference give_journal = map_journal.get(publisher);
					give_journal.setPublisher(publisher);
					reference=give_journal;
				}else if (strName_reftype.equalsIgnoreCase("Journal Article")){
					if (publisher != null) {
						article.setInJournal(journal);
						journal.setPublisher(publisher);
						reference= article;
					}
				} else {
					logger.warn("The type was not found...");
					map_generic.put(publisher, generic);
					Reference give_generic = map_generic.get(publisher);
					give_generic.setPublisher(publisher);
					reference=give_generic;

					success = false;
				}
			}

			/**
			// It was not used in this Implementation
			childName = "orig-pub"; // original grant number - the name in Endnote Programm
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elOrig_pub = doubleResult.getFirstResult();
			if (elOrig_pub != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elOrig_pub, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Orig_pub = doubleResult.getFirstResult();
				String strColor_Orig_pub = elStyle_Orig_pub.getAttributeValue("color");
				String strFace_Orig_pub = elStyle_Orig_pub.getAttributeValue("face");
				String strFont_Orig_pub = elStyle_Orig_pub.getAttributeValue("font");
				String strSize_Orig_pub = elStyle_Orig_pub.getAttributeValue("size");
			}
			*/

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

				String strName_reftype = elRef_type.getAttributeValue("name");
				String page = elStyle_Isbn.getTextNormalize();

				if (strName_reftype.equals("Book")) {
					map_book.put(page, book);
					Reference give_book = map_book.get(page);
					give_book.setIsbn(page);
					reference=give_book;
				}else if (strName_reftype.equals("Journal")){
					map_journal.put(page, journal);
					Reference give_journal = map_journal.get(page);
					give_journal.setIssn(page);
					reference=give_journal;
				}else {
					logger.warn("The type was not found...");
					success = false;
				}
			}

			/**
			// It was not used in this Implementation
			childName = "accession-num";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elAccession_num = doubleResult.getFirstResult();
			if (elAccession_num != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elAccession_num, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Accession_num = doubleResult.getFirstResult();
				String strColor_Accession_num = elStyle_Accession_num.getAttributeValue("color");
				String strFace_Accession_num = elStyle_Accession_num.getAttributeValue("face");
				String strFont_Accession_num = elStyle_Accession_num.getAttributeValue("font");
				String strSize_Accession_num = elStyle_Accession_num.getAttributeValue("size");
			}

			// It was not used in this Implementation
			childName = "call-num";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elCall_num = doubleResult.getFirstResult();
			if (elCall_num != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elCall_num, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Call_num = doubleResult.getFirstResult();
				String strColor_Call_num = elStyle_Call_num.getAttributeValue("color");
				String strFace_Call_num = elStyle_Call_num.getAttributeValue("face");
				String strFont_Call_num = elStyle_Call_num.getAttributeValue("font");
				String strSize_Call_num = elStyle_Call_num.getAttributeValue("size");
			}
			*/

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

				String annote = elStyle_Abstract.getTextNormalize();
				Annotation annotation = Annotation.NewInstance(annote, Language.DEFAULT());
				if (annote!= null) {
					reference.addAnnotation(annotation);
				}
				else {
					logger.warn("The type was not found...");
					success = false;
				}
			}

			/**
			// It was not used in this Implementation
			childName = "label";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elLabel = doubleResult.getFirstResult();
			if (elLabel != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elLabel, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Label = doubleResult.getFirstResult();
				String strColor_Label = elStyle_Label.getAttributeValue("color");
				String strFace_Label = elStyle_Label.getAttributeValue("face");
				String strFont_Label = elStyle_Label.getAttributeValue("font");
				String strSize_Label = elStyle_Label.getAttributeValue("size");
			}


			// It was not used in this Implementation
	     	logger.info("start make image ...");
			childName = "image"; //Figure - the name in Endnote Programm
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elImage = doubleResult.getFirstResult();

			Media media = Media.NewInstance();
			if (elImage != null){
				String strFile = elImage.getAttributeValue("file");
				String strImage_name = elImage.getAttributeValue("name");
				reference.getMedia();
			}


			/**
			//It was not used in this Implementation
			childName = "caption";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elCaption = doubleResult.getFirstResult();
			if(elCaption != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elCaption, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Caption = doubleResult.getFirstResult();
				String strColor_Caption = elStyle_Caption.getAttributeValue("color");
				String strFace_Caption = elStyle_Caption.getAttributeValue("face");
				String strFont_Caption = elStyle_Caption.getAttributeValue("font");
				String strSize_Caption = elStyle_Caption.getAttributeValue("size");
			}

			//It was not used in this Implementation
			childName = "notes";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elNotes = doubleResult.getFirstResult();
			if (elNotes != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elNotes, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Notes = doubleResult.getFirstResult();
				String strColor_Notes = elStyle_Notes.getAttributeValue("color");
				String strFace_Notes = elStyle_Notes.getAttributeValue("face");
				String strFont_Notes = elStyle_Notes.getAttributeValue("font");
				String strSize_Notes = elStyle_Notes.getAttributeValue("size");

				//Annotation annotation = null;
				//reference.addAnnotation(annotation);
				//referenceMap.put(elStyle_Notes, reference);
			}

			//It was not used in this Implementation
			childName = "research-notes";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elResearch_notes = doubleResult.getFirstResult();
			if (elResearch_notes != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elResearch_notes, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Research_notes = doubleResult.getFirstResult();
				String strColor_Research_notes = elStyle_Research_notes.getAttributeValue("color");
				String strFace_Research_notes = elStyle_Research_notes.getAttributeValue("face");
				String strFont_Research_notes = elStyle_Research_notes.getAttributeValue("font");
				String strSize_Research_notes = elStyle_Research_notes.getAttributeValue("size");
			}
			*/

			/**It was not used in this Implementation
			childName = "work-type"; // thesis type
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elWork_type = doubleResult.getFirstResult();
			if (elWork_type!= null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elWork_type, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Work_type = doubleResult.getFirstResult();
				String strColor_Work_type = elStyle_Work_type.getAttributeValue("color");
				String strFace_Work_type = elStyle_Work_type.getAttributeValue("face");
				String strFont_Work_type = elStyle_Work_type.getAttributeValue("font");
				String strSize_Work_type = elStyle_Work_type.getAttributeValue("size");

				String thesis_style =  elStyle_Work_type.getTextNormalize();
				String strName_reftype = elRef_type.getAttributeValue("name");

				Institution institution =Institution.NewInstance();
				school.setName(thesis_style);
				institution.setName(thesis_style);

				if (strName_reftype.equals("Thesis")) {
					thesis.setSchool(institution);
					reference= thesis;
				}else if (strName_reftype.equals("Report")){
					report.setInstitution(institution);
					reference= report;
				}else {
					logger.warn("The type was not found...");
					success = false;
					logger.info(reference);
				}
				logger.info(reference);
			}

			/**
			//It was not used in this Implementation
			childName = "reviewed-item";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elReviewed_item = doubleResult.getFirstResult();
			if(elReviewed_item!=null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elReviewed_item, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Reviewed_item = doubleResult.getFirstResult();
				String strColor_Reviewed_item = elStyle_Reviewed_item.getAttributeValue("color");
				String strFace_Reviewed_item = elStyle_Reviewed_item.getAttributeValue("face");
				String strFont_Reviewed_item = elStyle_Reviewed_item.getAttributeValue("font");
				String strSize_Reviewed_item = elStyle_Reviewed_item.getAttributeValue("size");
			}

			//It was not used in this Implementation
			childName = "remote-database-name"; //name of database - the name in endnote programm
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elRemote_database_name = doubleResult.getFirstResult();
			if (elRemote_database_name != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elRemote_database_name, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Remote_database_name = doubleResult.getFirstResult();
				String strColor_Remote_database_name = elStyle_Remote_database_name.getAttributeValue("color");
				String strFace_Remote_database_name = elStyle_Remote_database_name.getAttributeValue("face");
				String strFont_Remote_database_name = elStyle_Remote_database_name.getAttributeValue("font");
				String strSize_Remote_database_name = elStyle_Remote_database_name.getAttributeValue("size");
			}

			//It was not used in this Implementation
			childName = "remote-database-provider"; // database provider - the name in endnote programm
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elRemote_database_provider = doubleResult.getFirstResult();
			if (elRemote_database_provider != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elRemote_database_provider, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Remote_database_provider = doubleResult.getFirstResult();
				String strColor_Remote_database_provider = elStyle_Remote_database_provider.getAttributeValue("color");
				String strFace_Remote_database_provider = elStyle_Remote_database_provider.getAttributeValue("face");
				String strFont_Remote_database_provider = elStyle_Remote_database_provider.getAttributeValue("font");
				String strSize_Remote_database_provider = elStyle_Remote_database_provider.getAttributeValue("size");
			}
			*/

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
					try {
						reference.setUri(new URI(elStyle_Url.getTextNormalize()));
					} catch (URISyntaxException e) {
						logger.warn("Unvalid URL:" + elStyle_Url.getText());
						success = false;
					}
				}

				childName = "pdf-urls";
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
					try {
						reference.setUri(new URI(elStyle_PdfUrl.getText()));
					} catch (URISyntaxException e) {
						logger.warn("Unvalid URL:" + elStyle_PdfUrl.getText());
						success = false;
					}
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
					try {
						reference.setUri(new URI(elStyle_TextUrl.getText()));
					} catch (URISyntaxException e) {
						logger.warn("Unvalid URL:" + elStyle_TextUrl.getText());
						success = false;
					}
				}

				childName = "related-urls";
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
					try {
						reference.setUri(new URI(elStyle_RelatedUrl.getText()));
					} catch (URISyntaxException e) {
						logger.warn("Unvalid URL:" + elStyle_RelatedUrl.getText());
						success = false;
					}
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
					try {
						reference.setUri(new URI(elStyle_ImageUrl.getText()));
					} catch (URISyntaxException e) {
						logger.warn("Unvalid URL:" + elStyle_ImageUrl.getText());
						success = false;
					}
				}
			}

			/** It was not used in this Implementation
			childName = "access-date";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elAccess_date = doubleResult.getFirstResult();
			if (elAccess_date != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elAccess_date, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Access_date = doubleResult.getFirstResult();
				String strColor_Access_date = elStyle_Access_date.getAttributeValue("color");
				String strFace_Access_date = elStyle_Access_date.getAttributeValue("face");
				String strFont_Access_date = elStyle_Access_date.getAttributeValue("font");
				String strSize_Access_date = elStyle_Access_date.getAttributeValue("size");
			}


			//It was not used in this Implementation
			logger.info("start make modified-date ...");
			childName = "modified-date"; //custom 8 - name in endnote programm
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elModified_date = doubleResult.getFirstResult();
			if (elModified_date != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elModified_date, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Modified_date = doubleResult.getFirstResult();
				String strColor_Modified_date = elStyle_Modified_date.getAttributeValue("color");
				String strFace_Modified_date = elStyle_Modified_date.getAttributeValue("face");
				String strFont_Modified_date = elStyle_Modified_date.getAttributeValue("font");
				String strSize_Modified_date = elStyle_Modified_date.getAttributeValue("size");
			}

			/**It was not used in this Implementation
			childName = "custom1";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elCustom1 = doubleResult.getFirstResult();
			if (elCustom1 != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elCustom1, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Custom1 = doubleResult.getFirstResult();
				String strColor_Custom1 = elStyle_Custom1.getAttributeValue("color");
				String strFace_Custom1 = elStyle_Custom1.getAttributeValue("face");
				String strFont_Custom1 = elStyle_Custom1.getAttributeValue("font");
				String strSize_Custom1 = elStyle_Custom1.getAttributeValue("size");
			}

			childName = "custom2";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elCustom2 = doubleResult.getFirstResult();
			if (elCustom2 != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elCustom2, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Custom2 = doubleResult.getFirstResult();
				String strColor_Custom2 = elStyle_Custom2.getAttributeValue("color");
				String strFace_Custom2 = elStyle_Custom2.getAttributeValue("face");
				String strFont_Custom2 = elStyle_Custom2.getAttributeValue("font");
				String strSize_Custom2 = elStyle_Custom2.getAttributeValue("size");
			}

			childName = "custom3";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elCustom3 = doubleResult.getFirstResult();
			if (elCustom3 != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elCustom3, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Custom3 = doubleResult.getFirstResult();
				String strColor_Custom3 = elStyle_Custom3.getAttributeValue("color");
				String strFace_Custom3 = elStyle_Custom3.getAttributeValue("face");
				String strFont_Custom3 = elStyle_Custom3.getAttributeValue("font");
				String strSize_Custom3 = elStyle_Custom3.getAttributeValue("size");
			}

			childName = "custom4";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elCustom4 = doubleResult.getFirstResult();
			if (elCustom4 != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elCustom4, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Custom4 = doubleResult.getFirstResult();
				String strColor_Custom4 = elStyle_Custom4.getAttributeValue("color");
				String strFace_Custom4 = elStyle_Custom4.getAttributeValue("face");
				String strFont_Custom4 = elStyle_Custom4.getAttributeValue("font");
				String strSize_Custom4 = elStyle_Custom4.getAttributeValue("size");
			}

			childName = "custom5";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elCustom5 = doubleResult.getFirstResult();
			if (elCustom5 != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elCustom5, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Custom5 = doubleResult.getFirstResult();
				String strColor_Custom5 = elStyle_Custom5.getAttributeValue("color");
				String strFace_Custom5 = elStyle_Custom5.getAttributeValue("face");
				String strFont_Custom5 = elStyle_Custom5.getAttributeValue("font");
				String strSize_Custom5 = elStyle_Custom5.getAttributeValue("size");
			}

			childName = "custom6";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elCustom6 = doubleResult.getFirstResult();
			if (elCustom6 != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elCustom6, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Custom6 = doubleResult.getFirstResult();
				String strColor_Custom6 = elStyle_Custom6.getAttributeValue("color");
				String strFace_Custom6 = elStyle_Custom6.getAttributeValue("face");
				String strFont_Custom6 = elStyle_Custom6.getAttributeValue("font");
				String strSize_Custom6 = elStyle_Custom6.getAttributeValue("size");
			}

			childName = "custom7";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elRecord, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elCustom7 = doubleResult.getFirstResult();
			if (elCustom7 != null) {

				childName = "style";
				obligatory = false;
				doubleResult =  XmlHelp.getSingleChildElement(elCustom7, childName, tcsNamespace, obligatory);
				success &= doubleResult.getSecondResult();
				Element elStyle_Custom7 = doubleResult.getFirstResult();
				String strColor_Custom7 = elStyle_Custom7.getAttributeValue("color");
				String strFace_Custom7 = elStyle_Custom7.getAttributeValue("face");
				String strFont_Custom7 = elStyle_Custom7.getAttributeValue("font");
				String strSize_Custom7 = elStyle_Custom7.getAttributeValue("size");
			}
		 */
			authorMap.put(elRec_number, (Team) author);
			referenceMap.put(elRec_number, reference);
		}

		logger.info(i + " Records handled. Saving ...");
		referenceService.save(referenceMap.objects());
		logger.info("end make Records ...");
		if (!success){
			state.setUnsuccessfull();
		}
		return;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
    protected boolean isIgnore(EndnoteImportState state){
		EndnoteImportConfigurator tcsConfig = state.getConfig();
		return (! tcsConfig.isDoRecords());
	}
}
