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
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata.Item;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.app.images.AbstractImageImporter;
import eu.etaxonomy.cdm.app.images.ImageImportConfigurator;
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
		COPYRIGHTNOTICE,
		OBJECTNAME
	}
	
	private static int modCount = 300;

	private static String pluralString = "images";
	
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
				
				if(item.getKeyword().equals("ObjectName")){
					logger.debug("File: " + imageFile.getName() + ". ObjectName string is: " + item.getText());
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
		HashMap<MetaData, String> result = new HashMap<MetaData, String>();
		
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
			
			for (Object object : jpegMetadata.getItems()){
				Item item = (Item) object;
				
				if(metaDataStrings.contains(item.getKeyword().toLowerCase())){
					logger.debug("File: " + imageFile.getName() + ". "+ item.getKeyword() +"string is: " + item.getText());
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
			int count = 0;
			for( File file : sourceFolder.listFiles()){
				if(file.isFile()){
					doCount(count++, modCount, pluralString);
					
					taxonName= retrieveTaxonNameFromImageMetadata(file);
					logger.debug("Looking up taxa with taxon name: " + taxonName);
					
					//TODO:
					ArrayList<MetaData> metaDataList = new ArrayList<MetaData>();
					metaDataList.add (MetaData.ARTIST);
					metaDataList.add (MetaData.COPYRIGHT);
					metaDataList.add (MetaData.COPYRIGHTNOTICE);
					metaDataList.add (MetaData.OBJECTNAME);
					//metaDataList.add (MetaData.NAME);
					
					Map<MetaData, String> metaData = getMetaData(file, metaDataList);
					
					
					
					ReferenceBase sec = referenceService.find(config.getSecUuid());

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
						
						taxonService.saveOrUpdate(taxon);
						
						TextData feature = TextData.NewInstance();
						//MetaDataFactory metaDataFactory = MetaDataFactory.getInstance();
						//ImageMetaData imageMetaData = (ImageMetaData) metaDataFactory.readMediaData(file.toURI(), MimeType.IMAGE);
						try{
						ImageInfo imageinfo = Sanselan.getImageInfo(file);
						
						String mimeType = imageinfo.getMimeType();
						String suffix = "jpg";
						
						
						// URL for this image
						URL url = null;
						try {
							url = new URL(config.getMediaUrlString() + file.getName());
						} catch (MalformedURLException e) {
							logger.warn("URL is malformed: "+ url);
						}
						
						
						ImageFile imageFile = ImageFile.NewInstance(url.toString(),null, imageinfo.getHeight(), imageinfo.getWidth());
						
						
						MediaRepresentation representation = MediaRepresentation.NewInstance(mimeType, suffix);
						representation.addRepresentationPart(imageFile);
						
						Media media = Media.NewInstance();
						media.addRepresentation(representation);
						if (metaData.containsKey(MetaData.OBJECTNAME)){
							media.setTitleCache(metaData.get(MetaData.OBJECTNAME).replace("'", ""), true);
						}
						//TODO: add the rights and the author:
						Person artist = null;
						if (metaData.containsKey(MetaData.ARTIST)){
							//TODO search for the person first and then create the object...
							artist = Person.NewTitledInstance(metaData.get(MetaData.ARTIST).replace("'", ""));
							artist.setFirstname(getFirstName(metaData.get(MetaData.ARTIST)).replace("'", ""));
							artist.setLastname(getLastName(metaData.get(MetaData.ARTIST)).replace("'", ""));
							
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
						
						feature.setFeature(Feature.IMAGE());
						ReferenceBase sourceRef = config.getSourceReference();
						TaxonDescription description = taxon.getOrCreateImageGallery(sourceRef == null ? null :sourceRef.getTitleCache());
						
						description.addElement(feature);
						
						taxonService.saveOrUpdate(taxon);
						}catch(Exception e) {
							e.printStackTrace();
						}
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
			
	protected void doCount(int count, int modCount, String pluralString){
		if ((count % modCount ) == 0 && count!= 0 ){ logger.info(pluralString + " handled: " + (count));}
	}

}
