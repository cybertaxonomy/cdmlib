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

import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportReport;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportState;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.AbcdParseUtility;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaQuality;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SequenceDirection;
import eu.etaxonomy.cdm.model.molecular.SequenceString;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author pplitzner
 * @date Mar 4, 2015
 *
 */
public class AbcdGgbnParser {

    private static final String FORWARD = "forward";

    private static final String REVERSE = "reverse";

    private static final Logger logger = Logger.getLogger(AbcdGgbnParser.class);

    private final String prefix = "ggbn:";

    private final Abcd206ImportReport report;

    private final ICdmApplicationConfiguration cdmAppController;

    public AbcdGgbnParser(Abcd206ImportReport report, ICdmApplicationConfiguration cdmAppController) {
        this.report = report;
        this.cdmAppController = cdmAppController;
    }

    public DnaSample parse(NodeList ggbn, DnaSample dnaSample, Abcd206ImportState state) {

        for(int i=0;i<ggbn.getLength();i++){
            Node item = ggbn.item(i);
            if(item instanceof Element){
                Element element = (Element) item;
                NodeList methodDeterminationConcentrationAndRatiosList = element.getElementsByTagName(prefix+"methodDeterminationConcentrationAndRatios");
                NodeList volumeList = element.getElementsByTagName(prefix+"volume");
                NodeList weightList = element.getElementsByTagName(prefix+"weight");
                NodeList methodDeterminationWeightList = element.getElementsByTagName(prefix+"methodDeterminationWeight");
                NodeList DNADNAHybridizationList = element.getElementsByTagName(prefix+"DNADNAHybridization");
                NodeList DNAMeltingPointList = element.getElementsByTagName(prefix+"DNAMeltingPoint");
                NodeList estimatedSizeList = element.getElementsByTagName(prefix+"estimated_size");
                NodeList poolDnaExtractsList = element.getElementsByTagName(prefix+"pool_dna_extracts");
                NodeList gelImageList = element.getElementsByTagName(prefix+"gelImage");
                NodeList amplificationsList = element.getElementsByTagName(prefix+"Amplifications");

                dnaSample.setDnaQuality(parseDnaQuality(element, state));

                parseGelImage(gelImageList, state);
                parseAmplifications(amplificationsList, dnaSample, state);
            }
        }
        return dnaSample;
    }

    private DnaQuality parseDnaQuality(Element element, Abcd206ImportState state) {
        DnaQuality dnaQuality = DnaQuality.NewInstance();

        NodeList purificationMethodList = element.getElementsByTagName(prefix+"purificationMethod");
//        dnaQuality.setPurificationMethod(purificationMethod)

        NodeList concentrationList = element.getElementsByTagName(prefix+"concentration");
        if(concentrationList.getLength()==1){
            Node concentration = concentrationList.item(0);
            dnaQuality.setConcentration(AbcdParseUtility.parseDouble(concentration));
            if(concentration instanceof Element){
                String unit = ((Element) concentration).getAttribute("Unit");
//                dnaQuality.setConcentrationUnit(concentrationUnit)
            }
        }

        NodeList ratioOfAbsorbance260_280List = element.getElementsByTagName(prefix+"ratioOfAbsorbance260_280");
        dnaQuality.setRatioOfAbsorbance260_280(AbcdParseUtility.parseFirstDouble(ratioOfAbsorbance260_280List));

        NodeList ratioOfAbsorbance260_230List = element.getElementsByTagName(prefix+"ratioOfAbsorbance260_230");
        dnaQuality.setRatioOfAbsorbance260_230(AbcdParseUtility.parseFirstDouble(ratioOfAbsorbance260_230List));

        NodeList qualityCheckDateList = element.getElementsByTagName(prefix+"qualityCheckDate");
        if(qualityCheckDateList.item(0)!=null){
            dnaQuality.setQualityCheckDate(AbcdParseUtility.parseFirstDateTime(qualityCheckDateList));
        }

        NodeList qualityList = element.getElementsByTagName(prefix+"quality");
        NodeList qualityRemarksList = element.getElementsByTagName(prefix+"qualityRemarks");

//        dnaQuality.setQualityTerm(qualityTerm)

        return dnaQuality;
    }

    private void parseGelImage(NodeList gelImageList, Abcd206ImportState state) {
        if(gelImageList.item(0)!=null && gelImageList.item(0) instanceof Element){
            Element gelImage = (Element)gelImageList.item(0);
            NodeList fileURIList = gelImage.getElementsByTagName("fileURI");
            NodeList gelVoltageList = gelImage.getElementsByTagName("gelVoltage");
            NodeList gelConcentrationList = gelImage.getElementsByTagName("gelConcentration");
            NodeList gelDurationList = gelImage.getElementsByTagName("gelDuration");
            NodeList gelLadderList = gelImage.getElementsByTagName("gelLadder");
            NodeList gelStainList = gelImage.getElementsByTagName("gelStain");
            NodeList gelRemarksList = gelImage.getElementsByTagName("gelRemarks");

        }

    }

    private void parseAmplifications(NodeList amplificationsList, DnaSample dnaSample, Abcd206ImportState state) {
        if(amplificationsList.item(0)!=null && amplificationsList.item(0) instanceof Element){
            AmplificationResult amplificationResult = AmplificationResult.NewInstance();
            Amplification amplification = Amplification.NewInstance();
            NodeList amplificationList = ((Element) amplificationsList.item(0)).getElementsByTagName(prefix+"amplification");
            for(int i=0;i<amplificationList.getLength();i++){
                if(amplificationList.item(i) instanceof Element){
                    Element amplificationElement = (Element)amplificationList.item(i);
                    NodeList amplificationDateList = amplificationElement.getElementsByTagName(prefix+"amplificationDate");
                    NodeList amplificationStaffList = amplificationElement.getElementsByTagName(prefix+"amplificationStaff");

                    //amplification dna marker
                    NodeList markerList = amplificationElement.getElementsByTagName(prefix+"marker");
                    if(markerList.item(0)!=null){
                        String amplificationMarker = markerList.item(0).getTextContent();
                        DefinedTerm dnaMarker = null;
                        List<DefinedTermBase> markersFound = cdmAppController.getTermService().findByTitle(DefinedTerm.class, amplificationMarker, MatchMode.EXACT, null, null, null, null, null).getRecords();
                        if(markersFound.size()==1){
                            dnaMarker = (DefinedTerm) markersFound.get(0);
                        }
                        else{
                            dnaMarker = DefinedTerm.NewDnaMarkerInstance(amplificationMarker, amplificationMarker, amplificationMarker);
                            cdmAppController.getTermService().saveOrUpdate(dnaMarker);
                        }
                        amplification.setDnaMarker(dnaMarker);
                    }

                    NodeList markerSubfragmentList = amplificationElement.getElementsByTagName(prefix+"markerSubfragment");
                    NodeList amplificationSuccessList = amplificationElement.getElementsByTagName(prefix+"amplificationSuccess");
                    NodeList amplificationSuccessDetailsList = amplificationElement.getElementsByTagName(prefix+"amplificationSuccessDetails");
                    NodeList amplificationMethodList = amplificationElement.getElementsByTagName(prefix+"amplificationMethod");
                    NodeList purificationMethodList = amplificationElement.getElementsByTagName(prefix+"purificationMethod");
                    NodeList libReadsSeqdList = amplificationElement.getElementsByTagName(prefix+"lib_reads_seqd");
                    NodeList libScreenList = amplificationElement.getElementsByTagName(prefix+"lib_screen");
                    NodeList libVectorList = amplificationElement.getElementsByTagName(prefix+"lib_vector");
                    NodeList libConstMethList = amplificationElement.getElementsByTagName(prefix+"lib_const_meth");
                    NodeList plasmidList = amplificationElement.getElementsByTagName(prefix+"plasmid");

                    //consensus sequence
                    NodeList sequencingsList = amplificationElement.getElementsByTagName(prefix+"Sequencings");
                    if(sequencingsList.item(0)!=null && sequencingsList.item(0) instanceof Element){
                        parseAmplificationSequencings((Element)sequencingsList.item(0), amplification, amplificationResult, dnaSample, state);
                    }
                    parseAmplificationPrimers(amplificationElement.getElementsByTagName(prefix+"AmplificationPrimers"));
                }
            }
            amplificationResult.setAmplification(amplification);
            dnaSample.addAmplificationResult(amplificationResult);
        }
    }

    private void parseAmplificationPrimers(NodeList elementsByTagName) {
        // TODO Auto-generated method stub

    }

    private void parseAmplificationSequencings(Element sequencings, Amplification amplification, AmplificationResult amplificationResult, DnaSample dnaSample, Abcd206ImportState state) {
        NodeList sequencingList = sequencings.getElementsByTagName(prefix+"sequencing");
        for(int i=0;i<sequencingList.getLength();i++){
            Sequence sequence = Sequence.NewInstance("");
            dnaSample.addSequence(sequence);
            sequence.setDnaMarker(amplification.getDnaMarker());

            if(sequencingList.item(i) instanceof Element){
                Element sequencing = (Element)sequencingList.item(i);

                //singleSequencings
                NodeList singleSequencingsList = sequencing.getElementsByTagName(prefix+"SingleSequencings");
                parseSingleSequencings(singleSequencingsList, amplification, amplificationResult, sequence);
                //Consensus sequence
                NodeList consensusSequencesList = sequencing.getElementsByTagName(prefix+"consensusSequence");
                sequence.setConsensusSequence(SequenceString.NewInstance(AbcdParseUtility.parseFirstTextContent(consensusSequencesList)));
                //sequence length
                NodeList consensusSequencesLengthList = sequencing.getElementsByTagName(prefix+"consensusSequenceLength");
                if(sequence.getConsensusSequence()!=null){
                    //TODO: this can be different from the actual length in ABCD but not in CDM!
                    sequence.getConsensusSequence().setLength(AbcdParseUtility.parseFirstDouble(consensusSequencesLengthList).intValue());
                }
                //contig file URL
                NodeList consensusSequenceChromatogramFileURIList = sequencing.getElementsByTagName(prefix+"consensusSequenceChromatogramFileURI");
                URI uri = AbcdParseUtility.parseFirstUri(consensusSequenceChromatogramFileURIList);
                Media contigFile = Media.NewInstance(uri, null, null, null);
                sequence.setContigFile(contigFile);

                //genetic Accession
                NodeList geneticAccessionList = sequencing.getElementsByTagName(prefix+"geneticAccession");
                parseGeneticAccession(geneticAccessionList, sequence);

                //references
                NodeList referencesList = sequencing.getElementsByTagName(prefix+"References");
                if(referencesList.item(0)!=null && referencesList.item(0) instanceof Element){
                    parseSequencingReferences((Element) referencesList.item(0), sequence);
                }
            }
        }
//        if(nodeList.item(0)!=null && nodeList.item(0) instanceof Element){
//        NodeList plasmidList = amplificationElement.getElementsByTagName(prefix+"plasmid");

    }

    private void parseSequencingReferences(Element references, Sequence sequence) {
        NodeList referenceList = references.getElementsByTagName(prefix+"Reference");
        for(int i=0;i<referenceList.getLength();i++){
            if(referenceList.item(i) instanceof Element){
                Element element = (Element)referenceList.item(i);
                NodeList referenceCitationList = element.getElementsByTagName(prefix+"ReferenceCitation");
                String referenceCitation = AbcdParseUtility.parseFirstTextContent(referenceCitationList);
                List<Reference> matchedReferences = cdmAppController.getReferenceService().findByTitle(Reference.class, referenceCitation, MatchMode.EXACT, null, null, null, null, null).getRecords();
                Reference<?> reference;
                if(matchedReferences.size()==1){
                    reference = matchedReferences.iterator().next();
                }
                else{
                    reference = ReferenceFactory.newGeneric();
                    reference.setTitle(referenceCitation);
                    cdmAppController.getReferenceService().saveOrUpdate(reference);
                }
                sequence.addCitation(reference);
            }
        }

    }

    private void parseSingleSequencings(NodeList singleSequencingsList, Amplification amplification, AmplificationResult amplificationResult, Sequence sequence) {
        if(singleSequencingsList.item(0)!=null && singleSequencingsList.item(0) instanceof Element){
            Element singleSequencings = (Element)singleSequencingsList.item(0);
            NodeList singleSequencingList = singleSequencings.getElementsByTagName(prefix+"singleSequencing");
            for(int i=0;i<singleSequencingList.getLength();i++){
                //single read
                SingleRead singleRead = SingleRead.NewInstance();
                SingleReadAlignment.NewInstance(sequence, singleRead);
                amplificationResult.addSingleRead(singleRead);
                if(singleSequencingList.item(i) instanceof Element){
                    Element singleSequencing = (Element)singleSequencingList.item(i);
                    NodeList sequencingDirectionList = singleSequencing.getElementsByTagName(prefix+"sequencingDirection");
                    //read direction
                    String singleReadDirection = AbcdParseUtility.parseFirstTextContent(sequencingDirectionList);
                    if(singleReadDirection.equals(FORWARD)){
                        singleRead.setDirection(SequenceDirection.Forward);
                    }
                    else if(singleReadDirection.equals(REVERSE)){
                        singleRead.setDirection(SequenceDirection.Reverse);
                    }
                    //read pherogram URI
                    NodeList chromatogramFileURIList = singleSequencing.getElementsByTagName(prefix+"chromatogramFileURI");
                    singleRead.setPherogram(Media.NewInstance(AbcdParseUtility.parseFirstUri(chromatogramFileURIList), null, null, null));
                    NodeList sequencingPrimersList = singleSequencing.getElementsByTagName(prefix+"SequencingPrimers");
                    parseSequencingPrimers(sequencingPrimersList, singleRead, amplification);
                }
            }
        }
    }

    /**
     * @param sequencingPrimersList
     * @param singleRead
     * @param amplification
     */
    private void parseSequencingPrimers(NodeList sequencingPrimersList, SingleRead singleRead, Amplification amplification) {
        if(sequencingPrimersList.item(0)!=null && sequencingPrimersList.item(0) instanceof Element){
            Element sequencingPrimers = (Element)sequencingPrimersList.item(0);
            NodeList sequencingPrimerList = sequencingPrimers.getElementsByTagName(prefix+"sequencingPrimer");
            for(int i=0;i<sequencingPrimerList.getLength();i++){
                Primer primer = Primer.NewInstance(null);
                singleRead.setPrimer(primer);
                if(sequencingPrimerList.item(i) instanceof Element){
                    Element sequencingPrimer = (Element)sequencingPrimerList.item(i);
                    //primer sequence
                    NodeList primerSequenceList = sequencingPrimer.getElementsByTagName(prefix+"primerSequence");
                    primer.setSequence(SequenceString.NewInstance(AbcdParseUtility.parseFirstTextContent(primerSequenceList)));
                    //primer direction
                    String direction = parseFirstAttribute("Direction", primerSequenceList);
                    if(direction!=null){
                        if(direction.equals(FORWARD)){
                            amplification.setForwardPrimer(primer);
                        }
                        else if(direction.equals(REVERSE)){
                            amplification.setReversePrimer(primer);
                        }
                    }
                    //primer name
                    NodeList primerNameList = sequencingPrimer.getElementsByTagName(prefix+"primerName");
                    primer.setLabel(AbcdParseUtility.parseFirstTextContent(primerNameList));
                    //reference citation
                    NodeList primerReferenceCitationList = sequencingPrimer.getElementsByTagName(prefix+"primerReferenceCitation");
                    String primerReferenceCitation = AbcdParseUtility.parseFirstTextContent(primerReferenceCitationList);
                    List<Reference> matchingReferences = cdmAppController.getReferenceService().findByTitle(Reference.class, primerReferenceCitation, MatchMode.EXACT, null, null, null, null, null).getRecords();
                    Reference<?> primerReference;
                    if(matchingReferences.size()==1){
                        primerReference = matchingReferences.iterator().next();
                    }
                    else{
                        primerReference = ReferenceFactory.newGeneric();
                        primerReference.setTitle(primerReferenceCitation);
                        cdmAppController.getReferenceService().saveOrUpdate(primerReference);
                    }
                    primer.setPublishedIn(primerReference);
                }
            }
        }
    }

    private String parseFirstAttribute(String attributeName, NodeList nodeList) {
        String attribute = null;
        if(nodeList.item(0)!=null && nodeList.item(0) instanceof Element){
            Element element = (Element)nodeList.item(0);
            attribute = element.getAttribute(attributeName);
        }
        return attribute;
    }

    private void parseGeneticAccession(NodeList geneticAccessionList, Sequence sequence) {
        for(int i=0;i<geneticAccessionList.getLength();i++){
            if(geneticAccessionList.item(i) instanceof Element){
                //genetic accession number
                NodeList geneticAccessionNumberList = ((Element)geneticAccessionList.item(i)).getElementsByTagName(prefix+"geneticAccessionNumber");
                sequence.setGeneticAccessionNumber(AbcdParseUtility.parseFirstTextContent(geneticAccessionNumberList));

                //genetic accession number uri
                NodeList geneticAccessionNumberUriList = ((Element)geneticAccessionList.item(i)).getElementsByTagName(prefix+"geneticAccessionNumberURI");
                //TODO: this is different from the geneticAccessionNumber

            }
        }
    }

}
