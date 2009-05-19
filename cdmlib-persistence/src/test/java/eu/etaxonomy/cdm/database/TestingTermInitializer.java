package eu.etaxonomy.cdm.database;

import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.test.integration.HsqldbDataTypeFactory;

public class TestingTermInitializer extends PersistentTermInitializer {
    private static final Logger logger = Logger.getLogger(TestingTermInitializer.class);

	private DataSource dataSource;
	
	private Resource termsDataSet;
	
	private Resource termsDtd;
	
	private UUID[] vocabularyUuids = {
			UUID.fromString("45ac7043-7f5e-4f37-92f2-3874aaaef2de"), // Language.class
			UUID.fromString("e72cbcb6-58f8-4201-9774-15d0c6abc128"), // Continent.class
			UUID.fromString("006b1870-7347-4624-990f-e5ed78484a1a"), // WaterbodyOrCountry.class
            UUID.fromString("ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b"), // Rank.class
            UUID.fromString("ab177bd7-d3c8-4e58-a388-226fff6ba3c2"), // SpecimenTypeDesignationStatus.class
            UUID.fromString("bb28cdca-2f8a-4f11-9c21-517e9ae87f1f"), // NomenclaturalStatusType.class
            UUID.fromString("48917fde-d083-4659-b07d-413db843bd50"), // SynonymRelationshipType.class
            UUID.fromString("fc4abe52-9c25-4cfa-a682-8615bf4bbf07"), // HybridRelationshipType.class
            UUID.fromString("6878cb82-c1a4-4613-b012-7e73b413c8cd"), // NameRelationshipType.class
            UUID.fromString("15db0cf7-7afc-4a86-a7d4-221c73b0c9ac"), // TaxonRelationshipType.class
            UUID.fromString("19dffff7-e142-429c-a420-5d28e4ebe305"), // MarkerType.class
            UUID.fromString("ca04609b-1ba0-4d31-9c2e-aa8eb2f4e62d"), // AnnotationType.class
            UUID.fromString("e51d52d6-965b-4f7d-900f-4ba9c6f5dd33"), // NamedAreaType.class
            UUID.fromString("49034253-27c8-4219-97e8-f8d987d3d122"), // NamedAreaLevel.class
           // UUID.fromString("fa032b1a-1ad2-4fb0-a1d9-3016399a80fa"), // NomenclaturalCode.class
            UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8"), // Feature.class
            UUID.fromString("1fb40504-d1d7-44b0-9731-374fbe6cac77"), // NamedArea.class //TDWGArea
            UUID.fromString("adbbbe15-c4d3-47b7-80a8-c7d104e53a05"), // PresenceTerm.class
            UUID.fromString("5cd438c8-a8a1-4958-842e-169e83e2ceee"), // AbsenceTerm.class
            UUID.fromString("9718b7dd-8bc0-4cad-be57-3c54d4d432fe"), // Sex.class
            UUID.fromString("398b50bb-348e-4fe0-a7f5-a75afd846d1f"), // DerivationEventType.class
            UUID.fromString("a7dc20c9-e6b3-459e-8f05-8d6d8fceb465"), // PreservationMethod.class
            UUID.fromString("fe87ea8d-6e0a-4e5d-b0da-0ab8ea67ca77"), // DeterminationModifier.class
            UUID.fromString("066cc62e-7213-495e-a020-97a1233bc037"), // StatisticalMeasure.class
            UUID.fromString("8627c526-73af-44d9-902c-11c1f11b60b4"), //RightsTerm.class
            UUID.fromString("ab60e738-4d09-4c24-a1b3-9466b01f9f55") // NameTypeDesignationStatus.class
            };
	
	public void setTermsDataSet(Resource termsDataSet) {
		this.termsDataSet = termsDataSet;
	}
	
	public void setTermsDtd(Resource termsDtd) {
		this.termsDtd = termsDtd;
	}

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
    @Override
    @PostConstruct
	public void initialize(){
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		IDatabaseConnection connection = null;

		try {
			connection = getConnection();
			IDataSet dataSet = new FlatXmlDataSet(new InputStreamReader(termsDataSet.getInputStream()),new InputStreamReader(termsDtd.getInputStream()));
			DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
		} catch (Exception e) {
			logger.error(e);
			for(StackTraceElement ste : e.getStackTrace()) {
				logger.error(ste);
			}
		} finally {
			try {
				connection.close();
			} catch (SQLException sqle) {
				logger.error(sqle);
			}
		}
		transactionManager.commit(txStatus);
		
		for(int i = 0; i < vocabularyUuids.length; i++) {
			Class<? extends DefinedTermBase> clazz = classesToInitialize[i];
			UUID vocabularyUuid = vocabularyUuids[i];
			secondPass(clazz, vocabularyUuid,new HashMap<UUID,DefinedTermBase>());
		}
	}

	protected IDatabaseConnection getConnection() throws SQLException {
		IDatabaseConnection connection = null;
		try {
			connection = new DatabaseConnection(dataSource.getConnection());

			DatabaseConfig config = connection.getConfig();
			config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,new HsqldbDataTypeFactory());
		} catch (Exception e) {
			logger.error(e);
		}
		return connection;
	}
}
