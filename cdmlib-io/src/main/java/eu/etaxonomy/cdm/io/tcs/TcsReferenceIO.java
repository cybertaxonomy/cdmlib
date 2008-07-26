/**
 * 
 */
package eu.etaxonomy.cdm.io.tcs;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import static eu.etaxonomy.cdm.io.common.ImportHelper.FACULTATIVE;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 *
 */
public class TcsReferenceIO extends CdmIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TcsReferenceIO.class);

	private static int modCount = 1000;
	
	public TcsReferenceIO(){
		super();
	}
	
	@Override
	public boolean doCheck(IImportConfigurator config){
		boolean result = true;
		result &= checkArticlesWithoutJournal(config);
		result &= checkPartOfJournal(config);
		
		return result;
	}
		
	private static boolean checkArticlesWithoutJournal(IImportConfigurator bmiConfig){
		try {
			boolean result = true;
//			Source source = bmiConfig.getSource();
//			String strQueryArticlesWithoutJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
//						" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
//						" WHERE (Reference.RefCategoryFk = 1) AND (InRef.RefCategoryFk <> 9) ";
//			ResultSet resulSetarticlesWithoutJournal = source.getResultSet(strQueryArticlesWithoutJournal);
//			boolean firstRow = true;
//			while (resulSetarticlesWithoutJournal.next()){
//				if (firstRow){
//					System.out.println("========================================================");
//					logger.warn("There are Articles with wrong inRef type!");
//					System.out.println("========================================================");
//				}
//				int refId = resulSetarticlesWithoutJournal.getInt("RefId");
//				int categoryFk = resulSetarticlesWithoutJournal.getInt("RefCategoryFk");
//				String cat = resulSetarticlesWithoutJournal.getString("RefCategoryAbbrev");
//				int inRefFk = resulSetarticlesWithoutJournal.getInt("InRefId");
//				int inRefCategoryFk = resulSetarticlesWithoutJournal.getInt("InRefCatFk");
//				String inRefCat = resulSetarticlesWithoutJournal.getString("InRefCat");
//				String refCache = resulSetarticlesWithoutJournal.getString("RefCache");
//				String nomRefCache = resulSetarticlesWithoutJournal.getString("nomRefCache");
//				String title = resulSetarticlesWithoutJournal.getString("title");
//				String inRefTitle = resulSetarticlesWithoutJournal.getString("InRefTitle");
//				
//				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
//						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
//						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
//						"\n  inRefTitle: " + inRefTitle );
//				result = firstRow = false;
//			}
//			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean checkPartOfJournal(IImportConfigurator bmiConfig){
		try {
			boolean result = true;
//			Source source = bmiConfig.getSource();
//			String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
//			" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
//						" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 9) ";
//			ResultSet rs = source.getResultSet(strQueryPartOfJournal);
//			boolean firstRow = true;
//			while (rs.next()){
//				if (firstRow){
//					System.out.println("========================================================");
//					logger.warn("There are part-of-references that have a Journal as in-reference!");
//					System.out.println("========================================================");
//				}
//				int refId = rs.getInt("RefId");
//				int categoryFk = rs.getInt("RefCategoryFk");
//				String cat = rs.getString("RefCategoryAbbrev");
//				int inRefFk = rs.getInt("InRefId");
//				int inRefCategoryFk = rs.getInt("InRefCatFk");
//				String inRefCat = rs.getString("InRefCat");
//				String refCache = rs.getString("RefCache");
//				String nomRefCache = rs.getString("nomRefCache");
//				String title = rs.getString("title");
//				String inRefTitle = rs.getString("InRefTitle");
//				
//				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
//						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
//						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
//						"\n  inRefTitle: " + inRefTitle );
//				result = firstRow = false;
//			}
//			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	@Override
	public boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp,
			Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
		MapWrapper<TeamOrPersonBase> authorMap = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.AUTHOR_STORE);
		
//		MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(null);
//		MapWrapper<ReferenceBase> nomRefStore= new MapWrapper<ReferenceBase>(null);
		
		TcsImportConfigurator tcsConfig = (TcsImportConfigurator)config;
		Element root = tcsConfig.getSourceRoot();
		logger.info("start makeReferences ...");
		
		String tcsElementName;
		Namespace tcsNamespace;
		String cdmAttrName;
		String value;
		boolean success = true;
		IReferenceService referenceService = cdmApp.getReferenceService();
		
		
		//Map<Integer, ReferenceBase> referenceCollectionMap = new HashMap<Integer, ReferenceBase>();
		
		Namespace rdfNamespace = root.getNamespace();
		String prefix = "tn";
		Namespace taxonNameNamespace = root.getNamespace(prefix);
		prefix = "tc";
		Namespace taxonConceptNamespace = root.getNamespace(prefix);
		prefix = "tcom";
		Namespace commonNamespace = root.getNamespace(prefix);
		prefix = "tpub";
		Namespace publicationNamespace = root.getNamespace(prefix);

		
		
		tcsElementName = "PublicationCitation";
		tcsNamespace = publicationNamespace;
		List<Element> elPublicationCitations = root.getChildren(tcsElementName, tcsNamespace);

		int nomRefCount = 0;
		int biblioRefsCount = 0;
		
		int i = 0;
		//for each taxonName
		for (Element elPublicationCitation : elPublicationCitations){
			
			if ((++i % modCount) == 0){ logger.info("references handled: " + (i-1));}
			
			Attribute about = elPublicationCitation.getAttribute("about", rdfNamespace);

			//create TaxonName element
			String strAbout = elPublicationCitation.getAttributeValue("about", rdfNamespace);
			
			tcsElementName = "publicationType";
			tcsNamespace = publicationNamespace;
			String strPubType = XmlHelp.getChildAttributeValue(elPublicationCitation, tcsElementName, tcsNamespace, "resource", rdfNamespace);
			
			try {
				StrictReferenceBase ref = TcsTransformer.pubTypeStr2PubType(strPubType);
				if (ref==null){
					ref = Generic.NewInstance();
				}
				//attributes
				tcsElementName = "publisher";
				tcsNamespace = publicationNamespace;
				cdmAttrName = "publisher";
				success &= ImportHelper.addXmlStringValue(elPublicationCitation, ref, tcsElementName, 
						tcsNamespace, cdmAttrName, FACULTATIVE);

				tcsElementName = "year";
				tcsNamespace = publicationNamespace;
				String strYear = elPublicationCitation.getChildText(tcsElementName, tcsNamespace);
				TimePeriod datePublished = ImportHelper.getDatePublished(strYear);
				ref.setDatePublished(datePublished);
				
				tcsElementName = "pages";
				tcsNamespace = publicationNamespace;
				cdmAttrName = "pages";
				success &= ImportHelper.addXmlStringValue(elPublicationCitation, ref, tcsElementName, 
						tcsNamespace, cdmAttrName, FACULTATIVE);

				tcsElementName = "volume";
				tcsNamespace = publicationNamespace;
				cdmAttrName = "volume";
				success &= ImportHelper.addXmlStringValue(elPublicationCitation, ref, tcsElementName, 
						tcsNamespace, cdmAttrName, FACULTATIVE);
				
				
				//Reference
				//TODO
				tcsElementName = "parentPublication";
				tcsNamespace = publicationNamespace;
				String strParent = XmlHelp.getChildAttributeValue(elPublicationCitation, tcsElementName, tcsNamespace, "resource", rdfNamespace);
				ReferenceBase parent = referenceMap.get(strParent);
				if (parent != null){
					logger.warn("parent not yet implemented");
					//TODO refBase.setParent(parent);
				}

				
				//FIXME
				//nomRef and reference
				tcsElementName = "shortTitle";
				tcsNamespace = publicationNamespace;
				String strShortTitle = elPublicationCitation.getChildText(tcsElementName, tcsNamespace);
				if (! CdmUtils.Nz(strShortTitle).trim().equals("")){
					ref.setTitle(strShortTitle);
					nomRefMap.put(strAbout, ref);
					nomRefCount++;
				}
				tcsElementName = "title";
				tcsNamespace = publicationNamespace;
				String strTitle = elPublicationCitation.getChildText(tcsElementName, tcsNamespace);
				tcsNamespace = publicationNamespace;
				if (! CdmUtils.Nz(strTitle).trim().equals("")){
					//TODO
					StrictReferenceBase biblioRef = (StrictReferenceBase)ref.clone();
					biblioRef.setTitle(strTitle);
					referenceMap.put(strAbout, biblioRef);
					biblioRefsCount++;
				}
				
//				
//				if (! referenceStore.containsId(strAbout)){
//					referenceStore.put(strAbout, refBase);
//				}


				
				//ImportHelper.setOriginalSource(nameBase, tcsConfig.getSourceReference(), nameId);
				referenceMap.put(strAbout, ref);
				
			} catch (UnknownCdmTypeException e) {
				//FIXME
				logger.warn("Name with id " + strAbout + " has unknown type " + strPubType + " and could not be saved.");
				success = false; 
			}
		}
		
		//change conceptRef uuid
		ReferenceBase sec = referenceMap.get(config.getSourceSecId());
		if (sec == null){
			sec = nomRefMap.get(config.getSourceSecId());	
		}
		if (sec != null){
			sec.setUuid(config.getSecUuid());
			logger.info("concept reference uuid changed to: " + config.getSecUuid());
		}
		
		
		//save and store in map
		logger.info("Save nomenclatural references (" + nomRefCount + ")");
		referenceService.saveReferenceAll(nomRefMap.objects());
		logger.info("Save bibliographical references (" + biblioRefsCount +")");
		referenceService.saveReferenceAll(referenceMap.objects());
		
		//referenceService.saveReferenceAll(referenceMap.objects());
		logger.info("end makeReferences ...");
		return success;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return (config.getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}
	
}
