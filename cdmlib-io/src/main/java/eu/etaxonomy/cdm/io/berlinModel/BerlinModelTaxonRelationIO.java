/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_HOMOTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_INCLUDED_IN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_MISAPPLIED_NAME_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_SYNONYM_OF;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 *
 */
public class BerlinModelTaxonRelationIO  extends BerlinModelIOBase  {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonRelationIO.class);

	private static int modCount = 30000;
	
	public BerlinModelTaxonRelationIO(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		logger.warn("Checking for TaxonRelations not yet fully implemented");
		result &= checkInActivatedStatus(bmiConfig);
		//result &= checkArticlesWithoutJournal(bmiConfig);
		
		return result;
	}
	
	private boolean checkInActivatedStatus(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strSQL = 
				" SELECT RelPTaxon.RelPTaxonId, RelPTaxon.RelQualifierFk, FromName.FullNameCache AS FromName, RelPTaxon.PTNameFk1 AS FromNameID, "  +
		    			" Status.Status AS FromStatus, ToName.FullNameCache AS ToName, RelPTaxon.PTNameFk2 AS ToNameId, ToStatus.Status AS ToStatus " + 
    			" FROM PTaxon AS FromTaxon " + 
    				" INNER JOIN RelPTaxon ON FromTaxon.PTNameFk = RelPTaxon.PTNameFk1 AND FromTaxon.PTRefFk = RelPTaxon.PTRefFk1 " + 
    				" INNER JOIN PTaxon AS ToTaxon ON RelPTaxon.PTNameFk2 = ToTaxon.PTNameFk AND RelPTaxon.PTRefFk2 = ToTaxon.PTRefFk " + 
    				" INNER JOIN Name AS ToName ON ToTaxon.PTNameFk = ToName.NameId " + 
    				" INNER JOIN Name AS FromName ON FromTaxon.PTNameFk = FromName.NameId " + 
    				" INNER JOIN Status ON FromTaxon.StatusFk = Status.StatusId AND FromTaxon.StatusFk = Status.StatusId " + 
    				" INNER JOIN Status AS ToStatus ON ToTaxon.StatusFk = ToStatus.StatusId AND ToTaxon.StatusFk = ToStatus.StatusId " +
				" WHERE (RelPTaxon.RelQualifierFk = - 99)";
			
			ResultSet rs = source.getResultSet(strSQL);
			boolean firstRow = true;
			int i = 0;
			while (rs.next()){
				i++;
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are TaxonRelationships with status 'inactivated'(-99)!");
					System.out.println("========================================================");
				}
				
				int relPTaxonId = rs.getInt("RelPTaxonId");
				String fromName = rs.getString("FromName");
				int fromNameID = rs.getInt("FromNameID");
				String fromStatus = rs.getString("FromStatus");
				
				String toName = rs.getString("ToName");
				int toNameId = rs.getInt("ToNameId");
				String toStatus = rs.getString("ToStatus");
				
				System.out.println("RelPTaxonId:" + relPTaxonId + 
						"\n  FromName: " + fromName + "\n  FromNameID: " + fromNameID + "\n  FromStatus: " + fromStatus +
						"\n  ToName: " + toName + "\n  ToNameId: " + toNameId + "\n  ToStatus: " + toStatus);
				result = firstRow = false;
			}
			if (i > 0){
				System.out.println(" ");
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	

	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){				
			
		//make not needed maps empty
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)stores.get(ICdmIO.TAXONNAME_STORE);
		taxonNameMap.makeEmpty();
		MapWrapper<TeamOrPersonBase> authorMap = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.AUTHOR_STORE);
		authorMap.makeEmpty();

		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);

		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxonRelationships ...");
		
		ITaxonService taxonService = config.getCdmAppController().getTaxonService();

		try {
			//get data from database
			String strQuery = 
					" SELECT RelPTaxon.*, FromTaxon.RIdentifier as taxon1Id, ToTaxon.RIdentifier as taxon2Id, q.is_concept_relation " + 
					" FROM PTaxon as FromTaxon INNER JOIN " +
                      	" RelPTaxon ON FromTaxon.PTNameFk = RelPTaxon.PTNameFk1 AND FromTaxon.PTRefFk = RelPTaxon.PTRefFk1 INNER JOIN " +
                      	" PTaxon AS ToTaxon ON RelPTaxon.PTNameFk2 = ToTaxon.PTNameFk AND RelPTaxon.PTRefFk2 = ToTaxon.PTRefFk " +
                      	" INNER JOIN RelPTQualifier q ON q.RelPTQualifierId = RelPTaxon.RelQualifierFk " + 
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("RelPTaxa handled: " + (i-1));}
				
				int relPTaxonId = rs.getInt("RelPTaxonId");
				int taxon1Id = rs.getInt("taxon1Id");
				int taxon2Id = rs.getInt("taxon2Id");
				int relRefFk = rs.getInt("relRefFk");
				int relQualifierFk = rs.getInt("relQualifierFk");
				boolean isConceptRelationship = rs.getBoolean("is_concept_relation");
				
				TaxonBase taxon1 = taxonMap.get(taxon1Id);
				TaxonBase taxon2 = taxonMap.get(taxon2Id);
				
				//TODO
				ReferenceBase citation = null;
				String microcitation = null;

				if (taxon2 != null && taxon1 != null){
					if (!(taxon2 instanceof Taxon)){
						logger.error("TaxonBase (ID = " + taxon2.getId()+ ", RIdentifier = " + taxon2Id + ") can't be casted to Taxon");
						continue;
					}
					Taxon toTaxon = (Taxon)taxon2;
					if (isTaxonRelationship(relQualifierFk)){
						if (!(taxon1 instanceof Taxon)){
							logger.error("TaxonBase (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") can't be casted to Taxon");
							continue;
						}
						Taxon fromTaxon = (Taxon)taxon1;
						if (relQualifierFk == TAX_REL_IS_INCLUDED_IN){
							toTaxon.addTaxonomicChild(fromTaxon, citation, microcitation);
						}else if (relQualifierFk == TAX_REL_IS_MISAPPLIED_NAME_OF){
							toTaxon.addMisappliedName(fromTaxon, citation, microcitation);
						}
					}else if (isSynonymRelationship(relQualifierFk)){
						if (!(taxon1 instanceof Synonym)){
							logger.error("Taxon (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") can't be casted to Synonym");
							continue;
						}
						Synonym synonym = (Synonym)taxon1;
						SynonymRelationship synRel = getSynRel(relQualifierFk, toTaxon, synonym, citation, microcitation);
							
						if (relQualifierFk == TAX_REL_IS_SYNONYM_OF || 
								relQualifierFk == TAX_REL_IS_HOMOTYPIC_SYNONYM_OF ||
								relQualifierFk == TAX_REL_IS_HETEROTYPIC_SYNONYM_OF){
							addProParteAndPartial(synRel, synonym, bmiConfig);
						}else if (relQualifierFk == TAX_REL_IS_PROPARTE_SYN_OF ||
								relQualifierFk == TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF ||
								relQualifierFk == TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF ){
								synRel.setProParte(true);
						}else if(relQualifierFk == TAX_REL_IS_PARTIAL_SYN_OF || 
								relQualifierFk == TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF ||
								relQualifierFk == TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF ){
								synRel.setPartial(true);
						}else{
							logger.warn("Proparte/Partial not yet implemented for TaxonRelationShipType " + relQualifierFk);
						}
					}else if (isConceptRelationship){
						//TODO
						if (relQualifierFk == 11){
							
						}
						logger.warn("TaxonRelationShipType " + relQualifierFk + " (conceptRelationship) not yet implemented");
					}else {
						//TODO
						logger.warn("TaxonRelationShipType " + relQualifierFk + " not yet implemented");
					}
					taxonStore.add(taxon2);
					
					//TODO
					//Reference
					//ID
					//etc.
				}else{
					//TODO
					logger.warn("Taxa for RelPTaxon " + relPTaxonId + " do not exist in store");
				}
			}
			logger.info("Taxa to save: " + taxonStore.size());
			taxonService.saveTaxonAll(taxonStore);
			
			logger.info("end makeTaxonRelationships ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	private SynonymRelationship getSynRel (int relQualifierFk, Taxon toTaxon, Synonym synonym, ReferenceBase citation, String microcitation){
		SynonymRelationship result;
		if (relQualifierFk == TAX_REL_IS_HOMOTYPIC_SYNONYM_OF ||
				relQualifierFk == TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF ||
				relQualifierFk == TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF){
			result = toTaxon.addHomotypicSynonym(synonym, citation, microcitation);
		}else if (relQualifierFk == TAX_REL_IS_HETEROTYPIC_SYNONYM_OF ||
				relQualifierFk == TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF ||
				relQualifierFk == TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF){
			result = toTaxon.addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), citation, microcitation);
		}else if (relQualifierFk == TAX_REL_IS_SYNONYM_OF ||
				relQualifierFk == TAX_REL_IS_PROPARTE_SYN_OF ||
				relQualifierFk == TAX_REL_IS_PARTIAL_SYN_OF){
			result = toTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF(), citation, microcitation);
		}else{
			logger.warn("SynonymyRelationShipType could not be defined for relQualifierFk " + relQualifierFk + ". 'Unknown'-Type taken instead.");
			result = toTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF(), citation, microcitation);
		}
		return result;

	}
	
	private  boolean isSynonymRelationship(int relQualifierFk){
		if (relQualifierFk == TAX_REL_IS_SYNONYM_OF || 
			relQualifierFk == TAX_REL_IS_HOMOTYPIC_SYNONYM_OF || 
			relQualifierFk == TAX_REL_IS_HETEROTYPIC_SYNONYM_OF ||
			relQualifierFk == TAX_REL_IS_PROPARTE_SYN_OF || 
			relQualifierFk == TAX_REL_IS_PARTIAL_SYN_OF ||
			relQualifierFk == TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF ||
			relQualifierFk == TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF ||
			relQualifierFk == TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF ||
			relQualifierFk == TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF
		){
			return true;
		}else{
			return false;
		}
	}
	
	private  boolean isTaxonRelationship(int relQualifierFk){
		if (relQualifierFk == TAX_REL_IS_INCLUDED_IN || 
		relQualifierFk == TAX_REL_IS_MISAPPLIED_NAME_OF){
			return true;
		}else{
			return false;
		}
	}
	
	private void addProParteAndPartial(SynonymRelationship synRel, Synonym synonym, BerlinModelImportConfigurator bmiConfig){
		if (bmiConfig.isPartialSynonym(synonym)){
			synRel.setPartial(true);
		}
		if (bmiConfig.isProParteSynonym(synonym)){
			synRel.setProParte(true);
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoRelTaxa();
	}
	
}
