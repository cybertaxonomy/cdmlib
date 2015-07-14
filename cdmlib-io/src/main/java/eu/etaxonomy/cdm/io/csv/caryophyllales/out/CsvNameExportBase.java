package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HomotypicalGroupComparator;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

@Component
public class CsvNameExportBase extends CdmExportBase<CsvNameExportConfigurator, CsvNameExportState, IExportTransformer> implements ICdmExport<CsvNameExportConfigurator, CsvNameExportState>{
	private static final Logger logger = Logger.getLogger(CsvNameExportBase.class);
	
	final String NOT_DESIGNATED = "not designated";
	public CsvNameExportBase() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	
	@Override
	protected void doInvoke(CsvNameExportState state) {
		CsvNameExportConfigurator config = state.getConfig();
		TransactionStatus txStatus = startTransaction(true);
		
		PrintWriter writer = null;
		 
			try {
				OutputStream os = new FileOutputStream(config.getDestination());
			    os.write(239);
			    os.write(187);
			    os.write(191);
				writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
				
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


	public List<HashMap<String,String>> getRecordsForPrintPub(UUID classificationUUID){
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("childNodes");
		
		Classification classification = getClassificationService().load(classificationUUID);
		TaxonNode rootNode = classification.getRootNode();
		rootNode = getTaxonNodeService().load(rootNode.getUuid(), propertyPaths);
		Set<UUID> childrenUuids = new HashSet<UUID>();
		
		for (TaxonNode child: rootNode.getChildNodes()){
			child = HibernateProxyHelper.deproxy(child, TaxonNode.class);
			childrenUuids.add(child.getUuid());
		}
		propertyPaths.add("descriptions");
		propertyPaths.add("descriptions.elements");
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
		NameTypeDesignation typeDes;
		for(TaxonNode genusNode : genusNodes)   {
			nameRecord = new HashMap<String,String>();
			nameRecord.put("classification", genusNode.getClassification().getTitleCache());
			familyNode = getTaxonNodeService().load(genusNode.getParent().getUuid(), propertyPathsFamilyNode);
			familyNode = HibernateProxyHelper.deproxy(familyNode, TaxonNode.class);
			familyNode.getTaxon().setProtectedTitleCache(true);
			nameRecord.put("familyTaxon", familyNode.getTaxon().getTitleCache());
			taxon = (Taxon)getTaxonService().load(familyNode.getTaxon().getUuid(), propertyPaths);
			taxon = HibernateProxyHelper.deproxy(taxon, Taxon.class);
			//if publish flag is set
			if (taxon.isPublish()){
				
				name = HibernateProxyHelper.deproxy(taxon.getName(), BotanicalName.class);
				nameRecord.put("familyName", name.getNameCache());
				
				StringBuffer descriptionsString = new StringBuffer();
				for (DescriptionBase descriptionBase: taxon.getDescriptions()){
					Set<DescriptionElementBase> elements = descriptionBase.getElements();
					for (DescriptionElementBase element: elements){
						if (element.getFeature().equals(Feature.INTRODUCTION())){
							if (element instanceof TextData){
								textElement = HibernateProxyHelper.deproxy(element, TextData.class);
								descriptionsString.append(textElement.getText(Language.ENGLISH()));
								
							}
							
						}
					}
					
					
				}
				
				nameRecord.put("descriptionsFam", descriptionsString.toString());
				
				taxon = (Taxon) getTaxonService().load(genusNode.getTaxon().getUuid(), propertyPaths);
				taxon = HibernateProxyHelper.deproxy(taxon, Taxon.class);
				
				nameRecord.put("genusTaxon", taxon.getTitleCache());
				if (taxon.getSec()!= null){
					nameRecord.put("secRef", taxon.getSec().getTitleCache());
				}else{
					nameRecord.put("secRef", null);
				}
				
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
					typeDes = HibernateProxyHelper.deproxy(it.next(), NameTypeDesignation.class);
					
					
					typeName =  HibernateProxyHelper.deproxy(typeDes.getTypeName(), BotanicalName.class);
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
							doubtfulTitleCache = "?" + synonymName.getTitleCache();
							synonymName.setTitleCache(doubtfulTitleCache, true);
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
					Collections.sort(list, new TaxonComparator());
					first = true;
					for (TaxonBase synonym : list){
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
				for (TaxonBase synonym : homotypicSynonymsList){
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
								
							}
					}
					
					
				}
				
				nameRecord.put("descriptions", descriptionsString.toString());
				
		        nameRecords.add(nameRecord);
			}
	   }
			
		return nameRecords;	
		
	}
	
			
	

	private String createSynonymNameString(BotanicalName synonymName) {
		String synonymString = null;
		
		synonymString = synonymName.getTitleCache();
		
		if (synonymName.getGenusOrUninomial() != null){
			synonymString = synonymString.replaceAll(synonymName.getGenusOrUninomial(), "<i>"+ synonymName.getGenusOrUninomial() + "</i>");
		}
		if (synonymName.getInfraGenericEpithet() != null){
			synonymString = synonymString.replaceAll(synonymName.getInfraGenericEpithet(),  "<i>"+ synonymName.getInfraGenericEpithet() + "</i>");
		}
			
		return synonymString;
	}
	
	/*public  List<HashMap<String,String>> getNameRecords(UUID classificationUUID){
		List<HashMap<String, String>> nameRecords = new ArrayList<HashMap<String,String>>();
		HashMap<String, String> nameRecord;
		List<TaxonNode> genusNodes = getGenusNodes(classificationUUID);
		String famName;
		Taxon taxon;
		BotanicalName name;
		for(TaxonNode genusNode:genusNodes){
			nameRecord = new HashMap<String,String>();
			TaxonNode familyNode =  HibernateProxyHelper.deproxy(getFamilyName(genusNode),TaxonNode.class);
			taxon = HibernateProxyHelper.deproxy(familyNode.getTaxon(), Taxon.class);
			name = HibernateProxyHelper.deproxy(taxon.getName(), BotanicalName.class);
			famName =name.getNameCache();
			nameRecord.put("famName",famName);
			nameRecord.put("accFamName",(String)row[1]);
	      
			nameRecord.put("DTYPE",(String)row[2]);
			nameRecord.put("TaxonID",String.valueOf(row[3]));
			nameRecord.put("taxonTitle",(String)row[4]);
	        nameRecord.put("RankID",String.valueOf(row[5]));
	        nameRecord.put("NameID",String.valueOf(row[6]));
	        nameRecord.put("name",(String)row[7]);
	        nameRecord.put("nameAuthor",(String)row[8]);
	        nameRecord.put("nameAndNomRef",(String)row[9]);
	        nameRecord.put("nomRef",(String)row[10]);
	        nameRecord.put("nomRefAbbrevTitle",(String)row[11]);
	        nameRecord.put("nomRefTitle",(String)row[12]);
	        nameRecord.put("nomRefPublishedStart",(String)row[13]);
	        nameRecord.put("nomRefPublishedEnd",(String)row[14]);
	        nameRecord.put("nomRefPages",(String)row[15]);
	        nameRecord.put("inRefAbbrevTitle",(String)row[16]);
	        nameRecord.put("detail",(String)row[17]);
	        nameRecord.put("nameType",(String)row[18]);
	        nameRecord.put("nameTypeAuthor",(String)row[19]);
	        nameRecord.put("nameTypeFullTitle",(String)row[20]);
	        nameRecord.put("nameTypeRef",(String)row[21]);
	        nameRecord.put("inRefSeries",(String)row[22]);
	        nameRecord.put("inRefPublishedStart",(String)row[23]);
	        nameRecord.put("inRefPublishedEnd",(String)row[24]);
	        nameRecord.put("inRefVolume",(String)row[25]);
		
		}
		
		return result;
		
	}*/
	
	private List<TaxonNode> getGenusNodes (UUID classificationUUID){
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("childNodes");
		
		Classification classification = getClassificationService().load(classificationUUID);
		TaxonNode rootNode = classification.getRootNode();
		rootNode = getTaxonNodeService().load(rootNode.getUuid(), propertyPaths);
		Set<UUID> childrenUuids = new HashSet<UUID>();
		
		for (TaxonNode child: rootNode.getChildNodes()){
			child = HibernateProxyHelper.deproxy(child, TaxonNode.class);
			childrenUuids.add(child.getUuid());
		}
		propertyPaths.add("descriptions");
		propertyPaths.add("descriptions.elements");
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
	
	
}
