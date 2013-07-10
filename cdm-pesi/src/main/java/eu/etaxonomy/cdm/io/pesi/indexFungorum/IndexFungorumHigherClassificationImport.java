/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.indexFungorum;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.pesi.out.PesiTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 27.02.2012
 */
@Component
public class IndexFungorumHigherClassificationImport  extends IndexFungorumImportBase {
	private static final Logger logger = Logger.getLogger(IndexFungorumHigherClassificationImport.class);
	
	private static final String pluralString = "higher classifications";
	private static final String dbTableName = "tblPESIfungi-Classification";

	public IndexFungorumHigherClassificationImport(){
		super(pluralString, dbTableName, null);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(IndexFungorumImportConfigurator config) {
		String strRecordQuery = 
			" SELECT DISTINCT KingdomName, PhylumName, SubphylumName, ClassName, SubclassName, OrderName, FamilyName, g.PreferredName as GenusName, c.PreferredName as SpeciesName " + 
			" FROM [tblPESIfungi-Classification] c  LEFT OUTER JOIN " +
                      " tblGenera g ON c.PreferredNameFDCnumber = g.RECORD_NUMBER" +
//			" WHERE ( dr.id IN (" + ID_LIST_TOKEN + ") )";
			" ORDER BY KingdomName, PhylumName, SubphylumName, ClassName, SubclassName, OrderName, FamilyName, GenusName, SpeciesName ";
		return strRecordQuery;
	}

	
	
	
	
	@Override
	protected void doInvoke(IndexFungorumImportState state) {
		String sql = getRecordQuery(state.getConfig());
		ResultSet rs = state.getConfig().getSource().getResultSet(sql);
		
		//only 1 partition here
		
		String lastKingdom = "";
		String lastPhylum = "";
		String lastSubphylum = "";
		String lastClassname = "";
		String lastSubclass = "";
		String lastOrder = "";
		String lastFamily = "";
//		String lastGenus = "";
//		String lastSpecies = "";
		
		Taxon taxonKingdom = null;
		Taxon taxonPhylum = null;
		Taxon taxonSubphylum = null;
		Taxon taxonClass = null;
		Taxon taxonSubclass = null;
		Taxon taxonOrder = null;
		Taxon taxonFamily = null;
//		Taxon taxonGenus = null;
//		Taxon taxonSpecies = null;
		
		Taxon higherTaxon = null;
		
		
		TransactionStatus tx = startTransaction();
		ResultSet rsRelatedObjects = state.getConfig().getSource().getResultSet(sql);
		state.setRelatedObjects((Map)getRelatedObjectsForPartition(rsRelatedObjects));
		
		Classification classification = getClassification(state);
		
		try {
			while (rs.next()){
				String kingdom = rs.getString("KingdomName");
				String phylum = rs.getString("PhylumName");
				String subphylum = rs.getString("SubphylumName");
				String classname = rs.getString("ClassName");
				String subclass = rs.getString("SubclassName");
				String order = rs.getString("OrderName");
				String family = rs.getString("FamilyName");
//				String genus = rs.getString("GenusName");
//				String species = rs.getString("SpeciesName");
				
//				if (isNewTaxon(species, lastSpecies)){
//					if (isNewTaxon(genus, lastGenus)){
				if (isNewTaxon(family, lastFamily)){
					if (isNewTaxon(order,lastOrder)){
						if (isNewTaxon(subclass,lastSubclass)){
							if (isNewTaxon(classname,lastClassname)){
								if (isNewTaxon(subphylum, lastSubphylum)){
									if (isNewTaxon(phylum,lastPhylum)){
										if (isNewTaxon(kingdom,lastKingdom)){
											taxonKingdom = makeTaxon(state, kingdom, Rank.KINGDOM());
											lastKingdom = kingdom;
											logger.info("Import kingdom " +  kingdom);
											getTaxonService().saveOrUpdate(taxonKingdom);
										}else{
											higherTaxon = taxonKingdom;
										}
										higherTaxon = isIncertisSedis(kingdom) ? higherTaxon : taxonKingdom;
										Rank newRank = (lastKingdom.equals("Fungi") ? null : Rank.PHYLUM());
										taxonPhylum = makeTaxon(state, phylum, newRank);
										if (taxonPhylum != null){
											classification.addParentChild(higherTaxon, taxonPhylum, null, null);
										}
										higherTaxon = isIncertisSedis(phylum) ? higherTaxon : taxonPhylum;
										lastPhylum = phylum;
										logger.info("Import Phylum " +  phylum);
									}else{
										higherTaxon = taxonPhylum;
									}
									Rank newRank = (lastKingdom.equals("Fungi") ? null : Rank.SUBPHYLUM());
									taxonSubphylum = makeTaxon(state, subphylum, newRank);
									if (taxonSubphylum != null){
										getClassification(state).addParentChild(higherTaxon,taxonSubphylum, null, null);
									}
									higherTaxon = isIncertisSedis(subphylum) ? higherTaxon : taxonSubphylum;
									lastSubphylum = subphylum;
								}else{
									higherTaxon = taxonSubphylum;
								}
								taxonClass = makeTaxon(state, classname, Rank.CLASS());
								if (taxonClass != null){
									getClassification(state).addParentChild(higherTaxon, taxonClass, null, null);
								}
								higherTaxon = isIncertisSedis(classname) ? higherTaxon : taxonClass;
								lastClassname = classname;
							}else{
								higherTaxon = taxonClass;
							}
							taxonSubclass = makeTaxon(state, subclass, Rank.SUBCLASS());
							if (taxonSubclass != null){
								getClassification(state).addParentChild(higherTaxon, taxonSubclass,null, null);
							}
							higherTaxon = isIncertisSedis(subclass) ? higherTaxon : taxonSubclass;
							lastSubclass = subclass;
						}else{
							higherTaxon = taxonSubclass;
						}
						taxonOrder = makeTaxon(state, order, Rank.ORDER());
						if (taxonOrder != null){
							getClassification(state).addParentChild(higherTaxon, taxonOrder, null, null);
						}
						higherTaxon = isIncertisSedis(order) ? higherTaxon : taxonOrder;
						lastOrder = order;
					}else{
						higherTaxon = taxonOrder;
					}
					taxonFamily = makeTaxon(state, family, Rank.FAMILY());
					if (taxonFamily != null){
						getClassification(state).addParentChild(higherTaxon, taxonFamily, null, null);
					}
					higherTaxon = isIncertisSedis(family) ? higherTaxon : taxonFamily;
					lastFamily = family;
					getTaxonService().saveOrUpdate(higherTaxon);
				}
//						else{
//							higherTaxon = taxonFamily;
//						}
//						taxonGenus = makeTaxon(state, genus, Rank.GENUS());
//						if (taxonGenus != null){
//							getClassification(state).addParentChild(higherTaxon, taxonGenus, null, null);
//						}
//						higherTaxon = isIncertisSedis(genus) ? higherTaxon : taxonGenus;
//						lastGenus = genus;
//					}else{
//						higherTaxon = taxonGenus;
//					}
//					taxonSpecies = makeTaxon(state, species, Rank.SPECIES());
//					if (taxonSpecies != null){
//						getClassification(state).addParentChild(higherTaxon, taxonSpecies, null, null);
//					}
//					higherTaxon = isIncertisSedis(species) ? higherTaxon : taxonSpecies;
//					lastSpecies = species;
//					getTaxonService().saveOrUpdate(higherTaxon);
//				}
				getTaxonService().saveOrUpdate(higherTaxon);
			}

			
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			tx.setRollbackOnly();
			state.setSuccess(false);
		}
		commitTransaction(tx);
		return;
		
	}


	private boolean isIncertisSedis(String uninomial) {
		return  uninomial.equalsIgnoreCase(INCERTAE_SEDIS) || uninomial.equalsIgnoreCase(FOSSIL_FUNGI);
	}


	private boolean isNewTaxon(String uninomial, String lastUninomial) {
		boolean result =  !uninomial.equalsIgnoreCase(lastUninomial);
		result |= lastUninomial.equalsIgnoreCase(INCERTAE_SEDIS);
		result |= lastUninomial.equalsIgnoreCase(FOSSIL_FUNGI);
		return result;
	}

	private Taxon makeTaxon(IndexFungorumImportState state, String uninomial, Rank newRank) {
		if (uninomial.equalsIgnoreCase(INCERTAE_SEDIS) || uninomial.equalsIgnoreCase(FOSSIL_FUNGI)){
			return null;
		}
		Taxon taxon = state.getRelatedObject(IndexFungorumSupraGeneraImport.NAMESPACE_SUPRAGENERIC_NAMES, uninomial, Taxon.class);
		if (taxon == null){
			if (! newRank.equals(Rank.KINGDOM())){
				logger.warn("Taxon not found for " + uninomial);
			}
			NonViralName<?> name = BotanicalName.NewInstance(newRank);
			name.setGenusOrUninomial(uninomial);
			Reference<?> sourceReference = state.getRelatedObject(NAMESPACE_REFERENCE, SOURCE_REFERENCE, Reference.class);
			taxon = Taxon.NewInstance(name, sourceReference);
			taxon.addMarker(Marker.NewInstance(getMissingGUIDMarkerType(state), true));
		}else if (newRank != null){
			taxon.getName().setRank(newRank);
		}
		return taxon;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> taxonNameSet = new HashSet<String>();
//			while (rs.next()){
//				handleForeignKey(rs, taxonIdSet,"tu_acctaxon" );
//			}
			
			//taxon map
			nameSpace = IndexFungorumSupraGeneraImport.NAMESPACE_SUPRAGENERIC_NAMES ;
			cdmClass = TaxonBase.class;
//			idSet = taxonNameSet;
			Map<String, TaxonBase<?>> taxonMap = new HashMap<String, TaxonBase<?>>();
			List<TaxonBase> list = getTaxonService().list(Taxon.class, null, null, null, null);
			for (TaxonBase<?> taxon : list){
				taxonMap.put(CdmBase.deproxy(taxon.getName(), NonViralName.class).getGenusOrUninomial(), taxon);
			}
			result.put(nameSpace, taxonMap);
			
			//source reference
			Reference<?> sourceReference = getReferenceService().find(PesiTransformer.uuidSourceRefIndexFungorum);
			Map<String, Reference> referenceMap = new HashMap<String, Reference>();
			referenceMap.put(SOURCE_REFERENCE, sourceReference);
			result.put(NAMESPACE_REFERENCE, referenceMap);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IndexFungorumImportState state){
		return true;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IndexFungorumImportState state){
		return ! state.getConfig().isDoRelTaxa();
	}





}
