// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUris;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class GbifDescriptionCsv2CdmConverter implements IConverter<CsvStreamItem, IReader<CdmBase>, DwcaImportState>{
	private static Logger logger = Logger.getLogger(GbifDescriptionCsv2CdmConverter.class);

	private static final String CORE_ID = "coreId";

	public IReader<CdmBase> map(CsvStreamItem item, DwcaImportState state ){
		List<CdmBase> resultList = new ArrayList<CdmBase>(); 
		
		Map<String, String> csv = item.map;
		Reference<?> sourceReference = null;
		String sourceReferecenDetail = null;
		
		Taxon taxon = getTaxon(csv);
		if (taxon != null){
			resultList.add(taxon);
		}
		logger.warn("Not yet implemented");
		return new ListReader<CdmBase>(resultList);
		
	}
	
	private Taxon getTaxon(Map<String, String> csv) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString(){
		return this.getClass().getName();
	}

}
