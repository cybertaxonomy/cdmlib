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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportState;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.AbcdParseUtility;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.SpecimenImportReport;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTerm;
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
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author pplitzner
 * @since Mar 4, 2015
 *
 */
public class AbcdGgbnParser {

    //DNA Quality term
    private static final String HIGH = "high";
    private static final String MEDIUM = "medium";
    private static final String LOW = "low";
    private static final UUID HIGH_QUALITY_TERM = UUID.fromString("ec443c76-5987-4ec5-a66b-da207f70b47f");
    private static final UUID MEDIUM_QUALITY_TERM = UUID.fromString("2a174892-1246-4807-9022-71ce8639346b");
    private static final UUID LOW_QUALITY_TERM = UUID.fromString("a3bf12ff-b041-425f-bdaa-aa51da65eebc");

    private static final String FORWARD = "forward";

    private static final String REVERSE = "reverse";

    private static final Logger logger = Logger.getLogger(AbcdGgbnParser.class);

    private final String prefix = "ggbn:";

    private final SpecimenImportReport report;

    private final ICdmRepository cdmAppController;

    public AbcdGgbnParser(SpecimenImportReport report, ICdmRepository cdmAppController) {
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
        String purificationMethod = AbcdParseUtility.parseFirstTextContent(purificationMethodList);
        dnaQuality.setPurificationMethod(purificationMethod);

        NodeList concentrationList = element.getElementsByTagName(prefix+"concentration");
        if(concentrationList.getLength()==1){
            Node concentration = concentrationList.item(0);
            dnaQuality.setConcentration(AbcdParseUtility.parseDouble(concentration, report));
            if(concentration instanceof Element){
                String unit = ((Element) concentration).getAttribute("Unit");
                //TODO
//                dnaQuality.setConcentrationUnit(concentrationUnit)
            }
        }

        NodeList ratioOfAbsorbance260_280List = element.getElementsByTagName(prefix+"ratioOfAbsorbance260_280");
        dnaQuality.setRatioOfAbsorbance260_280(AbcdParseUtility.parseFirstDouble(ratioOfAbsorbance260_280List, report));

        NodeList ratioOfAbsorbance260_230List = element.getElementsByTagName(prefix+"ratioOfAbsorbance260_230");
        dnaQuality.setRatioOfAbsorbance260_230(AbcdParseUtility.parseFirstDouble(ratioOfAbsorbance260_230List, report));

        NodeList qualityCheckDateList = element.getElementsByTagName(prefix+"qualityCheckDate");
        if(qualityCheckDateList.item(0)!=null){
            dnaQuality.setQualityCheckDate(AbcdParseUtility.parseFirstDateTime(qualityCheckDateList));
        }

        NodeList qualityList = element.getElementsByTagName(prefix+"quality");
        String quality = AbcdParseUtility.parseFirstTextContent(qualityList);
        if(LOW.equals(quality)){
            dnaQuality.setQualityTerm((OrderedTerm) state.getCdmRepository().getTermService().load(LOW_QUALITY_TERM));
        }
        else if(MEDIUM.equals(quality)){
            dnaQuality.setQualityTerm((OrderedTerm) state.getCdmRepository().getTermService().load(MEDIUM_QUALITY_TERM));
        }
        else if(HIGH.equals(quality)){
            dnaQuality.setQualityTerm((OrderedTerm) state.getCdmRepository().getTermService().load(HIGH_QUALITY_TERM));
        }

        NodeList qualityRemarksList = element.getElementsByTagName(prefix+"qualityRemarks");


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
                        List<DefinedTermBase> markersFound = cdmAppController.getTermService().findByTitleWithRestrictions(DefinedTerm.class, amplificationMarker, MatchMode.EXACT, null, null, null, null, null).getRecords();
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
            //check if amplification already exists (can only be checked after all fields are initialized because comparison is done on the label cache))
            List<Amplification> matchingAmplifications = cdmAppController.getAmplificationService().findByLabelCache(amplification.getLabelCache(), MatchMode.EXACT, null, null, null, null, null).getRecords();
            if(matchingAmplifications.size()==1){
                amplification = matchingAmplifications.iterator().next();
            }
            cdmAppController.getAmplificationService().save(amplification);
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
                Double consensusSequenceLength = AbcdParseUtility.parseFirstDouble(sequencing.getElementsByTagName(prefix+"consensusSequenceLength"), report);
                if(sequence.getConsensusSequence()!=null && consensusSequenceLength!=null){
                    //TODO: this can be different from the actual length in ABCD but not in CDM!
                    sequence.getConsensusSequence().setLength(consensusSequenceLength.intValue());
                }
                //contig file URL
                NodeList consensusSequenceChromatogramFileURIList = sequencing.getElementsByTagName(prefix+"consensusSequenceChromatogramFileURI");
                URI uri = AbcdParseUtility.parseFirstUri(consensusSequenceChromatogramFileURIList, report);
                if (uri.toString().endsWith("fasta")){
                    state.putSequenceDataStableIdentifier(uri);
                }else{
                    Media contigFile = Media.NewInstance(uri, null, null, null);
                    sequence.setContigFile(contigFile);
                }
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
                Reference reference = AbcdParseUtility.parseFirstReference(referenceCitationList, cdmAppController);
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
                    singleRead.setPherogram(Media.NewInstance(AbcdParseUtility.parseFirstUri(chromatogramFileURIList, report), null, null, null));
                    NodeList sequencingPrimersList = singleSequencing.getElementsByTagName(prefix+"SequencingPrimers");
                    parseSequencingPrimers(sequencingPrimersList, singleRead, amplification);
                }
            }
        }
    }

    private void parseSequencingPrimers(NodeList sequencingPrimersList, SingleRead singleRead, Amplification amplification) {
        if(sequencingPrimersList.item(0)!=null && sequencingPrimersList.item(0) instanceof Element){
            Element sequencingPrimers = (Element)sequencingPrimersList.item(0);
            NodeList sequencingPrimerList = sequencingPrimers.getElementsByTagName(prefix+"sequencingPrimer");
            for(int i=0;i<sequencingPrimerList.getLength();i++){
                if(sequencingPrimerList.item(i) instanceof Element){
                    Element sequencingPrimer = (Element)sequencingPrimerList.item(i);
                    //primer name
                    String primerName = AbcdParseUtility.parseFirstTextContent(sequencingPrimer.getElementsByTagName(prefix+"primerName"));
                    //check if primer already exists
                    List<Primer> matchingPrimers = cdmAppController.getPrimerService().findByLabel(primerName, MatchMode.EXACT, null, null, null, null, null).getRecords();
                    Primer primer = null;
                    if(matchingPrimers.size()==1){
                        primer = matchingPrimers.iterator().next();
                        return;
                    }
                    else{
                        primer = Primer.NewInstance(null);
                        primer.setLabel(primerName);
                    }
                    singleRead.setPrimer(primer);
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
                    //reference citation
                    NodeList primerReferenceCitationList = sequencingPrimer.getElementsByTagName(prefix+"primerReferenceCitation");
                    String primerReferenceCitation = AbcdParseUtility.parseFirstTextContent(primerReferenceCitationList);
                    Reference reference = AbcdParseUtility.parseFirstReference(primerReferenceCitationList, cdmAppController);
                    primer.setPublishedIn(reference);

                    cdmAppController.getPrimerService().save(primer);
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
