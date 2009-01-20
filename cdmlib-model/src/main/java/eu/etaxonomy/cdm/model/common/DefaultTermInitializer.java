/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.init.ITermLoader;
import eu.etaxonomy.cdm.model.common.init.ITermInitializer;
import eu.etaxonomy.cdm.model.common.init.TermLoader;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.RightsTerm;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DeterminationModifier;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.mueller
 *
 */
public class DefaultTermInitializer implements ITermInitializer {
	private static final Logger logger = Logger.getLogger(DefaultTermInitializer.class);
	protected ITermLoader termLoader = new TermLoader();

	protected static Class[] classesToInitialize  =  {Language.class,Continent.class,WaterbodyOrCountry.class,
		                                            Rank.class,TypeDesignationStatus.class,
		                                            NomenclaturalStatusType.class,SynonymRelationshipType.class,HybridRelationshipType.class,
		                                            NameRelationshipType.class,TaxonRelationshipType.class,MarkerType.class,
		                                            AnnotationType.class,NamedAreaType.class,NamedAreaLevel.class,
		                                            NomenclaturalCode.class,Feature.class,NamedArea.class,PresenceTerm.class,AbsenceTerm.class,Sex.class,
		                                            DerivationEventType.class,PreservationMethod.class,DeterminationModifier.class,StatisticalMeasure.class,RightsTerm.class
		};
	
	public void initialize() {
		Map<UUID,DefinedTermBase> terms = new HashMap<UUID,DefinedTermBase>();
		
		for(Class clazz : classesToInitialize) {
			TermVocabulary voc  = termLoader.loadTerms((Class<? extends DefinedTermBase>)clazz, terms);
			setDefinedTerms((Class<? extends DefinedTermBase>)clazz,voc);
		}		
	}
	
	protected void setDefinedTerms(Class<? extends DefinedTermBase> clazz, TermVocabulary vocabulary) {
		try {
			DefinedTermBase newInstance = clazz.newInstance();
			newInstance.setDefaultTerms(vocabulary);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}


}
