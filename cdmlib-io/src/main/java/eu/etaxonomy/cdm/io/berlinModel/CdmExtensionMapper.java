/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class CdmExtensionMapper extends CdmSingleAttributeMapperBase {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CdmExtensionMapper.class);
	
	private boolean ignore = false;;
	private ExtensionType extensionType;
	private String label;
	private String text;
	private String labelAbbrev;
	private UUID uuid;
	
	private BerlinModelImportState state;
	private String tableName;

	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmExtensionMapper(String dbAttributeString, UUID uuid, String label, String text, String labelAbbrev) {
		super(dbAttributeString, dbAttributeString);
		this.uuid = uuid;
		this.label = label;
		this.text = text;
		this.labelAbbrev = labelAbbrev;
	}
	
	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmExtensionMapper(String dbAttributeString, ExtensionType extensionType) {
		super(dbAttributeString, dbAttributeString);
		this.extensionType = extensionType;
	}
	
	public void initialize(ITermService service, BerlinModelImportState state, String tableName) {
		this.state = state;
		this.tableName = tableName;
		if (checkSqlServerColumnExists()){
			if (this.extensionType == null){
				this.extensionType = getExtensionType(service, uuid, label, text, labelAbbrev);
			}
		}else{
			ignore = true;
		}
	}
	
	public boolean invoke(Map<String, Object> valueMap, CdmBase cdmBase){
		Object dbValueObject = valueMap.get(this.getSourceAttribute().toLowerCase());
		String dbValue = dbValueObject == null? null: dbValueObject.toString();
		return invoke(dbValue, cdmBase);
	}
	
	public boolean invoke(ResultSet rs, CdmBase cdmBase) throws SQLException{
		String dbValue  = rs.getString(this.getSourceAttribute());
		return invoke(dbValue, cdmBase);
	}
	
	private boolean invoke(String dbValue, CdmBase cdmBase){
		if (ignore){
			return true;
		}
		if (cdmBase.isInstanceOf(IdentifiableEntity.class)){
			IdentifiableEntity<?> identEntity = (IdentifiableEntity<?>)cdmBase;
			if (CdmUtils.isNotEmpty(dbValue)){
				Extension.NewInstance(identEntity, dbValue, extensionType);
			}
			return true;
		}else{
			throw new IllegalArgumentException();
		}
	}
	
	protected ExtensionType getExtensionType(ITermService service, UUID uuid, String label, String text, String labelAbbrev){
		ExtensionType extensionType = (ExtensionType)service.find(uuid);
		if (extensionType == null){
			extensionType = new ExtensionType(label, text, labelAbbrev);
			extensionType.setUuid(uuid);
			service.save(extensionType);
		}
		return extensionType;
	}
	
	protected boolean checkSqlServerColumnExists(){
		Source source = getState().getConfig().getSource();
		String strQuery = "SELECT  Count(t.id) as n " +
				" FROM sysobjects AS t " +
				" INNER JOIN syscolumns AS c ON t.id = c.id " +
				" WHERE (t.xtype = 'U') AND " + 
				" (t.name = '" + getTableName() + "') AND " + 
				" (c.name = '" + getDestinationAttribute() + "')";
		ResultSet rs = source.getResultSet(strQuery) ;		
		int n;
		try {
			rs.next();
			n = rs.getInt("n");
			return n>0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	protected BerlinModelImportState getState(){
		return this.state;
	}

	protected String getTableName(){
		return this.tableName;
	}

	
	//not used
	public Class<String> getTypeClass(){
		return String.class;
	}
	
}
