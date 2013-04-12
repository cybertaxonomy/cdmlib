package eu.etaxonomy.cdm.io.taxonx2013;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;

public class TaxonXXMLFieldGetter {

    private static final Logger logger = Logger.getLogger(TaxonXXMLFieldGetter.class);
    private final Document doc;


    private IReferenceService referenceService;
    private IAgentService agentService;
    private NomenclaturalCode nomenclaturalCode;
    private Classification classification;
    private INameService nameService;
    private ITaxonService taxonService;


    public TaxonXXMLFieldGetter(TaxonXDataHolder dataholder, String prefix,Document document){
        this.doc = document;
    }


    public void parseFile(){
        //taxonx
        Node root = doc.getFirstChild();
        TaxonXModsExtractor modsextractor = new TaxonXModsExtractor(agentService);
        TaxonXTreatmentExtractor treatmentextractor = new TaxonXTreatmentExtractor(nomenclaturalCode,classification,nameService,taxonService,referenceService);

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
                        referenceService.saveOrUpdate(ref);
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




    /**
     * @param referenceService
     */
    public void setReferenceService(IReferenceService referenceService) {
        this.referenceService = referenceService;

    }




    /**
     * @param agentService
     */
    public void setAgentService(IAgentService agentService) {
        this.agentService = agentService;

    }


    /**
     * @param nomenclaturalCode
     */
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
     * @param nameService
     */
    public void setNameService(INameService nameService) {
        this.nameService = nameService;

    }


    /**
     * @param taxonService
     */
    public void setTaxonService(ITaxonService taxonService) {
        this.taxonService = taxonService;

    }


}
