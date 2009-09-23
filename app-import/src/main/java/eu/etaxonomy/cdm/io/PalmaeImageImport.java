/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata.Item;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.app.images.AbstractImageImporter;
import eu.etaxonomy.cdm.app.images.ImageImportConfigurator;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsTerm;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;

/**
 * TODO not working at the moment
 * 
 * @author n.hoffmann
 * @created 18.11.2008
 * @version 1.0
 */
@Component
public class PalmaeImageImport extends AbstractImageImporter {
	private static final Logger logger = Logger.getLogger(PalmaeImageImport.class);
	
	enum MetaData{
		NAME,
		ARTIST,
		COPYRIGHT,
		COPYRIGHTNOTICE
	}
	
	private static int modCount = 300;

	
	/**
	 * Rudimetary implementation using apache sanselan. This implementation depends
	 * on the metadata standards used in the palmae images. The IPTC field ObjectName
	 * contains a string like this: "Arecaceae; Eugeissona utilis". The string 
	 * in front of the semicolon is the family name and the one behind, the taxon name.
	 * So we basically assume, that if the string gets split by ";" the element at 
	 * index 1 should be the taxon name.
	 * If this format changes this method breaks!
	 * 
	 * TODO The ImageMetaData class of the commons package should provide 
	 * convenient access to the metadata of an image as well as all the error handling
	 * 
	 * @param imageFile
	 * @return the name of the taxon as stored in ObjectName IPTC tag
	 */
	public String retrieveTaxonNameFromImageMetadata(File imageFile){
		String name = null;
		
		IImageMetadata metadata = null;
		
		try {
			metadata = Sanselan.getMetadata(imageFile);
		} catch (ImageReadException e) {
			logger.error("Error reading image" + " in " + imageFile.getName(), e);
		} catch (IOException e) {
			logger.error("Error reading file"  + " in " + imageFile.getName(), e);
		}
		
		if(metadata instanceof JpegImageMetadata){
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

			for (Object object : jpegMetadata.getItems()){
				
				Item item = (Item) object;
				//System.out.println(item.getKeyword());
				
				
				if(item.getKeyword().equals("ObjectName")){
					logger.info("File: " + imageFile.getName() + ". ObjectName string is: " + item.getText());
					String[] objectNameSplit = item.getText().split(";");
					
					try {
						name = objectNameSplit[1].trim();
					} catch (ArrayIndexOutOfBoundsException e) {
						logger.warn("ObjectNameSplit has no second part: " + item.getText() + " in " + imageFile.getName());
						//throw e;
					}
				}
			}
		}
		
		
		return name;
	}
	
	public Map<MetaData, String> getMetaData(File imageFile, List<MetaData> metaData){
		HashMap result = new HashMap();
		
		IImageMetadata metadata = null;
		List<String> metaDataStrings = new ArrayList<String>();
		
		for (MetaData data: metaData){
			metaDataStrings.add(data.name().toLowerCase());
		}
			
		
		try {
			metadata = Sanselan.getMetadata(imageFile);
		} catch (ImageReadException e) {
			logger.error("Error reading image" + " in " + imageFile.getName(), e);
		} catch (IOException e) {
			logger.error("Error reading file"  + " in " + imageFile.getName(), e);
		}
		
		
		
		if(metadata instanceof JpegImageMetadata){
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			
			int counter = 0;
				for (Object object : jpegMetadata.getItems()){
					Item item = (Item) object;
					
					if(metaDataStrings.contains(item.getKeyword().toLowerCase())){
						logger.info("File: " + imageFile.getName() + ". "+ item.getKeyword() +"string is: " + item.getText());
						result.put(MetaData.valueOf(item.getKeyword().toUpperCase()), item.getText());
						Set<Entry<MetaData, String>> resultSet = result.entrySet();
							
					}
						
				}
		}
		
		return result;
	}
		
	

	protected boolean invokeImageImport (ImageImportConfigurator config){
		
		logger.info("Importing images from directory: " + config.getSourceNameString());
		File sourceFolder = (File)config.getSource();
		String taxonName;
		if(sourceFolder.isDirectory()){
			int i = 0;
			for( File file : sourceFolder.listFiles()){
				if(file.isFile()){
					if ((i++ % modCount) == 0 && i != 1 ){ logger.info("Images handled: " + (i-1));}
					
					taxonName= retrieveTaxonNameFromImageMetadata(file);
					logger.info("Looking up taxa with taxon name: " + taxonName);
					
					//TODO:
					ArrayList<MetaData> metaDataList = new ArrayList();
					metaDataList.add (MetaData.ARTIST);
					metaDataList.add (MetaData.COPYRIGHT);
					metaDataList.add (MetaData.COPYRIGHTNOTICE);
					//metaDataList.add (MetaData.NAME);
					
					Map<MetaData, String> metaData = getMetaData(file, metaDataList);
					
					
					
					ReferenceBase sec = referenceService.getReferenceByUuid(config.getSecUuid());

					List<TaxonBase> taxa = new ArrayList<TaxonBase>();
					if (taxonName != null){
						taxa = taxonService.searchTaxaByName(taxonName, sec);			
					}else{
						logger.error("TaxonName is null "  + " in " + file.getName());
					}
					if(taxa.size() == 0){
						logger.warn("no taxon with this name found: " + taxonName + " in " + file.getName());
					}else if(taxa.size() > 1){
						logger.error(taxa);
						logger.error("multiple taxa with this name found: " + taxonName + " in " + file.getName());
					}else{
						Taxon taxon = (Taxon) taxa.get(0);
						
						taxonService.saveTaxon(taxon);
						
						TextData feature = TextData.NewInstance();
						
	
						ImageMetaData imageMetaData = new ImageMetaData();
						imageMetaData.readFrom(file);
						
						String mimeType = imageMetaData.getMimeType();
						String suffix = "jpg";
						
						
						// URL for this image
						URL url = null;
						try {
							url = new URL(config.getMediaUrlString() + file.getName());
						} catch (MalformedURLException e) {
							logger.warn("URL is malformed: "+ url);
						}
						
						
						ImageFile imageFile = ImageFile.NewInstance(url.toString(), null, imageMetaData);
						
						
						MediaRepresentation representation = MediaRepresentation.NewInstance(mimeType, suffix);
						representation.addRepresentationPart(imageFile);
						
						Media media = Media.NewInstance();
						media.addRepresentation(representation);
						Person artist = null;
						//TODO: add the rights and the author:
						if (metaData.containsKey(MetaData.ARTIST)){
							//TODO search for the person first and then create the object...
							artist = Person.NewTitledInstance(metaData.get(MetaData.ARTIST).replace("'", ""));
							artist.setFirstname(getFirstName(metaData.get(MetaData.ARTIST)).replace("'", ""));
							artist.setLastname(getLastName(metaData.get(MetaData.ARTIST)).replace("'", ""));
							//System.err.println("Artist-Titlecache: "+artist.getTitleCache());
							IMatchStrategy matchStrategy = DefaultMatchStrategy.NewInstance(AgentBase.class);
							try{
								List<Person> agents = commonService.findMatching(artist, matchStrategy);
								
								if (agents.size()!= 0){
									artist = agents.get(0);
								}
							}catch(eu.etaxonomy.cdm.strategy.match.MatchException e){
								logger.warn("MatchException occurred");
							}
							
							media.setArtist(artist);
						}
						
						if (metaData.containsKey(MetaData.COPYRIGHT)){
							//TODO: maybe search for the identic right... 
							Rights copyright = Rights.NewInstance();
							copyright.setType(RightsTerm.COPYRIGHT());
							Person copyrightOwner;
							if (artist != null && !artist.getLastname().equalsIgnoreCase(getLastName(metaData.get(MetaData.COPYRIGHT)))){
								copyrightOwner = Person.NewInstance();
														
								copyrightOwner.setFirstname(getFirstName(metaData.get(MetaData.COPYRIGHT)));
								copyrightOwner.setLastname(getLastName(metaData.get(MetaData.COPYRIGHT)));
							}else
							{
								copyrightOwner = artist;
							}
							copyright.setAgent(copyrightOwner);
							//IMatchStrategy matchStrategy = DefaultMatchStrategy.NewInstance(Rights.class);
							media.addRights(copyright);
						}
						
						
						
						
						feature.addMedia(media);
						
						feature.setType(Feature.IMAGE());

						TaxonDescription description = TaxonDescription.NewInstance(taxon);
						
						description.setTitleCache("TEST");
						description.addElement(feature);
						taxon.addDescription(description);
						
						taxonService.saveTaxon(taxon);
						
						//descriptionService.saveDescription(description);
						
					}
				}
			}
		}else{
			logger.error("given source folder is not a directory");
		}
		return true;
	}
	
	private String getFirstName(String artist){
		if (artist == null){
			return "";
		}
		if (!artist.contains(" ")) {
			return "";
		}
		if (artist.contains(",")){
			String [] artistSplits = artist.split(",");
			artist = artistSplits[0];
			 
		}
		
		try{
		return artist.substring(0, artist.lastIndexOf(' ')).replace("'", "");
		}catch (Exception e){
			return "";
		}
	}
	
	private String getLastName(String artist){
		
		if (artist.contains(",")){
			String [] artistSplits = artist.split(",");
			artist = artistSplits[0];
			
		}
		if (!artist.contains(" ")) {
			
			return artist;
		}
		try{
		return artist.substring(artist.lastIndexOf(' ')).replace(" ", "");
		}
		catch(Exception e){
			return "";
		}
	}
			
	
//	protected boolean invokeImageImport (IImportConfigurator config){
//		
//		logger.info("importing images from directory: " + config.getSourceNameString());
//		File sourceFolder = (File)config.getSource();
//		if( sourceFolder.isDirectory()){
//			for( File file : sourceFolder.listFiles()){
//				logger.info(file);
//				String taxonName = retrieveTaxonNameFromImageMetadata(file);
//			
//				List<TaxonBase> taxa = getTaxonService().searchTaxaByName(taxonName, config.getSourceReference());			
//				
//				
//				if(taxa.size() == 0){
//					logger.warn("no taxon with this name found" + taxonName);
//				}else if(taxa.size() > 1){
//					logger.warn("multiple taxa with this name found: " + taxonName);
//				}else{
//					Taxon taxon = (Taxon) taxa.get(0);
//					
//					taxonService.saveTaxon(taxon);
//					
//					TextData feature = TextData.NewInstance();
//					
//					URL url = null;
//					try {
//						url = new URL("test");
//					} catch (MalformedURLException e) {
//						logger.warn("URL is malformed: "+ url);
//					}
//					ImageMetaData imageMetaData = new ImageMetaData();
//					imageMetaData.readFrom(url);
//					
//					int size = 100;
//					
//					String mimeType = "mime";
//					String suffix = "suffix";
//					
//					
//					
//					MediaRepresentationPart mediaRepresentationPart = MediaRepresentationPart.NewInstance(url.toString(), size);
//					
//					MediaRepresentation representation = MediaRepresentation.NewInstance(mimeType, suffix);
//					representation.addRepresentationPart(mediaRepresentationPart);
//					
//					Media media = Media.NewInstance();
//					media.addRepresentation(representation);
//					
//					feature.addMedia(media);
//					
//					TaxonDescription description = TaxonDescription.NewInstance(taxon);
//					
//					description.addElement(feature);
//					
//					//taxon.addDescription(description);
//					//taxonService.saveTaxon(taxon);
//					//descriptionService.saveDescription(description);
//					
//				}
//				
//				logger.info(taxonName);
//			}
//		}else{
//			logger.error("given source folder is not a directory");
//		}
//		return true;
//	}


}
