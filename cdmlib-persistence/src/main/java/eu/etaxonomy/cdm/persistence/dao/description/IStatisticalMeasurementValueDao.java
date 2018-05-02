/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;

import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;

/**
 * @author a.babadshanjan
 * @since 07.11.2008
 */
public interface IStatisticalMeasurementValueDao extends ICdmEntityDao<StatisticalMeasurementValue> {

	public List<StatisticalMeasurementValue> list();
}
