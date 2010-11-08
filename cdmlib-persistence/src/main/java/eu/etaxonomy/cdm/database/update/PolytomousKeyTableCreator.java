/**
 * 
 */
package eu.etaxonomy.cdm.database.update;

import java.sql.SQLException;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 *
 */
public class PolytomousKeyTableCreator extends SchemaUpdaterStepBase implements ISchemaUpdaterStep {

	public static PolytomousKeyTableCreator NewInstance(String stepName){
		return new PolytomousKeyTableCreator(stepName);
	}
	
	protected PolytomousKeyTableCreator(String stepName) {
		super(stepName);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		String strSQLTable;
		String strSQLTableAUD;
		
		if (datasource.getDatabaseType().equals(DatabaseTypeEnum.PostgreSQL)){
			strSQLTable = "CREATE TABLE polytomouskey ( " + 
			" id integer NOT NULL, " + 
			" @AUD1 created timestamp without time zone, " +
			" uuid character varying(36), " +
			" updated timestamp without time zone, " +
			" lsid_authority character varying(255), " +
			" lsid_lsid character varying(255), " +
			" lsid_namespace character varying(255), " +
			" lsid_object character varying(255), " +
			" lsid_revision character varying(255), " +
			" protectedtitlecache boolean NOT NULL, " +
			" titlecache character varying(255), " +
			" createdby_id integer, " +
			" updatedby_id integer, " +
			" root_id integer, ";
			
			strSQLTableAUD = strSQLTable;
			strSQLTable += " CONSTRAINT polytomouskey_pkey PRIMARY KEY (id), " +
				" CONSTRAINT fka9e6b1384ff2db2c FOREIGN KEY (createdby_id) " +
					" REFERENCES useraccount (id) MATCH SIMPLE " +
					" ON UPDATE NO ACTION ON DELETE NO ACTION, " +
				" CONSTRAINT fka9e6b138576595c3 FOREIGN KEY (root_id) " +
					" REFERENCES polytomouskeynode (id) MATCH SIMPLE " +
					" ON UPDATE NO ACTION ON DELETE NO ACTION, " +
				" CONSTRAINT fka9e6b138bc5da539 FOREIGN KEY (updatedby_id) " +
					" REFERENCES useraccount (id) MATCH SIMPLE " +
					" ON UPDATE NO ACTION ON DELETE NO ACTION, " +
				" CONSTRAINT polytomouskey_uuid_key UNIQUE (uuid) " +
				" )";
			strSQLTableAUD += 
				" CONSTRAINT polytomouskey_aud_pkey PRIMARY KEY (id, rev), " +
			  	" CONSTRAINT fk867830934869aae FOREIGN KEY (rev)" +
			  		" REFERENCES auditevent (revisionnumber) MATCH SIMPLE " + 
			  		" ON UPDATE NO ACTION ON DELETE NO ACTION" + 
			  		")";
		}else if (datasource.getDatabaseType().equals(DatabaseTypeEnum.MySQL)){
			strSQLTable = " CREATE TABLE PolytomousKey " +
				" id integer NOT NULL, " + 
				" created timestamp without time zone, " +
				" uuid character varying(36), " +
				" updated timestamp without time zone, " +
				" lsid_authority character varying(255), " +
				" lsid_lsid character varying(255), " +
				" lsid_namespace character varying(255), " +
				" lsid_object character varying(255), " +
				" lsid_revision character varying(255), " +
				" protectedtitlecache boolean NOT NULL, " +
				" titlecache character varying(255), " +
				" createdby_id integer, " +
				" updatedby_id integer, " +
				" root_id integer, " +	
				" ( PRIMARY KEY (`id`), " + 
					" UNIQUE KEY `uuid` (`uuid`),  " + 
					" KEY `FKA9E6B1384FF2DB2C` (`createdby_id`),  " + 
					" KEY `FKA9E6B138576595C3` (`root_id`),  " + 
					" KEY `FKA9E6B138BC5DA539` (`updatedby_id`)) COLLATE utf8_general_ci ENGINE=MyISAM";
			strSQLTableAUD = "XXX";
		}else if (datasource.getDatabaseType().equals(DatabaseTypeEnum.H2)){
			strSQLTable = "CREATE TABLE polytomouskey ( " + 
			" id integer NOT NULL, " + 
			" created timestamp without time zone, " +
			" uuid character varying(36), " +
			" updated timestamp without time zone, " +
			" lsid_authority character varying(255), " +
			" lsid_lsid character varying(255), " +
			" lsid_namespace character varying(255), " +
			" lsid_object character varying(255), " +
			" lsid_revision character varying(255), " +
			" protectedtitlecache boolean NOT NULL, " +
			" titlecache character varying(255), " +
			" createdby_id integer, " +
			" updatedby_id integer, " +
			" root_id integer, " +
			" CONSTRAINT polytomouskey_pkey PRIMARY KEY (id), " +
			" CONSTRAINT fka9e6b1384ff2db2c FOREIGN KEY (createdby_id) " +
				" REFERENCES useraccount (id) MATCH SIMPLE " +
				" ON UPDATE NO ACTION ON DELETE NO ACTION, " +
			" CONSTRAINT fka9e6b138576595c3 FOREIGN KEY (root_id) " +
				" REFERENCES polytomouskeynode (id) MATCH SIMPLE " +
				" ON UPDATE NO ACTION ON DELETE NO ACTION, " +
			" CONSTRAINT fka9e6b138bc5da539 FOREIGN KEY (updatedby_id) " +
				" REFERENCES useraccount (id) MATCH SIMPLE " +
				" ON UPDATE NO ACTION ON DELETE NO ACTION, " +
			" CONSTRAINT polytomouskey_uuid_key UNIQUE (uuid) " +
			" )";
			strSQLTableAUD = "XXX";
		}else{
			throw new RuntimeException("Unsupported database type:" + datasource.getDatabaseType().getName());
		}
		datasource.executeQuery(strSQLTable);
		datasource.executeQuery(strSQLTableAUD);
		return 0;
	}

}
