/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author a.mueller
 * @created 09.06.2008
 * @version 1.0
 */
@Entity
public class MediaRepresentationPart extends VersionableEntity{
	private static final Logger logger = Logger.getLogger(MediaRepresentationPart.class);

	//sorting index
	private int sortIndex;
	//where the media file is stored
	private String uri;
	//in bytes
	private Integer size;
	//the MEdiaRepresentation this MediaRepresentation
	private MediaRepresentation mediaRepresentation;

	/**
	 * Factory method
	 * @return
	 */
	public static MediaRepresentationPart NewInstance(String uri, Integer size){
		MediaRepresentationPart result  = new MediaRepresentationPart();
		result.setUri(uri);
		result.setSize(size);
		return result;
	}
	
	
	/**
	 * 
	 */
	public MediaRepresentationPart() {
		super();
	}
	
/***************  getter /setter *************************************/
	
	
	@ManyToOne
	public MediaRepresentation getMediaRepresentation() {
		return this.mediaRepresentation;
	}

	@Deprecated //use only for bidirectional and hibernate
	protected void setMediaRepresentation(MediaRepresentation mediaRepresentation) {
		this.mediaRepresentation = mediaRepresentation;
	}
	

	public String getUri(){
		return this.uri;
	}

	/**
	 * 
	 * @param uri    uri
	 */
	public void setUri(String uri){
		this.uri = uri;
	}

	
	/**
	 * @return
	 */
	public Integer getSize(){
		return this.size;
	}
	/** 
	 * @param size    size
	 */
	public void setSize(Integer size){
		this.size = size;
	}


	/**
	 * @return the sortIndex
	 */
	public int getSortIndex() {
		return sortIndex;
	}


	/**
	 * @param sortIndex the sortIndex to set
	 */
	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}
}
