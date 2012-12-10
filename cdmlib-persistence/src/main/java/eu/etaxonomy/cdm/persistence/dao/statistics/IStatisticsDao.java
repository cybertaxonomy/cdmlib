package eu.etaxonomy.cdm.persistence.dao.statistics;

public interface IStatisticsDao {


	public Long countNomenclaturalReferences();


	public Long countDescriptiveSourceReferences();


	Long countNomenclaturalReferences(Class clazz);
}
