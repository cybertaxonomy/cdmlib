package eu.etaxonomy.cdm.persistence.dao.statistics;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public interface IStatisticsDao {


	public Long countNomenclaturalReferences();


	Long countNomenclaturalReferences(Class clazz);


	public Long countDescriptiveSourceReferences();

	//TODO remove this method:
	public void tryArround();


	public Long countTaxaInClassification(Class<? extends TaxonBase> clazz,
			Classification classification);


	Long countTaxonNames(Classification classification);



}
