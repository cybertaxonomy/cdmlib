/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

/**
 * @author k.luther
 * @date 26.01.2017
 *
 */
public class Abcd206ImportParser {

    private static final Logger logger = Logger.getLogger(Abcd206ImportParser.class);
    /**
     * Store the unit's properties into variables Look which unit is the
     * preferred one Look what kind of name it is supposed to be, for the
     * parsing (Botanical, Zoological)
     * @param state
     *
     * @param racine: the root node for a single unit
     */
    public static void setUnitPropertiesXML(Element root, Abcd206XMLFieldGetter abcdFieldGetter, Abcd206ImportState state) {
        try {
            NodeList group;

            group = root.getChildNodes();
            for (int i = 0; i < group.getLength(); i++) {
                if (group.item(i).getNodeName().equals( state.getPrefix()+ "Identifications")) {
                    group = group.item(i).getChildNodes();
                    break;
                }
            }
            state.getDataHolder().setIdentificationList(new ArrayList<Identification>());
            state.getDataHolder().setStatusList(new ArrayList<SpecimenTypeDesignationStatus>());
            state.getDataHolder().setAtomisedIdentificationList(new ArrayList<HashMap<String, String>>());
            state.getDataHolder().setReferenceList(new ArrayList<String[]>());
            state.getDataHolder().setMultimediaObjects(new HashMap<String,Map<String, String>>());
            state.getDataHolder().setGatheringMultimediaObjects(new HashMap<String,Map<String, String>>());

            abcdFieldGetter.getScientificNames(group);
            abcdFieldGetter.getType(root);


            logger.info("this.identificationList "+state.getDataHolder().getIdentificationList().toString());

            abcdFieldGetter.getIDs(root);
            abcdFieldGetter.getRecordBasis(root);
            abcdFieldGetter.getKindOfUnit(root);
            abcdFieldGetter.getMultimedia(root);
            abcdFieldGetter.getNumbers(root);
            abcdFieldGetter.getGeolocation(root, state);
            abcdFieldGetter.getGatheringPeople(root);
            abcdFieldGetter.getGatheringDate(root);
            abcdFieldGetter.getGatheringElevation(root);
            abcdFieldGetter.getGatheringNotes(root);
            abcdFieldGetter.getGatheringImages(root);
            abcdFieldGetter.getGatheringMethod(root);
            abcdFieldGetter.getAssociatedUnitIds(root);
            abcdFieldGetter.getUnitNotes(root);
            boolean referencefound = abcdFieldGetter.getReferences(root);
//            if (!referencefound && state.getRef() != null) {
//                String[]a = {state.getRef().getTitleCache(),"",""};
//                state.getDataHolder().getReferenceList().add(a);
//            }

        } catch (Exception e) {
            logger.info("Error occured while parsing XML file" + e);
            e.printStackTrace();
        }
    }
}
