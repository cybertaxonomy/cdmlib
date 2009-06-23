/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_HETEROTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_HOMOTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_INCLUDED_IN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_MISAPPLIED_NAME_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PARTIAL_SYN_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PROPARTE_SYN_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_SYNONYM_OF;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelTaxonRelationImport  extends BerlinModelImportBase  {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonRelationImport.class);

	private static int modCount = 30000;
	
	public BerlinModelTaxonRelationImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
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
	

	private boolean makeTaxonomicTrees(BerlinModelImportState state) throws SQLException{
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.NOMREF_STORE);
		Source source = state.getConfig().getSource();

		logger.info("start makeTaxonomicTree ...");
		
		String strQuery = "SELECT PTaxon.PTRefFk " + 
						" FROM RelPTaxon INNER JOIN " + 
						" PTaxon AS PTaxon ON RelPTaxon.PTNameFk2 = PTaxon.PTNameFk AND RelPTaxon.PTRefFk2 = PTaxon.PTRefFk " +
						" WHERE (RelPTaxon.RelQualifierFk = 1) " + 
						" GROUP BY PTaxon.PTRefFk ";
		ResultSet rs = source.getResultSet(strQuery) ;
		int i = 0;
		//for each reference
		try {
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("RelPTaxa handled: " + (i-1));}
				
				int ptRefFk = rs.getInt("PTRefFk");
				
				StrictReferenceBase ref = (StrictReferenceBase)referenceMap.get(ptRefFk);
				if (ref == null){
					ref = (StrictReferenceBase)nomRefMap.get(ptRefFk);
				}
				//FIXME treeName
				String treeName = "TaxonTree - No Name";
				if (ref != null && CdmUtils.isNotEmpty(ref.getTitleCache())){
					treeName = ref.getTitleCache();
				}
				TaxonomicTree tree = TaxonomicTree.NewInstance(treeName);
				tree.setReference(ref);
				
				UUID uuid = getTaxonService().saveTaxonomicTree(tree);
				state.putTree(ref, tree);
			}
		} catch (SQLException e) {
			logger.error("Error in BerlinModleTaxonRelationImport.makeTaxonomicTrees: " + e.getMessage());
			throw e;
		}
		logger.info("end makeTaxonomicTree ...");

		return true;
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(BerlinModelImportState state){				
		boolean success = true;
		//make not needed maps empty
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)state.getStore(ICdmIO.TAXONNAME_STORE);
		taxonNameMap.makeEmpty();
		
		String strTeamStore = ICdmIO.TEAM_STORE;
		MapWrapper<? extends CdmBase> map = state.getStore(strTeamStore);
		MapWrapper<TeamOrPersonBase> teamMap = (MapWrapper<TeamOrPersonBase>)map;
		teamMap.makeEmpty();

		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.NOMREF_STORE);
		MapWrapper<ReferenceBase> nomRefDetailMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.NOMREF_DETAIL_STORE);
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);

		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		
		try {
			if (config.isUseTaxonomicTree()){
				success &= makeTaxonomicTrees(state);
			}
		
		
			logger.info("start makeTaxonRelationships ...");

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
				ReferenceBase citation = nomRefDetailMap.get(relRefFk);
				if (citation == null){
					citation = referenceMap.get(relRefFk);
				}
				if (citation == null){
					citation = nomRefMap.get(relRefFk);
				}
				
				String microcitation = null; //does not exist in RelPTaxon

				if (taxon2 != null && taxon1 != null){
					if (!(taxon2 instanceof Taxon)){
						logger.error("TaxonBase (ID = " + taxon2.getId()+ ", RIdentifier = " + taxon2Id + ") can't be casted to Taxon");
						success = false;
						continue;
					}
					Taxon toTaxon = (Taxon)taxon2;
					if (isTaxonRelationship(relQualifierFk)){
						if (!(taxon1 instanceof Taxon)){
							logger.error("TaxonBase (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") can't be casted to Taxon");
							success = false;
							continue;
						}
						Taxon fromTaxon = (Taxon)taxon1;
						if (relQualifierFk == TAX_REL_IS_INCLUDED_IN){
							makeTaxonomicallyIncluded(state, fromTaxon, toTaxon, citation, microcitation);
						}else if (relQualifierFk == TAX_REL_IS_MISAPPLIED_NAME_OF){
							toTaxon.addMisappliedName(fromTaxon, citation, microcitation);
						}
					}else if (isSynonymRelationship(relQualifierFk)){
						if (!(taxon1 instanceof Synonym)){
							logger.error("Taxon (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") can't be casted to Synonym");
							success = false;
							continue;
						}
						Synonym synonym = (Synonym)taxon1;
						SynonymRelationship synRel = getSynRel(relQualifierFk, toTaxon, synonym, citation, microcitation);
							
						if (relQualifierFk == TAX_REL_IS_SYNONYM_OF || 
								relQualifierFk == TAX_REL_IS_HOMOTYPIC_SYNONYM_OF ||
								relQualifierFk == TAX_REL_IS_HETEROTYPIC_SYNONYM_OF){
							addProParteAndPartial(synRel, synonym, config);
						}else if (relQualifierFk == TAX_REL_IS_PROPARTE_SYN_OF ||
								relQualifierFk == TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF ||
								relQualifierFk == TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF ){
								synRel.setProParte(true);
						}else if(relQualifierFk == TAX_REL_IS_PARTIAL_SYN_OF || 
								relQualifierFk == TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF ||
								relQualifierFk == TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF ){
								synRel.setPartial(true);
						}else{
							success = false;
							logger.warn("Proparte/Partial not yet implemented for TaxonRelationShipType " + relQualifierFk);
						}
					}else if (isConceptRelationship){
						ResultWrapper<Boolean> isInverse = new ResultWrapper<Boolean>();
						try {
							TaxonRelationshipType relType = BerlinModelTransformer.taxonRelId2TaxonRelType(relQualifierFk, isInverse);	
							if (! (taxon1 instanceof Taxon)){
								success = false;
								logger.error("TaxonBase (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") can't be casted to Taxon");
							}else{
								Taxon fromTaxon = (Taxon)taxon1;
								fromTaxon.addTaxonRelation(toTaxon, relType, citation, microcitation);
							}
						} catch (UnknownCdmTypeException e) {
							//TODO other relationships
							logger.warn("TaxonRelationShipType " + relQualifierFk + " (conceptRelationship) not yet implemented");
							success = false;
						}
					}else {
						//TODO
						logger.warn("TaxonRelationShipType " + relQualifierFk + " not yet implemented");
						success = false;
					}
					taxonStore.add(taxon2);
					
					//TODO
					//etc.
				}else{
					//TODO
					logger.warn("Taxa for RelPTaxon " + relPTaxonId + " do not exist in store");
					success = false;
				}
			}
			logger.info("Taxa to save: " + taxonStore.size());
			getTaxonService().saveTaxonAll(taxonStore);
			
			logger.info("end makeTaxonRelationships ..." + getSuccessString(success));
			return success;
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
	
	private boolean makeTaxonomicallyIncluded(BerlinModelImportState state, Taxon child, Taxon parent, ReferenceBase citation, String microCitation){
		if (state.getConfig().isUseTaxonomicTree() == false){
			parent.addTaxonomicChild(child, citation, microCitation);
			return true;
		}else{
			ReferenceBase toRef = parent.getSec();
			TaxonomicTree tree = state.getTree(toRef);
			if (tree == null){
				throw new IllegalStateException("Tree for ToTaxon reference does not exist.");
			}
			return tree.addParentChild(parent, child, citation, microCitation);
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoRelTaxa();
	}
	
}
