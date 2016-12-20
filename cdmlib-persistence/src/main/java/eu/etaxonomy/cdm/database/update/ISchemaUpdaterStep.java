package eu.etaxonomy.cdm.database.update;

import java.sql.SQLException;
import java.util.List;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * Interface which represents a CDM model update step. See {@link CdmUpdater}
 * for further documentation or implementing classes for examples.
 * 
 * @see CdmUpdater
 * @see ISchemaUpdater
 * @see ISchemaUpdater
 * @see ITermUpdaterStep
 * 
 * @see CdmUpdater
 * @author a.mueller
 *
 */
public interface ISchemaUpdaterStep {

	/**
	 * 
	 * @param datasource
	 * @param monitor
	 * @param caseType 
	 * @return identifier of newly created term
	 * @throws SQLException
	 */
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException;

	public void setStepName(String stepName);

	public String getStepName();

	public List<ISchemaUpdaterStep> getInnerSteps();

	public boolean isIgnoreErrors();

	public void setIgnoreErrors(boolean ignoreErrors);

}
