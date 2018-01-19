package eu.etaxonomy.cdm.cache;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;

import eu.etaxonomy.cdm.database.CdmDataSource;

/**
 * This is a very preliminary class to get the model cache running. Need to better understand how 
 * the datasource works with hibernate service registry before implementing the correct way.

 * Or use a running source.
 * 
 * When changing this class please also adapt https://dev.e-taxonomy.eu/redmine/projects/edit/wiki/TaxonomicEditorDevelopersGuide#Model-Change-Actions 
 * 
 * @author a.mueller
 *
 */
public class CdmModelCacherConnectionProvider extends DatasourceConnectionProviderImpl{
	private static final long serialVersionUID = 454393966637126346L;

	public CdmModelCacherConnectionProvider() {
		super();
		setDataSource(getDataSourcePreliminary());
	}

	private DataSource getDataSourcePreliminary() {
		String database = "xyz";
		String path = "C:\\Users\\a.mueller\\.cdmLibrary\\writableResources\\h2\\LocalH2_" + database;
        String username = "sa";
        CdmDataSource dataSource = CdmDataSource.NewH2EmbeddedInstance("cdmTest", username, "", path);
        return dataSource;
	}
	
	

}
