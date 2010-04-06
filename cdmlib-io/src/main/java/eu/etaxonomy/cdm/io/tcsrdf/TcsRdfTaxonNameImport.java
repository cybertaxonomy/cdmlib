/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 29.05.2008
 * @version 1.0
 */
@Component
public class TcsRdfTaxonNameImport  extends TcsRdfImportBase implements ICdmIO<TcsRdfImportState> {
	private static final Logger logger = Logger.getLogger(TcsRdfTaxonNameImport.class);

	private static int modCount = 5000;
	
	public TcsRdfTaxonNameImport(){
		super();
	}

	@Override
	public boolean doCheck(TcsRdfImportState config){
		boolean result = true;
		logger.warn("BasionymRelations not yet implemented");
		logger.warn("Checking for TaxonNames not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);
		
		return result;
	}
	
	protected static CdmSingleAttributeXmlMapperBase[] standardMappers = new CdmSingleAttributeXmlMapperBase[]{
		new CdmTextElementMapper("genusPart", "genusOrUninomial") 
		, new CdmTextElementMapper("uninomial", "genusOrUninomial")  //TODO make it a more specific Mapper for both attributes
		, new CdmTextElementMapper("specificEpithet", "specificEpithet")
		, new CdmTextElementMapper("infraspecificEpithet", "infraSpecificEpithet")
		, new CdmTextElementMapper("infragenericEpithet", "infraGenericEpithet")
		, new CdmTextElementMapper("microReference", nsTcom, "nomenclaturalMicroReference")		
		
	};

	protected static CdmSingleAttributeXmlMapperBase[] operationalMappers = new CdmSingleAttributeXmlMapperBase[]{
		new CdmUnclearMapper("basionymAuthorship")
		, new CdmUnclearMapper("combinationAuthorship")
		, new CdmUnclearMapper("hasAnnotation")
		, new CdmUnclearMapper("rank")
		, new CdmUnclearMapper("nomenclaturalCode")
		, new CdmUnclearMapper("publishedIn", nsTcom)
		, new CdmUnclearMapper("year")
	};
	
	protected static CdmSingleAttributeXmlMapperBase[] unclearMappers = new CdmSingleAttributeXmlMapperBase[]{
		new CdmUnclearMapper("authorship")
		, new CdmUnclearMapper("rankString")
		, new CdmUnclearMapper("nameComplete")
		, new CdmUnclearMapper("hasBasionym")
		, new CdmUnclearMapper("dateOfEntry", nsTpalm)	
	};
	
	@Override
	protected boolean doInvoke(TcsRdfImportState state){
		
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		MapWrapper<TeamOrPersonBase> authorMap = (MapWrapper<TeamOrPersonBase>)state.getStore(ICdmIO.TEAM_STORE);

		String tcsElementName;
		Namespace tcsNamespace;
		String value;
		
		logger.info("start makeTaxonNames ...");
		TcsRdfImportConfigurator config = state.getConfig();
		Element root = config.getSourceRoot();
		boolean success =true;
		
		Namespace rdfNamespace = config.getRdfNamespace();
		Namespace taxonNameNamespace = config.getTnNamespace();
		
		String idNamespace = "TaxonName";
		
		List<Element> elTaxonNames = root.getChildren("TaxonName", taxonNameNamespace);
		
		int i = 0;
		//for each taxonName
		for (Element elTaxonName : elTaxonNames){
			
			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}
			
			Attribute about = elTaxonName.getAttribute("about", rdfNamespace);

			//create TaxonName element
			String nameAbout = elTaxonName.getAttributeValue("about", rdfNamespace);
			String strRank = XmlHelp.getChildAttributeValue(elTaxonName, "rank", taxonNameNamespace, "resource", rdfNamespace);
			String strNomenclaturalCode = XmlHelp.getChildAttributeValue(elTaxonName, "nomenclaturalCode", taxonNameNamespace, "resource", rdfNamespace);
			
			try {
				Rank rank = TcsRdfTransformer.rankString2Rank(strRank);
				NomenclaturalCode nomCode = TcsRdfTransformer.nomCodeString2NomCode(strNomenclaturalCode);
				TaxonNameBase nameBase = nomCode.getNewTaxonNameInstance(rank);
				
				Set<String> omitAttributes = null;
				makeStandardMapper(elTaxonName, nameBase, omitAttributes, standardMappers);
				
				//Reference
				//TODO
				tcsElementName = "publishedIn";
				tcsNamespace = config.getCommonNamespace();
				value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, tcsNamespace);
				if (value != null){
					ReferenceFactory refFactory = ReferenceFactory.newInstance();
					IGeneric nomRef = refFactory.newGeneric(); //TODO
					nomRef.setTitleCache(value);
					nameBase.setNomenclaturalReference(nomRef);
					
					//TODO
					tcsElementName = "year";
					tcsNamespace = taxonNameNamespace;
					Integer year = null;
					value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, tcsNamespace);
					if (value != null){
						try {
							year = Integer.valueOf(value);
							TimePeriod timeP = TimePeriod.NewInstance(year);
							nomRef.setDatePublished(timeP);
						} catch (RuntimeException e) {
							logger.warn("year could not be parsed");
						}
					}
					if (state.getConfig().isPublishReferences()){
						((ReferenceBase)nomRef).addMarker(Marker.NewInstance(MarkerType.PUBLISH(), false));
					}
				}
						
				//Status
				tcsNamespace = taxonNameNamespace;
				Element elAnnotation = elTaxonName.getChild("hasAnnotation", tcsNamespace);
				if (elAnnotation != null){
					Element elNomenclaturalNote = elAnnotation.getChild("NomenclaturalNote", tcsNamespace);
					if (elNomenclaturalNote != null){
						String statusValue = (String)ImportHelper.getXmlInputValue(elNomenclaturalNote, "note", tcsNamespace);
						String type = XmlHelp.getChildAttributeValue(elNomenclaturalNote, "type", tcsNamespace, "resource", rdfNamespace);
						String tdwgType = "http://rs.tdwg.org/ontology/voc/TaxonName#PublicationStatus";
						if (tdwgType.equalsIgnoreCase(type)){
							try {
								NomenclaturalStatusType statusType = TcsRdfTransformer.nomStatusString2NomStatus(statusValue);
								//NomenclaturalStatusType statusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusValue);
								if (statusType != null){
									nameBase.addStatus(NomenclaturalStatus.NewInstance(statusType));
								}
							} catch (UnknownCdmTypeException e) {
								if (! statusValue.equals("valid")){
									logger.warn("Unknown NomenclaturalStatusType: " +  statusValue);
								}
							}
						}
					}
				}
				
				if (nameBase instanceof NonViralName){
					NonViralName<?> nonViralName = (NonViralName<?>)nameBase;
					
					//AuthorTeams
					//TODO
					tcsElementName = "basionymAuthorship";
					String basionymAuthorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
					if (basionymAuthorValue != null){
						INomenclaturalAuthor basionymAuthor = Team.NewInstance();
						basionymAuthor.setNomenclaturalTitle(basionymAuthorValue);
						nonViralName.setBasionymAuthorTeam(basionymAuthor);
					}
						
					//TODO
					tcsElementName = "combinationAuthorship";
					String combinationAuthorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
					if (combinationAuthorValue != null){
						INomenclaturalAuthor combinationAuthor = Team.NewInstance();
						combinationAuthor.setNomenclaturalTitle(combinationAuthorValue);
						nonViralName.setCombinationAuthorTeam(combinationAuthor);
					}
					
					//set the authorshipCache
					tcsElementName = "authorship";
					String authorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
					String cache = nonViralName.getAuthorshipCache();
					if ( authorValue != null){
						//compare existing authorship cache with new one and check if it is necessary to 
						//make cache protected  //TODO refinement
						if (cache == null){
							nonViralName.setAuthorshipCache(authorValue);
						}else{
							cache = basionymAuthorValue == null ? cache : cache.replace(basionymAuthorValue, "");
							cache = combinationAuthorValue == null ? cache : cache.replace(combinationAuthorValue, "");
							cache = cache.replace("\\(|\\)", "");
							cache = cache.trim();
							if (! cache.equals("")){
								nonViralName.setAuthorshipCache(authorValue);
							}
						}
					}
				}
				ImportHelper.setOriginalSource(nameBase, config.getSourceReference(), nameAbout, idNamespace);
				
				checkAdditionalContents(elTaxonName, standardMappers, operationalMappers, unclearMappers);
				
				//nameId
				//TODO
				//ImportHelper.setOriginalSource(nameBase, tcsConfig.getSourceReference(), nameId);
				taxonNameMap.put(nameAbout, nameBase);
				
			} catch (UnknownCdmTypeException e) {
				//FIXME
				logger.warn("Name with id " + nameAbout + " has unknown rank " + strRank + " and could not be saved.");
				success = false; 
			}
		}
		logger.info(i + " names handled");
		getNameService().save(taxonNameMap.objects());
//		makeNameSpecificData(nameMap);
		logger.info("end makeTaxonNames ...");
		return success;

	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(TcsRdfImportState state){
		return ! state.getConfig().isDoTaxonNames();
	}

}
