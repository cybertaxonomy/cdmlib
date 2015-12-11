package eu.etaxonomy.cdm.persistence.dao.statistics;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public interface IStatisticsDao {


	/**
	 * counts items of the type Taxon, Synonym or TaxonBase (Taxon & Synonym)
	 * 
	 * @param clazz - the Type that will be counted
	 * @param classification - only items in this classification will be counted
	 * @return the amount of the items in the classification
	 */
	public Long countTaxaInClassification(Class<? extends TaxonBase> clazz,
			Classification classification);

	
	/**
	 * counts all TaxonNames that are attached to the given classification
	 * 
	 * @param classification
	 * @return - the amount of the TaxonNames
	 */
	public Long countTaxonNames(Classification classification);

	
	/**
	 * counts all NomenclaturalReferences in the data base
	 * 
	 * @return the amount of the NRs
	 */
	public Long countNomenclaturalReferences();	

	/**
	 * counts all NomenclaturalReference items attached to the given classification
	 * 
	 * @param classification
	 * @return - the amount of the items in the classification
	 */
	public Long countNomenclaturalReferences(Classification classification);

	
	/**
	 * count all references that are attached to any description in the data base
	 * 
	 * @return - count result - number of these references
	 */
	public Long countDescriptiveSourceReferences();

	
	/**
	 * count all references that are attached to any description in the given classification
	 * 
	 * @return - count result - number of these references
	 */
	public Long countDescriptive(Boolean sourceRef,Classification classification);


	/**
	 * count all Reference items attached to the given classification
	 * but not nomenclatural references
	 * 
	 * @param sourceRef - Boolean, tells if to count the descriptions 
	 * 		directly or count the descriptive source references
	 * @param classification
	 * @return - the amount of the items in the classification
	 */
	public Long countReferencesInClassification(Classification classification);

	
	
	public List<UUID> getTaxonTree(IdentifiableEntity<?> filter);

//	public List<UUID> getAllTaxonIds(UUID rootUuid);


	public void getAllTaxonIds();


	public List<UUID> getAllChildNodeIds(UUID rootUuid);


	Long countReferencesInClassificationWithUuids(Classification classification);


	


	


	



}
