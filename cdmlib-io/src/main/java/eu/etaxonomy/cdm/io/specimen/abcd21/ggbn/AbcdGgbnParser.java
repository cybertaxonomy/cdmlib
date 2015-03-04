// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd21.ggbn;

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

    private final String prefix = "ggbn:";

    /**
     * @param ggbn
     */
    public void parse(NodeList ggbn) {
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
                parseAmplification(amplificationsList);
            }
        }

    }

    /**
     * @param element
     */
    private void parseDnaQuality(Element element) {
        DnaQuality dnaQuality = DnaQuality.NewInstance();

        NodeList purificationMethodList = element.getElementsByTagName(prefix+"purificationMethod");
        NodeList concentrationList = element.getElementsByTagName(prefix+"concentration");
        NodeList ratioOfAbsorbance260_280List = element.getElementsByTagName(prefix+"ratioOfAbsorbance260_280");
        if(ratioOfAbsorbance260_280List.getLength()>0){
            Node ratioOfAbsorbance260_280 = ratioOfAbsorbance260_280List.item(0);
        }
        NodeList ratioOfAbsorbance260_230List = element.getElementsByTagName(prefix+"ratioOfAbsorbance260_230");
        NodeList qualityCheckDateList = element.getElementsByTagName(prefix+"qualityCheckDate");
        NodeList qualityList = element.getElementsByTagName(prefix+"quality");
        NodeList qualityRemarksList = element.getElementsByTagName(prefix+"qualityRemarks");

//        dnaQuality.setPurificationMethod(purificationMethod)
//        dnaQuality.setConcentration(concentration)
//        dnaQuality.setRatioOfAbsorbance260_280(ratioOfAbsorbance260_280)
//        dnaQuality.setRatioOfAbsorbance260_230(ratioOfAbsorbance260_230)
//        dnaQuality.setQualityCheckDate(qualityCheckDate)
//        dnaQuality.setQualityTerm(qualityTerm)
//        dnaQuality.setConcentrationUnit(concentrationUnit)

    }

    /**
     * @param amplificationsList
     */
    private void parseAmplification(NodeList amplificationsList) {
        // TODO Auto-generated method stub

    }

    /**
     * @param gelImageList
     */
    private void parseGelImage(NodeList gelImageList) {
        // TODO Auto-generated method stub

    }

}
