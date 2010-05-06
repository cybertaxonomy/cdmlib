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

import common.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

public class FaunaEuErmsMergeActivator {
	
	static final ICdmDataSource faunaEuropaeaSource = CdmDestinations.cdm_test_patricia();
	
	static final int faunaEuUuid = 0;
	static final int ermsUuid = 9;
	private static final Logger logger = Logger.getLogger(FaunaEuErmsMergeActivator.class);
	
	//csv files starting with...
	static String sFileName = "c:\\test";
	
	private CdmApplicationController initDb(ICdmDataSource db) {

		// Init source DB
		CdmApplicationController appCtrInit = null;

		appCtrInit = TestDatabase.initDb(db, DbSchemaValidation.VALIDATE, false);

		return appCtrInit;
	}
	
	public static void main(String[] args) {
		
		FaunaEuErmsMergeActivator sc = new FaunaEuErmsMergeActivator();
		
		CdmApplicationController appCtrFaunaEu = sc.initDb(faunaEuropaeaSource);
			
		mergeAuthors(appCtrFaunaEu);
		
		//set the ranks of Agnatha and Gnathostomata to 50 instead of 45
		List<TaxonBase> taxaToChangeRank = new ArrayList<TaxonBase>();
		Pager<TaxonBase> agnatha = appCtrFaunaEu.getTaxonService().findTaxaByName(TaxonBase.class, "Agnatha", null, null, null, Rank.INFRAPHYLUM(), 10, 0);
		List<TaxonBase> agnathaList = agnatha.getRecords();
		taxaToChangeRank.addAll(agnathaList);
		Pager<TaxonBase> gnathostomata = appCtrFaunaEu.getTaxonService().findTaxaByName(TaxonBase.class, "Gnathostomata", null, null, null, Rank.INFRAPHYLUM(), 10, 0);
		List<TaxonBase> gnathostomataList = gnathostomata.getRecords();
		taxaToChangeRank.addAll(gnathostomataList);
		
		setSpecificRank(taxaToChangeRank,Rank.SUPERCLASS());
		
		//ermsTaxon is accepted, fauna eu taxon is synonym
		
		
		
		
		
		
		//change the relationships to the one used in erms
		//if the taxonomic status differs, take the one of erms
		// -> fauna europaea akzeptiert, erms synonym -> 
		
		
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
	
	
	private static void mergeAuthors(CdmApplicationController appCtrFaunaEu){
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
			taxonFaunaEu = appCtrFaunaEu.getTaxonService().find(uuidFaunaEu);
			taxonErms = appCtrFaunaEu.getTaxonService().find(uuidFaunaEu);
			
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
	
	public static void setSpecificRank(List<TaxonBase> taxa, Rank rank){
		
		for (TaxonBase taxon: taxa){
			taxon.getName().setRank(rank);
		}
	}
	
	private static void mergeDiffStatus(CdmApplicationController appCtrFaunaEu){
		List<List<String>> diffStatus = readCsvFile(sFileName + "_status.csv");
		
		//find all taxa accepted in erms, but synonyms in FauEu  and the same rank
		List<List<String>> accErmsSynFaunaEu = new ArrayList<List<String>>();
		for (List<String> rowList: diffStatus){
			if ((rowList.get(5).equals("synonym")) && (rowList.get(4).equals(rowList.get(12)))){
				//both conditions are true
				accErmsSynFaunaEu.add(rowList);
			}
		}
		mergeErmsAccFaEuSyn(appCtrFaunaEu, accErmsSynFaunaEu);
		
	
	}
	
	private static void mergeErmsAccFaEuSyn(CdmApplicationController appCtrFaunaEu, List<List<String>> ermsAccFaEuSyn){
		
		//finde NameRelationships, jeweils identischen Namen (titleCache) der beiden Homonyme
		 
		/*
UPDATE RT SET TaxonFk2 = ERMSAcc.TaxonId
FROM         RelTaxon AS RT INNER JOIN
                      Taxon AS FaEuSyn ON RT.TaxonFk2 = FaEuSyn.TaxonId INNER JOIN
                      Taxon AS ERMSAcc ON FaEuSyn.RankFk = ERMSAcc.RankFk AND FaEuSyn.FullName = ERMSAcc.FullName AND ISNULL(FaEuSyn.TaxonStatusFk, 0)
                      <> ERMSAcc.TaxonStatusFk INNER JOIN
                      Taxon AS FaEuHom ON RT.TaxonFk1 = FaEuHom.TaxonId INNER JOIN
                      RelTaxon AS RelTaxon_1 ON ERMSAcc.TaxonId = RelTaxon_1.TaxonFk2 LEFT OUTER JOIN
                      Taxon AS ERMSHom ON RelTaxon_1.TaxonFk1 = ERMSHom.TaxonId AND FaEuHom.FullName = ERMSHom.FullName
WHERE     (ERMSAcc.OriginalDB = N'ERMS') AND (FaEuSyn.OriginalDB = N'FaEu') AND (RT.RelTaxonQualifierFk < 100) AND (ERMSAcc.TaxonStatusFk = 1) AND
                      (ERMSAcc.KingdomFk = 2) AND (RelTaxon_1.RelTaxonQualifierFk < 100)
                      AND NOT Exists (SELECT * FROM RelTaxon WHERE TaxonFk1 = ERMSHom.TaxonId AND TaxonFk2 = ERMSAcc.TaxonId AND RelTaxonQualifierFk = RT.RelTaxonQualifierFk)

					  
					  
DELETE RT
FROM         RelTaxon AS RT INNER JOIN
                      Taxon AS FaEuSyn ON RT.TaxonFk2 = FaEuSyn.TaxonId INNER JOIN
                      Taxon AS ERMSAcc ON FaEuSyn.RankFk = ERMSAcc.RankFk AND FaEuSyn.FullName = ERMSAcc.FullName AND ISNULL(FaEuSyn.TaxonStatusFk, 0)
                      <> ERMSAcc.TaxonStatusFk INNER JOIN
                      Taxon AS FaEuHom ON RT.TaxonFk1 = FaEuHom.TaxonId INNER JOIN
                      RelTaxon AS RelTaxon_1 ON ERMSAcc.TaxonId = RelTaxon_1.TaxonFk2 LEFT OUTER JOIN
                      Taxon AS ERMSHom ON RelTaxon_1.TaxonFk1 = ERMSHom.TaxonId AND FaEuHom.FullName = ERMSHom.FullName
WHERE     (ERMSAcc.OriginalDB = N'ERMS') AND (FaEuSyn.OriginalDB = N'FaEu') AND (RT.RelTaxonQualifierFk < 100) AND (ERMSAcc.TaxonStatusFk = 1) AND
                      (ERMSAcc.KingdomFk = 2) AND (RelTaxon_1.RelTaxonQualifierFk < 100)
                      AND Exists (SELECT * FROM RelTaxon WHERE TaxonFk1 = ERMSHom.TaxonId AND TaxonFk2 = ERMSAcc.TaxonId AND RelTaxonQualifierFk = RT.RelTaxonQualifierFk)

		 * 
		 */
		//lösche alle SynonymRelationships von dem FaunaEu Syn
		
		for (List<String> rowList: ermsAccFaEuSyn){
			UUID faunaUUID = UUID.fromString(rowList.get(faunaEuUuid));
			//UUID ermsUUID = UUID.fromString(rowList.get(ermsUuid));
			Synonym syn = (Synonym)appCtrFaunaEu.getTaxonService().find(faunaUUID);
			appCtrFaunaEu.getTaxonService().deleteSynonyms(syn);
			
			
		
		}
		
		
	}
	
	private static void mergeFaunaEuAccErmsSyn (CdmApplicationController appCtrFaunaEu, List<List<String>> ermsAccFaEuSyn){
		//occurence: verknüpfe statt dem Fauna Europaea Taxon das akzeptierte Taxon, des Synonyms mit der Occurence (CDM -> distribution)
		//suche distribution (über das Taxon der TaxonDescription), dessen Taxon, das entsprechende Fauna Eu Taxon ist und verknüpfe es mit dem akzeptieren Taxon des Erms Syn
		Taxon taxonFaunaEu = null;;
		Taxon taxonErms = null;
		Synonym synErms = null;
		for (List<String> row: ermsAccFaEuSyn){
			taxonFaunaEu = (Taxon)appCtrFaunaEu.getTaxonService().find(UUID.fromString(row.get(faunaEuUuid)));
			synErms = (Synonym)appCtrFaunaEu.getTaxonService().find(UUID.fromString(row.get(ermsUuid)));
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
			List<DescriptionElementBase> distributions = appCtrFaunaEu.getDescriptionService().getDescriptionElementsForTaxon(taxonFaunaEu, features, Distribution.class, 10, 0, null);
			
			
			for(DescriptionElementBase distribution: distributions){
				TaxonDescription description = (TaxonDescription)distribution.getInDescription();
				TaxonDescription newDescription = TaxonDescription.NewInstance(taxonErms);
				newDescription.addElement(distribution);
				appCtrFaunaEu.getDescriptionService().delete(description);
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
			moveFaunaEuSynonymsToErmsTaxon(taxonFaunaEu, taxonErms);
			moveOriginalDbToErmsTaxon(taxonFaunaEu, taxonErms);
			
		}
		
		//NameRelations finden -> nochmal Marc fragen!!
		//Status des FaunaEu Taxon in Synonym umwandeln. oder gleich die Daten übertragen und löschen?
		//alle Synonyme des Taxons an das akzeptierte Taxon von Erms hängen und bei Fauna Eu löschen
		
		
	
	}
	
	//wenn Name und Rang identisch sind und auch der Status gleich, dann alle Informationen vom Fauna Europaea Taxon/Synonym zum Erms Taxon/Synonym
	
	private static void moveAllInformationsFromFaunaEuToErms(TaxonBase faunaEu, TaxonBase erms){
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
	
	//wenn Name, Rang und Status (=1) gleich sind, alle Synonyme zum ErmsTaxon
	
	private static void moveFaunaEuSynonymsToErmsTaxon(Taxon faunaEu, Taxon erms){
		Set<SynonymRelationship> synRel =faunaEu.getSynonymRelations();
		Iterator<SynonymRelationship> synRelIterator = synRel.iterator();
		SynonymRelationship rel;
		while (synRelIterator.hasNext()){
			rel = synRelIterator.next();
			faunaEu.removeSynonym(rel.getSynonym());
			erms.addSynonym(rel.getSynonym(), rel.getType());
		}
	}
	
	private static void moveOriginalDbToErmsTaxon(TaxonBase faunaEu, TaxonBase erms){
		Set<IdentifiableSource> sourcesFaunaEu = faunaEu.getSources();
		IdentifiableSource sourceFaunaEu = sourcesFaunaEu.iterator().next();
		erms.addSource(sourceFaunaEu);
	}
	
	
	
	
}
