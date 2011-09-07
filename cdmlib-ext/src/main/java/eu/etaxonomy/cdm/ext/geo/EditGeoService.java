// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;

/**
 * @author a.kohlbecker
 * @date 18.06.2009
 *
 */
@Service
@Transactional(readOnly=true)
public class EditGeoService implements IEditGeoService{
	public static final Logger logger = Logger.getLogger(EditGeoService.class);
	
	private static final String DEFAULT_BACK_LAYER = "tdwg4";

	@Autowired
	private IDescriptionDao dao;
	

    @Autowired
    private IGeoServiceAreaMapping areaMapping;
 	
	private IDefinedTermDao termDao;
		
	@Autowired
	public void setTermDao(IDefinedTermDao termDao) {
		this.termDao = termDao;
		EditGeoServiceUtilities.setTermDao(termDao);
	}

	@Autowired
	private IOccurrenceDao occurrenceDao;

	
	private Set<Feature> getDistributionFeatures() {
		Set<Feature> distributionFeature = new HashSet<Feature>();
		Feature feature = (Feature) termDao.findByUuid(Feature.DISTRIBUTION().getUuid());
		distributionFeature.add(feature);
		return distributionFeature;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.IEditGeoService#getEditGeoServiceUrlParameterString(java.util.List, java.util.Map, int, int, java.lang.String, java.lang.String, java.util.List)
	 */
	public String getDistributionServiceRequestParameterString(
			List<TaxonDescription> taxonDescriptions,
			Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceTermColors,
			int width, int height, String bbox, String backLayer,
			List<Language> langs) {
		Set<Distribution> distributions = new HashSet<Distribution>();
		for(TaxonDescription taxonDescription : taxonDescriptions){
			List<Distribution> result = (List)dao.getDescriptionElements(taxonDescription, getDistributionFeatures(), Distribution.class, null, null, null);
			distributions.addAll(result);
		}
		
		if(backLayer == null){
			backLayer = DEFAULT_BACK_LAYER;
		}
		String uriParams = EditGeoServiceUtilities.getDistributionServiceRequestParameterString(distributions, areaMapping, presenceAbsenceTermColors, width, height, bbox, backLayer, null, langs);

		return uriParams;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.IEditGeoService#getEditGeoServiceUrlParameterString(eu.etaxonomy.cdm.model.description.TaxonDescription, java.util.Map, int, int, java.lang.String, java.lang.String)
	 */
	public String getDistributionServiceRequestParameterString(
			TaxonDescription taxonDescription,
			Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceTermColors,
			int width, int height, String bbox, String backLayer,
			List<Language> langs) {
		
		List<TaxonDescription> taxonDescriptions = new ArrayList<TaxonDescription>();
		taxonDescriptions.add(taxonDescription);
		
		return getDistributionServiceRequestParameterString(taxonDescriptions, presenceAbsenceTermColors, width, height, bbox, backLayer, langs);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IEditGeoService#getEditGeoServiceUrlParameterString(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.Map, int, int, java.lang.String, java.lang.String)
	 */
	public String getDistributionServiceRequestParameterString(Taxon taxon,
			Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceTermColors, int width, int height, String bbox,
			String backLayer,
			List<Language> langs) {
		
		List<TaxonDescription> taxonDescriptions = dao.getTaxonDescriptions(taxon, null, null, null, null, null);
		
		Set<Distribution> distCollection = new HashSet<Distribution>();
		// get descriptions elements for each description
		for (TaxonDescription td : taxonDescriptions) {
			List<Distribution> dists = (List)dao.getDescriptionElements(td, getDistributionFeatures(), Distribution.class, null, null, null);
			distCollection.addAll(dists);
		}
		// generate the uri parameter string
		if(backLayer == null){
			backLayer = DEFAULT_BACK_LAYER;
		}
		String uriParams = EditGeoServiceUtilities.getDistributionServiceRequestParameterString(distCollection,areaMapping,
			presenceAbsenceTermColors, width, height, bbox, backLayer, null, langs);

		return uriParams;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.geo.IEditGeoService#getOccurrenceServiceRequestParameterString(eu.etaxonomy.cdm.model.description.TaxonDescription, java.util.Map, int, int, java.lang.String, java.lang.String)
	 */
	@Override
	public String getOccurrenceServiceRequestParameterString(
			List<SpecimenOrObservationBase> specimensOrObersvations,
			Map<Class<? extends SpecimenOrObservationBase>, Color> specimenOrObservationTypeColors, Boolean doReturnImage,
			Integer width, Integer height, String bbox, String backLayer) {
		
			List<Point> fieldObservationPoints = new ArrayList<Point>();
			List<Point> derivedUnitPoints = new ArrayList<Point>();
			
			IndividualsAssociation individualsAssociation;
			DerivedUnitBase derivedUnit;
			
			for(SpecimenOrObservationBase specimenOrObservationBase : specimensOrObersvations){
				SpecimenOrObservationBase<?> specimenOrObservation = occurrenceDao.load(specimenOrObservationBase.getUuid());
				
				if(specimenOrObservation instanceof FieldObservation){
					fieldObservationPoints.add(((FieldObservation)specimenOrObservation).getGatheringEvent().getExactLocation());
				} 
				if(specimenOrObservation instanceof DerivedUnitBase<?>){						
					registerDerivedUnitLocations((DerivedUnitBase)specimenOrObservation, derivedUnitPoints);
				}			
			}
			
		return EditGeoServiceUtilities.getOccurrenceServiceRequestParameterString(
						fieldObservationPoints, derivedUnitPoints,
						specimenOrObservationTypeColors, doReturnImage , width,
						height, bbox, backLayer);

	}

	/**
	 * @param derivedUnit
	 * @param derivedUnitPoints
	 */
	private void registerDerivedUnitLocations(DerivedUnitBase<?> derivedUnit,
			List<Point> derivedUnitPoints) {

		Set<SpecimenOrObservationBase> originals = derivedUnit.getOriginals();
		if(originals != null){
			for (SpecimenOrObservationBase original : originals) {
				if (original instanceof FieldObservation) {
					Point point =  ((FieldObservation) original).getGatheringEvent().getExactLocation();
					if(point != null){
						//FIXME: remove next statement after DerivedUnitFacade or ABCD import is fixed
						if(point.getLatitude() == 0.0 && point.getLongitude() == 0.0){
							continue;
						}
						derivedUnitPoints.add(point);
					}
				} else {
					registerDerivedUnitLocations((DerivedUnitBase) original, derivedUnitPoints);
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.geo.IEditGeoService#setMapping(eu.etaxonomy.cdm.model.location.NamedArea, eu.etaxonomy.cdm.ext.geo.GeoServiceArea)
	 */
	@Override
	public void setMapping(NamedArea area, GeoServiceArea geoServiceArea) {
		areaMapping.set(area, geoServiceArea);
		
	}

}
