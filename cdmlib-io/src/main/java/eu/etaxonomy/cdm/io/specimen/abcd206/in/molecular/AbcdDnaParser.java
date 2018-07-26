/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in.molecular;

import java.util.Date;
import java.util.UUID;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportState;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.AbcdParseUtility;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.AbcdPersonParser;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.SpecimenImportReport;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;

/**
 * @author pplitzner
 * @since 15.06.2015
 *
 */
public class AbcdDnaParser {

    private final String prefix;

    private final SpecimenImportReport report;

    private final ICdmRepository cdmAppController;

    public AbcdDnaParser(String prefix, SpecimenImportReport report, ICdmRepository cdmAppController) {
        this.prefix = prefix;
        this.report = report;
        this.cdmAppController = cdmAppController;
    }

    public DnaSample parse(Element item, Abcd206ImportState state) {
        FieldUnit fieldUnit = state.getFieldUnit(state.getDataHolder().getFieldNumber());
        if (fieldUnit == null){
            fieldUnit = FieldUnit.NewInstance();
        }
        DnaSample dnaSample = DnaSample.NewInstance();
        DerivationEvent.NewSimpleInstance(fieldUnit, dnaSample, DerivationEventType.DNA_EXTRACTION());

        //specimen unit
        NodeList specimenUnitList = item.getElementsByTagName(prefix+"SpecimenUnit");
        if(specimenUnitList.item(0)!=null && specimenUnitList.item(0) instanceof Element){
            parseSpecimenUnit((Element)specimenUnitList.item(0), dnaSample);
        }
        NodeList unitExtensions = item.getElementsByTagName(prefix+"UnitExtension");
        for(int i=0;i<unitExtensions.getLength();i++){
            if(unitExtensions.item(i) instanceof Element){
                Element unitExtension = (Element) unitExtensions.item(i);
                NodeList ggbn = unitExtension.getElementsByTagName("ggbn:GGBN");
                if(ggbn.getLength()>0){
                    AbcdGgbnParser ggbnParser = new AbcdGgbnParser(report, cdmAppController);
                    ggbnParser.parse(ggbn, dnaSample, state);
                }
            }
        }
        return dnaSample;
    }

    private void parseSpecimenUnit(Element item, DnaSample dnaSample) {
        NodeList preparationsList = item.getElementsByTagName(prefix+"Preparations");
        if(preparationsList.item(0)!=null && preparationsList.item(0) instanceof Element){
            parsePreparations((Element) preparationsList.item(0), dnaSample);
        }
        NodeList preservationsList = item.getElementsByTagName(prefix+"Preservations");
        if(preservationsList.item(0)!=null && preservationsList.item(0) instanceof Element){
            parsePreservations((Element) preservationsList.item(0), dnaSample);
        }
    }

    private void parsePreparations(Element item, DnaSample dnaSample) {
        NodeList preparationList = item.getElementsByTagName(prefix+"preparation");
        for(int i=0;i<preparationList.getLength();i++){
            Node node = preparationList.item(i);
            if(node instanceof Element){
                DerivationEvent derivedFrom = dnaSample.getDerivedFrom();

                String preparationType = AbcdParseUtility.parseFirstTextContent(((Element) node).getElementsByTagName(prefix+"preparationType"));

                //preparation materials
                String preparationMaterials = AbcdParseUtility.parseFirstTextContent(((Element) node).getElementsByTagName(prefix+"preparationMaterials"));
                derivedFrom.setDescription(preparationMaterials);
                //preparation actor
                NodeList preparationAgentList = ((Element) node).getElementsByTagName(prefix+"preparationAgent");
                if(preparationAgentList.item(0)!=null && preparationAgentList.item(0) instanceof Element){
                    AgentBase<?> preparationAgent = parsePreparationAgent((Element)preparationAgentList.item(0));
                    derivedFrom.setActor(preparationAgent);
                }
                //preparation date
                Date preparationDate = AbcdParseUtility.parseFirstDate(((Element) node).getElementsByTagName(prefix+"preparationDate"));
                derivedFrom.setTimeperiod(TimePeriod.NewInstance(preparationDate, null));
                //sample designation
                NodeList sampleDesignationsList = ((Element) node).getElementsByTagName(prefix+"sampleDesignations");
                if(sampleDesignationsList.item(0)!=null && sampleDesignationsList.item(0) instanceof Element){
                    parseSampleDesignations((Element)sampleDesignationsList.item(0), dnaSample);
                }
            }
        }
    }

    /**
     * @param item
     * @param dnaSample
     */
    private void parseSampleDesignations(Element item, DnaSample dnaSample) {
        NodeList sampleDesignationList = item.getElementsByTagName(prefix+"sampleDesignation");
        for(int i=0;i<sampleDesignationList.getLength();i++){
            dnaSample.addIdentifier(sampleDesignationList.item(i).getTextContent(), (DefinedTerm)cdmAppController.getTermService().find(UUID.fromString("fadeba12-1be3-4bc7-9ff5-361b088d86fc")));
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

    private void parsePreservations(Element item, DnaSample dnaSample) {
        NodeList preservationList = item.getElementsByTagName(prefix+"preservation");
        for(int i=0;i<preservationList.getLength();i++){
            Node node = preservationList.item(i);
            if(node instanceof Element){
                PreservationMethod preservation = PreservationMethod.NewInstance();
                dnaSample.setPreservation(preservation);

                String preservationType = AbcdParseUtility.parseFirstTextContent(((Element) node).getElementsByTagName(prefix+"preservationType"));

                Double preservationTemperature = AbcdParseUtility.parseFirstDouble(((Element) node).getElementsByTagName(prefix+"preservationTemperature"), report);
                preservation.setTemperature(preservationTemperature);
            }
        }
    }


}
