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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class GbifReferenceCsv2CdmConverter extends PartitionableConverterBase<DwcaDataImportConfiguratorBase, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase>>  
						implements IPartitionableConverter<StreamItem, IReader<CdmBase>, String>{
	
	private static final Logger logger = Logger.getLogger(GbifReferenceCsv2CdmConverter.class);

	private static final String CORE_ID = "coreId";
	
	/**
	 * @param state
	 */
	public GbifReferenceCsv2CdmConverter(DwcaDataImportStateBase state) {
		super(state);
	}

	public IReader<MappedCdmBase> map(StreamItem item ){
		List<MappedCdmBase> resultList = new ArrayList<MappedCdmBase>(); 
		
		Map<String, String> csv = item.map;
		Reference<?> sourceReference = state.getTransactionalSourceReference();
		String sourceReferecenDetail = null;
		
		String id = getSourceId(item);
		TaxonBase<?> taxon = getTaxonBase(id, item, TaxonBase.class, state);
		if (isNotBlank(id) && taxon == null){
			String message = "Taxon for id %s could not be found";
			message = String.format(message, id);
			fireWarningEvent(message, item, 8);
		}
		
		String strCreator = getValue(item, TermUri.DC_CREATOR);
		String strDate = getValue(item, TermUri.DC_DATE);
		String strTitle = getValue(item, TermUri.DC_TITLE);
		String strSource = getValue(item, TermUri.DC_SOURCE);
		String strIdentifier = getValue(item, TermUri.DC_IDENTIFIER);
		String strType = getValue(item, TermUri.DC_TYPE);
		
		Reference<?> reference = ReferenceFactory.newGeneric();
		resultList.add(new MappedCdmBase<CdmBase>(reference));

		//author
		TeamOrPersonBase<?> author = handleCreator(strCreator);
		reference.setAuthorship(author);
		//date
		TimePeriod publicationDate = handleDate(strDate);
		reference.setDatePublished(publicationDate);
		//title
		reference.setTitle(strTitle);
		//inreference
		Reference<?> inRef = handleInRef(strSource);
		if (inRef != null){
			reference.setInReference(inRef);
			resultList.add(new MappedCdmBase<CdmBase>(inRef));
		}

		//URI
		handleIdentifier(strIdentifier, reference);
		
		//type
		handleType(reference, strType, taxon, resultList, item);
		
		
		return new ListReader<MappedCdmBase>(resultList);
	}

	
	private void handleType(Reference<?> reference, String strType, TaxonBase<?> taxon, List<MappedCdmBase> resultList, StreamItem item) {
		// TODO handleType not yet implemented
		
		if (taxon == null){
			String message = "Taxon is null. Reference not imported.";
			fireWarningEvent(message,item, 4);
			//do nothing
		}else{
			boolean isNomRef = false;
			if (isNotBlank(strType)){
				if (strType.matches("Botanical Protologue")){
					if (taxon.getName() != null && reference != null && taxon.getName().isInstanceOf(NonViralName.class)){
						NonViralName<?> nvn = CdmBase.deproxy(taxon.getName(), NonViralName.class);
						nvn.setNomenclaturalReference(reference);
						isNomRef = true;
					}else{
						//TODO
					}
				}
			}
			
			//guess a nom ref
			if (isNomRef == false && config.isGuessNomenclaturalReferences()){
				//if reference equals in author and year we assume that it is the nom ref
				//this information is usually only available for ICZN names
				if (taxon.getName() != null && reference != null && taxon.getName().isInstanceOf(NonViralName.class)){
					NonViralName<?> nvn = CdmBase.deproxy(taxon.getName(), NonViralName.class);
					String taxonAuthor = nvn.getAuthorshipCache();
					String refAuthor = reference.getAuthorship().getNomenclaturalTitle();
					Integer combYear = null;
					Integer origYear = null;
					if (nvn.isInstanceOf(ZoologicalName.class)){
						ZoologicalName zooName = CdmBase.deproxy(nvn, ZoologicalName.class);
						combYear = zooName.getPublicationYear();
						origYear = zooName.getOriginalPublicationYear();
					}
					String refYear = reference.getYear();
					
					//combination compare
					if (taxonAuthor != null && taxonAuthor.equals(refAuthor)){
						if (combYear != null && String.valueOf(combYear).equals(refYear)){
							//is nom Ref
							isNomRef = true;
							nvn.setNomenclaturalReference(reference);
						}else if (origYear != null && String.valueOf(origYear).equals(refYear)){
							//TODO not yet handled by CDM
						}
					}
	
				}
			}
			if (config.isHandleAllRefsAsCitation()){
				if (taxon.isInstanceOf(Taxon.class)){
					TaxonDescription desc = getTaxonDescription(CdmBase.deproxy(taxon, Taxon.class), false);
					createCitation(desc, reference, taxon.getName());
					resultList.add(new MappedCdmBase<CdmBase>(desc));
				}else if (taxon.isInstanceOf(Synonym.class)){
					Synonym syn = CdmBase.deproxy(taxon, Synonym.class);
					for (Taxon tax: syn.getAcceptedTaxa()){
						TaxonDescription desc = getTaxonDescription(tax, false);
						createCitation(desc, reference, syn.getName());
						resultList.add(new MappedCdmBase<CdmBase>(desc));
					}
				}
				
			}
		
		}		
		
		
	}

	private void createCitation(TaxonDescription desc, Reference ref, TaxonNameBase nameUsedInSource) {
		Feature feature = Feature.CITATION();
		TextData textData = TextData.NewInstance(feature);
		DescriptionElementSource source = DescriptionElementSource.NewPrimarySourceInstance(ref, null, nameUsedInSource, null);
		textData.addSource(source);
		desc.addElement(textData);
	}

	private void handleIdentifier(String strIdentifier, Reference reference) {
		if (StringUtils.isBlank(strIdentifier)){
			return;
		}else if (LSID.isLsid(strIdentifier)){
			LSID lsid;
			try {
				lsid = new LSID(strIdentifier);
				reference.setLsid(lsid);
			} catch (MalformedLSIDException e) {
				//TODO should not happen as we have checked before
				throw new RuntimeException(e);
			}
		}
		try {
			URI uri = URI.create(strIdentifier);
			reference.setUri(uri);
		} catch (Exception e) {
			logger.debug("Reference is not an URI");
		}
		//TODO further identifier types
		
	}

	private Reference<?> handleInRef(String strSource) {
		if (StringUtils.isBlank(strSource)){
			return null;
		}else{
			Reference<?> inRef = ReferenceFactory.newGeneric();
			return inRef;
		}
	}
	

	private TimePeriod handleDate(String strDate) {
		TimePeriod tp = TimePeriodParser.parseString(strDate);
		return tp;
	}

	private TeamOrPersonBase handleCreator(String strCreator) {
		Team team = Team.NewTitledInstance(strCreator, strCreator);
		return team;
	}

	@Override
	public String getSourceId(StreamItem item) {
		String id = item.get(CORE_ID);
		return id;
	}

	
//********************** PARTITIONABLE **************************************/

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
	public Set<String> requiredSourceNamespaces() {
		Set<String> result = new HashSet<String>();
 		result.add(TermUri.DWC_TAXON.toString());
 		return result;
	}
	
//******************* TO STRING ******************************************/
	
	@Override
	public String toString(){
		return this.getClass().getName();
	}


}
