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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.stream.IPartitionableConverter;
import eu.etaxonomy.cdm.io.stream.IReader;
import eu.etaxonomy.cdm.io.stream.ListReader;
import eu.etaxonomy.cdm.io.stream.MappedCdmBase;
import eu.etaxonomy.cdm.io.stream.PartitionableConverterBase;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class GbifVernacularNameCsv2CdmConverter
            extends PartitionableConverterBase<DwcaDataImportConfiguratorBase, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase>>
		    implements IPartitionableConverter<StreamItem, IReader<CdmBase>, String> {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GbifVernacularNameCsv2CdmConverter.class);
	private static final String CORE_ID = "coreId";

	/**
	 * @param state
	 */
	public GbifVernacularNameCsv2CdmConverter(DwcaDataImportStateBase state) {
		super(state);
	}


	@Override
    public IReader<MappedCdmBase<? extends CdmBase>> map(StreamItem item ){
		List<MappedCdmBase<? extends CdmBase>> resultList = new ArrayList<>();

		Map<String, String> csv = item.map;
		Reference sourceReference = state.getTransactionalSourceReference();
		String sourceReferecenDetail = null;

		String id = csv.get(CORE_ID);
		Taxon taxon = getTaxonBase(id, item, Taxon.class, state);
		if (taxon != null){
			MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(item.term, csv.get(CORE_ID), taxon);
			String vernacular = item.get(TermUri.DWC_VERNACULAR_NAME);
			TaxonDescription desc = getTaxonDescription(taxon, false);

			//TODO area,
			Language language = getDcLanguage(item, resultList);

			CommonTaxonName commonName = CommonTaxonName.NewInstance(vernacular, language);
			desc.addElement(commonName);
			resultList.add(mcb);
		}else{
			String message = "Can't retrieve taxon from database for id '%s'";
			fireWarningEvent(String.format(message, id), item, 12);
		}

		//return
		return new ListReader<>(resultList);

	}



	@Override
	public String getSourceId(StreamItem item) {
		String id = item.get(CORE_ID);
		return id;
	}

//**************************** PARTITIONABLE ************************************************

	@Override
	protected void makeForeignKeysForItem(StreamItem item, Map<String, Set<String>> fkMap) {
		String value;
		String key;
		if ( hasValue(value = item.get(CORE_ID))){
			key = TermUri.DWC_TAXON.toString();
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
	}


	@Override
	public final Set<String> requiredSourceNamespaces() {
		Set<String> result = new HashSet<>();
 		result.add(TermUri.DWC_TAXON.toString());
 		result.add(TermUri.DC_LANGUAGE.toString());
        return result;
	}

//************************ STRING ************************************************/



	@Override
	public String toString(){
		return this.getClass().getName();
	}

}
