package eu.etaxonomy.cdm.persistence.dao.statistics;

public interface IStatisticsDao {


	public Long countNomenclaturalReferences();


	Long countNomenclaturalReferences(Class clazz);


	public Long countDescriptiveSourceReferences();
}
