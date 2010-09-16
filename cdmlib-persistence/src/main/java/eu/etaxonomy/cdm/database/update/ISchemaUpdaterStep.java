package eu.etaxonomy.cdm.database.update;

import java.sql.SQLException;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public interface ISchemaUpdaterStep {

	/**
	 * 
	 * @param datasource
	 * @param monitor
	 * @return identifier of newly created term
	 * @throws SQLException
	 */
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException;

	public void setStepName(String stepName);

	public String getStepName();

}