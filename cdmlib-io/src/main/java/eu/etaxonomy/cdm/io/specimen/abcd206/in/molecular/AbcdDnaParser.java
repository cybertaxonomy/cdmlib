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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportState;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.AbcdParseUtility;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.AbcdPersonParser;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.SpecimenImportReport;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.term.IdentifierType;

/**
 * @author pplitzner
 * @since 15.06.2015
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

    public DnaSample parse(Element item, Abcd206ImportState state, DnaSample dnaSample, Set<CdmBase> entitiesToSave) {



        //specimen unit
//        NodeList specimenUnitList = item.getElementsByTagName(prefix+"SpecimenUnit");
//        if(specimenUnitList.item(0)!=null && specimenUnitList.item(0) instanceof Element){
//            parseSpecimenUnit((Element)specimenUnitList.item(0), dnaSample, entitiesToSave);
//        }

        NodeList unitExtensions = item.getElementsByTagName(prefix+"UnitExtensions");
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

    /**
     * @param state
     * @param dnaSample
     */
    public DnaSample createDNASampleAndFieldUnit(Abcd206ImportState state ) {
        FieldUnit fieldUnit = null;
        String fieldNumber = state.getDataHolder().getFieldNumber();
        if (StringUtils.isNotBlank(fieldNumber) && !fieldNumber.equals("0") && !fieldNumber.equalsIgnoreCase("s.n.")){
            fieldUnit = state.getFieldUnit(fieldNumber);
        }
        if (fieldUnit == null){
            fieldUnit = state.getLastFieldUnit();
            if (fieldUnit == null){
                fieldUnit = FieldUnit.NewInstance();
                if (StringUtils.isNotBlank(fieldNumber)){
                    fieldUnit.setFieldNumber(fieldNumber);
                }
            }

            state.setFieldUnit(fieldUnit);
            state.setLastFieldUnit(fieldUnit);
        }
        DnaSample dnaSample = DnaSample.NewInstance();
        DerivationEvent.NewSimpleInstance(fieldUnit, dnaSample, DerivationEventType.DNA_EXTRACTION());
        return dnaSample;
    }




    public void parseSpecimenUnit(Element item, DnaSample dnaSample,Abcd206ImportState state, Set<CdmBase> entitiesToSave) {

        NodeList preparationsList = item.getElementsByTagName(prefix+"Preparations");
        if(preparationsList.item(0)!=null && preparationsList.item(0) instanceof Element){
            parsePreparations((Element) preparationsList.item(0), dnaSample, entitiesToSave);
        }
        NodeList preservationsList = item.getElementsByTagName(prefix+"Preservations");
        if(preservationsList.item(0)!=null && preservationsList.item(0) instanceof Element){
            parsePreservations((Element) preservationsList.item(0), dnaSample);
        }
    }

    private void parsePreparations(Element item, DnaSample dnaSample, Set<CdmBase> entitiesToSave) {
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
                    AgentBase<?> preparationAgent = parsePreparationAgent((Element)preparationAgentList.item(0), entitiesToSave);
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

    private void parseSampleDesignations(Element item, DnaSample dnaSample) {
        NodeList sampleDesignationList = item.getElementsByTagName(prefix+"sampleDesignation");
        for(int i=0;i<sampleDesignationList.getLength();i++){
            dnaSample.addIdentifier(sampleDesignationList.item(i).getTextContent(), (IdentifierType)cdmAppController.getTermService().find(IdentifierType.uuidSampleDesignation));
        }
    }

    private AgentBase<?> parsePreparationAgent(Element item, Set<CdmBase> entitiesToSave) {
        AgentBase<?> agentBase = null;
        NodeList personList = item.getElementsByTagName(prefix+"Person");
        if(personList.item(0)!=null && personList.item(0) instanceof Element){
            agentBase = new AbcdPersonParser(prefix, report, cdmAppController).parse((Element)personList.item(0));
            entitiesToSave.add(agentBase);
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