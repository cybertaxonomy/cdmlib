/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_ARTICLE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_BOOK;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_DATABASE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_INFORMAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_JOURNAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_PART_OF_OTHER_TITLE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_UNKNOWN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_WEBSITE;
import static eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES.ALL;
import static eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES.CONCEPT_REFERENCES;
import static eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES.NOMENCLATURAL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.reference.WebPage;

/**
 * @author a.mueller
 *
 */
public class BerlinModelReferenceIO extends BerlinModelIOBase {
	private static final Logger logger = Logger.getLogger(BerlinModelReferenceIO.class);

	private int modCount = 1000;
	
	public BerlinModelReferenceIO(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		result &= checkArticlesWithoutJournal(bmiConfig);
		result &= checkPartOfJournal(bmiConfig);
		result &= checkPartOfUnresolved(bmiConfig);
		result &= checkPartOfPartOf(bmiConfig);
		result &= checkPartOfArticle(bmiConfig);
		
		
		if (result == false ){System.out.println("========================================================");}
		
		
		return result;
	}
		
	private static boolean checkArticlesWithoutJournal(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryArticlesWithoutJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
						" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
						" WHERE (Reference.RefCategoryFk = 1) AND (InRef.RefCategoryFk <> 9) ";
			ResultSet resulSetarticlesWithoutJournal = source.getResultSet(strQueryArticlesWithoutJournal);
			boolean firstRow = true;
			while (resulSetarticlesWithoutJournal.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are Articles with wrong inRef type!");
					System.out.println("========================================================");
				}
				int refId = resulSetarticlesWithoutJournal.getInt("RefId");
				int categoryFk = resulSetarticlesWithoutJournal.getInt("RefCategoryFk");
				String cat = resulSetarticlesWithoutJournal.getString("RefCategoryAbbrev");
				int inRefFk = resulSetarticlesWithoutJournal.getInt("InRefId");
				int inRefCategoryFk = resulSetarticlesWithoutJournal.getInt("InRefCatFk");
				String inRefCat = resulSetarticlesWithoutJournal.getString("InRefCat");
				String refCache = resulSetarticlesWithoutJournal.getString("RefCache");
				String nomRefCache = resulSetarticlesWithoutJournal.getString("nomRefCache");
				String title = resulSetarticlesWithoutJournal.getString("title");
				String inRefTitle = resulSetarticlesWithoutJournal.getString("InRefTitle");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
						"\n  inRefTitle: " + inRefTitle );
				result = firstRow = false;
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean checkPartOfJournal(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
			" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
						" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 9) ";
			ResultSet rs = source.getResultSet(strQueryPartOfJournal);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are part-of-references that have a Journal as in-reference!");
					System.out.println("========================================================");
				}
				int refId = rs.getInt("RefId");
				int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				int inRefCategoryFk = rs.getInt("InRefCatFk");
				String inRefCat = rs.getString("InRefCat");
				String refCache = rs.getString("RefCache");
				String nomRefCache = rs.getString("nomRefCache");
				String title = rs.getString("title");
				String inRefTitle = rs.getString("InRefTitle");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
						"\n  inRefTitle: " + inRefTitle );
				result = firstRow = false;
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	private static boolean checkPartOfUnresolved(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
			" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
						" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 10) ";
			ResultSet rs = source.getResultSet(strQueryPartOfJournal);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are part-of-references that have an 'unresolved' in-reference!");
					System.out.println("========================================================");
				}
				int refId = rs.getInt("RefId");
				int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				int inRefCategoryFk = rs.getInt("InRefCatFk");
				String inRefCat = rs.getString("InRefCat");
				String refCache = rs.getString("RefCache");
				String nomRefCache = rs.getString("nomRefCache");
				String title = rs.getString("title");
				String inRefTitle = rs.getString("InRefTitle");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
						"\n  inRefTitle: " + inRefTitle );
				result = firstRow = false;
			}
			if (result == false){
				System.out.println("\nChoose a specific type from the following reference types: \n" +
						"  1) Article \n  2) Book \n  3) BookSection \n  4) CdDvd \n  5) ConferenceProceeding \n  6) Database\n" + 
						"  7) Generic \n  7) InProceedings \n  8) Journal \n  9) Map \n 10) Patent \n 11) PersonalCommunication\n" +
						" 12) PrintSeries \n 13) Proceedings \n 14) Report \n 15) Thesis \n 16) WebPage");
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean checkPartOfPartOf(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle, InRef.InRefFk as InInRefId, InInRef.Title as inInRefTitle, InInRef.RefCategoryFk as inInRefCategory " + 
						" FROM Reference " +
							" INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId " + 
							" INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId " + 
							" INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
							" INNER JOIN Reference AS InInRef ON InRef.InRefFk = InInRef.RefId " + 
						" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 2) ";
			ResultSet rs = source.getResultSet(strQueryPartOfJournal);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are part-of-references that are part of an other 'part-of' reference!\n" + 
							"         This is invalid or ambigous. Please try to determine the reference types more detailed ");
					System.out.println("========================================================");
				}
				int refId = rs.getInt("RefId");
				int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				int inRefCategoryFk = rs.getInt("InRefCatFk");
				String inRefCat = rs.getString("InRefCat");
				String refCache = rs.getString("RefCache");
				String nomRefCache = rs.getString("nomRefCache");
				String title = rs.getString("title");
				String inRefTitle = rs.getString("InRefTitle");
				int inInRefId = rs.getInt("InInRefId");
				String inInRefTitle = rs.getString("inInRefTitle");
				int inInRefCategory = rs.getInt("inInRefCategory");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
						"\n  inRefTitle: " + inRefTitle + "\n  inInRefId: " + inInRefId + "\n  inInRefTitle: " + inInRefTitle +
						"\n  inInRefCategory: " + inInRefCategory );
				result = firstRow = false;
			}
			if (result == false){
				System.out.println("\nChoose a specific type from the following reference types: \n" +
						"  1) BookSection - Book - PrintSeries \n" +
						"  2) InProceedings - pProceedings  - PrintSeries");
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	
	private static boolean checkPartOfArticle(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, Reference.NomTitleAbbrev as nomTitleAbbrev, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle, InRef.nomTitleAbbrev AS inRefnomTitleAbbrev, InRef.refCache AS inRefCache, InRef.nomRefCache AS inRefnomRefCache " + 
			" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
						" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 1) ";
			ResultSet rs = source.getResultSet(strQueryPartOfJournal);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are part-of-references that have an article as in-reference!");
					System.out.println("========================================================");
				}
				int refId = rs.getInt("RefId");
				int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				int inRefCategoryFk = rs.getInt("InRefCatFk");
				String inRefCat = rs.getString("InRefCat");
				String refCache = rs.getString("RefCache");
				String nomRefCache = rs.getString("nomRefCache");
				String title = rs.getString("title");
				String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
				String inRefTitle = rs.getString("InRefTitle");
				String inRefnomTitleAbbrev = rs.getString("inRefnomTitleAbbrev");
				String inRefnomRefCache = rs.getString("inRefnomRefCache");
				String inRefCache = rs.getString("inRefCache");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + "\n  titleAbbrev: " + nomTitleAbbrev + 
						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
						"\n  inRefTitle: " + inRefTitle + "\n  inRefTitleAbbrev: " + inRefnomTitleAbbrev +
						"\n  inRefnomRefCache: " + inRefnomRefCache + "\n  inRefCache: " + inRefCache 
						);
				result = firstRow = false;
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp,
			Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<TeamOrPersonBase> authorMap = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.AUTHOR_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
				
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		boolean success = true;
		MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(null);
		MapWrapper<ReferenceBase> nomRefStore= new MapWrapper<ReferenceBase>(null);
		
		//Map<Integer, ReferenceBase> referenceCollectionMap = new HashMap<Integer, ReferenceBase>();
		
		logger.info("start makeReferences ...");
		IReferenceService referenceService = cdmApp.getReferenceService();
		
		try {
			//get data from database
			String strQueryBase = 
					" SELECT Reference.* , InReference.RefId as InRefId, InReference.RefCategoryFk as InRefCategoryFk,  " +
						" InInReference.RefId as InInRefId, InInReference.RefCategoryFk as InInRefCategoryFk, " +
						" InReference.InRefFk AS InRefInRefFk, InInReference.InRefFk AS InInRefInRefFk " +
                    " FROM Reference AS InInReference " +
                    	" RIGHT OUTER JOIN Reference AS InReference ON InInReference.RefId = InReference.InRefFk " + 
                    	" RIGHT OUTER JOIN Reference ON InReference.RefId = dbo.Reference.InRefFk " + 
					" WHERE (1=1) "; 
			//strQueryBase += " AND Reference.refId = 7000000 " ;
			String strQueryNoInRef = strQueryBase + 
				" AND (Reference.InRefFk is NULL) ";
			
			String strQuery1InRef = strQueryBase + 
				" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NULL) ";

			String strQuery2InRef = strQueryBase + 
				" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NOT NULL) AND (InInReference.InRefFk is NULL) ";

			String strQueryTesMaxRecursion = strQueryBase + 
				" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NOT NULL) AND (InInReference.InRefFk is NOT NULL) ";

			ResultSet testMaxRecursionResultSet = source.getResultSet(strQueryTesMaxRecursion);
			if (testMaxRecursionResultSet.next() == true){
				logger.error("Maximum allowed InReference recursions exceeded in Berlin Model. Maximum recursion level is 2.");
				return false;
			}

			if (bmiConfig.getDoReferences() == CONCEPT_REFERENCES){
				strQueryNoInRef += " AND ( Reference.refId IN ( SELECT ptRefFk FROM PTaxon) ) ";
			}
			
			List<ResultSet> resultSetList = new ArrayList<ResultSet>();
			resultSetList.add(source.getResultSet(strQueryNoInRef));
			if (bmiConfig.getDoReferences() == ALL || bmiConfig.getDoReferences() == NOMENCLATURAL){
				resultSetList.add(source.getResultSet(strQuery1InRef));
				resultSetList.add(source.getResultSet(strQuery2InRef));
			}
			
			
			int j = 0;
			Iterator<ResultSet> resultSetListIterator =  resultSetList.listIterator();
			//for each resultsetlist
			while (resultSetListIterator.hasNext()){
				int i = 0;
				Integer nomRefCount = 0;
				Integer biblioRefsCount = 0;
				
				ResultSet rs = resultSetListIterator.next();
				//for each resultset
				while (rs.next()){
					
					if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("References handled: " + (i-1) + " in round " + j);}
					
					//create TaxonName element
					int refId = rs.getInt("refId");
					int categoryFk = rs.getInt("refCategoryFk");
					Object inRefFk = rs.getObject("inRefFk");
					int inRefCategoryFk = rs.getInt("InRefCategoryFk");
					String refYear = rs.getString("refYear");
					
					String pages = rs.getString("pageString");
					String issn = rs.getString("issn");
					String isbn = rs.getString("isbn");
					String edition = rs.getString("Edition");
					String volume = rs.getString("Volume");
					String series = rs.getString("Series");
					String publicationTown = rs.getString("PublicationTown");
					String publisher = rs.getString("publisher");
					String url = rs.getString("url");

					
					StrictReferenceBase referenceBase;
					try {
						logger.debug("RefCategoryFk: " + categoryFk);
						
						if (categoryFk == REF_JOURNAL){
							Journal journal = Journal.NewInstance();
							referenceBase = journal;
							journal.setIssn(issn);
							journal.setPlacePublished(publicationTown);
							journal.setPublisher(publisher);
						}else if(categoryFk == REF_BOOK){
							Book book = Book.NewInstance();
							referenceBase = book;
							book.setEdition(edition);
							book.setIsbn(isbn);
							book.setPages(pages);
							book.setPlacePublished(publicationTown);
							book.setPublisher(publisher);
							book.setVolume(volume);
							book.setEditor(null);
							book.setInSeries(null);
							
						}else if(categoryFk == REF_ARTICLE){
							Article article = Article.NewInstance();
							referenceBase = article;
							if (inRefFk != null){
								if (inRefCategoryFk == REF_JOURNAL){
									int inRefFkInt = (Integer)inRefFk;
									if (nomRefStore.containsId(inRefFkInt) || referenceStore.containsId(inRefFkInt)){
										ReferenceBase inJournal = nomRefStore.get(inRefFkInt);
										if (inJournal == null){
											inJournal = referenceStore.get(inRefFkInt);
											logger.info("inJournal (" + inRefFkInt + ") found in referenceStore instead of nomRefStore.");
											nomRefStore.put(inRefFkInt, inJournal);
										}
										if (inJournal == null){
											logger.warn("inJournal for " + inRefFkInt + " is null. "+
											" InReference relation could not be set");
										}else if (Journal.class.isAssignableFrom(inJournal.getClass())){
											article.setInJournal((Journal)inJournal);
											article.setPages(pages);
											article.setSeries(series);
											article.setVolume(volume);
											//logger.info("InJournal success " + inRefFkInt);
										}else{
											logger.warn("InJournal is not of type journal but of type " + inJournal.getClass().getSimpleName() +
												" Inreference relation could not be set");
										}
									}else{
										logger.error("Journal (refId = " + inRefFkInt + " ) for Article (refID = " + refId +") could not be found in nomRefStore. Inconsistency error. ");
										success = false;;
									}
								}else{
									logger.warn("Wrong inrefCategory for Article (refID = " + refId +"). Type must be 'Journal' but was not (RefCategoryFk=" + inRefCategoryFk + "))." +
										" InReference was not added to Article! ");
								}
							}
						}else if(categoryFk == REF_DATABASE){
							referenceBase = new Database();
						}else if(categoryFk == REF_PART_OF_OTHER_TITLE){
							if (inRefCategoryFk == REF_BOOK){
								//BookSection
								BookSection bookSection = BookSection.NewInstance();
								referenceBase = bookSection;
								bookSection.setPages(pages);
								if (inRefFk != null){
									int inRefFkInt = (Integer)inRefFk;
									if (nomRefStore.containsId(inRefFkInt) || referenceStore.containsId(inRefFkInt)){
										ReferenceBase inBook = nomRefStore.get(inRefFkInt);
										if (inBook == null){
											inBook = referenceStore.get(inRefFkInt);
											logger.info("inBook (" + inRefFkInt + ") found in referenceStore instead of nomRefStore.");
											nomRefStore.put(inRefFkInt, inBook);
										}
										if (inBook == null){
											logger.warn("inBook for " + inRefFkInt + " is null. "+
											" InReference relation could not be set");;
										}else if (Book.class.isAssignableFrom(inBook.getClass())){
											bookSection.setInBook((Book)inBook);
											bookSection.setPages(pages);
											//logger.info("InBook success " + inRefFkInt);
											//TODO
										}else{
											logger.warn("InBook is not of type book but of type " + inBook.getClass().getSimpleName() +
													" Inreference relation could not be set");
										}
									}else{
										logger.error("Book (refId = " + inRefFkInt + ") for part_of_other_title (refID = " + refId +") could not be found in nomRefStore. Inconsistency error. ");
										success = false;
									}
								}
							}else if (inRefCategoryFk == REF_ARTICLE){
								//Article
								//TODO 
								logger.warn("Reference (refId = " + refId + ") of type 'part_of_other_title' is part of 'article'." +
										" This type is not implemented yet. Generic reference created instead") ;
								referenceBase = Generic.NewInstance();
							}else if (inRefCategoryFk == REF_JOURNAL){
								//TODO 
								logger.warn("Reference (refId = " + refId + ") of type 'part_of_other_title' has inReference of type 'journal'." +
										" This is not allowed! Generic reference created instead") ;
								referenceBase = Generic.NewInstance();
								referenceBase.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
							}else{
								logger.warn("InReference type (catFk = " + inRefCategoryFk + ") of part-of-reference not recognized for refId " + refId + "." +
									" Create 'Generic' reference instead");
								referenceBase = Generic.NewInstance();
							}
						}else if(categoryFk == REF_INFORMAL){
							if (logger.isDebugEnabled()){logger.debug("RefType 'Informal'");}
							Generic informal = Generic.NewInstance();
							referenceBase = informal;
							//informal.setEditor(edition);
							informal.setPages(pages);
							informal.setPlacePublished(publicationTown);
							informal.setPublisher(publisher);
							informal.setSeries(series);
							informal.setVolume(volume);
						}else if(categoryFk == REF_WEBSITE){
							if (logger.isDebugEnabled()){logger.debug("RefType 'Website'");}
							WebPage webPage = WebPage.NewInstance();
							referenceBase = webPage;
							webPage.setPlacePublished(publicationTown);
							webPage.setPublisher(publisher);
						}else if(categoryFk == REF_UNKNOWN){
							if (logger.isDebugEnabled()){logger.debug("RefType 'Unknown'");}
							Generic generic = Generic.NewInstance();
							referenceBase = generic;
							generic.setPages(pages);
							generic.setPlacePublished(publicationTown);
							generic.setPublisher(publisher);
							generic.setSeries(series);
							generic.setVolume(volume);
							//TODO
						}else{
							logger.warn("Unknown categoryFk (" + categoryFk + "). Create 'Generic instead'");
							referenceBase = Generic.NewInstance();
						}
						
						referenceBase.setUri(url);
						//created, notes
						doIdCreatedUpdatedNotes(bmiConfig, referenceBase, rs, refId );						
						//refId
						ImportHelper.setOriginalSource(referenceBase, bmiConfig.getSourceReference(), refId);							
						
						
						//TODO
						referenceBase.setDatePublished(ImportHelper.getDatePublished(refYear)); 
						
						success &= makeNomAndBiblioReference(rs, refId, referenceBase, nomRefCount, biblioRefsCount, 
								referenceStore, nomRefStore, authorMap, nomRefMap, referenceMap );

					} catch (Exception e) {
						logger.warn("Reference with id " + refId +  " threw Exception and could not be saved");
						e.printStackTrace();
						success = false;
						return success;
					}
				} // end resultSet
				
				//change conceptRef uuid
				ReferenceBase sec = referenceStore.get(bmiConfig.getSourceSecId());
				if (sec == null){
					sec = nomRefStore.get(bmiConfig.getSourceSecId());	
				}
				if (sec != null){
					sec.setUuid(bmiConfig.getSecUuid());
					logger.info("SecUuid changed to: " + bmiConfig.getSecUuid());
				}
				
				//save and store in map
				logger.info("Save nomenclatural references (" + nomRefCount + ")");
				referenceService.saveReferenceAll(nomRefStore.objects());
				logger.info("Save bibliographical references (" + biblioRefsCount +")");
				referenceService.saveReferenceAll(referenceStore.objects());
				j++;
			}//end resultSetList	

			logger.info("end makeReferences ...");
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
	private boolean makeNomAndBiblioReference(ResultSet rs, 
				int refId, 
				StrictReferenceBase referenceBase,  
				Integer nomRefCount, 
				Integer biblioRefsCount,
				MapWrapper<ReferenceBase> referenceStore, 
				MapWrapper<ReferenceBase> nomRefStore, 
				MapWrapper<TeamOrPersonBase> authorMap,
				MapWrapper<ReferenceBase> nomRefMap,
				MapWrapper<ReferenceBase> referenceMap
				) throws SQLException{
		
		String refCache = rs.getString("refCache");
		String nomRefCache = rs.getString("nomRefCache");
		String title = rs.getString("title");
		String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
		boolean isPreliminary = rs.getBoolean("PreliminaryFlag");
		String refAuthorString = rs.getString("refAuthorString");
		int nomAuthorTeamFk = rs.getInt("NomAuthorTeamFk");
		TeamOrPersonBase nomAuthor = authorMap.get(nomAuthorTeamFk);
		
		boolean hasNomRef = false;
		//is Nomenclatural Reference
		if ( (! CdmUtils.Nz(nomRefCache).equals("") && isPreliminary) || (! CdmUtils.Nz(nomTitleAbbrev).equals("") && ! isPreliminary) ){
			referenceBase.setTitle(nomTitleAbbrev);
			TeamOrPersonBase author = getAuthorTeam(refAuthorString , nomAuthor, true);
			referenceBase.setAuthorTeam(author);
			//referenceBase.setNomenclaturallyRelevant(true);
			if (isPreliminary){
				referenceBase.setTitleCache(nomRefCache);
			}
			if (! nomRefStore.containsId(refId)){
				if (referenceBase == null){
					logger.warn("refBase is null");
				}
				nomRefStore.put(refId, referenceBase);
			}else{
				logger.warn("Duplicate refId in Berlin Model database. Second reference was not imported !!");
			}
			nomRefMap.put(refId, referenceBase);
			hasNomRef = true;
			nomRefCount++;
		}
		//is bibliographical Reference
		if ((! CdmUtils.Nz(refCache).equals("") && isPreliminary) || (! CdmUtils.Nz(title).equals("") && ! isPreliminary) || hasNomRef == false){
			if (hasNomRef){
				referenceBase = (StrictReferenceBase)referenceBase.clone();
			}
			referenceBase.setTitle(title);
			TeamOrPersonBase author = getAuthorTeam(refAuthorString , nomAuthor, false);
			referenceBase.setAuthorTeam(author);
			referenceBase.setNomenclaturallyRelevant(false);
			if (isPreliminary){
				referenceBase.setTitleCache(refCache);
			}
			if (! referenceStore.containsId(refId)){
				referenceStore.put(refId, referenceBase);
			}else{
				logger.warn("Duplicate refId in Berlin Model database. Second reference was not imported !!");
			}
			referenceMap.put(refId, referenceBase);
			biblioRefsCount++;
		}
		return true;
		
	}
	
	
	private static TeamOrPersonBase getAuthorTeam(String authorString, TeamOrPersonBase nomAuthor, boolean preferNomeclaturalAuthor){
		TeamOrPersonBase result;
		if (preferNomeclaturalAuthor){
			if (nomAuthor != null){
				result = nomAuthor;
			}else{
				if (CdmUtils.Nz(authorString).equals("")){
					result = null;
				}else{
					TeamOrPersonBase team = Team.NewInstance();
					//TODO which one to use??
					team.setNomenclaturalTitle(authorString);
					team.setTitleCache(authorString);
					result = team;
				}
			}
		}else{ //prefer bibliographic
			if (! CdmUtils.Nz(authorString).equals("")){
				TeamOrPersonBase team = Team.NewInstance();
				//TODO which one to use??
				team.setNomenclaturalTitle(authorString);
				team.setTitleCache(authorString);
				result = team;
			}else{
				result = nomAuthor;
			}
		}
		return result;
	}

	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TimePeriod timePeriod = ImportHelper.getDatePublished("1756 - 1783");
		System.out.println(timePeriod.getYear());
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return (config.getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}

	
	
}
