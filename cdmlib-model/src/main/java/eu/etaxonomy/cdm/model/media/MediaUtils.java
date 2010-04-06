package eu.etaxonomy.cdm.model.media;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class MediaUtils {

	private static final Logger logger = Logger.getLogger(MediaUtils.class);
	
	/**
	 * @param mimeTypeRegexes
	 * @param size
	 * @param widthOrDuration
	 * @param height
	 * @return
	 * 
	 * 
	 */
	public static MediaRepresentation findBestMatchingRepresentation(Media media, Integer size, Integer height, Integer widthOrDuration, String[] mimeTypes){
		// find best matching representations of each media
		SortedMap<Integer, MediaRepresentation> prefRepresentations 
			= orderMediaRepresentations(media, mimeTypes, size, widthOrDuration, height);
			try {
				// take first one and remove all other representations
				MediaRepresentation prefOne = prefRepresentations.get(prefRepresentations.firstKey());
				
				return prefOne;
				
			} catch (NoSuchElementException nse) {
				/* IGNORE */
			}
			return null;
		}
	
	/**
	 * @param mediaList
	 * @param mimeTypes
	 * @param sizeTokens
	 * @param widthOrDuration
	 * @param height
	 * @param size
	 * @return
	 */
	public static List<Media> findPreferredMedia(List<Media> mediaList,
			String[] mimeTypes, String[] sizeTokens, Integer widthOrDuration,
			Integer height, Integer size) {
		for(int i=0; i<mimeTypes.length; i++){
			mimeTypes[i] = mimeTypes[i].replace(':', '/');
		}
		
		if(sizeTokens.length > 0){
			try {
				size = Integer.valueOf(sizeTokens[0]);
			} catch (NumberFormatException nfe) {
				/* IGNORE */
			}
		}
		if(sizeTokens.length > 1){
			try {
				widthOrDuration = Integer.valueOf(sizeTokens[1]);
			} catch (NumberFormatException nfe) {
				/* IGNORE */
			}
		}
		if(sizeTokens.length > 2){
			try {
				height = Integer.valueOf(sizeTokens[2]);
			} catch (NumberFormatException nfe) {
				/* IGNORE */
			}
		}
		
		List<Media> returnMedia = new ArrayList<Media>(mediaList.size());
		if(mediaList != null){
			for(Media media : mediaList){
				SortedMap<Integer, MediaRepresentation> prefRepresentations 
					= orderMediaRepresentations(media, mimeTypes, size, widthOrDuration, height);
				try {
					// take first one and remove all other representations
					MediaRepresentation prefOne = prefRepresentations.get(prefRepresentations.firstKey());
					for (MediaRepresentation representation : media.getRepresentations()) {
						if (representation != prefOne) {
							media.removeRepresentation(representation);
						}
					}
					returnMedia.add(media);
				} catch (NoSuchElementException nse) {
					logger.debug(nse);
					/* IGNORE */
				}
			}
		}
		return returnMedia;
	}
	
	/**
	 * @param media
	 * @param mimeTypeRegexes
	 * @param size
	 * @param widthOrDuration
	 * @param height
	 * @return
	 * 
	 * TODO move into a media utils class
	 * TODO implement the quality filter  
	 
	public static SortedMap<String, MediaRepresentation> orderMediaRepresentations(Media media, String[] mimeTypeRegexes,
			Integer size, Integer widthOrDuration, Integer height) {
		SortedMap<String, MediaRepresentation> prefRepr = new TreeMap<String, MediaRepresentation>();
		for (String mimeTypeRegex : mimeTypeRegexes) {
			// getRepresentationByMimeType
			Pattern mimeTypePattern = Pattern.compile(mimeTypeRegex);
			int representationCnt = 0;
			for (MediaRepresentation representation : media.getRepresentations()) {
				int dwa = 0;
				if(representation.getMimeType() == null){
					prefRepr.put((dwa + representationCnt++) + "_NA", representation);
				} else {
					Matcher mather = mimeTypePattern.matcher(representation.getMimeType());
					if (mather.matches()) {
	
						/* TODO the quality filter part is being skipped 
						 * // look for representation with the best matching parts
						for (MediaRepresentationPart part : representation.getParts()) {
							if (part instanceof ImageFile) {
								ImageFile image = (ImageFile) part;
								int dw = image.getWidth() * image.getHeight() - height * widthOrDuration;
								if (dw < 0) {
									dw *= -1;
								}
								dwa += dw;
							}
							dwa = (representation.getParts().size() > 0 ? dwa / representation.getParts().size() : 0);
						}
						prefRepr.put((dwa + representationCnt++) + '_' + representation.getMimeType(), representation);
											
						// preferred mime type found => end loop
						break;
					}
				}
			}
		}
		return prefRepr;
	}

	*/
	/**
	 * @param mimeTypeRegexes
	 * @param size
	 * @param widthOrDuration
	 * @param height
	 * @return
	 * 
	 * 
	 */
	private static SortedMap<Integer, MediaRepresentation> orderMediaRepresentations(Media media, String[] mimeTypeRegexes,
			Integer size, Integer widthOrDuration, Integer height) {
		
		SortedMap<Integer, MediaRepresentation> prefRepr = new TreeMap<Integer, MediaRepresentation>();
//		SortedMap<String, MediaRepresentation> sortedForSizeDistance = new TreeMap<String, MediaRepresentation>();		
//		String keyString = "";
		
		size = (size == null ? new Integer(0) : size );
		widthOrDuration = (widthOrDuration == null ? new Integer(0) : widthOrDuration);
		height = (height == null ? new Integer(0) : height);
		mimeTypeRegexes = (mimeTypeRegexes == null ? new String[]{} : mimeTypeRegexes);
		
		if(media != null){
			
			for (String mimeTypeRegex : mimeTypeRegexes) {
				// getRepresentationByMimeType
				Pattern mimeTypePattern = Pattern.compile(mimeTypeRegex);
				int representationCnt = 0;
				for (MediaRepresentation representation : media.getRepresentations()) {
					
					Matcher mather = mimeTypePattern.matcher(representation.getMimeType());
					if (mather.matches()) {
						int dwa = 0;
						
						//first the size is used for comparison
						for (MediaRepresentationPart part : representation.getParts()) {
							if (part.getSize()!= null){
								int sizeOfPart = part.getSize();
								int distance = sizeOfPart - size;
								if (distance < 0) {
									distance*= -1;
								}
								dwa += distance;
							}
							//if height and width/duration is defined, add this information, too
							if (height != 0 && widthOrDuration != 0){
								int dw = 0;
								
								if (part instanceof ImageFile) {
									ImageFile image = (ImageFile) part;
									dw = image.getWidth() * image.getHeight() - height * widthOrDuration;
								}
								else if (part instanceof MovieFile){
									MovieFile movie = (MovieFile) part;
									dw = movie.getDuration() - widthOrDuration;
											
								}else if (part instanceof AudioFile){
									AudioFile audio = (AudioFile) part;
									dw = audio.getDuration() - widthOrDuration;
									
								}
								if (dw < 0) {
									dw *= -1;
								}
								dwa += dw;
								
							}
						}
						dwa = (representation.getParts().size() > 0 ? dwa / representation.getParts().size() : 0);
						
						//keyString =(dwa + representationCnt++) + '_' + representation.getMimeType();
						
						prefRepr.put((dwa + representationCnt++), representation);
					}
						
				}										
			}
		}
		return prefRepr;
	}
}
