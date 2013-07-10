// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.common.DbExportBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.ZooNameNoMarkerCacheStrategy;

/**
 * @author e.-m.lee
 * @date 12.02.2010
 *
 */
public abstract class PesiExportBase extends DbExportBase<PesiExportConfigurator, PesiExportState, PesiTransformer> {
	private static final Logger logger = Logger.getLogger(PesiExportBase.class);
	
	protected static final boolean IS_CACHE = true;
	
	private static Set<NameRelationshipType> excludedRelTypes = new HashSet<NameRelationshipType>();
	
	private static NonViralNameDefaultCacheStrategy<?> zooNameStrategy = ZooNameNoMarkerCacheStrategy.NewInstance();
	private static NonViralNameDefaultCacheStrategy<?> botanicalNameStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	
	public PesiExportBase() {
		super();
	}
	

	protected <CLASS extends TaxonBase> List<CLASS> getNextTaxonPartition(Class<CLASS> clazz, int limit, int partitionCount, List<String> propertyPath) {
		List<OrderHint> orderHints = new ArrayList<OrderHint>();
		orderHints.add(new OrderHint("id", OrderHint.SortOrder.ASCENDING ));
		List<CLASS> list = (List<CLASS>)getTaxonService().list(clazz, limit, partitionCount * limit, orderHints, propertyPath);
		if (list.isEmpty()){
			return null;
		}
		
		Iterator<CLASS> it = list.iterator();
		while (it.hasNext()){
			TaxonBase<?> taxonBase = it.next();
			if (! isPesiTaxon(taxonBase)){
				it.remove();
			}
			taxonBase = null;
		}
		
		return list;
	}
	
	protected List<TaxonNameDescription> getNextNameDescriptionPartition(int limit, int partitionCount, List<String> propertyPath) {
		List<OrderHint> orderHints = new ArrayList<OrderHint>();
		orderHints.add(new OrderHint("id", OrderHint.SortOrder.ASCENDING ));
		Pager<TaxonNameDescription> l = getDescriptionService().getTaxonNameDescriptions(null, limit, partitionCount, propertyPath);
		List<TaxonNameDescription> list = l.getRecords();
		if (list.isEmpty()){
			return null;
		}
		
		Iterator<TaxonNameDescription> it = list.iterator();
		while (it.hasNext()){
			TaxonNameDescription nameDescription = it.next();
			if (! isPesiNameDescriptionTaxon(nameDescription)){
				it.remove();
			}
		}
		return list;
	}
	

	private boolean isPesiNameDescriptionTaxon(TaxonNameDescription nameDescription) {
		TaxonNameBase<?,?> name = nameDescription.getTaxonName();
		if (isPurePesiName(name)){
			return true;
		}else{
			Set<TaxonBase> taxa = name.getTaxonBases();
			for (TaxonBase<?> taxonBase : taxa){
				if (isPesiTaxon(taxonBase)){
					return true;
				}
			}
			
		}
		return false;
	}


	/**
	 * Returns the next list of pure names. If finished result will be null. If list is empty there may be result in further partitions.
	 * @param clazz
	 * @param limit
	 * @param partitionCount
	 * @return
	 */
	protected List<NonViralName<?>> getNextPureNamePartition(Class<? extends NonViralName> clazz,int limit, int partitionCount) {
		List<OrderHint> orderHints = new ArrayList<OrderHint>();
		orderHints.add(new OrderHint("id", OrderHint.SortOrder.ASCENDING ));
		List<String> propPath = Arrays.asList(new String[]{"taxonBases"});
		
		List<NonViralName<?>> list = (List)getNameService().list(clazz, limit, partitionCount * limit, orderHints, null);
		if (list.isEmpty()){
			return null;
		}
		Iterator<NonViralName<?>> it = list.iterator();
		while (it.hasNext()){
			NonViralName<?> taxonName = HibernateProxyHelper.deproxy(it.next(), NonViralName.class);
			if (! isPurePesiName(taxonName)){
				it.remove();
			}
		}
		return list;
	}
	
	protected <CLASS extends RelationshipBase> List<CLASS> getNextNameRelationshipPartition(Class<CLASS> clazz, int limit, int partitionCount, List<String> propertyPath) {
		List<CLASS> result = new ArrayList<CLASS>();
		String[] propertyPaths = null;
		String orderHints = null;
		List<CLASS> list = (List<CLASS>)getNameService().getAllRelationships(limit, partitionCount * limit);
		if (list.isEmpty()){
			return null;
		}
		for (CLASS rel : list){
			if (isPesiNameRelationship(rel)){
				result.add(rel);
			}
		}
		return result;
	}
	
	protected <CLASS extends RelationshipBase> List<CLASS> getNextTaxonRelationshipPartition( int limit, int partitionCount, List<String> propertyPath) {
		List<CLASS> result = new ArrayList<CLASS>();
		String[] propertyPaths = null;
		String orderHints = null;
		List<CLASS> list = (List<CLASS>)getTaxonService().getAllRelationships(limit, partitionCount * limit);
		
		if (list.isEmpty()){
			return null;
		}
		
		for (CLASS rel : list){
			if (isPesiTaxonOrSynonymRelationship(rel)){
				result.add(rel);
			}
		}
		return result;
	}
	
	protected boolean isPesiNameRelationship(RelationshipBase rel){
		TaxonNameBase<?,?> name1;
		TaxonNameBase<?,?> name2;
		if (rel.isInstanceOf(HybridRelationship.class)){
			HybridRelationship hybridRel = CdmBase.deproxy(rel, HybridRelationship.class);
			name1 = hybridRel.getParentName();
			name2 = hybridRel.getHybridName();
		}else if (rel.isInstanceOf(NameRelationship.class)){
			NameRelationship nameRel = CdmBase.deproxy(rel, NameRelationship.class);
			name1 = nameRel.getFromName();
			name2 = nameRel.getToName();
		}else{
			logger.warn ("Only hybrid- and name-relationships alowed here");
			return false;
		}
		return (isPesiName(name1) && isPesiName(name2));
		
	}
	
	private boolean isPesiName(TaxonNameBase<?,?> name) {
		return hasPesiTaxon(name) || isPurePesiName(name);
	}

	protected boolean isPesiTaxonOrSynonymRelationship(RelationshipBase rel){
		TaxonBase<?> fromTaxon;
		Taxon toTaxon;
		if (rel.isInstanceOf(SynonymRelationship.class)){
			SynonymRelationship synRel = CdmBase.deproxy(rel, SynonymRelationship.class);
			fromTaxon = synRel.getSynonym();
			toTaxon = synRel.getAcceptedTaxon();
			synRel = null;
		}else if (rel.isInstanceOf(TaxonRelationship.class)){
			TaxonRelationship taxRel = CdmBase.deproxy(rel, TaxonRelationship.class);
			fromTaxon = taxRel.getFromTaxon();
			toTaxon = taxRel.getToTaxon();
			taxRel = null;
		}else{
			logger.warn ("Only synonym - and taxon-relationships allowed here");
			return false;
		}
		return (isPesiTaxon(fromTaxon, false) && isPesiTaxon(toTaxon, true));
		
	}

	

	/**
	 * Decides if a name is not used as the name part of a PESI taxon (and therefore is
	 * exported to PESI as taxon already) but is related to a name used as a PESI taxon
	 * (e.g. as basionym, orthographic variant, etc.) and therefore should be exported
	 * to PESI as part of the name relationship.
	 * @param taxonName
	 * @return
	 */
	protected boolean isPurePesiName(TaxonNameBase<?,?> taxonName){
		if (hasPesiTaxon(taxonName)){
			return false;
		}
		
		//from names
		for (NameRelationship rel :taxonName.getRelationsFromThisName()){
			TaxonNameBase<?,?> relatedName = rel.getToName();
			if (hasPesiTaxon(relatedName)){
				return true;
			}
		}
		
		//excluded relationships on to-side
		initExcludedRelTypes();
		
		//to names
		for (NameRelationship rel :taxonName.getRelationsToThisName()){
			//exclude certain types
			if (excludedRelTypes.contains(rel.getType())){
				continue;
			}
			TaxonNameBase<?,?> relatedName = rel.getFromName();
			if (hasPesiTaxon(relatedName)){
				return true;
			}
		}
		
		//include hybrid parents, but no childs
		NonViralName nvn = CdmBase.deproxy(taxonName, NonViralName.class);
		for (HybridRelationship rel : (Set<HybridRelationship>)nvn.getHybridParentRelations()){
			NonViralName<?> child = rel.getHybridName();
			if (hasPesiTaxon(child)){
				return true;
			}
		}
		
		return false;
	}
	

	private void initExcludedRelTypes() {
		if (excludedRelTypes.isEmpty()){
			excludedRelTypes.add(NameRelationshipType.BASIONYM());
			excludedRelTypes.add(NameRelationshipType.REPLACED_SYNONYM());
			excludedRelTypes.add(NameRelationshipType.ORTHOGRAPHIC_VARIANT());
		}		
	}


	/**
	 * Decides if a given name has "PESI taxa" attached.
	 * 
	 * @see #getPesiTaxa(TaxonNameBase)
	 * @see #isPesiTaxon(TaxonBase)
	 * @param taxonName
	 * @return
	 */
	protected boolean hasPesiTaxon(TaxonNameBase<?,?> taxonName) {
		for (TaxonBase<?> taxon : taxonName.getTaxonBases()){
			if (isPesiTaxon(taxon)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns those concepts (taxon bases) for the given name that
	 * are pesi taxa.
	 * 
	 *  @see #isPesiTaxon(TaxonBase)
	 * @param name
	 * @return
	 */
	protected Set<TaxonBase<?>> getPesiTaxa(TaxonNameBase<?,?> name){
		Set<TaxonBase<?>> result = new HashSet<TaxonBase<?>>();
		for (TaxonBase<?> taxonBase : name.getTaxonBases()){
			if (isPesiTaxon(taxonBase)){
				result.add(taxonBase);
			}
		}
		return result;
	}


	/**
	 * @see #isPesiTaxon(TaxonBase, boolean)
	 * @param taxonBase
	 * @return
	 */
	protected static boolean isPesiTaxon(TaxonBase taxonBase) {
		return isPesiTaxon(taxonBase, false);
	}

	
	/**
	 * Checks if this taxon base is a taxon that is to be exported to PESI. This is generally the case
	 * but not for taxa that are marked as "unpublish". Synonyms and misapplied names are exported if they are
	 * related at least to one accepted taxon that is also exported, except for those misapplied names 
	 * marked as misapplied names created by Euro+Med common names ({@linkplain http://dev.e-taxonomy.eu/trac/ticket/2786} ).
	 * The list of conditions may change in future.
	 * @param taxonBase
	 * @return
	 */
	protected static boolean isPesiTaxon(TaxonBase taxonBase, boolean excludeMisappliedNames) {
		//handle accepted taxa
		if (taxonBase.isInstanceOf(Taxon.class)){
			Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
			for (Marker marker : taxon.getMarkers()){
				if (marker.getValue() == false && marker.getMarkerType().equals(MarkerType.PUBLISH())){
					taxon = null;
					return false;
				//probably not needed any more after #2786 was fixed
				}else if (marker.getValue() == true && marker.getMarkerType().getUuid().equals(BerlinModelTransformer.uuidMisappliedCommonName)){
					logger.warn("Misapplied common name still exists");
					taxon = null;
					return false;
				}
				
			}
			//handle PESI accepted taxa
			if (! taxon.isMisapplication()){
				for (Marker marker : taxon.getMarkers()){
					if (marker.getValue() == false && marker.getMarkerType().equals(MarkerType.PUBLISH())){
						taxon = null;
						return false;
					}
				}
				return true;
			//handle misapplied names
			}else{
				if (excludeMisappliedNames){
					taxon = null;
					return false;
				}
				for (Marker marker : taxon.getMarkers()){
					//probably not needed any more after #2786 was fixed
					if (marker.getValue() == true && marker.getMarkerType().getUuid().equals(BerlinModelTransformer.uuidMisappliedCommonName)){
						logger.warn("Misapplied common name still exists");
						taxon = null;
						return false;
					}
				}
				for (TaxonRelationship taxRel : taxon.getRelationsFromThisTaxon()){
					if (taxRel.getType().equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())){
//						logger.warn(taxRel.getUuid() + "; " + taxRel.getToTaxon().getUuid() + " + " + taxRel.getToTaxon().getTitleCache());
						if (isPesiTaxon(taxRel.getToTaxon(), true)){
							taxon = null;
							return true;
						}
					}
				}
				if (logger.isDebugEnabled()){ logger.debug("Misapplied name has no accepted PESI taxon: " +  taxon.getUuid() + ", (" +  taxon.getTitleCache() + ")");}
				taxon = null;
				return false;
			}
		//handle synonyms
		}else if (taxonBase.isInstanceOf(Synonym.class)){
			Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
			boolean hasAcceptedPesiTaxon = false;
			for (Taxon accTaxon : synonym.getAcceptedTaxa()){
				if (isPesiTaxon(accTaxon)){
					hasAcceptedPesiTaxon = true;
				}
			}
			if (!hasAcceptedPesiTaxon) {if (logger.isDebugEnabled()){logger.debug("Synonym has no accepted PESI taxon: " +  synonym.getUuid() + ", (" +  synonym.getTitleCache() + ")");}}
			synonym = null;
			return hasAcceptedPesiTaxon;
		}else {
			throw new RuntimeException("Unknown taxon base type: " + taxonBase.getClass());
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.DbExportBase#getDbIdCdmWithExceptions(eu.etaxonomy.cdm.model.common.CdmBase, eu.etaxonomy.cdm.io.common.ExportStateBase)
	 */
	protected Object getDbIdCdmWithExceptions(CdmBase cdmBase, PesiExportState state) {
		if (cdmBase.isInstanceOf(TaxonNameBase.class)){
			return ( cdmBase.getId() + state.getConfig().getNameIdStart() );
		}if (isAdditionalSource(cdmBase) ){
			return ( cdmBase.getId() + 2 * state.getConfig().getNameIdStart() );  //make it a separate variable if conflicts occur.
		}else{
			return super.getDbIdCdmWithExceptions(cdmBase, state);
		}
	}
	
	
	
	private boolean isAdditionalSource(CdmBase cdmBase) {
		if (cdmBase.isInstanceOf(TextData.class)){
			TextData textData = CdmBase.deproxy(cdmBase, TextData.class);
			if (textData.getFeature().equals(Feature.ADDITIONAL_PUBLICATION()) ||
					textData.getFeature().equals(Feature.CITATION())){
				return true;
			}
		}
		return false;
	}


	protected MarkerType getUuidMarkerType(UUID uuid, PesiExportState state){
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		
		MarkerType markerType = state.getMarkerType(uuid);
			if (markerType == null){
				if (uuid.equals(PesiTransformer.uuidMarkerGuidIsMissing)){
					markerType = MarkerType.NewInstance("Uuid is Missing", "Uuid is missing", null);
					markerType.setUuid(uuid);
				} else if (uuid.equals(PesiTransformer.uuidMarkerTypeHasNoLastAction)){
					markerType = MarkerType.NewInstance("Has no last Action", "Has no last action", null);
					markerType.setUuid(uuid);
				}
			}

			state.putMarkerType(markerType);
			return markerType;
		}
	
	
	
	protected static NonViralNameDefaultCacheStrategy getCacheStrategy(TaxonNameBase<?, ?> taxonName) {
		NonViralNameDefaultCacheStrategy cacheStrategy;
		if (taxonName.isInstanceOf(ZoologicalName.class)){
			cacheStrategy = zooNameStrategy;
		}else if (taxonName.isInstanceOf(BotanicalName.class)) {
			cacheStrategy = botanicalNameStrategy;
		}else{
			logger.error("Unhandled taxon name type. Can't define strategy class");
			cacheStrategy = botanicalNameStrategy;
		}
		return cacheStrategy;
	}
	

	
	
	/**
	 * Checks whether a given taxon is a misapplied name.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return Whether the given TaxonName is a misapplied name or not.
	 */
	protected static boolean isMisappliedName(TaxonBase<?> taxon) {
		return getAcceptedTaxonForMisappliedName(taxon) != null;
		
	}
	

	/**
	 * Returns the first accepted taxon for this misapplied name.
	 * If this misapplied name is not a misapplied name, <code>null</code> is returned. 
	 * @param taxon The {@link TaxonBase Taxon}.
	 */
	private static Taxon getAcceptedTaxonForMisappliedName(TaxonBase<?> taxon) {
		if (! taxon.isInstanceOf(Taxon.class)){
			return null;
		}
		Set<TaxonRelationship> taxonRelations = CdmBase.deproxy(taxon, Taxon.class).getRelationsFromThisTaxon();
		for (TaxonRelationship taxonRelationship : taxonRelations) {
			TaxonRelationshipType taxonRelationshipType = taxonRelationship.getType();
			if (taxonRelationshipType.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())) {
				return taxonRelationship.getToTaxon();
			}
		}
		return null;
	}





	
	
	

//	protected List<TaxonBase> getNextDescriptionPartition(Class<? extends DescriptionElementBase> clazz,int limit, int partitionCount) {
//		List<DescriptionElementBase> list = getDescriptionService().listDescriptionElements(null, null, pageSize, pageNumber, propPath);
//		
//		Iterator<TaxonBase> it = list.iterator();
//		while (it.hasNext()){
//			TaxonBase<?> taxonBase = it.next();
//			if (! isPesiTaxon(taxonBase)){
//				it.remove();
//			}
//		}
//		return list;
//	}

}
