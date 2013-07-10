package eu.etaxonomy.cdm.app.pesi.merging;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.common.TestDatabase;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.pesi.merging.FaunaEuErmsMerging;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

public class FaunaEuErmsFindIdenticalNamesActivator {

	static final ICdmDataSource faunaEuropaeaSource = CdmDestinations.localH2();
	//static final ICdmDataSource ermsSource = CdmDestinations.cdm_test_andreasM();
	
	//TODO hole aus beiden DB alle TaxonNameBases
	
	
	private CdmApplicationController initDb(ICdmDataSource db) {

		// Init source DB
		CdmApplicationController appCtrInit = null;

		appCtrInit = TestDatabase.initDb(db, DbSchemaValidation.VALIDATE, false);

		return appCtrInit;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		FaunaEuErmsFindIdenticalNamesActivator sc = new FaunaEuErmsFindIdenticalNamesActivator();
		
		CdmApplicationController appCtrFaunaEu = sc.initDb(faunaEuropaeaSource);
		String sFileName = "c:\\test";
		//CdmApplicationController appCtrErms = sc.initDb(ermsSource);
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("sources.*");
		propertyPaths.add("sources.idInSource");
		propertyPaths.add("sources.idNamespace");
		propertyPaths.add("taxonBases.*");
		propertyPaths.add("taxonBases.relationsFromThisTaxon");
		propertyPaths.add("taxonBases.taxonNodes.*");
		propertyPaths.add("taxonBases.taxonNodes.parent.*");
		propertyPaths.add("taxonBases.taxonNodes.parent.taxon.name.*");
		System.err.println("Start getIdenticalNames...");
		List<TaxonNameBase> namesOfIdenticalTaxa = appCtrFaunaEu.getTaxonService().findIdenticalTaxonNameIds(propertyPaths);
		//List<UUID> namesOfIdenticalTaxa = appCtrFaunaEu.getTaxonService().findIdenticalTaxonNameIds(propertyPaths);
		
		System.err.println("first name: " + namesOfIdenticalTaxa.get(0) + " " + namesOfIdenticalTaxa.size());
		TaxonNameBase zooName = (TaxonNameBase)namesOfIdenticalTaxa.get(0);
		System.err.println(zooName + " nr of taxa " + namesOfIdenticalTaxa.size());
		//TaxonNameComparator taxComp = new TaxonNameComparator();
		
		//Collections.sort(namesOfIdenticalTaxa,taxComp);
		System.err.println(namesOfIdenticalTaxa.get(0) + " - " + namesOfIdenticalTaxa.get(1) + " - " + namesOfIdenticalTaxa.get(2));
		List<FaunaEuErmsMerging> mergingObjects = new ArrayList<FaunaEuErmsMerging>();
		FaunaEuErmsMerging mergeObject;
		TaxonNameBase faunaEuTaxName;
		TaxonNameBase ermsTaxName;
				
		mergingObjects= sc.createMergeObjects(namesOfIdenticalTaxa, appCtrFaunaEu);
		
		sc.writeSameNamesdifferentAuthorToCsv(mergingObjects, sFileName + "_authors.csv");
		sc.writeSameNamesdifferentStatusToCsv(mergingObjects, sFileName + "_status.csv");
		sc.writeSameNamesToCsVFile(mergingObjects, sFileName + "_names.csv");
		sc.writeSameNamesdifferentPhylumToCsv(mergingObjects, sFileName + "_phylum.csv");
		
		
		System.out.println("End merging Fauna Europaea and Erms");
		
	}
	
	private boolean writeSameNamesToCsVFile(
			List<FaunaEuErmsMerging> mergingObjects, String string) {
	    try{
		FileWriter writer = new FileWriter(string);
	
	    //create Header
	    String firstLine = "same names";
	    createHeader(writer, firstLine);
		for (FaunaEuErmsMerging merging : mergingObjects){
	    	writeCsvLine(writer, merging) ;
		}
		writer.flush();
		writer.close();
	}
	catch(IOException e)
	{
	 return false;
	} 
	return true;
	}


	private boolean writeSameNamesdifferentPhylumToCsv(List<FaunaEuErmsMerging> mergingObjects, String sfileName){
		try
		{
		    FileWriter writer = new FileWriter(sfileName);
		    
		    //create Header
		   String firstLine = "same names but different phylum";
		   createHeader(writer, firstLine);
		    
			//write data
			for (FaunaEuErmsMerging merging : mergingObjects){
		    	//TODO
				if ((merging.getPhylumInErms()== null )^ (merging.getPhylumInFaunaEu()== null)){
					writeCsvLine(writer, merging) ;
				}else if(!((merging.getPhylumInErms()==null) && (merging.getPhylumInFaunaEu()==null))){ 
					if(!merging.getPhylumInErms().equals(merging.getPhylumInFaunaEu())){
						writeCsvLine(writer, merging) ;
					}
				}
			}
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
		 return false;
		} 
		return true;
	}
	
	private boolean writeSameNamesdifferentRankToCsv(List<FaunaEuErmsMerging> mergingObjects, String sfileName){
		try
		{
		    FileWriter writer = new FileWriter(sfileName);
		    String firstLine = "same names but different rank";
		    //create Header
		    createHeader(writer, firstLine);
			
			//write data
			for (FaunaEuErmsMerging merging : mergingObjects){
		    	
				if (!merging.getRankInErms().equals(merging.getRankInFaunaEu())){
					writeCsvLine(writer, merging);
				}
			}
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
		 return false;
		} 
		return true;
	}
	
	private void createHeader(FileWriter writer, String firstLine) throws IOException{
		 	writer.append(firstLine);
		    writer.append('\n');
		    writer.append("uuid in Fauna Europaea");
			writer.append(';');
			writer.append("id in Fauna Europaea");
			writer.append(';');
			writer.append("name");
			writer.append(';');
			writer.append("author");
			writer.append(';');
			writer.append("rank");
			writer.append(';');
			writer.append("state");
			writer.append(';');
			writer.append("phylum");
			writer.append(';');
			writer.append("parent");
			writer.append(';');
			writer.append("parent rank");
			writer.append(';');
			
			writer.append("uuid in Erms");
			writer.append(';');
			writer.append("id in Erms");
			writer.append(';');
			writer.append("name");
			writer.append(';');
			writer.append("author");
			writer.append(';');
			writer.append("rank");
			writer.append(';');
			writer.append("state");
			writer.append(';');
			writer.append("phylum");
			writer.append(';');
			writer.append("parent");
			writer.append(';');
			writer.append("parent rank");
			writer.append('\n');
	}
	
	private boolean writeSameNamesdifferentStatusToCsv(List<FaunaEuErmsMerging> mergingObjects, String sfileName){
		try
		{
		    FileWriter writer = new FileWriter(sfileName);
		    
		    //create Header
		    String firstLine = "same names but different status";
		    createHeader(writer, firstLine);
		    
			//write data
			for (FaunaEuErmsMerging merging : mergingObjects){
		    	
				if (merging.isStatInErms()^merging.isStatInFaunaEu()){
					 writeCsvLine(writer, merging);
				}
			}
			
 
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
		 return false;
		} 
		return true;
	}
	
	private boolean writeSameNamesdifferentAuthorToCsv(List<FaunaEuErmsMerging> mergingObjects, String sfileName){
		try
		{
		    FileWriter writer = new FileWriter(sfileName);
		    
		    //create Header
		   String firstLine = "same names but different authors";
		   createHeader(writer, firstLine);
		    
			//write data
			for (FaunaEuErmsMerging merging : mergingObjects){
		    	
				if (!merging.getAuthorInErms().equals(merging.getAuthorInFaunaEu())){
					 writeCsvLine(writer, merging);
				}
			}
			
 
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
		 return false;
		} 
		return true;
	}
	
	private void writeCsvLine(FileWriter writer, FaunaEuErmsMerging merging) throws IOException{
		
		writer.append(merging.getUuidFaunaEu());
		writer.append(';');
		writer.append(merging.getIdInFaunaEu());
		writer.append(';');
		writer.append(merging.getNameCacheInFaunaEu());
		writer.append(';');
		writer.append(merging.getAuthorInFaunaEu());
		writer.append(';');
		writer.append(merging.getRankInFaunaEu());
		writer.append(';');
		if (merging.isStatInFaunaEu()){
			writer.append("accepted");
		}else{
			writer.append("synonym");
		}
		writer.append(';');
		writer.append(merging.getPhylumInFaunaEu());
		writer.append(';');
		writer.append(merging.getParentStringInFaunaEu());
		writer.append(';');
		writer.append(merging.getParentRankStringInFaunaEu());
		writer.append(';');
		
		writer.append(merging.getUuidErms());
		writer.append(';');
		writer.append(merging.getIdInErms());
		writer.append(';');
		writer.append(merging.getNameCacheInErms());
		writer.append(';');
		writer.append(merging.getAuthorInErms());
		writer.append(';');
		writer.append(merging.getRankInErms());
		writer.append(';');
		if (merging.isStatInErms()){
			writer.append("accepted");
		}else{
			writer.append("synonym");
		}
		
		writer.append(';');
		writer.append(merging.getPhylumInErms());
		writer.append(';');
		writer.append(merging.getParentStringInErms());
		writer.append(';');
		writer.append(merging.getParentRankStringInErms());
		writer.append('\n');
	}
	
	
	private List<FaunaEuErmsMerging> createMergeObjects(List<TaxonNameBase> names, CdmApplicationController appCtr){
		
		List<FaunaEuErmsMerging> merge = new ArrayList<FaunaEuErmsMerging>();
		ZoologicalName zooName, zooName2;
		FaunaEuErmsMerging mergeObject;
		String idInSource1;
		for (int i = 0; i<names.size()-1; i=i+2){
			zooName = (ZoologicalName)names.get(i);
			zooName2 = (ZoologicalName)names.get(i+1);
			mergeObject = new FaunaEuErmsMerging();
			//TODO:überprüfen, ob die beiden Namen identisch sind und aus unterschiedlichen DB kommen
			
			//getPhylum
			String phylum1 = null;
			if (!zooName.getRank().isHigher(Rank.PHYLUM())){
				phylum1 =appCtr.getTaxonService().getPhylumName(zooName);
			}
			
			String phylum2 = null;
			if (!zooName2.getRank().isHigher(Rank.PHYLUM())){
				phylum2 = appCtr.getTaxonService().getPhylumName(zooName2);
			}
			mergeObject.setPhylumInErms(phylum1);
			mergeObject.setPhylumInFaunaEu(phylum2);
			
			//getUuids
			mergeObject.setUuidErms(zooName.getUuid().toString());
			mergeObject.setUuidFaunaEu(zooName.getUuid().toString());
			
			Iterator sources = zooName.getSources().iterator();
			if (sources.hasNext()){
				IdentifiableSource source = (IdentifiableSource)sources.next();
				idInSource1 = source.getIdInSource();
				mergeObject.setIdInErms(idInSource1);
			}
			sources = zooName2.getSources().iterator();
			if (sources.hasNext()){
				IdentifiableSource source = (IdentifiableSource)sources.next();
				idInSource1 = source.getIdInSource();
				mergeObject.setIdInFaunaEu(idInSource1);
			}
			
			mergeObject.setNameCacheInErms(zooName.getNameCache());
			mergeObject.setNameCacheInFaunaEu(zooName2.getNameCache());
			
			mergeObject.setAuthorInErms(zooName.getAuthorshipCache());
			mergeObject.setAuthorInFaunaEu(zooName2.getAuthorshipCache());
			Set<Taxon> taxa = zooName.getTaxa();
			if (!taxa.isEmpty()){
				mergeObject.setStatInErms(true);
				Iterator taxaIterator = taxa.iterator();
				Taxon taxon = null;
				while (taxaIterator.hasNext()){
					taxon = (Taxon) taxaIterator.next();
					if (!taxon.isMisapplication()){
						break;
					}
				}
				Set<TaxonNode> nodes = taxon.getTaxonNodes();
				Iterator taxonNodeIterator = nodes.iterator();
				TaxonNode node, parentNode = null;
				while (taxonNodeIterator.hasNext()){
					node = (TaxonNode)taxonNodeIterator.next();
					if (!node.isTopmostNode()){
						parentNode = node.getParent();
					}
				}
				//TODO: ändern mit erweitertem Initializer..
				if (parentNode != null){
					ZoologicalName parentName = HibernateProxyHelper.deproxy(parentNode.getTaxon().getName(), ZoologicalName.class);
					String parentNameCache = parentName.getNameCache();
					mergeObject.setParentStringInErms(parentNameCache);
					mergeObject.setParentRankStringInErms(parentName.getRank().getLabel());
					//System.err.println("parentName: " + parentNameCache);
				}
			}else{
				mergeObject.setStatInErms(false);
			}
			taxa = zooName2.getTaxa();
			if (!taxa.isEmpty()){
				mergeObject.setStatInFaunaEu(true);
				Iterator taxaIterator = taxa.iterator();
				Taxon taxon = null;
				while (taxaIterator.hasNext()){
					taxon = (Taxon) taxaIterator.next();
					if (!taxon.isMisapplication()){
						break;
					}
				}
				Set<TaxonNode> nodes = taxon.getTaxonNodes();
				Iterator taxonNodeIterator = nodes.iterator();
				TaxonNode node, parentNode = null;
				while (taxonNodeIterator.hasNext()){
					node = (TaxonNode)taxonNodeIterator.next();
					if (!node.isTopmostNode()){
						parentNode = node.getParent();
					}
				}
				//TODO: ändern mit erweitertem Initializer..
				if (parentNode != null){
					if (parentNode.getTaxon().getName() instanceof ZoologicalName){
					
					ZoologicalName parentName = HibernateProxyHelper.deproxy(parentNode.getTaxon().getName(), ZoologicalName.class);
					String parentNameCache = parentName.getNameCache();
					mergeObject.setParentStringInFaunaEu(parentNameCache);
					mergeObject.setParentRankStringInFaunaEu(parentName.getRank().getLabel());
					System.err.println("parentName: " + parentNameCache);
					}else{
						System.err.println("no zoologicalName: " + parentNode.getTaxon().getName().getTitleCache() +" . "+parentNode.getTaxon().getName().getUuid());
					}
					
				}
			}else{
				mergeObject.setStatInErms(false);
			}
			taxa = zooName2.getTaxa();
			if (!taxa.isEmpty()){
				mergeObject.setStatInFaunaEu(true);
			}else{
				mergeObject.setStatInFaunaEu(false);
				
			}
			
			mergeObject.setRankInErms(zooName.getRank().getLabel());
			mergeObject.setRankInFaunaEu(zooName2.getRank().getLabel());
			
			
			
			
			//set parent informations
			
			
			/*
			Set<HybridRelationship> parentRelations = zooName.getParentRelationships();
			Iterator parentIterator = parentRelations.iterator();
			HybridRelationship parentRel;
			ZoologicalName parentName;
			while (parentIterator.hasNext()){
				parentRel = (HybridRelationship)parentIterator.next();
				parentName = (ZoologicalName)parentRel.getParentName();
				mergeObject.setParentRankStringInErms(parentName.getRank().getLabel());
				mergeObject.setParentStringInErms(parentName.getNameCache());
			}
			
			parentRelations = zooName2.getParentRelationships();
			parentIterator = parentRelations.iterator();
		
			while (parentIterator.hasNext()){
				parentRel = (HybridRelationship)parentIterator.next();
				parentName = (ZoologicalName)parentRel.getParentName();
				mergeObject.setParentRankStringInFaunaEu(parentName.getRank().getLabel());
				mergeObject.setParentStringInFaunaEu(parentName.getNameCache());
			}*/
			merge.add(mergeObject);
		}
		
		return merge;
		
		
	}
	
}
