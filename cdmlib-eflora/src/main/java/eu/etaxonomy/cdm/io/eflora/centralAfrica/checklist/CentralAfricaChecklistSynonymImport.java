/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.checklist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.checklist.validation.CentralAfricaChecklistTaxonImportValidator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class CentralAfricaChecklistSynonymImport  extends CentralAfricaChecklistImportBase<TaxonBase> implements IMappingImport<TaxonBase, CentralAfricaChecklistImportState>{
	private static final Logger logger = Logger.getLogger(CentralAfricaChecklistSynonymImport.class);
	
	private NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
	
	
	private DbImportMapping mapping;
	
	//second path is not used anymore, there is now an ErmsTaxonRelationImport class instead
	private boolean isSecondPath = false;
	
	private int modCount = 10000;
	private static final String pluralString = "synonyms";
	private static final String dbTableName = "synonyms";
	private static final Class cdmTargetClass = TaxonBase.class;
	private static final String strOrderBy = "";

	public CentralAfricaChecklistSynonymImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		String strQuery = " SELECT syn_id FROM " + dbTableName + strOrderBy;
		return strQuery;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getMapping()
	 */
	protected DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
			mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this, "syn_id", SYNONYM_NAMESPACE));
			//TODO Synonym mapper gibts es auch
			
		}
		return mapping;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(CentralAfricaChecklistImportConfigurator config) {
		String strSelect = " SELECT * ";
		String strFrom = " FROM " + dbTableName;
		String strWhere = " WHERE ( syn_id IN (" + ID_LIST_TOKEN + ") )";
		String strRecordQuery = strSelect + strFrom + strWhere + strOrderBy;
		return strRecordQuery;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
				Set<String> taxonIdSet = new HashSet<String>();
				while (rs.next()){
					handleForeignKey(rs, taxonIdSet, "acc_id");
				}

			//taxon map
			nameSpace = TAXON_NAMESPACE;
			cdmClass = Taxon.class;
			idSet = taxonIdSet;
			Map<String, Taxon> taxonMap = (Map<String, Taxon>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet)
	 */
	public TaxonBase createObject(ResultSet rs, CentralAfricaChecklistImportState state) throws SQLException {
		BotanicalName speciesName = BotanicalName.NewInstance(Rank.SPECIES());
		
		
		Integer accId = rs.getInt("acc_id");
		Taxon taxon = CdmBase.deproxy(state.getRelatedObject(TAXON_NAMESPACE, String.valueOf(accId)), Taxon.class);
		
		Reference sec = taxon.getSec();
		
		String genusString = rs.getString("synonym genus");
		String speciesString = rs.getString("synonym species");
		String authorityString = rs.getString("synonym authority");
		
		Synonym synonym = Synonym.NewInstance(speciesName, sec);

		speciesName.setGenusOrUninomial(genusString);
		speciesName.setSpecificEpithet(speciesString);
		parser.handleAuthors(speciesName, CdmUtils.concat(" ", new String[] {"", genusString, speciesString, authorityString}), authorityString);
		
		if (taxon != null){
			taxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
		}else{
			logger.warn("Taxon (" + accId + ") not available for Synonym " + synonym.getTitleCache());
		}
		return synonym;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(CentralAfricaChecklistImportState state){
		IOValidator<CentralAfricaChecklistImportState> validator = new CentralAfricaChecklistTaxonImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(CentralAfricaChecklistImportState state){
		return ! state.getConfig().isDoTaxa();
	}



}
