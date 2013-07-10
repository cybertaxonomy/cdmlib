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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.mapping.DbImportAnnotationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportCommonNameCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.pesi.erms.validation.ErmsVernacularImportValidator;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class ErmsVernacularImport  extends ErmsImportBase<CommonTaxonName> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ErmsVernacularImport.class);

	private DbImportMapping<ErmsImportState, ErmsImportConfigurator> mapping;
	private ErmsImportState state;   //import instance is never used more than once for Erms ; dirty
	
	
	private static final String pluralString = "vernaculars";
	private static final String dbTableName = "vernaculars";
	private static final Class<?> cdmTargetClass = CommonTaxonName.class;

	public ErmsVernacularImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(ErmsImportConfigurator config) {
		String strRecordQuery = 
			" SELECT vernaculars.*, tu.tu_acctaxon, tu.id  " + 
			" FROM vernaculars INNER JOIN tu ON vernaculars.tu_id = tu.id  " +
			" WHERE ( vernaculars.id IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getMapping()
	 */
	protected DbImportMapping<ErmsImportState, ErmsImportConfigurator> getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping<ErmsImportState, ErmsImportConfigurator>();
			
			mapping.addMapper(DbImportCommonNameCreationMapper.NewInstance("id", VERNACULAR_NAMESPACE, "tu_id", ErmsTaxonImport.TAXON_NAMESPACE));
			
			mapping.addMapper(DbImportObjectMapper.NewInstance("lan_id", "language", LANGUAGE_NAMESPACE));
			mapping.addMapper(DbImportStringMapper.NewInstance("vername", "name"));
			mapping.addMapper(DbImportAnnotationMapper.NewInstance("note", AnnotationType.EDITORIAL(), Language.DEFAULT()));
		}
		return mapping;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class<?> cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> languageIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "tu_id");
				handleForeignKey(rs, languageIdSet, "lan_id");
			}
			
			//taxon map
			nameSpace = ErmsTaxonImport.TAXON_NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase<?>> taxonMap = (Map<String, TaxonBase<?>>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);
			
			//language map
			nameSpace = LANGUAGE_NAMESPACE;
			Map<String, Language> languageMap = new HashMap<String, Language>();
			ErmsTransformer transformer = new ErmsTransformer();
			for (String lanAbbrev: languageIdSet){
				Language language = null;
				try {
					language = transformer.getLanguageByKey(lanAbbrev);
					if (language == null || language.equals(Language.UNDETERMINED())){
						UUID uuidLang = transformer.getLanguageUuid(lanAbbrev);
						if (uuidLang != null){
							language = getLanguage(state, uuidLang, lanAbbrev, lanAbbrev, lanAbbrev);
							
						}
						if (language == null || language.equals(Language.UNDETERMINED() )){
							logger.warn("Langauge undefined: " + lanAbbrev);
						}
					}
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (UndefinedTransformerMethodException e) {
					e.printStackTrace();
				}
				languageMap.put(lanAbbrev, language);
			}
			result.put(nameSpace, languageMap);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(ErmsImportState state){
		IOValidator<ErmsImportState> validator = new ErmsVernacularImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(ErmsImportState state){
		return ! state.getConfig().isDoVernaculars();
	}


	@Override
	protected void doInvoke(ErmsImportState state) {
		this.state = state;
		super.doInvoke(state);
	}





}
