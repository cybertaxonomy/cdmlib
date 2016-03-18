package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HomotypicalGroupComparator;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.HomotypicGroupTaxonComparator;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.query.MatchMode;


@Component
public class CsvNameExport extends CsvNameExportBase {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     *
     */

    private static final Logger logger = Logger.getLogger(CsvNameExport.class);

    public CsvNameExport() {
        super();
        this.ioName = this.getClass().getSimpleName();
    }

    @Override
    protected void doInvoke(CsvNameExportState state) {
        CsvNameExportConfigurator config = state.getConfig();


        PrintWriter writer = null;

        try {

            switch(config.getTarget()) {
            case FILE :
                OutputStream os = new FileOutputStream(config.getDestination());
                os.write(239);
                os.write(187);
                os.write(191);
                writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
                break;
            case EXPORT_DATA :
                exportStream = new ByteArrayOutputStream();
                writer = new PrintWriter(exportStream);
                break;
            default:
                break;

            }

            List<HashMap<String, String>> result;
            if (config.isNamesOnly()){
                txStatus = startTransaction();
                result = getNameService().getNameRecords();
            } else {
                result = getRecordsForPrintPub(state);
            }
            NameRecord nameRecord;
            int count = 0;
            boolean isFirst = true;
            for (HashMap<String,String> record:result){
                if (count > 0){
                    isFirst = false;
                }
                count++;
                nameRecord = new NameRecord(record, isFirst);
                nameRecord.print(writer, config);

            }
            writer.flush();

            writer.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        commitTransaction(txStatus);
        return;


    }

    @Override
    protected boolean doCheck(CsvNameExportState state) {
        boolean result = true;
        logger.warn("No check implemented for " + this.ioName);
        return result;
    }

    @Override
    protected boolean isIgnore(CsvNameExportState state) {
        return false;
    }


    public List<HashMap<String,String>> getRecordsForPrintPub(CsvNameExportState state){
        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("childNodes");
        txStatus = startTransaction();
        Classification classification = getClassificationService().load(state.getConfig().getClassificationUUID());
        TaxonNode rootNode = classification.getRootNode();
        rootNode = getTaxonNodeService().load(rootNode.getUuid(), propertyPaths);
        Set<UUID> childrenUuids = new HashSet<UUID>();

        for (TaxonNode child: rootNode.getChildNodes()){
            child = HibernateProxyHelper.deproxy(child, TaxonNode.class);
            childrenUuids.add(child.getUuid());
        }
        List<UUID> familyNodes = new ArrayList<UUID>(childrenUuids);
        childrenUuids.clear();
        List<TaxonNode> genusNodes = new ArrayList<TaxonNode>();
        TaxonNode familyNode;
        for (UUID familyNodeUUID: familyNodes){
            familyNode = getTaxonNodeService().find(familyNodeUUID);
            for (TaxonNode child: familyNode.getChildNodes()){
                child = HibernateProxyHelper.deproxy(child, TaxonNode.class);
                childrenUuids.add(child.getUuid());
            }

            genusNodes = getTaxonNodeService().find(childrenUuids);
            refreshTransaction();
        }

        List<HashMap<String,String>> nameRecords = new ArrayList<HashMap<String,String>>();
        HashMap<String,String> nameRecord = new HashMap<String,String>();

        Taxon taxon;
        BotanicalName name;
        BotanicalName typeName;
        TextData textElement;
        NameTypeDesignation typeDes;
        for(TaxonNode genusNode : genusNodes)   {

            nameRecords.add(createNewRecord(genusNode, state));

            refreshTransaction();

        }

        return nameRecords;

    }




    private String createSynonymNameString(BotanicalName synonymName) {
        String synonymString = null;

        synonymString = synonymName.generateFullTitle();

        if (synonymName.getGenusOrUninomial() != null){
            synonymString = synonymString.replaceAll(synonymName.getGenusOrUninomial(), "<i>"+ synonymName.getGenusOrUninomial() + "</i>");
        }
        if (synonymName.getInfraGenericEpithet() != null){
            synonymString = synonymString.replaceAll(synonymName.getInfraGenericEpithet(),  "<i>"+ synonymName.getInfraGenericEpithet() + "</i>");
        }

        return synonymString;
    }



    private List<TaxonNode> getGenusNodes (UUID classificationUUID){
        Classification classification = getClassificationService().load(classificationUUID);
        TaxonNode rootNode = classification.getRootNode();
        rootNode = getTaxonNodeService().load(rootNode.getUuid());
        Set<UUID> childrenUuids = new HashSet<UUID>();

        for (TaxonNode child: rootNode.getChildNodes()){
            child = HibernateProxyHelper.deproxy(child, TaxonNode.class);
            childrenUuids.add(child.getUuid());
        }

        List<TaxonNode> familyNodes = getTaxonNodeService().find(childrenUuids);
        childrenUuids.clear();
        List<TaxonNode> genusNodes = new ArrayList<TaxonNode>();
        for (TaxonNode familyNode: familyNodes){
            for (TaxonNode child: familyNode.getChildNodes()){
                child = HibernateProxyHelper.deproxy(child, TaxonNode.class);
                childrenUuids.add(child.getUuid());
            }

            genusNodes = getTaxonNodeService().find(childrenUuids);
        }
        return genusNodes;
    }

    private TaxonNode getFamilyName(TaxonNode node){

        Rank nodeRank = node.getTaxon().getName().getRank();
        if (nodeRank.isKindOf(Rank.FAMILY())){
            return node;

        }else if (nodeRank.isHigher(Rank.FAMILY())){
            return null;
        } else {
            return node.getAncestorOfRank(Rank.FAMILY());
        }
    }

    private void extractDescriptions(HashMap<String, String> nameRecord, Taxon taxon, Feature feature, String columnName){
        StringBuffer descriptionsString = new StringBuffer();
        TextData textElement;
        for (DescriptionBase<?> descriptionBase: taxon.getDescriptions()){
            Set<DescriptionElementBase> elements = descriptionBase.getElements();
            for (DescriptionElementBase element: elements){
                if (element.getFeature().equals(feature)){
                    if (element instanceof TextData){
                        textElement = HibernateProxyHelper.deproxy(element, TextData.class);
                        descriptionsString.append(textElement.getText(Language.ENGLISH()));

                    }

                }
            }


        }

        nameRecord.put(columnName, descriptionsString.toString());
    }

    private Feature getNotesFeature(CsvNameExportState state){
        if (state.getNotesFeature() != null){
            return state.getNotesFeature();
        } else{
            Pager<DefinedTermBase> notesFeature = getTermService().findByTitle(Feature.class, "Notes" ,MatchMode.EXACT, null, null, null, null, null);
            if (notesFeature.getRecords().size() == 0){
                return null;
            }else{
                DefinedTermBase feature=  notesFeature.getRecords().iterator().next();
                if (feature instanceof Feature){
                    state.setNotesFeature((Feature)feature);
                    return (Feature) feature;
                } else{
                    return null;
                }
            }
        }

    }

    private HashMap<String, String> createNewRecord(TaxonNode genusNode, CsvNameExportState state){
        HashMap<String, String> nameRecord = new HashMap<String,String>();
        nameRecord.put("classification", genusNode.getClassification().getTitleCache());
        TaxonNode familyNode = getTaxonNodeService().load(genusNode.getParent().getUuid());
        familyNode = HibernateProxyHelper.deproxy(familyNode, TaxonNode.class);
        familyNode.getTaxon().setProtectedTitleCache(true);
        nameRecord.put("familyTaxon", familyNode.getTaxon().getTitleCache());

        Taxon taxon = (Taxon)getTaxonService().load(familyNode.getTaxon().getUuid());
        taxon = HibernateProxyHelper.deproxy(taxon, Taxon.class);
        //if publish flag is set

        //  if (taxon.isPublish()){
        BotanicalName name = HibernateProxyHelper.deproxy(taxon.getName(), BotanicalName.class);
        nameRecord.put("familyName", name.getNameCache());
        extractDescriptions(nameRecord, taxon, Feature.INTRODUCTION(), "descriptionsFam");

        if (genusNode.getTaxon() == null){
            nameRecord.put("genusTaxon", null);
            return nameRecord;
        }else{
            taxon = (Taxon) getTaxonService().load(genusNode.getTaxon().getUuid());
            taxon = HibernateProxyHelper.deproxy(taxon, Taxon.class);
            nameRecord.put("genusTaxon", taxon.getTitleCache());

            if (taxon.getSec()!= null){
                nameRecord.put("secRef", taxon.getSec().getTitleCache());
            }else{
                nameRecord.put("secRef", null);
            }
        }

        //  if (taxon.isPublish()){



        name = HibernateProxyHelper.deproxy(getNameService().load(taxon.getName().getUuid()), BotanicalName.class);
        nameRecord.put("genusName",name.getTitleCache());
        nameRecord.put("nameId", String.valueOf(name.getId()));
        nameRecord.put("nameCache", name.getNameCache());
        nameRecord.put("titleName", name.getTitleCache());
        if (name.getNomenclaturalReference() != null){
            nameRecord.put("NomRefTitleCache", name.getNomenclaturalReference().getTitleCache());
        } else{
            nameRecord.put("NomRefTitleCache",null);
        }
        nameRecord.put("fullName", name.getNameCache());
        nameRecord.put("fullTitleCache",  name.getFullTitleCache());
        Set<TypeDesignationBase> typeDesSet =  name.getTypeDesignations();
        Iterator<TypeDesignationBase> it = typeDesSet.iterator();
        String typeNameString = NOT_DESIGNATED;
        String statusString = null;
        if (it.hasNext()){
            NameTypeDesignation typeDes = HibernateProxyHelper.deproxy(it.next(), NameTypeDesignation.class);


            BotanicalName typeName =  HibernateProxyHelper.deproxy(typeDes.getTypeName(), BotanicalName.class);
            if (typeName != null){

                typeNameString = "<i>" + typeName.getNameCache() +"</i> "  + typeName.getAuthorshipCache();
                if (typeDes.getTypeStatus() != null){
                    NameTypeDesignationStatus status = HibernateProxyHelper.deproxy(typeDes.getTypeStatus(), NameTypeDesignationStatus.class);
                    statusString = status.getTitleCache();
                }
            }

        }
        nameRecord.put("typeName", typeNameString);
        StringBuffer homotypicalSynonyms = new StringBuffer();
        TreeMap<HomotypicalGroup,List<Synonym>> heterotypicSynonymsList = new TreeMap<HomotypicalGroup,List<Synonym>>(new HomotypicalGroupComparator());
        List<Synonym> homotypicSynonymsList = new ArrayList<Synonym>();
        StringBuffer heterotypicalSynonyms = new StringBuffer();
        List<Synonym> homotypicSynonyms;

        HomotypicalGroup group;
        BotanicalName synonymName;
        String doubtfulTitleCache;
        for (SynonymRelationship synRel: taxon.getSynonymRelations()){
            synonymName = HibernateProxyHelper.deproxy(synRel.getSynonym().getName(), BotanicalName.class);
            if (synRel.getType().equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())){
                group = HibernateProxyHelper.deproxy(synonymName.getHomotypicalGroup(), HomotypicalGroup.class);
                synonymName.generateFullTitle();
                if (synRel.getSynonym().isDoubtful()){
                    doubtfulTitleCache = "?" + synonymName.getFullTitleCache();
                    synonymName.setFullTitleCache(doubtfulTitleCache, true);
                }
                if (heterotypicSynonymsList.containsKey(group)){
                    heterotypicSynonymsList.get(group).add(synRel.getSynonym());
                }else{
                    homotypicSynonyms = new ArrayList<Synonym>();
                    homotypicSynonyms.add(synRel.getSynonym());
                    heterotypicSynonymsList.put(group, homotypicSynonyms);
                    homotypicSynonyms= null;
                }
            } else{
                synonymName.generateFullTitle();
                homotypicSynonymsList.add(synRel.getSynonym());
            }
        }



        String synonymString;
        boolean first = true;

        for (List<Synonym> list: heterotypicSynonymsList.values()){
            Collections.sort(list, new HomotypicGroupTaxonComparator(null));
            first = true;
            for (TaxonBase<?> synonym : list){
                if (first){
                    heterotypicalSynonyms.append(" <heterotypic> ");
                }else{
                    heterotypicalSynonyms.append(" <homonym> ");
                }
                first = false;
                synonymName = HibernateProxyHelper.deproxy(synonym.getName(), BotanicalName.class);
                synonymString = createSynonymNameString(synonymName);
                heterotypicalSynonyms.append(synonymString);
            }
        }

        first = true;
        Collections.sort(homotypicSynonymsList, new TaxonComparator());
        for (TaxonBase<?> synonym : homotypicSynonymsList){
            if (!first){
                homotypicalSynonyms.append(" <homonym> ");
            }
            first = false;
            synonymName = HibernateProxyHelper.deproxy(synonym.getName(), BotanicalName.class);
            synonymString = createSynonymNameString(synonymName);

            homotypicalSynonyms.append(synonymString);

        }

        nameRecord.put("synonyms_homotypic", homotypicalSynonyms.toString());
        nameRecord.put("synonyms_heterotypic", heterotypicalSynonyms.toString());
        nameRecord.put("status", statusString);

        Set<NameRelationship> nameRelations = name.getNameRelations();

        String relatedName = null;
        String nameRelType = null;
        if (nameRelations.size()>0){
            NameRelationship nameRel = nameRelations.iterator().next();
            BotanicalName fromName = HibernateProxyHelper.deproxy(nameRel.getFromName(), BotanicalName.class);
            if (fromName.equals(taxon.getName())){
                relatedName = nameRel.getToName().getTitleCache();

            }else{
                relatedName = nameRel.getFromName().getTitleCache();
            }
            nameRel = HibernateProxyHelper.deproxy(nameRel, NameRelationship.class);
            nameRelType = nameRel.getType().getTitleCache();
        }


        nameRecord.put("relatedName", relatedName);
        nameRecord.put("nameRelType", nameRelType);

        extractDescriptions(nameRecord, taxon, getNotesFeature(state), "description");
        return nameRecord;
    }


}
