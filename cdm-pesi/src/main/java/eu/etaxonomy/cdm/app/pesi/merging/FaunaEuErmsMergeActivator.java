package eu.etaxonomy.cdm.app.pesi.merging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.common.TestDatabase;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

public class FaunaEuErmsMergeActivator {
	
//	static final ICdmDataSource faunaEuropaeaSource = CdmDestinations.cdm_test_patricia();
	static final ICdmDataSource faunaEuropaeaSource = CdmDestinations.localH2();
	
	static final int faunaEuUuid = 0;
	static final int ermsUuid = 9;
	static final int rankFaunaEu = 4;
	static final int rankErms = 13;
	
	CdmApplicationController appCtrInit;
	
	private static final Logger logger = Logger.getLogger(FaunaEuErmsMergeActivator.class);
	
	//csv files starting with...
	static String sFileName = "c:\\test";
	
	private void initDb(ICdmDataSource db) {

		// Init source DB
		appCtrInit = TestDatabase.initDb(db, DbSchemaValidation.VALIDATE, false);

		
	}
	
	public static void main(String[] args) {
		
		FaunaEuErmsMergeActivator sc = new FaunaEuErmsMergeActivator();
		
		sc.initDb(faunaEuropaeaSource);
			
		sc.mergeAuthors();
		
		//set the ranks of Agnatha and Gnathostomata to 50 instead of 45
		List<TaxonBase> taxaToChangeRank = new ArrayList<TaxonBase>();
		Pager<TaxonBase> agnatha = sc.appCtrInit.getTaxonService().findTaxaByName(TaxonBase.class, "Agnatha", null, null, null, Rank.INFRAPHYLUM(), 10, 0);
		List<TaxonBase> agnathaList = agnatha.getRecords();
		taxaToChangeRank.addAll(agnathaList);
		Pager<TaxonBase> gnathostomata = sc.appCtrInit.getTaxonService().findTaxaByName(TaxonBase.class, "Gnathostomata", null, null, null, Rank.INFRAPHYLUM(), 10, 0);
		List<TaxonBase> gnathostomataList = gnathostomata.getRecords();
		taxaToChangeRank.addAll(gnathostomataList);
		
		sc.setSpecificRank(taxaToChangeRank,Rank.SUPERCLASS());
		
		//ermsTaxon is accepted, fauna eu taxon is synonym
		//ermsTaxon is synonym, faunaEu is accepted
		
		sc.mergeDiffStatus();
		
		//erms is synonym, faunaEu as well
		
		// erms is accepted, faunaEu as well
		
		
		
		
		
		
		
	}
	
	private static List readCsvFile(String fileName){
		
		List<List<String>> result = new ArrayList<List<String>>();
		File file = new File(fileName);
		BufferedReader bufRdr;
		try {
			bufRdr = new BufferedReader(new FileReader(file));
			String line = null;
			//read each line of text file
			while((line = bufRdr.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line,",");
				List<String> rowList = new ArrayList<String>();
				while (st.hasMoreTokens()){
					//get next token and store it in the array
					rowList.add(st.nextToken());
				}
			result.add(rowList);
			}
			//close the file
			bufRdr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	
	private void mergeAuthors(){
		List<List<String>> authors = readCsvFile(sFileName + "_authors.csv");
		//authors: get firstAuthor if isFauEu = 1 otherwise get secondAuthor
		
		Iterator<List<String>> authorIterator = authors.iterator();
		List<String> row;
		TaxonBase taxonFaunaEu;
		TaxonBase taxonErms;
		List<TaxonBase> taxaToSave = new ArrayList<TaxonBase>();
		while (authorIterator.hasNext()){
			row = authorIterator.next();
			UUID uuidFaunaEu = UUID.fromString(row.get(faunaEuUuid));
			UUID uuidErms = UUID.fromString(row.get(ermsUuid));
			taxonFaunaEu = appCtrInit.getTaxonService().find(uuidFaunaEu);
			taxonErms = appCtrInit.getTaxonService().find(uuidFaunaEu);
			
			if (Integer.parseInt(row.get(18)) == 1){
				//isFaunaEu = 1 -> copy the author of Fauna Europaea to Erms
				if (((ZoologicalName)taxonFaunaEu.getName()).getBasionymAuthorTeam()!= null){
					((ZoologicalName)taxonErms.getName()).setBasionymAuthorTeam(((ZoologicalName)taxonFaunaEu.getName()).getBasionymAuthorTeam());
				} 
				if (((ZoologicalName)taxonFaunaEu.getName()).getCombinationAuthorTeam()!= null){
					((ZoologicalName)taxonErms.getName()).setCombinationAuthorTeam(((ZoologicalName)taxonFaunaEu.getName()).getCombinationAuthorTeam());
				}
				((ZoologicalName)taxonErms.getName()).generateAuthorship();
				taxaToSave.add(taxonErms);
			}else{
				if (((ZoologicalName)taxonErms.getName()).getBasionymAuthorTeam()!= null){
					((ZoologicalName)taxonFaunaEu.getName()).setBasionymAuthorTeam(((ZoologicalName)taxonErms.getName()).getBasionymAuthorTeam());
				} 
				if (((ZoologicalName)taxonErms.getName()).getCombinationAuthorTeam()!= null){
					((ZoologicalName)taxonFaunaEu.getName()).setCombinationAuthorTeam(((ZoologicalName)taxonErms.getName()).getCombinationAuthorTeam());
				}
				((ZoologicalName)taxonFaunaEu.getName()).generateAuthorship();
				taxaToSave.add(taxonFaunaEu);
			}
			
			
		}
	}
	
	public void setSpecificRank(List<TaxonBase> taxa, Rank rank){
		
		for (TaxonBase taxon: taxa){
			taxon.getName().setRank(rank);
		}
	}
	
	private void mergeDiffStatus(){
		List<List<String>> diffStatus = readCsvFile(sFileName + "_status.csv");
		
		//find all taxa accepted in erms, but synonyms in FauEu  and the same rank
		List<List<String>> accErmsSynFaunaEu = new ArrayList<List<String>>();
		for (List<String> rowList: diffStatus){
			if ((rowList.get(5).equals("synonym")) && (rowList.get(rankFaunaEu).equals(rowList.get(rankErms)))){
				//both conditions are true
				accErmsSynFaunaEu.add(rowList);
			}
		}
		mergeErmsAccFaunaEuSyn(accErmsSynFaunaEu);
		
		//find all taxa accepted in faunaEu, but synonyms in Erms and the same rank
		List<List<String>> synErmsAccFaunaEu = new ArrayList<List<String>>();
		for (List<String> rowList: diffStatus){
			if ((rowList.get(5).equals("accepted")) && (rowList.get(rankFaunaEu).equals(rowList.get(rankErms)))){
				//both conditions are true
				synErmsAccFaunaEu.add(rowList);
			}
		}
		mergeErmsSynFaunaEuAcc(synErmsAccFaunaEu);
		
	
	}
	
	private void mergeSameStatus(){
		List<List<String>> sameStatus = readCsvFile(sFileName + "_names.csv");
		
		TaxonBase taxonFaunaEu;
		TaxonBase taxonErms;
		
		for (List<String> row: sameStatus){
			taxonFaunaEu = appCtrInit.getTaxonService().find(UUID.fromString(row.get(faunaEuUuid)));
			taxonErms = appCtrInit.getTaxonService().find(UUID.fromString(row.get(ermsUuid)));
			moveAllInformationsFromFaunaEuToErms(taxonFaunaEu, taxonErms);
			if (taxonErms instanceof Taxon){
				moveFaunaEuSynonymsToErmsTaxon((Taxon)taxonFaunaEu, (Taxon)taxonErms);
			}
		}
	}
	
	

	private void mergeErmsAccFaunaEuSyn(List<List<String>> ermsAccFaEuSyn){
		
		// update nameRelationships -> if the nameRelationship does not exist, then create a new one with ermsAcc as relatedTo TaxonName
		updateNameRelationships(ermsAccFaEuSyn);
		
		//delete all synonymRelationships of FaunaEu Syn
		for (List<String> rowList: ermsAccFaEuSyn){
			UUID faunaUUID = UUID.fromString(rowList.get(faunaEuUuid));
			//UUID ermsUUID = UUID.fromString(rowList.get(ermsUuid));
			Synonym syn = (Synonym)appCtrInit.getTaxonService().find(faunaUUID);
			appCtrInit.getTaxonService().deleteSynonymRelationships(syn);
		}
		
		//merge the infos of 
		
		
	}
	
	private  void mergeErmsSynFaunaEuAcc (List<List<String>> ermsAccFaEuSyn){
		//occurence: verkn�pfe statt dem Fauna Europaea Taxon das akzeptierte Taxon, des Synonyms mit der Occurence (CDM -> distribution)
		//suche distribution (�ber das Taxon der TaxonDescription), dessen Taxon, das entsprechende Fauna Eu Taxon ist und verkn�pfe es mit dem akzeptieren Taxon des Erms Syn
		Taxon taxonFaunaEu = null;
		Taxon taxonErms = null;
		Synonym synErms = null;
		for (List<String> row: ermsAccFaEuSyn){
			taxonFaunaEu = (Taxon)appCtrInit.getTaxonService().find(UUID.fromString(row.get(faunaEuUuid)));
			synErms = (Synonym)appCtrInit.getTaxonService().find(UUID.fromString(row.get(ermsUuid)));
			synErms = HibernateProxyHelper.deproxy(synErms, Synonym.class);
			Set<SynonymRelationship> synRel=synErms.getSynonymRelations();
			
			if (synRel.size()>1){
				//TODO: which Relationship??
				Iterator<SynonymRelationship> iterator = synRel.iterator();
				taxonErms = iterator.next().getAcceptedTaxon();
			}else if (synRel.size() == 1){
				Iterator<SynonymRelationship> iterator = synRel.iterator();
				taxonErms = iterator.next().getAcceptedTaxon();
			} else {
				taxonErms = null;
				logger.debug("There is no SynonymRelationship for the synonym" + synErms.getTitleCache());
			}
			
			Set<Feature> features = new HashSet<Feature>();
			features.add(Feature.DISTRIBUTION());
			List<String> propertyPaths = new ArrayList<String>();
			propertyPaths.add("inDescription.Taxon.*");
			List<Distribution> distributions = appCtrInit.getDescriptionService().getDescriptionElementsForTaxon(taxonFaunaEu, features, Distribution.class, 10, 0, null);
			
			
			for(Distribution distribution: distributions){
				TaxonDescription description = (TaxonDescription)distribution.getInDescription();
				TaxonDescription newDescription = TaxonDescription.NewInstance(taxonErms);
				newDescription.addElement(distribution);
				appCtrInit.getDescriptionService().delete(description);
			}
			
			//Child-Parent Relationship aktualisieren -> dem Child des Fauna Europaea Taxons als parent das akzeptierte Taxon von synErms
			Set<TaxonNode> nodesErms = taxonErms.getTaxonNodes();
			Set<TaxonNode> nodesFaunaEu =taxonFaunaEu.getTaxonNodes();
			if (nodesFaunaEu.size()>1 || nodesFaunaEu.isEmpty()){
				
			}else{
				Iterator<TaxonNode> iteratorNodesErms = nodesErms.iterator();
				
				Iterator<TaxonNode> iteratorNodesFaunaEu = nodesFaunaEu.iterator();
				TaxonNode node = iteratorNodesFaunaEu.next();
				Set<TaxonNode> children = node.getChildNodes();
				Iterator<TaxonNode> childrenIterator = children.iterator();
				TaxonNode childNode;
				if (iteratorNodesErms.hasNext()){
					TaxonNode ermsNode = iteratorNodesErms.next();
					while (childrenIterator.hasNext()){
						childNode = childrenIterator.next();
						ermsNode.addChildNode(childNode, childNode.getReference(), childNode.getMicroReference(), null);
					}
				}
				
			}
			moveFaunaEuSynonymsToErmsTaxon(taxonFaunaEu, taxonErms);
			moveAllInformationsFromFaunaEuToErms(taxonFaunaEu, taxonErms);
			moveOriginalDbToErmsTaxon(taxonFaunaEu, taxonErms);
			//neue sec Referenz an das ErmsTaxon oder an das Synonym und Taxon oder nur Synonym??
			deleteFaunaEuTaxon(taxonFaunaEu);
			
		}
		
		
		
	
	}
	
	
	
	private void updateNameRelationships(List<List<String>> ermsAccFaEuSyn){
		//suche alle NameRelationships aus FaunaEu und Erms, wo (faunaEu)relatedFrom.name.titleCache = (erms)relatedFrom.name.titleCache und ersetze in der faunaEu Relationship den relatedTo.name durch den relatedTo.name der erms-relationship
		//wenn es diese relationship noch nicht gibt und der typ der gleiche ist!!
		//wenn der relatedTo Name zu einem Erms Taxon und einem FaunaEu Synonym geh�rt
		
		Synonym synFaunaEu;
		Taxon taxonErms;
		for (List<String> row: ermsAccFaEuSyn){
			synFaunaEu = (Synonym)appCtrInit.getTaxonService().find(UUID.fromString(row.get(faunaEuUuid)));
			taxonErms = (Taxon)appCtrInit.getTaxonService().find(UUID.fromString(row.get(ermsUuid)));
			List<NameRelationship> relSynFaunaEu = appCtrInit.getNameService().listToNameRelationships(synFaunaEu.getName(), null, 100, 0, null, null);
			List<NameRelationship> relTaxonErms = appCtrInit.getNameService().listToNameRelationships(taxonErms.getName(), null, 100, 0, null, null);
			
			List<NameRelationship> deleteRel = new ArrayList<NameRelationship>();
			for (NameRelationship relFauEu: relSynFaunaEu){
				boolean createNewRelationship = true;
				for (NameRelationship relErms: relTaxonErms){
					if ((relErms.getFromName().getTitleCache().equals(relFauEu.getFromName().getTitleCache())) && (relErms.getToName().getTitleCache().equals(relFauEu.getFromName().getTitleCache()))){
						//delete the faunaEu relationship because there exist an analogous relationship in erms
						deleteRel.add(relFauEu);
						createNewRelationship = false;
						break;
					}
				}
				if (createNewRelationship){
					//if relationship does not exist, create a new one with erms synonym
					taxonErms.getName().addRelationshipFromName(relFauEu.getFromName(), relFauEu.getType(), relFauEu.getRuleConsidered());
				}
			}
			
		}
	}
	
	private void updateSynonymRelationships(List<List<String>> ermsSynFaEuAcc){
//		-- Update queries for RelTaxon (synonym relationships - move relationships to ERMS accepted taxon if not already existent or delete if already existent)
//		UPDATE RelTaxon_1 SET RelTaxon_1.TaxonFk2 = RT.TaxonFk2
//		FROM         Taxon AS ERMSSyn INNER JOIN
//		                      Taxon AS FaEuAcc ON ERMSSyn.RankFk = FaEuAcc.RankFk AND ERMSSyn.FullName = FaEuAcc.FullName AND
//		                      ERMSSyn.TaxonStatusFk <> ISNULL(FaEuAcc.TaxonStatusFk, 0) INNER JOIN
//		                      RelTaxon AS RT ON ERMSSyn.TaxonId = RT.TaxonFk1 INNER JOIN
//		                      RelTaxon AS RelTaxon_1 ON FaEuAcc.TaxonId = RelTaxon_1.TaxonFk2 INNER JOIN
//		                      Taxon AS FaEuSyn ON RelTaxon_1.TaxonFk1 = FaEuSyn.TaxonId LEFT OUTER JOIN
//		                      Taxon AS ERMSAllSyn ON RT.TaxonFk1 = ERMSAllSyn.TaxonId AND FaEuSyn.FullName <> ERMSAllSyn.FullName --(!!)
//		WHERE     (ERMSSyn.OriginalDB = N'ERMS') AND (RT.RelTaxonQualifierFk > 100) AND (ERMSSyn.TaxonStatusFk <> 1) AND (ERMSSyn.KingdomFk = 2) AND
//		                      (FaEuAcc.OriginalDB = N'FaEu') AND (RelTaxon_1.RelTaxonQualifierFk > 100)
		Taxon taxonFaunaEu;
		Synonym synErms;
		Taxon taxonErms;
		Set<Taxon> acceptedTaxa = new HashSet<Taxon>();
		for (List<String> row: ermsSynFaEuAcc){
			taxonFaunaEu = (Taxon)appCtrInit.getTaxonService().find(UUID.fromString(row.get(faunaEuUuid)));
			synErms = (Synonym)appCtrInit.getTaxonService().find(UUID.fromString(row.get(ermsUuid)));
			acceptedTaxa.clear();
			acceptedTaxa.addAll( synErms.getAcceptedTaxa());
			if (!acceptedTaxa.isEmpty()){
				taxonErms = acceptedTaxa.iterator().next();
				if (acceptedTaxa.size() > 1){
					logger.debug("There are more than one accepted taxon for synonym " + synErms.getTitleCache());
				}
			}else{
				taxonErms = null;
				logger.debug("There is no accepted taxon for synonym "  + synErms.getTitleCache());
			}
			
			if (taxonErms != null){
				List<SynonymRelationship> relTaxonFaunaEu = appCtrInit.getTaxonService().listSynonymRelationships(taxonFaunaEu, null, 100, 0, null, null, Direction.relatedTo);
				List<SynonymRelationship> relTaxonErms = appCtrInit.getTaxonService().listSynonymRelationships(taxonErms, null, 100, 0, null, null, Direction.relatedTo);
				
				List<SynonymRelationship> deleteRel = new ArrayList<SynonymRelationship>();
				for (SynonymRelationship relFauEu: relTaxonFaunaEu){
					//TODO: wenn es noch keine SynonymRelationship gibt zu einem Synonym mit gleichem Namen, dann erzeuge die SynonymRelationship vom FaunaEuSyn (des FaunaEu Taxons, dass identischen Namen hat) zum akzeptierten Taxon des Erms Syn
					boolean createNewRelationship = true;
					for (SynonymRelationship relErms: relTaxonErms){
						if (relErms.getSynonym().getTitleCache().equals(relFauEu.getSynonym().getTitleCache())){
							//es gibt schon eine Relationship zu einem Synonym mit dem gleichen Namen wie das FaunaEu Synonym, also Relationship l�schen.
							createNewRelationship = false;
							break;
						}
					}
					if (createNewRelationship){
						taxonErms.addSynonym(relFauEu.getSynonym(), relFauEu.getType());
					}
					
					deleteRel.add(relFauEu);
				}
			}
		
		}
	}
	
	
	private void deleteFaunaEuTaxon(Taxon taxonFaunaEu) {
		appCtrInit.getTaxonService().delete(taxonFaunaEu);
		
	}

	//wenn Name und Rang identisch sind und auch der Status gleich, dann alle Informationen vom Fauna Europaea Taxon/Synonym zum Erms Taxon/Synonym
	
	private void moveAllInformationsFromFaunaEuToErms(TaxonBase faunaEu, TaxonBase erms){
		Set<Annotation> annotations = faunaEu.getAnnotations();
		Set<Extension> extensions = faunaEu.getExtensions();
		Set<Marker> markers = faunaEu.getMarkers();
		List<Credit> credits = faunaEu.getCredits();
		if (faunaEu instanceof Taxon){
			Set<TaxonDescription> descriptions = ((Taxon)faunaEu).getDescriptions();
			Set<Taxon> misappliedNames = ((Taxon)faunaEu).getMisappliedNames();
			
			if (erms instanceof Taxon){
				Iterator<TaxonDescription> descriptionsIterator = descriptions.iterator();
				TaxonDescription description;
				while (descriptionsIterator.hasNext()){
					description = descriptionsIterator.next();
					((Taxon) erms).addDescription(description);
				}
				
				Iterator<Taxon> misappliedNamesIterator = misappliedNames.iterator();
				Taxon misappliedName;
				while (misappliedNamesIterator.hasNext()){
					misappliedName = misappliedNamesIterator.next();
					((Taxon) erms).addMisappliedName(misappliedName, null, null);
				}
			}
		}
		
		//move all these informations to the erms taxon
		Iterator<Annotation> annotationsIterator = annotations.iterator();
		Annotation annotation;
		while (annotationsIterator.hasNext()){
			annotation = annotationsIterator.next();
			erms.addAnnotation(annotation);
		}
		
		Iterator<Extension> extensionIterator = extensions.iterator();
		Extension extension;
		while (extensionIterator.hasNext()){
			extension = extensionIterator.next();
			erms.addExtension(extension);
		}
		
		Iterator<Marker> markerIterator = markers.iterator();
		Marker marker;
		while (markerIterator.hasNext()){
			marker = markerIterator.next();
			erms.addMarker(marker);
		}
		
		for (Credit credit: credits){
			erms.addCredit(credit);
		}
		
		
	}
	
	//if name, rank, and status (accepted) are the same, then move the synonyms of faunaEu taxon to the erms taxon
	
	private void moveFaunaEuSynonymsToErmsTaxon(Taxon faunaEu, Taxon erms){
		Set<SynonymRelationship> synRel =faunaEu.getSynonymRelations();
		Iterator<SynonymRelationship> synRelIterator = synRel.iterator();
		SynonymRelationship rel;
		while (synRelIterator.hasNext()){
			rel = synRelIterator.next();
			faunaEu.removeSynonym(rel.getSynonym());
			erms.addSynonym(rel.getSynonym(), rel.getType());
		}
	}
	
	//after merging faunaEu taxon and erms taxon, the originalSource of the faunaEu taxon has to be moved to the erms taxon
	private void moveOriginalDbToErmsTaxon(TaxonBase faunaEu, TaxonBase erms){
		Set<IdentifiableSource> sourcesFaunaEu = faunaEu.getSources();
		IdentifiableSource sourceFaunaEu = sourcesFaunaEu.iterator().next();
		erms.addSource(sourceFaunaEu);
	}
	
	//merged taxon should have a new sec reference
	private void addNewSecForMergedTaxon(Taxon taxon, Reference sec){
		taxon.setSec(sec);
		taxon.setUuid(UUID.randomUUID());
	}
	
	// ----------- methods for merging Erms synonyms and Fauna Europaea Taxon
	
	
	
	
}
