// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in.molecular;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportReport;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportState;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.AbcdImportUtility;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.AbcdParseUtility;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.AbcdPersonParser;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;

/**
 * @author pplitzner
 * @date 15.06.2015
 *
 */
public class AbcdDnaParser {

    private final String prefix;

    private final Abcd206ImportReport report;

    private final ICdmApplicationConfiguration cdmAppController;

    public AbcdDnaParser(String prefix, Abcd206ImportReport report, ICdmApplicationConfiguration cdmAppController) {
        this.prefix = prefix;
        this.report = report;
        this.cdmAppController = cdmAppController;
    }

    public DnaSample parse(Element item, DerivedUnit derivedUnitBase, Abcd206ImportState state, boolean associatedSpecimenCreated) {
        Collection dnaCollection = null;
        String dnaUnitID = null;
        if(associatedSpecimenCreated){
            /*cache UnitID and collection to swap later with the dna sample
            This is done because initially ABCD unit contains both the specimen data
            and the DNA data. But the actual specimen data (collection, unitID) are stored
            under the UnitAssociation whereas the collection and unitID of the dna sample are
            the ones of the ABCD unit itself which are initially imported and set for the
            specimen.
             */
            dnaCollection = derivedUnitBase.getCollection();
            dnaUnitID = AbcdImportUtility.getUnitID(derivedUnitBase, state.getConfig());
        }
        DnaSample dnaSample = null;
        NodeList specimenUnitList = item.getElementsByTagName(prefix+"SpecimenUnit");
        if(specimenUnitList.item(0)!=null && specimenUnitList.item(0) instanceof Element){
            parseSpecimenUnit((Element)specimenUnitList.item(0));
        }
        NodeList unitExtensions = item.getElementsByTagName(prefix+"UnitExtension");
        for(int i=0;i<unitExtensions.getLength();i++){
            if(unitExtensions.item(i) instanceof Element){
                Element unitExtension = (Element) unitExtensions.item(i);
                NodeList ggbn = unitExtension.getElementsByTagName("ggbn:GGBN");
                if(ggbn.getLength()>0){
                    AbcdGgbnParser ggbnParser = new AbcdGgbnParser(report, cdmAppController);
                    dnaSample = ggbnParser.parse(ggbn, state);
                    if(associatedSpecimenCreated){
                        dnaSample.setCollection(dnaCollection);
                        AbcdImportUtility.setUnitID(dnaSample, dnaUnitID, state.getConfig());
                    }
                }
            }
        }
        return dnaSample;
    }

    private void parseSpecimenUnit(Element item) {
        NodeList preparationsList = item.getElementsByTagName(prefix+"Preparations");
        if(preparationsList.item(0)!=null && preparationsList.item(0) instanceof Element){
            parsePreparations((Element) preparationsList.item(0));
        }
        NodeList preservationsList = item.getElementsByTagName(prefix+"Preservations");
        if(preservationsList.item(0)!=null && preservationsList.item(0) instanceof Element){
            parsePreservations((Element) preservationsList.item(0));
        }
    }

    private void parsePreparations(Element item) {
        NodeList preparationList = item.getElementsByTagName(prefix+"preparation");
        for(int i=0;i<preparationList.getLength();i++){
            Node node = preparationList.item(i);
            if(node instanceof Element){
                String preparationType = AbcdParseUtility.parseFirstTextContent(((Element) node).getElementsByTagName(prefix+"preparationType"));
                String preparationMaterials = AbcdParseUtility.parseFirstTextContent(((Element) node).getElementsByTagName(prefix+"preparationMaterials"));
                NodeList preparationAgentList = ((Element) node).getElementsByTagName(prefix+"preparationAgent");
                if(preparationAgentList.item(0)!=null && preparationAgentList.item(0) instanceof Element){
                    AgentBase<?> preparationAgent = parsePreparationAgent((Element)preparationAgentList.item(0));
                }
                NodeList preparationDate = ((Element) node).getElementsByTagName(prefix+"preparationDate");
            }
        }
    }

    private AgentBase<?> parsePreparationAgent(Element item) {
        AgentBase<?> agentBase = null;
        NodeList personList = item.getElementsByTagName(prefix+"Person");
        if(personList.item(0)!=null && personList.item(0) instanceof Element){
            agentBase = new AbcdPersonParser(prefix,report, cdmAppController).parse((Element)personList.item(0));
        }
        return agentBase;
    }

    private void parsePreservations(Element item) {
        NodeList preservationList = item.getElementsByTagName(prefix+"preservation");
        for(int i=0;i<preservationList.getLength();i++){
            Node node = preservationList.item(i);
            if(node instanceof Element){
                String preservationType = AbcdParseUtility.parseFirstTextContent(((Element) node).getElementsByTagName(prefix+"preservationType"));
                String preservationTemperature = AbcdParseUtility.parseFirstTextContent(((Element) node).getElementsByTagName(prefix+"preservationTemperature"));
            }
        }
    }


}
