/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author n.hoffmann
 * @since Jan 27, 2010
 * @version 1.0
 */
public class ImagesUtility {
	private static final Logger logger = Logger.getLogger(ImagesUtility.class);
	
	/**
	 * Quick and dirty method to get an element's first image file.
	 * 
	 * @param element
	 * @return
	 * @deprecated not used by EDITor anymore
	 */
	@Deprecated
	public static ImageFile getImage(DescriptionElementBase element) {
		List<Media> medias = element.getMedia();
		
		for(Media media : medias){
			Set<MediaRepresentation> representations = media.getRepresentations();
			
			for(MediaRepresentation representation : representations){
				List<MediaRepresentationPart> parts = representation.getParts();
				
				for (MediaRepresentationPart part : parts){
					if(part instanceof ImageFile){
						return (ImageFile) part;
					}
				}
			}							
		}
		return null;
	}
	
	/**
	 * @deprecated not used by EDITor anymore
	 */
	@Deprecated
	public static List<ImageFile> getOrderedImages(DescriptionElementBase element){
		List<ImageFile> imageList = new ArrayList<ImageFile>();
		MediaRepresentation representation = getImageMediaRepresentation(element);
		if (representation != null) {
			for (MediaRepresentationPart part : representation.getParts()){
				if(!(part instanceof ImageFile)){
					throw new RuntimeException("Your database contains media that mix Image Files with non-Image Files.");
				} else {
					imageList.add((ImageFile) part);
				}
			}
		}
		return imageList;
	}
		
	/**
	 * Returns the first Representation with images. If none is found, a
	 * Representation for storing images is created and returned.
	 * 
	 * @deprecated not used by EDITor anymore
	 * @param element
	 * @return
	 */
	@Deprecated
	private static MediaRepresentation getImageMediaRepresentation(DescriptionElementBase element) {
		// Drill down until a representation with images is found
		for(Media media : element.getMedia()){
			Set<MediaRepresentation> representations = media.getRepresentations();
			for(MediaRepresentation representation : representations){
				List<MediaRepresentationPart> parts = representation.getParts();
				for (MediaRepresentationPart part : parts){
					if(part instanceof ImageFile){
						return representation;
					}
				}
			}							
		}		
		// No representation with images found - create
		MediaRepresentation representation = MediaRepresentation.NewInstance();
		Media media = Media.NewInstance();
		element.addMedia(media);
		media.addRepresentation(representation);
		return representation;
	}
	
	/**
	 * @deprecated not used by EDITor anymore
	 * @param description
	 * @return
	 */
	@Deprecated
	public static Set<ImageFile> getImages(TaxonDescription description){
		Set<ImageFile> images = new HashSet<ImageFile>();
		
		for (DescriptionElementBase element : description.getElements()){
			
			Feature feature = element.getFeature();
			
			if(feature.equals(Feature.IMAGE())){
				List<Media> medias = element.getMedia();
				
				for(Media media : medias){
					Set<MediaRepresentation> representations = media.getRepresentations();
					
					for(MediaRepresentation representation : representations){
						List<MediaRepresentationPart> parts = representation.getParts();
						
						for (MediaRepresentationPart part : parts){
							if(part instanceof ImageFile){
								images.add((ImageFile) part);
							}
						}
					}							
				}										
			}
		}
		
		return images;
	}

	/**
	 * @deprecated not used by EDITor anymore
	 * @param taxon
	 * @param imageFile
	 */
	@Deprecated
	public static void addTaxonImage(Taxon taxon, DescriptionBase<?> imageGallery, ImageFile imageFile) {
				
		imageGallery.addElement(createImageElement(imageFile));
		
	}
	
	/**
	 * @deprecated not used by EDITor anymore
	 * @param imageFile
	 * @return
	 */
	@Deprecated
	public static DescriptionElementBase createImageElement(ImageFile imageFile) {
		
		DescriptionElementBase descriptionElement = TextData.NewInstance(Feature.IMAGE());
		
		Media media = Media.NewInstance();
		MediaRepresentation representation = MediaRepresentation.NewInstance();
		
		representation.addRepresentationPart(imageFile);
		
		media.addRepresentation(representation);
		
		descriptionElement.addMedia(media);
		
		return descriptionElement;
		
	}
	
	/**
	 * Adds a new, empty image file to the end of a description element's
	 * ordered list of images.
	 * 
	 * @deprecated not used by EDITor anymore
	 * @param element
	 * @return 
	 */
	@Deprecated
	public static ImageFile addImagePart(DescriptionElementBase element) {
		ImageFile imageFile = ImageFile.NewInstance(null, null);
		getImageMediaRepresentation(element).addRepresentationPart(imageFile);
		return imageFile;
	}
	
	/**
	 * @deprecated not used by EDITor anymore
	 * @param taxon
	 * @param imageFile
	 */
	@Deprecated
	public static void removeTaxonImage(Taxon taxon, DescriptionBase<?> imageGallery, ImageFile imageFile) {
		Set<DescriptionElementBase> descriptionElementsToRemove = new HashSet<DescriptionElementBase>();
		Set<MediaRepresentationPart> representationPartsToRemove = new HashSet<MediaRepresentationPart>();
		
		Set<DescriptionElementBase> images = imageGallery.getElements();
		
		// overmodelling of media in cdmlib makes this a little bit complicated
		for(DescriptionElementBase descriptionElement : images){
			for(Media media : descriptionElement.getMedia()){
				for(MediaRepresentation representation : media.getRepresentations()){
					for(MediaRepresentationPart part : representation.getParts()){
						if(part.equals(imageFile)){
							// because of concurrent modification, we just collect the parts to remove 
							representationPartsToRemove.add(part);
						}
					}

					// and then remove the representation parts here
					for (MediaRepresentationPart part : representationPartsToRemove){
						representation.removeRepresentationPart(part);
					}
					// clear set for next run
					representationPartsToRemove.clear();
					
					// description elements with empty representations should be deleted as well
					if(representation.getParts().size() == 0){
						descriptionElementsToRemove.add(descriptionElement);
					}
				}
			}
		}	
		
		// remove the empty description elements
		for(DescriptionElementBase descriptionElement : descriptionElementsToRemove){
			imageGallery.removeElement(descriptionElement);
		}		
	}

	public static final int UP = 1;
	public static final int DOWN = -1;
	
	/**
	 * @deprecated not used by EDITor anymore
	 * @param input
	 * @param selectedImage
	 * @param direction
	 */
	@Deprecated
	public static void moveImage(DescriptionElementBase element, ImageFile image,
			int direction) {
		
		MediaRepresentation representation = getImageMediaRepresentation(element);
		int index = representation.getParts().indexOf(image);
		if (index == -1) {
			return;
		}		
		if (direction == UP && index != 0) {
			Collections.swap(representation.getParts(), index, index - 1);
		}
		if (direction == DOWN && index < representation.getParts().size() - 1) {
			Collections.swap(representation.getParts(), index, index + 1);
		}
	}

	/**
	 * @deprecated not used by EDITor anymore
	 * @param input
	 * @param selectedImage
	 */
	@Deprecated
	public static void removeImage(DescriptionElementBase element, ImageFile image) {
		getImageMediaRepresentation(element).getParts().remove(image);
	}

	/**
	 * Iterate through all taxon's image galleries until the descriptive element containing
	 * the ImageFile is found.
	 * 
	 * @deprecated not used by EDITor anymore
	 * @param image
	 * @param taxon 
	 * @return 
	 */
	@Deprecated
	public static DescriptionElementBase findImageElement(ImageFile image, Taxon taxon) {
		if (taxon == null) {
			return null;
		}
		for (TaxonDescription description : taxon.getDescriptions()) {
			if (description.isImageGallery()) {
				for (DescriptionElementBase element : description.getElements()) {
					if (getOrderedImages(element).contains(image)) {
						return element;
					}
				}
			}
		}
		return null;		
	}
	
	/**
	 * 
	 * @param description
	 * @param media
	 */
	public static void addMediaToGallery(DescriptionBase description, Media media){
		DescriptionElementBase element = getGalleryElement(description);
		element.addMedia(media);
	}
	
	/**
	 * 
	 * @param description
	 * @param media
	 */
	public static void removeMediaFromGallery(DescriptionBase description, Media media){
		DescriptionElementBase element = getGalleryElement(description);
		element.removeMedia(media);
	}
	
	/**
	 * 
	 * 
	 * @param description
	 * @return
	 */
	private static DescriptionElementBase getGalleryElement(DescriptionBase description){
		if(! description.isImageGallery()){
			logger.error("Description has to have imageGallery flag set.");
		}
		
		Set<DescriptionElementBase> elements = description.getElements();
		
		if(elements.size() != 1){
			logger.error("Image gallery should have only one description");
		}
		
		return elements.iterator().next();
	}
}
