/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.persistence.dao.description.IStatisticalMeasurementValueDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;

/**
 * @author a.babadshanjan
 * @since 07.11.2008
 */
@Repository
public class StatisticalMeasurementValueDaoImpl
        extends CdmEntityDaoBase<StatisticalMeasurementValue>
        implements IStatisticalMeasurementValueDao {

	public StatisticalMeasurementValueDaoImpl() {
		super(StatisticalMeasurementValue.class);
	}
}
