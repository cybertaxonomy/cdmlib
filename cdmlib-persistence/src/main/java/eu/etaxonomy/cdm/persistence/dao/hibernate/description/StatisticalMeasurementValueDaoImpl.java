/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;

import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.persistence.dao.description.IStatisticalMeasurementValueDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;

/**
 * @author a.babadshanjan
 * @since 07.11.2008
 */
@Repository
public class StatisticalMeasurementValueDaoImpl extends CdmEntityDaoBase<StatisticalMeasurementValue> 
implements IStatisticalMeasurementValueDao {

	public StatisticalMeasurementValueDaoImpl() {
		super(StatisticalMeasurementValue.class); 
	}

	public List<StatisticalMeasurementValue> list() {
		Criteria crit = getSession().createCriteria(type); 
		return crit.list(); 
	}
}
