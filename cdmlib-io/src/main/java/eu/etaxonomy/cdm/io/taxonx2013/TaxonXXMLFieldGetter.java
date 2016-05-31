package eu.etaxonomy.cdm.io.taxonx2013;

import java.net.URI;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;

public class TaxonXXMLFieldGetter {

    private static final Logger logger = Logger.getLogger(TaxonXXMLFieldGetter.class);
    private final Document doc;


    private final NomenclaturalCode nomenclaturalCode;
    private Classification classification;
    private final TaxonXImport importer;
    private final TaxonXImportState taxonXstate;
    private TaxonXModsExtractor modsextractor;
    private TaxonXTreatmentExtractor treatmentextractor ;

    public TaxonXXMLFieldGetter(TaxonXDataHolder dataholder, String prefix,Document document, TaxonXImport taxonXImport,
            TaxonXImportState taxonXstate, Classification classif, Map<String,Feature> featuresMap){
        this.doc = document;
        this.importer = taxonXImport;
        this.nomenclaturalCode = taxonXstate.getConfig().getNomenclaturalCode();
        this.classification = classif;
        logger.info("CLASSIFICATION "+classification);
        this.taxonXstate=taxonXstate;
        modsextractor = new TaxonXModsExtractor(importer);
        Reference originalSourceUrl =taxonXstate.getConfig().getOriginalSourceURL();
        treatmentextractor = new TaxonXTreatmentExtractor(nomenclaturalCode,classification,importer, taxonXstate,featuresMap,originalSourceUrl );
    }


    /**
     * parse the Mods from the TaxonX file
     *
     *@return the created Reference object
     **/
    public Reference parseMods(){
        //        System.out.println("PARSEMODS");
        //taxonx
        Node root = doc.getFirstChild();


        //taxonHeader, taxonBody
        NodeList nodes = root.getChildNodes();
        Reference ref = null;
        for (int i=0; i< nodes.getLength();i++) {
            //            System.out.println(nodes.item(i).getNodeName());
            if (nodes.item(i).getNodeName().equalsIgnoreCase("tax:taxonxheader")){
                NodeList nodes2 = nodes.item(i).getChildNodes();
                for (int j=0; j< nodes2.getLength();j++){
                    if (nodes2.item(j).getNodeName().equalsIgnoreCase("mods:mods")){
                        ref = modsextractor.extractMods(nodes2.item(j));
                        importer.getReferenceService().saveOrUpdate(ref);
                    }
                }
            }
        }
        if (ref!=null) {
            taxonXstate.getConfig().setClassificationName(ref.getCitation());
        } else {
            taxonXstate.getConfig().setClassificationName("no reference title");
        }
        ref=CdmBase.deproxy(ref, Reference.class);
        return ref;
    }


    /**
     * Foreach treatment section, launches the treatment "extractor"
     * @param ref : the current reference, extracted from the mods
     * @param sourcename: the URI of the TaxonX document
     */
    public void parseTreatment(Reference ref, URI sourceName){
        System.out.println("PARSETREATMENT "+ref);
        //taxonx
        Node root = doc.getFirstChild();
        //taxonHeader, taxonBody
        NodeList nodes = root.getChildNodes();

        for ( int i=0; i< nodes.getLength();i++) {
            //            System.out.println(nodes.item(i).getNodeName());
            if (nodes.item(i).getNodeName().equalsIgnoreCase("tax:taxonxBody")){
                NodeList nodes2 = nodes.item(i).getChildNodes();
                for (int j=0; j< nodes2.getLength();j++){
                    if (nodes2.item(j).getNodeName().equalsIgnoreCase("tax:treatment")){
                        try {
							treatmentextractor.extractTreatment(nodes2.item(j), ref,sourceName);
						} catch (Exception e) {
							logger.error("Unhandled exception occurred in treatment. Treatment not fully imported.");
							e.printStackTrace();
						}
                    }
                }
            }
        }
    }



    /**
     * updates the classification in the treatment extractor
     * @param classification2
     */
    public void updateClassification(Classification classification2) {
        //System.out.println("UPDATECLASSIFICATIONS "+classification2);
        classification=classification2;
        if (treatmentextractor != null) {
            treatmentextractor.updateClassification(classification);
        }

    }


    /**
     * @return
     */
    public Map<String,Feature> getFeaturesUsed() {
       return treatmentextractor.getFeaturesUsed();
    }


}
