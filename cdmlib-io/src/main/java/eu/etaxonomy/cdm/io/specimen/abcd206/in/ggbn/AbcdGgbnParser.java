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

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.model.molecular.DnaQuality;
import eu.etaxonomy.cdm.model.molecular.DnaSample;

/**
 * @author pplitzner
 * @date Mar 4, 2015
 *
 */
public class AbcdGgbnParser {

    private static final Logger logger = Logger.getLogger(AbcdGgbnParser.class);

    private final String prefix = "ggbn:";

    /**
     * @param ggbn
     * @return
     */
    public DnaSample parse(NodeList ggbn) {
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

                parseDnaQuality(element);

                parseGelImage(gelImageList);
                parseAmplifications(amplificationsList);
            }
        }
        return dnaSample;
    }

    /**
     * @param element
     */
    private void parseDnaQuality(Element element) {
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

    }

    /**
     * @param gelImageList
     */
    private void parseGelImage(NodeList gelImageList) {
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

    /**
     * @param amplificationsList
     */
    private void parseAmplifications(NodeList amplificationsList) {
        if(amplificationsList.item(0)!=null && amplificationsList.item(0) instanceof Element){
            NodeList amplificationList = ((Element) amplificationsList.item(0)).getElementsByTagName(prefix+"amplification");
            for(int i=0;i<amplificationList.getLength();i++){
                if(amplificationList.item(i) instanceof Element){
                    Element amplification = (Element)amplificationList.item(i);
                    NodeList amplificationDateList = amplification.getElementsByTagName(prefix+"amplificationDate");
                    NodeList amplificationStaffList = amplification.getElementsByTagName(prefix+"amplificationStaff");
                    NodeList markerList = amplification.getElementsByTagName(prefix+"marker");
                    NodeList markerSubfragmentList = amplification.getElementsByTagName(prefix+"markerSubfragment");
                    NodeList amplificationSuccessList = amplification.getElementsByTagName(prefix+"amplificationSuccess");
                    NodeList amplificationSuccessDetailsList = amplification.getElementsByTagName(prefix+"amplificationSuccessDetails");
                    NodeList amplificationMethodList = amplification.getElementsByTagName(prefix+"amplificationMethod");
                    NodeList purificationMethodList = amplification.getElementsByTagName(prefix+"purificationMethod");
                    NodeList libReadsSeqdList = amplification.getElementsByTagName(prefix+"lib_reads_seqd");
                    NodeList libScreenList = amplification.getElementsByTagName(prefix+"lib_screen");
                    NodeList libVectorList = amplification.getElementsByTagName(prefix+"lib_vector");
                    NodeList libConstMethList = amplification.getElementsByTagName(prefix+"lib_const_meth");
                    NodeList plasmidList = amplification.getElementsByTagName(prefix+"plasmid");

                    parseAmplificationSequencing(amplification.getElementsByTagName(prefix+"Sequencings"));
                    parseAmplificationPrimers(amplification.getElementsByTagName(prefix+"AmplificationPrimers"));
                }
            }
        }
    }

    /**
     * @param elementsByTagName
     */
    private void parseAmplificationPrimers(NodeList elementsByTagName) {
        // TODO Auto-generated method stub

    }

    /**
     * @param nodeList
     *
     */
    private void parseAmplificationSequencing(NodeList nodeList) {
        // TODO Auto-generated method stub

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
