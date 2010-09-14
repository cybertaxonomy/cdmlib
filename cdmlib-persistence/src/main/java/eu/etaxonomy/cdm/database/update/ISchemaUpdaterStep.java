package eu.etaxonomy.cdm.database.update;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public interface ISchemaUpdaterStep {

	public boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor);

	public void setStepName(String stepName);

	public String getStepName();

}