/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.out;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * Transformer for WFO Content export.
 *
 * @author a.mueller
 * @date 2024-01-30
 */
public class WfoContentExportTransformer extends WfoExportTransformerBase {

    private static final long serialVersionUID = -527652844010832994L;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Override
    public String getCacheByFeature(Feature feature) throws UndefinedTransformerMethodException {
        if (feature == null) {
            return null;
        }

        //see also https://rs.gbif.org/vocabulary/gbif/description_type.xml

        if (feature.equals(Feature.DESCRIPTION())) {
//      } else if (feature.equals(Feature.MORPHOLOGY())) {  //morphology does not yet exist as feature in CDM
            //general
            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/morphology";
        } else if (feature.equals(Feature.DIAGNOSIS())) {
            //diagnostic
            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/diagnostic";
        } else if (feature.equals(Feature.NOTES())) {
            //general
            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/general";
//        } else if (feature.equals(Feature.MORPHOLOGY())) {  //mor
//            //morphology
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/morphology";
//        } else if (feature.equals(Feature.HABIT())) {
//            //habit
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/habit";
//        } else if (feature.equals(Feature.CYTOLOGY())) {
//            //cytology
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/cytology";
//        } else if (feature.equals(Feature.PHYSIOLOGY())) {
//            //physiology
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/physiology";
//        } else if (feature.equals(Feature.size())) {
//            //size
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/size";
//        } else if (feature.equals(Feature.lifespan())) {
//            //lifespan
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/lifespan";
//        } else if (feature.equals(Feature.lifetime())) {
//            //lifetime
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/lifetime";
//        //TODO Biology / Ecology
//        } else if (feature.equals(Feature.BIOLOGY())) {
//            //biology
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/biology";
//        } else if (feature.equals(Feature.ecology())) {
//            //ecology
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/ecology";
        }else if (feature.equals(Feature.HABITAT())) {
            //habitat
            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/habitat";
        }else if (feature.equals(Feature.DISTRIBUTION()) || feature.equals(Feature.DISTRIBUTION_GENERAL())) {
            //distribution
            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/distribution";
//        }else if (feature.equals(Feature.reproduction())) {
//            //reproduction
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/reproduction";
        }else if (feature.equals(Feature.CONSERVATION())) {
            //conservation
            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/conservation";
        }else if (feature.equals(Feature.USES())) {
            //use
            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/use";
//        }else if (feature.equals(Feature.dispersal())) {
//            //dispersal
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/dispersal";
//        }else if (feature.equals(Feature.lifecycle())) {
//            //lifecycle
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/lifecycle";
//        }else if (feature.equals(Feature.growth())) {
//            //growth
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/growth";
        }else if (feature.equals(Feature.CHROMOSOME_NUMBER()) /*feature.equals(Feature.genetics())*/) {
            //genetics
            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/genetics";
//        }else if (feature.equals(Feature.chemistry())) {
//            //chemistry
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/chemistry";
//        }else if (feature.equals(Feature.associations())) {
//            //associations
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/associations";
//        }else if (feature.equals(Feature.population())) {
//            //population
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/population";
//        }else if (feature.equals(Feature.management())) {
//            //management
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/management";
//        }else if (feature.equals(Feature.legislation())) {
//            //legislation
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/legislation";
        }else if ( feature.equals(Feature.IUCN_STATUS()) /*feature.equals(Feature.threats())*/ ) {
            //threats
            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/threats";
//        }else if (feature.equals(Feature.typematerial())) {
//            //typematerial
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/typematerial";
//        }else if (feature.equals(Feature.typelocality())) {
//            //typelocality
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/typelocality";
//        }else if (feature.equals(Feature.typematerial())) {
//            //typematerial
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/typematerial";
//        }else if (feature.equals(Feature.phylogeny())) {
//            //phylogeny
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/phylogeny";
//        }else if (feature.equals(Feature.hybrids())) {
//            //hybrids
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/hybrids";
        }else if (feature.equals(Feature.CITATION())) {
            //literature
            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/literature";
//        }else if (feature.equals(Feature.culture())) {
//            //culture
//            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/culture";
        }else if (feature.equals(Feature.COMMON_NAME())) {
            //vernacular
            return "http://rs.gbif.org/vocabulary/gbif/descriptionType/vernacular";
        }

        //no mapping found
        return null;
    }
}