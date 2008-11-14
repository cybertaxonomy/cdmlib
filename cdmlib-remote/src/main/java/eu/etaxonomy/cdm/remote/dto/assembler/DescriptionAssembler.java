/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto.assembler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.remote.dto.BaseSTO;
import eu.etaxonomy.cdm.remote.dto.DescriptionElementSTO;
import eu.etaxonomy.cdm.remote.dto.DescriptionTO;
import eu.etaxonomy.cdm.remote.dto.FeatureTO;
import eu.etaxonomy.cdm.remote.dto.FeatureTreeTO;

/**
 * @author a.kohlbecker
 * @created 24.06.2008
 * @version 1.0
 */
@Component
public class DescriptionAssembler extends AssemblerBase<BaseSTO, DescriptionTO, DescriptionBase> {
	private static Logger logger = Logger.getLogger(DescriptionAssembler.class);
	
	@Autowired
	private MediaAssembler mediaAssembler;
	@Autowired
	private IDefinedTermDao languageDao;
	@Autowired
	private IDescriptionDao descriptionDao;
	@Autowired
	private LocalisedTermAssembler localisedTermAssembler;
	@Autowired
	private ReferenceAssembler referenceAssembler;

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	@Deprecated
	BaseSTO getSTO(DescriptionBase cdmObj, Enumeration<Locale> locales) {
		throw new RuntimeException("unimplemented method");
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	public DescriptionTO getTO(DescriptionBase descriptionBase,
			Enumeration<Locale> locales) {
		DescriptionTO to = new DescriptionTO();
		to.setUuid(descriptionBase.getUuid().toString());
		to.setCreated(descriptionBase.getCreated());
		to.setCreatedBy(descriptionBase.getCreatedBy());
		//TODO to.setLabel(label);
		for(DescriptionElementBase descriptionElementBase :  descriptionBase.getElements()){
			to.addElement(getDescriptionElementSTO(descriptionElementBase, locales));
		}
		for(ReferenceBase source : descriptionBase.getDescriptionSources()){
			to.addSource(referenceAssembler.getSTO(source, locales));
		}
		//TODO add describedSpecimenOrObservations
		return to;
	}
	
	/**
	 * @param descriptionBaseSet
	 * @param locales
	 * @return
	 */
	public Set<DescriptionTO> getTOs(Set<? extends DescriptionBase> descriptionBaseSet,
			Enumeration<Locale> locales){
		HashSet<DescriptionTO> tos = new HashSet<DescriptionTO>(descriptionBaseSet.size());
		for (DescriptionBase descriptionBase : descriptionBaseSet) {
			tos.add(getTO(descriptionBase, locales));
		}
		return tos;
	}

//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
//	 */
	public FeatureTreeTO getTO(FeatureTree featureTree, Set<TaxonDescription> descriptions,
			Enumeration<Locale> locales) {
	 	FeatureTreeTO to = new FeatureTreeTO();
		List<DescriptionTO> descriptionTOs = new ArrayList<DescriptionTO>();  
		
		
		if(!featureTree.isDescriptionSeparated()){
	
			TaxonDescription superDescription = TaxonDescription.NewInstance();
			//put all descriptionElements in superDescription and make it invisible
			for(TaxonDescription description: descriptions){
				for(DescriptionElementBase element: description.getElements()){
					superDescription.addElement(element);
				}
			}
			DescriptionTO descriptionTO = getDescriptionTO(superDescription, featureTree, locales);
			descriptionTO.setVisible(false);
			descriptionTOs.add(descriptionTO);
		}else{
			for (TaxonDescription description: descriptions){
				descriptionTOs.add(getDescriptionTO(description, featureTree, locales)); 
			}
		}
		to.setUuid(featureTree.getUuid().toString());
		to.setCreated(featureTree.getCreated());
		//TODO etc.
		
		
		to.setDescriptions(descriptionTOs);
		
		return to;
	}
	
	/**
	 * TODO this is work in progress
	 * @param featureTree
	 * @param locales
	 * @return
	 */
	public FeatureTreeTO getTO(FeatureTree featureTree, Enumeration<Locale> locales){
		FeatureTreeTO to = new FeatureTreeTO();
		List<FeatureTO> featureTOs = new ArrayList<FeatureTO>();  
		
		for(FeatureNode featureNode : featureTree.getRootChildren()){
			featureTOs.add(getTO(featureNode.getFeature(), locales));
		}
		
		to.setFeatures(featureTOs);
		
		to.setUuid(featureTree.getUuid().toString());
		to.setCreated(featureTree.getCreated());
		
		return to;
	}
	
	/**
	 * TODO this is work in progress
	 * @param feature
	 * @param locales
	 * @return
	 */
	public FeatureTO getTO(Feature feature, Enumeration<Locale> locales){
		FeatureTO to = new FeatureTO();
		
		to.setUuid(feature.getUuid().toString());
		to.setLabel(feature.getLabel());
		
		return to;
	}
	
	private DescriptionTO getDescriptionTO(DescriptionBase description, FeatureTree featureTree, Enumeration<Locale> locales){
		DescriptionTO descriptionTO = new DescriptionTO();
		Map<Feature, List<DescriptionElementBase>> elementListMap = getElementListMap(description);
		
		for (FeatureNode node: featureTree.getRootChildren()){
			if (node.isLeaf()){
				FeatureTO featureTO = getFeatureTO(node, elementListMap, locales);
				descriptionTO.addFeature(featureTO);
				featureTO.setUuid(node.getFeature().getUuid().toString());
				

				// KLUDGE can this be handled more generic?  
				if(featureTO.getFeature().getTerm().equals("Distribution")){
					
					Set<Distribution> distributionSet = new HashSet<Distribution>();
					for (DescriptionElementBase des : description.getElements()){
						if(des instanceof Distribution){
							distributionSet.add((Distribution) des);
						}
					}
					
					// don't break in case something is wrong with the URL
					try{
//						featureTO.setUrl(distribution.getWebServiceUrl(distributionSet, webServiceUrl, 780, 390, null));
						//TODO: fill presenceAbsenceColorMap 
						Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceColorMap = new HashMap<PresenceAbsenceTermBase<?>, Color>();
						presenceAbsenceColorMap.put(PresenceTerm.PRESENT(), Color.BLUE);
						presenceAbsenceColorMap.put(PresenceTerm.INTRODUCED(), Color.RED);
						featureTO.addExternalResource("geoServiceParameters", Distribution.getEditGeoServiceUrlParameterString(distributionSet, presenceAbsenceColorMap, 0, 0, null, null));

					}catch(Exception e){
						// pass
					}
				}
				
				
			}else{ //is not leaf
				FeatureTO featureTO = new FeatureTO();
				for (FeatureNode childNode: node.getChildren()){
					FeatureTO childFeatureTO = getFeatureTO(childNode, elementListMap, locales);
					featureTO.addChild(childFeatureTO);
				}
				
				
			}
			

		}
		return descriptionTO;
	}

	private FeatureTO getFeatureTO(FeatureNode node, Map<Feature, List<DescriptionElementBase>> elementListMap, Enumeration<Locale> locales ){
//		//only create FeatureTO for used features
//		if (elementListMap.get(node) == null){
//			return null;
//		}
		FeatureTO featureTO = new FeatureTO();
		Set<DescriptionElementSTO> elementList = getDescriptionElements(elementListMap, node.getFeature(), locales);
		featureTO.setDescriptionElements(elementList);
		featureTO.setFeature(localisedTermAssembler.getSTO(node.getFeature(), locales));
		
		return featureTO;
	}
	
	private Set<DescriptionElementSTO> getDescriptionElements(Map<Feature, List<DescriptionElementBase>> elementListMap, Feature feature, Enumeration<Locale> locales){
		Set<DescriptionElementSTO> result = new HashSet<DescriptionElementSTO>() ;
		List<DescriptionElementBase> elBaseList = elementListMap.get(feature);
		if (elBaseList  != null){
			for (DescriptionElementBase elBase: elBaseList){
				result.add(this.getDescriptionElementSTO(elBase, locales));
			}
		}
		return result;
	}
	
	private Map<Feature, List<DescriptionElementBase>> getElementListMap(DescriptionBase description){
		Map<Feature, List<DescriptionElementBase>> result = new HashMap<Feature, List<DescriptionElementBase>>();
		for (DescriptionElementBase descriptionElement : description.getElements()){
			
			Feature elementFeature = descriptionElement.getFeature();
			List<DescriptionElementBase> elementList = result.get(elementFeature);
			if (elementList == null){
				elementList = new ArrayList<DescriptionElementBase>();
				result.put(elementFeature, elementList);
			}
			elementList.add(descriptionElement);
		}
		return result;
	}
	
	
	/**
	 * @param descriptionElementBase
	 * @param locales
	 * @return
	 */
	public DescriptionElementSTO getDescriptionElementSTO(
			DescriptionElementBase descriptionElementBase,
			Enumeration<Locale> locales) {
		
		List<Language> languages = languageDao.getLanguagesByLocale(locales);

		DescriptionElementSTO sto = new DescriptionElementSTO();
		sto.setUuid(descriptionElementBase.getUuid().toString());
		// reference
		if(descriptionElementBase.getCitation() != null){
			sto.setReference(referenceAssembler.getSTO(descriptionElementBase.getCitation(), locales));
		}
		
		// media
		for(Media media : descriptionElementBase.getMedia()){
			sto.addMedia(mediaAssembler.getSTO(media, locales));
			sto.setMediaType(localisedTermAssembler.getSTO(descriptionElementBase.getFeature(), locales));
		}

		
		
		// TextData specific
		if(descriptionElementBase instanceof TextData){
			TextData textdata = (TextData)descriptionElementBase;
			
			sto.setClassType(TextData.class.getSimpleName());
			
			//TODO extract method for finding text by preferred languages
			for (Language language : languages) {
				String text = textdata.getText(language);
				if(text != null){
					sto.setLanguage(language.getLabel());
					sto.setDescription(text);
					break;
				}
			}
			// HACK:
			if(textdata.getMultilanguageText().keySet().iterator().hasNext()){
			Language language = textdata.getMultilanguageText().keySet().iterator().next();
				String text = textdata.getMultilanguageText().get(language).getText();
				sto.setLanguage(language.getLabel());
				sto.setDescription(text);
			}
		}
		// Distribution specific
		if(descriptionElementBase instanceof Distribution){
			Distribution distribution = (Distribution) descriptionElementBase;
			
			sto.setClassType(Distribution.class.getSimpleName());
			
			
			
			sto.setArea(localisedTermAssembler.getSTO(distribution.getArea(), locales));
			
		}
		
		return sto;
	}

	
}
