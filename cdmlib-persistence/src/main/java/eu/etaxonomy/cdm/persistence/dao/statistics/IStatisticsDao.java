package eu.etaxonomy.cdm.persistence.dao.statistics;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public interface IStatisticsDao {

	//TODO add all public methods

	public Long countNomenclaturalReferences();


//	public Long countNomenclaturalReferences(Class clazz);


	public Long countDescriptiveSourceReferences();


	public Long countTaxaInClassification(Class<? extends TaxonBase> clazz,
			Classification classification);


	public Long countTaxonNames(Classification classification);



}
