/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.erms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.mapping.DbImportAnnotationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.pesi.erms.validation.ErmsAreaImportValidator;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class ErmsAreaImport  extends ErmsImportBase<NamedArea> implements IMappingImport<NamedArea, ErmsImportState>{
	private static final Logger logger = Logger.getLogger(ErmsAreaImport.class);

	private DbImportMapping mapping;
	
	
	private int modCount = 10000;
	private static final String pluralString = "areas";
	private static final String dbTableName = "gu";
	private static final Class cdmTargetClass = NamedArea.class;

	public ErmsAreaImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(ErmsImportConfigurator config) {
		String strRecordQuery = 
			" SELECT * " + 
			" FROM gu " +
			" WHERE ( gu.id IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getMapping()
	 */
	protected DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
			mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this, "id", AREA_NAMESPACE)); //id
			mapping.addMapper(DbImportStringMapper.NewInstance("gu_name", "titleCache"));
			mapping.addMapper(DbImportExtensionMapper.NewInstance("gazetteer_id", ErmsTransformer.GAZETTEER_UUID, "Gazetteer ID", "Gazetteer ID", "G-ID"));
			mapping.addMapper(DbImportAnnotationMapper.NewInstance("note", AnnotationType.EDITORIAL()));

		}
		return mapping;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		return result;  //not needed
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet)
	 */
	public NamedArea createObject(ResultSet rs, ErmsImportState state) throws SQLException {
		int id = rs.getInt("id");
		String strGuName = rs.getString("gu_name");
		UUID uuid = ErmsTransformer.uuidFromGuName(strGuName);
		String label = strGuName;
		String text = strGuName;
		//TODO
		String labelAbbrev = String.valueOf(id);
		NamedAreaType areaType = null;
		NamedAreaLevel level = null;
		NamedArea area = getNamedArea(state, uuid, label, text, labelAbbrev, areaType, level);
		return area;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(ErmsImportState state){
		IOValidator<ErmsImportState> validator = new ErmsAreaImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(ErmsImportState state){
		//TODO
//		return ! state.getConfig().isDoAreas();
		return false;
	}




}
