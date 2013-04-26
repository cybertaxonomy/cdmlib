package eu.etaxonomy.cdm.io.taxonx2013;

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


    private NomenclaturalCode nomenclaturalCode;
    private Classification classification;
    private TaxonXImport importer;
    private TaxonXImportState configState;


    public TaxonXXMLFieldGetter(TaxonXDataHolder dataholder, String prefix,Document document){
        this.doc = document;
    }


    public void parseFile(){
        System.out.println("PARSE");
        //taxonx
        Node root = doc.getFirstChild();
        TaxonXModsExtractor modsextractor = new TaxonXModsExtractor(importer);
        TaxonXTreatmentExtractor treatmentextractor = new TaxonXTreatmentExtractor(nomenclaturalCode,classification,importer, configState);

        //taxonHeader, taxonBody
        NodeList nodes = root.getChildNodes();
        for (int i=0; i< nodes.getLength();i++) {
            System.out.println(nodes.item(i).getNodeName());
            if (nodes.item(i).getNodeName().equalsIgnoreCase("tax:taxonxheader")){
                NodeList nodes2 = nodes.item(i).getChildNodes();
                for (int j=0; j< nodes2.getLength();j++){
                    System.out.println("nodes2 : "+nodes2.item(j).getNodeName());
                    if (nodes2.item(j).getNodeName().equalsIgnoreCase("mods:mods")){
                        Reference<?> ref = modsextractor.extractMods(nodes2.item(j));
                        System.out.println("reference: "+ref.getTitleCache());
                        importer.getReferenceService().saveOrUpdate(ref);
                    }
                }
            }
            if (nodes.item(i).getNodeName().equalsIgnoreCase("tax:taxonxBody")){
                NodeList nodes2 = nodes.item(i).getChildNodes();
                for (int j=0; j< nodes2.getLength();j++){
                    if (nodes2.item(j).getNodeName().equalsIgnoreCase("tax:treatment")){
                        List<Object> tosave = new ArrayList<Object>();
                        treatmentextractor.extractTreatment(nodes2.item(j),tosave);
                    }
                }
            }
        }
    }


    public void setNomenclaturalCode(NomenclaturalCode nomenclaturalCode) {
       this.nomenclaturalCode = nomenclaturalCode;

    }


    /**
     * @param classification
     */
    public void setClassification(Classification classification) {
       this.classification=classification;
    }


    /**
     * @param taxonXImport
     */
    public void setImporter(TaxonXImport taxonXImport) {
       this.importer=taxonXImport;

    }


    /**
     * @param state
     */
    public void setConfig(TaxonXImportState state) {
       this.configState=state;

    }


}
