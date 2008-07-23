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
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
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
		MapWrapper<TeamOrPersonBase> authorMap = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.AUTHOR_STORE);
		
		TcsImportConfigurator tcsConfig = (TcsImportConfigurator)config;
		Element source = tcsConfig.getSourceRoot();
		Element root = tcsConfig.getSourceRoot();
		logger.info("start makeReferences ...");
		
		String tcsElementName;
		Namespace tcsNamespace;
		String cdmAttrName;
		String value;
		boolean success = true;
		IReferenceService referenceService = cdmApp.getReferenceService();
		
		
		MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(null);
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

		
		int i = 0;
		//for each taxonName
		for (Element elPublicationCitation : elPublicationCitations){
			
			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}
			
			Attribute about = elPublicationCitation.getAttribute("about", rdfNamespace);

			//create TaxonName element
			String strAbout = elPublicationCitation.getAttributeValue("about", rdfNamespace);
			
			String pages = 
			tcsElementName = "publicationType";
			tcsNamespace = publicationNamespace;
			String strPubType = XmlHelp.getChildAttributeValue(elPublicationCitation, tcsElementName, tcsNamespace, "resource", rdfNamespace);
			
			try {
				ReferenceBase refBase = TcsTransformer.pubTypeStr2PubType(strPubType);
				
				//attributes
				tcsElementName = "publisher";
				tcsNamespace = publicationNamespace;
				cdmAttrName = "genusOrUninomial";
				success &= ImportHelper.addXmlStringValue(elPublicationCitation, refBase, tcsElementName, tcsNamespace, cdmAttrName);

				tcsElementName = "shortTitle";
				tcsNamespace = publicationNamespace;
				cdmAttrName = "title";
				success &= ImportHelper.addXmlStringValue(elPublicationCitation, refBase, tcsElementName, tcsNamespace, cdmAttrName);

				tcsElementName = "title";
				tcsNamespace = publicationNamespace;
				cdmAttrName = "title";
				success &= ImportHelper.addXmlStringValue(elPublicationCitation, refBase, tcsElementName, tcsNamespace, cdmAttrName);

				tcsElementName = "year";
				tcsNamespace = publicationNamespace;
				cdmAttrName = "year";
				success &= ImportHelper.addXmlStringValue(elPublicationCitation, refBase, tcsElementName, tcsNamespace, cdmAttrName);

				//TODO
				tcsElementName = "year";
				tcsNamespace = publicationNamespace;
				Integer year = null;
				try {
					value = (String)ImportHelper.getXmlInputValue(elPublicationCitation, tcsElementName, tcsNamespace);
					year = Integer.valueOf(value);
					Calendar cal = Calendar.getInstance();
					//FIXME
					cal.set(year, 1, 1);
					if (refBase instanceof StrictReferenceBase){
						StrictReferenceBase ref = (StrictReferenceBase)refBase;
						ref.setDatePublished(TimePeriod.NewInstance(cal));
					}else{
						logger.warn("year not implemented for ReferenceBase type " +  ((refBase == null) ? "(null)" : refBase.getClass().getSimpleName()));
					}
				} catch (RuntimeException e) {
					logger.warn("year could not be parsed");
				}
			

				
				
				tcsElementName = "pages";
				tcsNamespace = publicationNamespace;
				cdmAttrName = "pages";
				success &= ImportHelper.addXmlStringValue(elPublicationCitation, refBase, tcsElementName, tcsNamespace, cdmAttrName);

				tcsElementName = "volume";
				tcsNamespace = publicationNamespace;
				cdmAttrName = "volume";
				success &= ImportHelper.addXmlStringValue(elPublicationCitation, refBase, tcsElementName, tcsNamespace, cdmAttrName);
				
				
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
				
				//ImportHelper.setOriginalSource(nameBase, tcsConfig.getSourceReference(), nameId);
				referenceMap.put(strAbout, refBase);
				
			} catch (UnknownCdmTypeException e) {
				//FIXME
				logger.warn("Name with id " + strAbout + " has unknown type " + strPubType + " and could not be saved.");
				success = false; 
			}
		}
		logger.info(i + " names handled");
		referenceService.saveReferenceAll(referenceMap.objects());
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
