package eu.etaxonomy.cdm.io.taxonx2013;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    TaxonXModsExtractor modsextractor;
    TaxonXTreatmentExtractor treatmentextractor ;

    public TaxonXXMLFieldGetter(TaxonXDataHolder dataholder, String prefix,Document document, TaxonXImport taxonXImport, TaxonXImportState taxonXstate, Classification classif){
        this.doc = document;
        this.importer = taxonXImport;
        this.nomenclaturalCode = taxonXstate.getConfig().getNomenclaturalCode();
        this.classification = classif;
        logger.info("CLASSIFICATION "+classification);
        this.taxonXstate=taxonXstate;
        modsextractor = new TaxonXModsExtractor(importer);
        treatmentextractor = new TaxonXTreatmentExtractor(nomenclaturalCode,classification,importer, taxonXstate);
    }



    public Reference<?> parseMods(){
//        System.out.println("PARSEMODS");
        //taxonx
        Node root = doc.getFirstChild();


        //taxonHeader, taxonBody
        NodeList nodes = root.getChildNodes();
        Reference<?> ref = null;
        for (int i=0; i< nodes.getLength();i++) {
//            System.out.println(nodes.item(i).getNodeName());
            if (nodes.item(i).getNodeName().equalsIgnoreCase("tax:taxonxheader")){
                NodeList nodes2 = nodes.item(i).getChildNodes();
                for (int j=0; j< nodes2.getLength();j++){
//                    System.out.println("nodes2 : "+nodes2.item(j).getNodeName());
                    if (nodes2.item(j).getNodeName().equalsIgnoreCase("mods:mods")){
                        ref = modsextractor.extractMods(nodes2.item(j));
//                        System.out.println("reference: "+ref.getTitleCache());
                        importer.getReferenceService().saveOrUpdate(ref);
                    }
                }
            }
        }
        if (ref!=null) {
            taxonXstate.getConfig().setClassificationName(ref.getTitleCache());
        } else {
            taxonXstate.getConfig().setClassificationName( "no reference title");
        }
        return ref;
    }


    public void parseTreatment(Reference<?> ref, URI sourceName){
        System.out.println("PARSETREATMENT "+ref);
        //taxonx
        Node root = doc.getFirstChild();
        //taxonHeader, taxonBody
        NodeList nodes = root.getChildNodes();

        for (int i=0; i< nodes.getLength();i++) {
//            System.out.println(nodes.item(i).getNodeName());
            if (nodes.item(i).getNodeName().equalsIgnoreCase("tax:taxonxBody")){
                NodeList nodes2 = nodes.item(i).getChildNodes();
                for (int j=0; j< nodes2.getLength();j++){
                    if (nodes2.item(j).getNodeName().equalsIgnoreCase("tax:treatment")){
                        List<Object> tosave = new ArrayList<Object>();
                        treatmentextractor.extractTreatment(nodes2.item(j),tosave,ref,sourceName);
                    }
                }
            }
        }
    }



    /**
     * @param classification2
     */
    public void updateClassification(Classification classification2) {
       this.classification=classification2;
       if (treatmentextractor != null) {
        treatmentextractor.updateClassification(classification);
    }

    }


}
