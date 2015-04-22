package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;





import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
@Component
public class CsvNameExportBase extends CdmExportBase<CsvNameExportConfigurator, CsvNameExportState, IExportTransformer> implements ICdmExport<CsvNameExportConfigurator, CsvNameExportState>{
	private static final Logger logger = Logger.getLogger(CsvNameExportBase.class);
	public CsvNameExportBase() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	
	@Override
	protected void doInvoke(CsvNameExportState state) {
		CsvNameExportConfigurator config = state.getConfig();
		TransactionStatus txStatus = startTransaction(true);
		
		PrintWriter writer = null;
		ByteArrayOutputStream byteArrayOutputStream;
		
			byteArrayOutputStream = config.getByteOutputStream();
			try {
				writer = new PrintWriter(config.getDestination());
				List<HashMap<String, String>> result;
				if (config.isNamesOnly()){
					result = getNameService().getNameRecords();
				} else {
					result = getRecordsForPrintPub(state.getConfig().getClassificationUUID());
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
			
			} catch (FileNotFoundException e) {
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


	public List<HashMap<String,String>> getRecordsForPrintPub(UUID classificationUUID){
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("childNodes");
		
		Classification classification = getClassificationService().load(classificationUUID);
		TaxonNode rootNode = classification.getRootNode();
		rootNode = getTaxonNodeService().load(rootNode.getUuid(), propertyPaths);
		Set<UUID> childUuids = new HashSet<UUID>();
		
		for (TaxonNode child: rootNode.getChildNodes()){
			child = HibernateProxyHelper.deproxy(child, TaxonNode.class);
			childUuids.add(child.getUuid());
		}
		propertyPaths.add("descriptions");
		propertyPaths.add("descriptions.elements");
		List<TaxonNode> familyNodes = getTaxonNodeService().find(childUuids);
		childUuids.clear();
		List<TaxonNode> genusNodes = new ArrayList<TaxonNode>();
		for (TaxonNode familyNode: familyNodes){
			for (TaxonNode child: familyNode.getChildNodes()){
				child = HibernateProxyHelper.deproxy(child, TaxonNode.class);
				childUuids.add(child.getUuid());
			}
			
			genusNodes = getTaxonNodeService().find(childUuids);
		}
		
		List<HashMap<String,String>> nameRecords = new ArrayList();
		HashMap<String,String> nameRecord = new HashMap<String,String>();
		List<String> propertyPathsFamilyNode = new ArrayList<String>();
		propertyPathsFamilyNode.add("taxon");
		propertyPathsFamilyNode.add("taxon.name");
		TaxonNode familyNode;
		Taxon taxon;
		BotanicalName name;
		BotanicalName typeName;
		TextData textElement;
		for(TaxonNode genusNode : genusNodes)   {
			nameRecord = new HashMap<String,String>();
			nameRecord.put("classification", genusNode.getClassification().getTitleCache());
			familyNode = getTaxonNodeService().load(genusNode.getParent().getUuid(), propertyPathsFamilyNode);
			familyNode = HibernateProxyHelper.deproxy(familyNode, TaxonNode.class);
			nameRecord.put("familyTaxon", familyNode.getTaxon().getTitleCache());
			taxon = (Taxon)getTaxonService().load(familyNode.getTaxon().getUuid(), propertyPaths);
			taxon = HibernateProxyHelper.deproxy(taxon, Taxon.class);
			nameRecord.put("familyName", taxon.getName().getTitleCache());
			
			StringBuffer descriptionsString = new StringBuffer();
			for (DescriptionBase descriptionBase: taxon.getDescriptions()){
				Set<DescriptionElementBase> elements = descriptionBase.getElements();
				for (DescriptionElementBase element: elements){
					if (element.getFeature().equals(Feature.INTRODUCTION())){
						if (element instanceof TextData){
							textElement = HibernateProxyHelper.deproxy(element, TextData.class);
							descriptionsString.append(textElement.getText(Language.ENGLISH()));
							descriptionsString.append("\\n");
						}
						
					}
				}
				
				
			}
			
			nameRecord.put("descriptionsFam", descriptionsString.toString());
			
			taxon = (Taxon) getTaxonService().load(genusNode.getTaxon().getUuid(), propertyPaths);
			taxon = HibernateProxyHelper.deproxy(taxon, Taxon.class);
			
			nameRecord.put("genusTaxon", taxon.getTitleCache());
			nameRecord.put("secRef", taxon.getSec().getTitleCache());
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
			String typeNameString = "not desit.";
			String statusString = null;
			if (it.hasNext()){
				TypeDesignationBase typeDes = it.next();
				typeDes = HibernateProxyHelper.deproxy(typeDes, TypeDesignationBase.class);
				Set<TaxonNameBase> nameBases = typeDes.getTypifiedNames();
				if (nameBases.iterator().hasNext()){
					typeName = HibernateProxyHelper.deproxy(nameBases.iterator().next(), BotanicalName.class);
					typeNameString = "<i>" + typeName.getNameCache() +"</i> "  + typeName.getAuthorshipCache();
					if (typeDes.getTypeStatus() != null){
						statusString = typeDes.getTypeStatus().getTitleCache();
					}
				}
				
			}
			nameRecord.put("typeName", typeNameString);
			StringBuffer homotypicalSynonyms = new StringBuffer();
			List<TaxonBase> heterotypicSynonymsList = new ArrayList<TaxonBase>();
			List<TaxonBase> homotypicSynonymsList = new ArrayList<TaxonBase>();
			StringBuffer heterotypicalSynonyms = new StringBuffer();
			for (SynonymRelationship synRel: taxon.getSynonymRelations()){
				if (synRel.getType().equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())){
					name.generateFullTitle();
					heterotypicSynonymsList.add(synRel.getSynonym());
				} else{
					name.generateFullTitle();
					homotypicSynonymsList.add(synRel.getSynonym());
				}
			}
			TaxonComparator comparator = new TaxonComparator();
			Collections.sort(heterotypicSynonymsList, comparator);
			String synonymString;
			boolean first = true;
			BotanicalName synonymName;
			for (TaxonBase synonym : heterotypicSynonymsList){
				if (!first){
					heterotypicalSynonyms.append(" <heterotypic>");
				}
				first = false;
				synonymName = HibernateProxyHelper.deproxy(synonym.getName(), BotanicalName.class);
				if (synonymName.getNomenclaturalReference() != null){
					synonymString = "<i>" +synonymName.getNameCache() + "</i> " + synonymName.getNomenclaturalReference().getTitleCache();
				} else{
					synonymString = "<i>" +synonymName.getNameCache() + "</i>";
				}
				heterotypicalSynonyms.append(synonymString);
				
			}
			
			first = true;
			Collections.sort(homotypicSynonymsList, comparator);
			for (TaxonBase synonym : homotypicSynonymsList){
				if (!first){
					homotypicalSynonyms.append(" <homotypic>");
				}
				first = false;
				synonymName = HibernateProxyHelper.deproxy(synonym.getName(), BotanicalName.class);
				if (synonymName.getNomenclaturalReference() != null){
					synonymString = "<i>" +synonymName.getNameCache() + "</i> " + synonymName.getNomenclaturalReference().getTitleCache();
				} else{
					synonymString = "<i>" +synonymName.getNameCache() + "</i>";
				}
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
				if (nameRel.getFromName().equals(genusNode.getTaxon().getName())){
					relatedName = nameRel.getToName().getTitleCache();
					
				}else{
					relatedName = nameRel.getFromName().getTitleCache();
				}
				nameRel = HibernateProxyHelper.deproxy(nameRel, NameRelationship.class);
				nameRelType = nameRel.getType().getTitleCache();
			}
			
			
			nameRecord.put("relatedName", relatedName);
			nameRecord.put("nameRelType", nameRelType);
		
			descriptionsString = new StringBuffer();
			for (DescriptionBase descriptionBase: genusNode.getTaxon().getDescriptions()){
				Set<DescriptionElementBase> elements = descriptionBase.getElements();
				for (DescriptionElementBase element: elements){
					if (element.getFeature().getTitleCache().equals("Notes"))
						if (element instanceof TextData){
							textElement = HibernateProxyHelper.deproxy(element, TextData.class);
							descriptionsString.append(textElement.getText(Language.ENGLISH()));
							descriptionsString.append("\\n");
						}
				}
				
				
			}
			
			nameRecord.put("descriptions", descriptionsString.toString());
			
	        nameRecords.add(nameRecord);
	   }
			
		return nameRecords;	
		
	}
	
	

}
