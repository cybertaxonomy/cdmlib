package eu.etaxonomy.cdm.database.update;

import java.sql.SQLException;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public interface ISchemaUpdaterStep {

	public boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException;

	public void setStepName(String stepName);

	public String getStepName();

}