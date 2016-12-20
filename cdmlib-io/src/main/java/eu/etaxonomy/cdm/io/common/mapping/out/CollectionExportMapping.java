/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.mapping.CdmAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.mapping.CdmMapperBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class CollectionExportMapping<STATE extends DbExportStateBase<CONFIG, TRANSFORM>, CONFIG extends DbExportConfiguratorBase<STATE, TRANSFORM>, TRANSFORM extends IExportTransformer> extends CdmDbExportMapping<STATE, CONFIG, TRANSFORM> {
	private static final Logger logger = Logger.getLogger(CollectionExportMapping.class);
	
	private IdMapper parentMapper;
	private DbSequenceMapper sequenceMapper;
	private String collectionAttributeName;
	private List<DbSimpleFilterMapper> filterMapper = new ArrayList<DbSimpleFilterMapper>() ;
	
	public static CollectionExportMapping NewInstance(String tableName, String collectionAttributeName, IdMapper parentMapper){
		return new CollectionExportMapping(tableName, collectionAttributeName, parentMapper, null);
	}
	
	public static CollectionExportMapping NewInstance(String tableName, String collectionAttributeName, IdMapper parentMapper, String seqenceAttribute){
		return NewInstance(tableName, collectionAttributeName, parentMapper, seqenceAttribute, 0);
	}
	
	public static CollectionExportMapping NewInstance(String tableName, String collectionAttributeName, IdMapper parentMapper, String seqenceAttribute, int sequenceStart){
		DbSequenceMapper sequenceMapper = DbSequenceMapper.NewInstance(seqenceAttribute, sequenceStart);
		CollectionExportMapping result = new CollectionExportMapping(tableName, collectionAttributeName, parentMapper, sequenceMapper);
		return result;
	}

	
	@Override
	public void addMapper(CdmAttributeMapperBase mapper) {
		if (mapper instanceof DbSimpleFilterMapper){
			DbSimpleFilterMapper filterMapper = (DbSimpleFilterMapper)mapper;
			this.filterMapper.add(filterMapper);
		}else{
			super.addMapper(mapper);
		}
	}

	private CollectionExportMapping(String tableName, String collectionAttributeName, IdMapper parentMapper, DbSequenceMapper sequenceMapper){
		super(tableName);
		this.parentMapper = parentMapper;
		this.addMapper(parentMapper);
		this.sequenceMapper = sequenceMapper;
		this.addMapper(sequenceMapper);
		this.collectionAttributeName = collectionAttributeName;
	}

	
	@Override
	public boolean invoke(CdmBase parent) throws SQLException{
		try {
			boolean result = true;
			Collection<CdmBase> collection = getCollection(parent);
			if (this.sequenceMapper != null){
				this.sequenceMapper.reset();
			}
			for(CdmBase collectionObject : collection){
				if (collectionObject == null){
					logger.warn("Collection object was null");
					result = false;
					continue;
				}
				if (isFiltered(collectionObject)){
					continue;
				}
				for (CdmMapperBase mapper : this.mapperList){
					if (mapper == this.parentMapper){
						parentMapper.invoke(parent);
					}else if (mapper == this.sequenceMapper){
						this.sequenceMapper.invoke(null);
					}else if (mapper instanceof IDbExportMapper){
						IDbExportMapper<DbExportStateBase<?, TRANSFORM>, TRANSFORM> dbMapper = (IDbExportMapper)mapper;
						try {
							result &= dbMapper.invoke(collectionObject);
						} catch (Exception e) {
							result = false;
							logger.error("Error occurred in mapping.invoke");
							e.printStackTrace();
							continue;
						}
					}else{
						logger.warn("mapper is not of type " + IDbExportMapper.class.getSimpleName());
					}
				}
				int count = getPreparedStatement().executeUpdate();
				if (logger.isDebugEnabled())logger.debug("Number of rows affected: " + count);
			}
			return result;
		} catch(SQLException e){
			e.printStackTrace();
			logger.error(e.getMessage() + ": " + parent.toString());
			return false;
		}
	}
	
	private boolean isFiltered(CdmBase cdmBase) throws SQLException {
		for (DbSimpleFilterMapper filterMapper : this.filterMapper){
			boolean result = filterMapper.doInvoke(cdmBase);
			if (result == true){
				return true;
			}
		}
		return false;
	}

	private Collection<CdmBase> getCollection(CdmBase cdmBase){
		Object result = ImportHelper.getValue(cdmBase, collectionAttributeName, false, true);
		return (Collection<CdmBase>)result;
	}
	
	
}
