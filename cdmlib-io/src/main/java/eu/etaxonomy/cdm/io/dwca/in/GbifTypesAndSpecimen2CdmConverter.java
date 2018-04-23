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

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.io.stream.IPartitionableConverter;
import eu.etaxonomy.cdm.io.stream.IReader;
import eu.etaxonomy.cdm.io.stream.ListReader;
import eu.etaxonomy.cdm.io.stream.MappedCdmBase;
import eu.etaxonomy.cdm.io.stream.PartitionableConverterBase;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 \* @since 22.11.2011
 *
 */
public class GbifTypesAndSpecimen2CdmConverter extends PartitionableConverterBase<DwcaDataImportConfiguratorBase, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase>>
						implements IPartitionableConverter<StreamItem, IReader<CdmBase>, String>{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GbifTypesAndSpecimen2CdmConverter.class);

	private static final String CORE_ID = "coreId";

	/**
	 * @param state
	 */
	public GbifTypesAndSpecimen2CdmConverter(DwcaDataImportStateBase state) {
		super(state);
	}

	@Override
    public IReader<MappedCdmBase<? extends CdmBase>> map(StreamItem item ){
		List<MappedCdmBase<? extends CdmBase>> resultList = new ArrayList<>();

		Reference sourceReference = state.getTransactionalSourceReference();
		String sourceReferecenDetail = null;

		String id = getSourceId(item);
		Taxon taxon = getTaxonBase(id, item, Taxon.class, state);
		if (taxon != null){
			String typeStatusStr = item.get(TermUri.DWC_TYPE_STATUS);
			boolean isType = false;
			TypeDesignationStatusBase<?> typeStatus = null;
			if ( ! isNoTypeStatus(typeStatusStr)){
				isType = true;
				typeStatus = getTypeStatus(typeStatusStr, item);
			}

			SpecimenOrObservationType unitType = SpecimenOrObservationType.DerivedUnit;

			if (hasDerivedUnit(item, isType)){
				unitType = SpecimenOrObservationType.PreservedSpecimen;
			}else{
				unitType = SpecimenOrObservationType.FieldUnit;
			}

			DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(unitType);

			String catalogNumber = item.get(TermUri.DWC_CATALOG_NUMBER);
			Collection collection = getCollection(state, item, resultList);
			facade.setCollection(collection);
			facade.setCatalogNumber(catalogNumber);

			DerivedUnit specimen = facade.innerDerivedUnit();

			if (isType){
				TaxonName name = taxon.getName();
				if (typeStatus.isInstanceOf(SpecimenTypeDesignationStatus.class)){
					SpecimenTypeDesignationStatus status = CdmBase.deproxy(typeStatus, SpecimenTypeDesignationStatus.class);
					name.addSpecimenTypeDesignation(specimen, status, null, null, null, false, true);
					MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(taxon);
					resultList.add(mcb);
				}else if (typeStatus.isInstanceOf(NameTypeDesignationStatus.class)){
					String message = "NameTypeDesignation not yet implemented";
					fireWarningEvent(message, item, 8);
				}else{
					String message = "Undefined type status: %s";
					message = String.format(message, typeStatus);
					fireWarningEvent(message, item, 8);
				}
			}

			MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(specimen);
			resultList.add(mcb);

		}else{
			String message = "Can't retrieve taxon from database for id '%s'";
			fireWarningEvent(String.format(message, id), item, 12);
		}

		//return
		return new ListReader<>(resultList);
	}


	private Collection getCollection(DwcaDataImportStateBase state, StreamItem item,
	        List<MappedCdmBase<? extends CdmBase>> resultList) {
		String institutionCode = item.get(TermUri.DWC_INSTITUTION_CODE);
		String collectionCode = item.get(TermUri.DWC_COLLECTION_CODE);
		//institution
		Institution institution = getInstitutionByInstitutionCode(item, institutionCode);
		if (institution != null){
			MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(item.term, item.get(TermUri.DWC_INSTITUTION_CODE), institution);
			resultList.add(mcb);
		}
		//collection
		Collection collection = getCollectionByCollectionCode(item, collectionCode, institution);
		if (collection != null){
			MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(item.term, item.get(TermUri.DWC_COLLECTION_CODE), collection);
			resultList.add(mcb);
		}
		return collection;
	}

	private Collection getCollectionByCollectionCode(StreamItem item, String collectionCode, Institution institution) {
		String namespace = TermUri.DWC_COLLECTION_CODE.toString();
		List<Collection> result = state.get(namespace, collectionCode, Collection.class);
		if (result.isEmpty()){
			return makeNewCollection(collectionCode, institution);
		}else if (result.size() == 1){
			return result.iterator().next();
		}else {
			int equalInstitutes = 0;
			Collection lastEqualInstituteCollection = null;
			String collectionInstitutionCode = makeCollectionInstitutionCode(collectionCode, institution);
			for (Collection collection: result){
				String collectionInstitutionCode2 = makeCollectionInstitutionCode(collection.getCode() ,institution);
				if (collectionInstitutionCode.equals(collectionInstitutionCode2)){
					equalInstitutes++;
					lastEqualInstituteCollection = collection;
				}
			}
			if(equalInstitutes == 0){
				return makeNewCollection(collectionCode, institution);
			}else{
				if (equalInstitutes > 1){
					String message = "There is more than 1 cdm entity matching given collection code '%s'. I take an arbitrary one.";
					fireWarningEvent(String.format(message, collectionCode), item, 4);
				}
				return lastEqualInstituteCollection;
			}
		}

	}

	/**
	 * @param collectionCode
	 * @param institution
	 */
	private String makeCollectionInstitutionCode(String collectionCode, Institution institution) {
		String collectionInstitutionCode = collectionCode + "@" + institution == null ? "NULL" : institution.getCode();
		return collectionInstitutionCode;
	}

	/**
	 * @param collectionCode
	 * @param institution
	 * @return
	 */
	private Collection makeNewCollection(String collectionCode,
			Institution institution) {
		//try to find in cdm
		Collection newCollection = Collection.NewInstance();
		newCollection.setCode(collectionCode);
		newCollection.setInstitute(institution);
		return newCollection;
	}

	private Institution getInstitutionByInstitutionCode(StreamItem item, String institutionCode) {
		String namespace = TermUri.DWC_COLLECTION_CODE.toString();
		List<Institution> result = state.get(namespace, institutionCode, Institution.class);
		if (result.isEmpty()){
			//try to find in cdm
			Institution newInstitution = Institution.NewInstance();
			newInstitution.setCode(institutionCode);
			return newInstitution;
		}
		if (result.size() > 1){
			String message = "There is more than 1 cdm entity matching given institution code '%s'. I take an arbitrary one.";
			fireWarningEvent(String.format(message, institutionCode), item, 4);
		}
		return result.iterator().next();
	}

	private boolean hasDerivedUnit(StreamItem item, boolean isType) {
		return isNotBlank(item.get(TermUri.DWC_INSTITUTION_CODE)) ||
				isNotBlank(item.get(TermUri.DWC_COLLECTION_CODE)) ||
				isNotBlank(item.get(TermUri.DWC_CATALOG_NUMBER))||
				isType;
		//TO BE CONTINUED
	}

	private boolean isNoTypeStatus(String typeStatus) {
		if (isBlank(typeStatus)){
			return true;
		}else if (typeStatus.equalsIgnoreCase("Nontype")){   //eMonocats Scratchpads
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Returns the type designation. Should never return null
	 * except for blank typeStatus.
	 * @param typeStatus
	 * @param item
	 * @return
	 */
	private TypeDesignationStatusBase<?> getTypeStatus(String typeStatus, StreamItem item) {
		//TODO move to transformer or handle somehow different (e.g. transformer for http://vocabularies.gbif.org/vocabularies/type_status )
		//preliminary implementation for those types needed for eMonocots import
		if (isBlank(typeStatus)){
			return null;
		}else if (typeStatus.matches("(?i)holotype")){
			return SpecimenTypeDesignationStatus.HOLOTYPE();
		}else if (typeStatus.matches("(?i)syntype")){
			return SpecimenTypeDesignationStatus.SYNTYPE();
		}else{
			String message = "Type status not recognized: %s";
			message = String.format(message, typeStatus);
			fireWarningEvent(message, item, 12);
			return SpecimenTypeDesignationStatus.TYPE();
		}
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
		//taxon
		if ( hasValue(value = item.get(CORE_ID))){
			key = TermUri.DWC_TAXON.toString();
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}

		//collection code
		TermUri uri = TermUri.DWC_COLLECTION_CODE;
		String valueStr = item.get(uri);
		if ( hasValue(value = valueStr)){
			key = uri.toString();
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}

	}


	@Override
	public Set<String> requiredSourceNamespaces() {
		Set<String> result = new HashSet<>();
 		result.add(TermUri.DWC_TAXON.toString());
 		result.add(TermUri.DWC_LOCATION_ID.toString());
 		return result;
	}

//******************* TO STRING ******************************************/

	@Override
	public String toString(){
		return this.getClass().getName();
	}


}
