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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.ext.geo.CondensedDistributionRecipe;
import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HomotypicalGroupComparator;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.HomotypicGroupTaxonComparator;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.query.MatchMode;



@Component
public class CsvNameExport extends CsvNameExportBase {
    private static final long serialVersionUID = 7289805663701807425L;

    private static final Logger logger = Logger.getLogger(CsvNameExport.class);

    @Autowired
    IEditGeoService geoService;

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
            if (config.getRank() == null){
                config.setRank(Rank.GENUS());
            }
            if (config.isNamesOnly()){
                txStatus = startTransaction();
                result = getNameService().getNameRecords();
            } else {
                result = getRecordsForPrintPub(state);
            }
            CsvRecord nameRecord;
            int count = 0;
            boolean isFirst = true;
            for (HashMap<String,String> record:result){
                if (count > 0){
                    isFirst = false;
                }
                count++;
                nameRecord = new CsvRecord(record, isFirst);
                nameRecord.print(writer, config);

            }
            if (exportStream != null){
                state.getResult().addExportData(getByteArray());
                //this.exportData.addExportData(exportStream.toByteArray());
            }
            writer.flush();

            writer.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (txStatus != null  ){
            commitTransaction(txStatus);
        }
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
        TaxonNode rootNode;
        if (classification != null){
            rootNode = classification.getRootNode();
        }else{
            List<Classification> classifications = getClassificationService().list(Classification.class, 10, 0, null, null);
            if (classifications.isEmpty()){
                return null;
            }
            classification = classifications.get(0);
            rootNode = classification.getRootNode();
        }
        rootNode = getTaxonNodeService().load(rootNode.getUuid(), propertyPaths);
        Set<UUID> childrenUuids = new HashSet<UUID>();


        rootNode = CdmBase.deproxy(rootNode);
        rootNode.removeNullValueFromChildren();
        for (TaxonNode child: rootNode.getChildNodes()){
            child = CdmBase.deproxy(child);
            childrenUuids.add(child.getUuid());
        }
        Set<UUID> parentsNodesUUID = new HashSet<UUID>(childrenUuids);
        childrenUuids.clear();
        List<TaxonNode> childrenNodes = new ArrayList<TaxonNode>();

        findChildren(state, childrenUuids, parentsNodesUUID);


        List<HashMap<String,String>> nameRecords = new ArrayList<>();

        childrenNodes = getTaxonNodeService().find(childrenUuids);
        for(TaxonNode genusNode : childrenNodes)   {
            nameRecords.add(createNewRecord(genusNode, state));
            refreshTransaction();
        }

        return nameRecords;

    }

    /**
     * @param state
     * @param childrenUuids
     * @param parentsNodesUUID
     * @param familyNode
     */
    private void findChildren(CsvNameExportState state, Set<UUID> childrenUuids, Set<UUID> parentsNodesUUID) {
        TaxonName name;
        List<TaxonNode> familyNodes = getTaxonNodeService().find(parentsNodesUUID);
        parentsNodesUUID =new HashSet<>();
        for (TaxonNode familyNode: familyNodes){
            familyNode = CdmBase.deproxy(familyNode);
            familyNode.removeNullValueFromChildren();
            for (TaxonNode child: familyNode.getChildNodes()){
                child = CdmBase.deproxy(child);
                Taxon taxon = CdmBase.deproxy(child.getTaxon());
                if (taxon != null){
                    name = CdmBase.deproxy(taxon.getName());
                    if (child.getTaxon().getName().getRank().isLower(state.getConfig().getRank()) ) {
                        childrenUuids.add(child.getUuid());
                        if (child.hasChildNodes()){
                            parentsNodesUUID.add(child.getUuid());
                        }
                    }else{
                        parentsNodesUUID.add(child.getUuid());
                    }
                }
            }
            //refreshTransaction();
            if (!parentsNodesUUID.isEmpty()){
                findChildren(state, childrenUuids, parentsNodesUUID);
            }
        }

    }




    private String createSynonymNameString(IBotanicalName synonymName, boolean isInvalid) {
        String synonymString = null;

        synonymString= createTaggedNameString(synonymName, isInvalid);
        Set<NameRelationship> nameRelations = synonymName.getNameRelations();

        INonViralName relatedName = null;
        String nameRelType = null;
        boolean first = true;
        boolean isInvalidRel = false;
        for (NameRelationship nameRel: nameRelations){
           // NameRelationship nameRel = nameRelations.iterator().next();
            IBotanicalName fromName = CdmBase.deproxy(nameRel.getFromName());

            nameRel = CdmBase.deproxy(nameRel);
            nameRelType = nameRel.getType().getTitleCache();
            String relatedNameString = "";
            if (fromName.equals(synonymName)){
                relatedName = nameRel.getToName();
            }else{
                relatedName = nameRel.getFromName();
            }
            if (!nameRel.getType().equals(NameRelationshipType.BASIONYM())){
                isInvalidRel = getStatus(relatedName);
                relatedNameString = createTaggedNameString(relatedName, isInvalidRel&&isInvalid);

                if (nameRel.getType().equals(NameRelationshipType.LATER_HOMONYM())){
                    if (synonymName.equals(nameRel.getFromName())){
                        if (first){
                            synonymString = synonymString + " [non " + relatedNameString ;
                            first = false;
                        } else{
                            synonymString = synonymString + " nec " + relatedNameString ;
                        }
                    }
                } else if (nameRel.getType().equals(NameRelationshipType.REPLACED_SYNONYM())){
                    //synonymString = synonymString + " [non " + relatedNameString + "]";
                } else if (nameRel.getType().equals(NameRelationshipType.BLOCKING_NAME_FOR())){
                    if (synonymName.equals(nameRel.getToName())){
                        if (first){
                            synonymString = synonymString + " [non " + relatedNameString ;
                            first = false;
                        } else{
                            synonymString = synonymString + " nec " + relatedNameString ;
                        }

                    }
                } else if (nameRel.getType().equals(NameRelationshipType.TREATED_AS_LATER_HOMONYM())){
                    if (first){
                        synonymString = synonymString + " [non " + relatedNameString ;
                        first = false;
                    } else{
                        synonymString = synonymString + " nec " + relatedNameString ;
                    }
                } else if (nameRel.getType().equals(NameRelationshipType.ALTERNATIVE_NAME())){
                    if (first){
                        synonymString = synonymString + " [non " + relatedNameString ;
                        first = false;
                    } else{
                        synonymString = synonymString + " nec " + relatedNameString ;
                    }
                } else if (nameRel.getType().equals(NameRelationshipType.CONSERVED_AGAINST())) {

                }else if (nameRel.getType().equals(NameRelationshipType.ORTHOGRAPHIC_VARIANT())){

                }
            }
        }
        if (!first){
            synonymString = synonymString + "]";
        }

        return synonymString;
    }

    /**
     * @param relatedName
     * @return
     */
    private boolean getStatus(INonViralName relatedName) {
        boolean result;
        if (!relatedName.getStatus().isEmpty()){
            NomenclaturalStatus status = CdmBase.deproxy(relatedName.getStatus().iterator().next());
            if (status.getType().isInvalidType()){
                result = true;
            }else{
                result = false;
            }
        }else{
            result = false;
        }
        return result;
    }

    private String createTaggedNameString(INonViralName name, boolean isInvalid){
        String nameString = null;
        if (name == null){
            return nameString;
        }

        nameString = name.generateFullTitle();
        if (isInvalid){
            nameString = nameString.replace(name.getTitleCache(), "\""+name.getTitleCache()+"\"");
        }
        if (name.getGenusOrUninomial() != null){
            nameString = nameString.replaceAll(name.getGenusOrUninomial(), "<i>"+ name.getGenusOrUninomial() + "</i>");
        }
        if (name.getInfraGenericEpithet() != null){
            nameString = nameString.replaceAll(name.getInfraGenericEpithet(),  "<i>"+ name.getInfraGenericEpithet() + "</i>");
        }
        if (name.getSpecificEpithet() != null){
            nameString = nameString.replaceAll(name.getSpecificEpithet(),  "<i>"+ name.getSpecificEpithet() + "</i>");
        }
        if (name.getInfraSpecificEpithet() != null && !name.isAutonym()){
            nameString = nameString.replaceAll(name.getInfraSpecificEpithet(),  "<i>"+ name.getInfraSpecificEpithet() + "</i>");
        }

        return nameString;
    }



    private List<TaxonNode> getGenusNodes (UUID classificationUUID){
        Classification classification = getClassificationService().load(classificationUUID);
        TaxonNode rootNode = classification.getRootNode();
        rootNode = getTaxonNodeService().load(rootNode.getUuid());
        Set<UUID> childrenUuids = new HashSet<>();

        for (TaxonNode child: rootNode.getChildNodes()){
            child = CdmBase.deproxy(child);
            childrenUuids.add(child.getUuid());
        }

        List<TaxonNode> familyNodes = getTaxonNodeService().find(childrenUuids);
        childrenUuids.clear();
        List<TaxonNode> genusNodes = new ArrayList<>();
        for (TaxonNode familyNode: familyNodes){
            for (TaxonNode child: familyNode.getChildNodes()){
                child = CdmBase.deproxy(child);
                childrenUuids.add(child.getUuid());
            }

            genusNodes = getTaxonNodeService().find(childrenUuids);
        }
        return genusNodes;
    }

    private TaxonNode getHigherNode(TaxonNode node, Rank rank){

        Rank nodeRank = node.getTaxon().getName().getRank();
        if (nodeRank.isKindOf(rank)){
            return null;

        }else if (nodeRank.isHigher(rank)){
            return null;
        } else {
            return node.getAncestorOfRank(rank);
        }
    }

    private void extractDescriptions(HashMap<String, String> nameRecord, Taxon taxon, Feature feature, String columnName, CsvNameExportState state){
        StringBuffer descriptionsString = new StringBuffer();
        TextData textElement;
        Set<Distribution> distributions = new HashSet<>();
        if (taxon.getDescriptions().isEmpty()){
            nameRecord.put(columnName, null);
                return;
        }
        for (DescriptionBase<?> descriptionBase: taxon.getDescriptions()){
            if (!descriptionBase.isImageGallery()){
                Set<DescriptionElementBase> elements = descriptionBase.getElements();
                for (DescriptionElementBase element: elements){
                    if (element.getFeature().equals(feature)){
                        if (element instanceof TextData){
                            textElement = CdmBase.deproxy(element, TextData.class);
                            descriptionsString.append(textElement.getText(Language.ENGLISH()));

                        }else if (element instanceof Distribution ){

                            Distribution distribution = CdmBase.deproxy(element, Distribution.class);
                            distributions.add(distribution);
                        }

                    }
                }
            }
        }
        if (state.getConfig().isCondensedDistribution()){
            List<Language> langs = new ArrayList<Language>();
            langs.add(Language.ENGLISH());

            CondensedDistribution conDis = geoService.getCondensedDistribution(distributions, true, null,null,CondensedDistributionRecipe.FloraCuba, langs );

            nameRecord.put(columnName, conDis.toString());

        } else{
            NamedArea area ;
            for (Distribution distribution:distributions){

                if (descriptionsString.length()> 0 ){
                    descriptionsString.append(", ");
                }
                area = CdmBase.deproxy(distribution.getArea());
                if (area.getIdInVocabulary() != null){
                    descriptionsString.append(area.getIdInVocabulary());
                }else if (area.getSymbol() != null){
                    descriptionsString.append(area.getSymbol());
                }else{
                    descriptionsString.append(area.getLabel());
                }

            }
            nameRecord.put(columnName, descriptionsString.toString());
        }

    }

    private Feature getNotesFeature(CsvNameExportState state){
        if (state.getNotesFeature() != null){
            return state.getNotesFeature();
        } else{
            Pager<DefinedTermBase> notesFeature = getTermService().findByTitle(Feature.class, "Notes" ,MatchMode.EXACT, null, null, null, null, null);
            if (notesFeature.getRecords().size() == 0){
                return null;
            }else{
                DefinedTermBase<?> feature=  notesFeature.getRecords().iterator().next();
                if (feature instanceof Feature){
                    state.setNotesFeature((Feature)feature);
                    return (Feature) feature;
                } else{
                    return null;
                }
            }
        }

    }

    private HashMap<String, String> createNewRecord(TaxonNode childNode, CsvNameExportState state){
        HashMap<String, String> nameRecord = new HashMap<>();
        nameRecord.put("classification", childNode.getClassification().getTitleCache());
        TaxonNode familyNode = getHigherNode(childNode, Rank.FAMILY());
        Taxon taxon;
        String nameString;
        IBotanicalName name;
        if (familyNode == null){
            nameRecord.put("familyTaxon", null);
            nameRecord.put("familyName", null);
            nameRecord.put("descriptionsFam", null);
        }else{
            familyNode = CdmBase.deproxy(familyNode);
            familyNode.getTaxon().setProtectedTitleCache(true);
            nameRecord.put("familyTaxon", familyNode.getTaxon().getTitleCache());

            taxon = (Taxon)getTaxonService().load(familyNode.getTaxon().getUuid());
            taxon = CdmBase.deproxy(taxon);
            //if publish flag is set

            //  if (taxon.isPublish()){
            name = CdmBase.deproxy(taxon.getName());
            nameRecord.put("familyName", name.getNameCache());
            extractDescriptions(nameRecord, taxon, Feature.INTRODUCTION(), "descriptionsFam", state);
        }
        TaxonNode genusNode = getHigherNode(childNode, Rank.GENUS());
        if (genusNode!= null){
            genusNode = CdmBase.deproxy(genusNode);
            genusNode.getTaxon().setProtectedTitleCache(true);
            nameRecord.put("genusTaxon", genusNode.getTaxon().getTitleCache());

            taxon = (Taxon)getTaxonService().load(genusNode.getTaxon().getUuid());
            taxon = CdmBase.deproxy(taxon);
            //if publish flag is set

            //  if (taxon.isPublish()){
            name = CdmBase.deproxy(taxon.getName());
            if (name.getNameCache() != null){
                nameRecord.put("genusName", name.getNameCache());
            }else{
                nameRecord.put("genusName", name.getGenusOrUninomial());
            }
            extractDescriptions(nameRecord, taxon,getNotesFeature(state), "descriptionsGen", state);
        }else{
            nameRecord.put("genusTaxon", null);
            nameRecord.put("genusName", null);
            nameRecord.put("descriptionsGen", null);
        }

        taxon = (Taxon) getTaxonService().load(childNode.getTaxon().getUuid());
        taxon = CdmBase.deproxy(taxon);
        //  if (taxon.isPublish()){

        INonViralName nonViralName = taxon.getName();

        nameString = createTaggedNameString(nonViralName, false);
        nameRecord.put("childTaxon", taxon.getTitleCache());
        if (taxon.getSec()!= null){
            nameRecord.put("secRef", taxon.getSec().getTitleCache());
        }else{
            nameRecord.put("secRef", null);
        }

        getTaxonRelations(nameRecord, taxon);

        name = CdmBase.deproxy(getNameService().load(taxon.getName().getUuid()));
        nameRecord.put("childName",nameString);
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

            TypeDesignationBase<?> typeDes = CdmBase.deproxy(it.next());

            if (typeDes instanceof NameTypeDesignation){
                NameTypeDesignation nameTypeDes = CdmBase.deproxy(typeDes, NameTypeDesignation.class);

                IBotanicalName typeName =  CdmBase.deproxy(nameTypeDes.getTypeName());
                if (typeName != null){

                    typeNameString = "<i>" + typeName.getNameCache() +"</i> "  + typeName.getAuthorshipCache();
                    if (nameTypeDes.getTypeStatus() != null){
                        NameTypeDesignationStatus status = CdmBase.deproxy(nameTypeDes.getTypeStatus());
                        statusString = status.getTitleCache();
                    }
                }
            }
        }
        nameRecord.put("typeName", typeNameString);
        StringBuffer homotypicalSynonyms = new StringBuffer();
        TreeMap<HomotypicalGroup,List<Synonym>> heterotypicSynonymsList = new TreeMap<>(new HomotypicalGroupComparator());

        List<Synonym> homotypicSynonymsList = new ArrayList<>();
        StringBuffer heterotypicalSynonyms = new StringBuffer();
        List<Synonym> homotypicSynonyms;

        HomotypicalGroup group;
        IBotanicalName synonymName;
        String doubtfulTitleCache;
        for (Synonym synonym: taxon.getSynonyms()){
            synonymName = CdmBase.deproxy(synonym.getName());
            group = CdmBase.deproxy(synonymName.getHomotypicalGroup());
            synonymName.generateFullTitle();
            if (synonym.isDoubtful()){
                if (!synonymName.getFullTitleCache().startsWith("?")){
                    doubtfulTitleCache = "?" + synonymName.getFullTitleCache();
                    synonymName = (IBotanicalName) synonymName.clone();
                    synonymName.setFullTitleCache(doubtfulTitleCache, true);
                }
            }
            if (!group.equals(name.getHomotypicalGroup())){
                if (heterotypicSynonymsList.containsKey(group)){
                    heterotypicSynonymsList.get(group).add(synonym);
                }else{
                    homotypicSynonyms = new ArrayList<>();
                    homotypicSynonyms.add(synonym);
                    heterotypicSynonymsList.put(group, homotypicSynonyms);
                    homotypicSynonyms= null;
                }
            } else{
                synonymName.generateFullTitle();
                homotypicSynonymsList.add(synonym);
            }
        }



        String synonymString;
        boolean first = true;

        for (List<Synonym> list: heterotypicSynonymsList.values()){
            Collections.sort(list, new HomotypicGroupTaxonComparator(null));
            first = true;
            for (TaxonBase<?> synonym : list){
                NomenclaturalStatus status = null;
                if (!synonym.getName().getStatus().isEmpty()){
                    status = CdmBase.deproxy(synonym.getName().getStatus().iterator().next());
                    if (status.getType().isInvalidType()){
                        heterotypicalSynonyms.append(" <invalid> ");
                        synonymName = CdmBase.deproxy(synonym.getName());

                        synonymString = createSynonymNameString(synonymName, state.getConfig().isInvalidNamesQuoted()) ;
                        heterotypicalSynonyms.append(synonymString);
                        continue;
                    }
                }
                if (first){
                    heterotypicalSynonyms.append(" <heterotypic> ");
                }else{
                    heterotypicalSynonyms.append(" <homonym> ");
                }
                first = false;
                synonymName = CdmBase.deproxy(synonym.getName());
                synonymString = createSynonymNameString(synonymName, false);
                heterotypicalSynonyms.append(synonymString);
            }
        }

        first = true;
        Collections.sort(homotypicSynonymsList, new HomotypicGroupTaxonComparator(null)  );
        NomenclaturalStatus status = null;
        for (TaxonBase<?> synonym : homotypicSynonymsList){

            if (!synonym.getName().getStatus().isEmpty()){
                status = CdmBase.deproxy(synonym.getName().getStatus().iterator().next());
                if (status.getType().isInvalidType()){
                    homotypicalSynonyms.append(" <invalid> ");
                    synonymName = CdmBase.deproxy(synonym.getName());
                    synonymString = createSynonymNameString(synonymName, true);
                    homotypicalSynonyms.append(synonymString);
                    continue;
                }else if (!first){
                    homotypicalSynonyms.append(" <homonym> ");
                }

            }else if (!first){
                homotypicalSynonyms.append(" <homonym> ");
            }
            first = false;
            synonymName = CdmBase.deproxy(synonym.getName());

            synonymString = createSynonymNameString(synonymName, false);

            homotypicalSynonyms.append(synonymString);

        }

        nameRecord.put("synonyms_homotypic", homotypicalSynonyms.toString());
        nameRecord.put("synonyms_heterotypic", heterotypicalSynonyms.toString());
        nameRecord.put("status", statusString);

        Set<NameRelationship> nameRelations = name.getNameRelations();

        INonViralName relatedName = null;
        String nameRelType = null;
        String relNameString = null;
        if (nameRelations.size()>0){
            NameRelationship nameRel = nameRelations.iterator().next();
            IBotanicalName fromName = CdmBase.deproxy(nameRel.getFromName());
            if (fromName.equals(taxon.getName())){
                relatedName = nameRel.getToName();

            }else{
                relatedName = nameRel.getFromName();
            }
            nameRel = CdmBase.deproxy(nameRel);
            nameRelType = nameRel.getType().getTitleCache();
            relNameString  = createTaggedNameString(relatedName, getStatus(relatedName));
        }


        nameRecord.put("relatedName", relNameString);
        nameRecord.put("nameRelType", nameRelType);

        extractDescriptions(nameRecord, taxon, Feature.DISTRIBUTION(), "descriptions", state);
        return nameRecord;
    }

    /**
     * @param nameRecord
     * @param taxon
     */
    private void getTaxonRelations(HashMap<String, String> nameRecord, Taxon taxon) {
        Set<TaxonRelationship> relations = new HashSet<>();
        relations =taxon.getTaxonRelations();
        if (relations.isEmpty()){
            nameRecord.put("missappliedNames", null);
        }else{
            Taxon relatedTaxon = null;
            StringBuffer nameString = new StringBuffer();
            for (TaxonRelationship rel : relations){
                if (rel.getType().isAnyMisappliedName()){
                    relatedTaxon = rel.getFromTaxon();
                    Reference secRef = relatedTaxon.getSec();
                    String appendedPhrase = "";
                    if (relatedTaxon.getAppendedPhrase() != null){
                        appendedPhrase = relatedTaxon.getAppendedPhrase();
                    }
                    if (secRef == null){
                        nameString.append("<misapplied>\"" + createTaggedNameString(relatedTaxon.getName(), false) + "\" " + appendedPhrase);
                    } else if (secRef.getAuthorship() == null){
                        nameString.append("<misapplied>\"" + createTaggedNameString(relatedTaxon.getName(), false) + "\" " + appendedPhrase + " sensu " + secRef.getTitleCache());
                    } else {
                        nameString.append("<misapplied>\"" + createTaggedNameString(relatedTaxon.getName(), false) + "\" " + appendedPhrase + " sensu " + secRef.getAuthorship().getNomenclaturalTitle());
                    }

                }
            }
            nameRecord.put("missappliedNames", nameString.toString());
        }
    }

}

