// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in.ggbn;

import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportReport;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportState;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaQuality;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SequenceString;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author pplitzner
 * @date Mar 4, 2015
 *
 */
public class AbcdGgbnParser {

    private static final Logger logger = Logger.getLogger(AbcdGgbnParser.class);

    private final String prefix = "ggbn:";

    private final Abcd206ImportReport report;

    private final ICdmApplicationConfiguration cdmAppController;

    public AbcdGgbnParser(Abcd206ImportReport report, ICdmApplicationConfiguration cdmAppController) {
        this.report = report;
        this.cdmAppController = cdmAppController;
    }

    public DnaSample parse(NodeList ggbn, Abcd206ImportState state) {
        DnaSample dnaSample = DnaSample.NewInstance();

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

//                dnaSample.setDnaQuality(parseDnaQuality(element, state));

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
            dnaQuality.setConcentration(parseDouble(concentration));
            if(concentration instanceof Element){
                String unit = ((Element) concentration).getAttribute("Unit");
//                dnaQuality.setConcentrationUnit(concentrationUnit)
            }
        }

        NodeList ratioOfAbsorbance260_280List = element.getElementsByTagName(prefix+"ratioOfAbsorbance260_280");
        dnaQuality.setRatioOfAbsorbance260_280(parseFirstNodeDouble(ratioOfAbsorbance260_280List));

        NodeList ratioOfAbsorbance260_230List = element.getElementsByTagName(prefix+"ratioOfAbsorbance260_230");
        dnaQuality.setRatioOfAbsorbance260_230(parseFirstNodeDouble(ratioOfAbsorbance260_230List));

        NodeList qualityCheckDateList = element.getElementsByTagName(prefix+"qualityCheckDate");
        if(qualityCheckDateList.item(0)!=null){
            dnaQuality.setQualityCheckDate(DateTime.parse(qualityCheckDateList.item(0).getTextContent()));
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

                    NodeList sequencingsList = amplificationElement.getElementsByTagName(prefix+"Sequencings");
                    if(sequencingsList.item(0)!=null && sequencingsList.item(0) instanceof Element){
                        parseAmplificationSequencings((Element)sequencingsList.item(0), dnaSample, state);
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

    private void parseAmplificationSequencings(Element sequencings, DnaSample dnaSample, Abcd206ImportState state) {
        NodeList sequencingList = sequencings.getElementsByTagName(prefix+"sequencing");
        for(int i=0;i<sequencingList.getLength();i++){
            Sequence sequence = Sequence.NewInstance("");
            dnaSample.addSequence(sequence);

            if(sequencingList.item(i) instanceof Element){
                Element sequencing = (Element)sequencingList.item(i);
                //Consensus sequence
                NodeList consensusSequencesList = sequencing.getElementsByTagName(prefix+"consensusSequence");
                if(consensusSequencesList.item(0)!=null){
                    String sequenceString = consensusSequencesList.item(0).getTextContent();
                    sequenceString = sequenceString.replaceAll("\n", "");
                    sequenceString = sequenceString.replaceAll("( )+", " ");
                    sequence.setConsensusSequence(SequenceString.NewInstance(sequenceString));
                }
                //sequence length
                NodeList consensusSequencesLengthList = sequencing.getElementsByTagName(prefix+"consensusSequenceLength");
                if(sequence.getConsensusSequence()!=null){
                    sequence.getConsensusSequence().setLength(parseFirstNodeDouble(consensusSequencesLengthList).intValue());
                }
                //contig file URL
                NodeList consensusSequenceChromatogramFileURIList = sequencing.getElementsByTagName(prefix+"consensusSequenceChromatogramFileURI");
                URI uri = parseFirstUri(consensusSequenceChromatogramFileURIList);
                Media contigFile = Media.NewInstance(uri, null, null, null);
                sequence.setContigFile(contigFile);
            }
        }
//        if(nodeList.item(0)!=null && nodeList.item(0) instanceof Element){
//        NodeList plasmidList = amplificationElement.getElementsByTagName(prefix+"plasmid");

    }

    private URI parseFirstUri(NodeList nodeList){
        URI uri = null;
        if(nodeList.item(0)!=null){
            String textContent = nodeList.item(0).getTextContent();
            if(textContent!=null){
                try {
                    uri = URI.create(textContent);
                } catch (IllegalArgumentException e) {
                    //nothing
                }
            }
        }
        return uri;
    }

    private String parseFirstTextContent(NodeList nodeList){
        String string = null;
        if(nodeList.getLength()>0){
            string = nodeList.item(0).getTextContent();
        }
        return string;
    }

    private Double parseFirstNodeDouble(NodeList nodeList){
        if(nodeList.getLength()>0){
            return parseDouble(nodeList.item(0));
        }
        return null;
    }

    private Double parseDouble(Node node){
        String message = "Could not parse double value for node " + node.getNodeName();
        Double doubleValue = null;
        try{
            String textContent = node.getTextContent();
            //remove 1000 dots
            textContent = textContent.replace(".","");
            //convert commmas
            textContent = textContent.replace(",",".");
            doubleValue = Double.parseDouble(textContent);
        } catch (NullPointerException npe){
            logger.error(message, npe);
        } catch (NumberFormatException nfe){
            logger.error(message, nfe);
        }
        return doubleValue;
    }

}
