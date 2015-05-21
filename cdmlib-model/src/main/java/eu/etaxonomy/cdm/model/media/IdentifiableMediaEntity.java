/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.media;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentifiableMediaEntity", propOrder = {
    "media"
})
@MappedSuperclass
@Audited
public abstract class IdentifiableMediaEntity<S extends IIdentifiableEntityCacheStrategy<?>> extends IdentifiableEntity<S> implements IMediaEntity{
	private static final long serialVersionUID = 4038647011021908313L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IdentifiableMediaEntity.class);

    @XmlElementWrapper(name = "Media", nillable = true)
    @XmlElement(name = "Medium")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	//TODO
	@Merge(MergeMode.ADD_CLONE)
    private Set<Media> media = new HashSet<Media>();
	
	
    @Override
	public Set<Media> getMedia() {
		return media;
	}
	
    @Override
	public void addMedia(Media media) {
		this.media.add(media);
	}
    @Override
	public void removeMedia(Media media) {
		this.media.remove(media);
	}
	
//******************** CLONE **********************************************/
	
	@Override
	public Object clone() throws CloneNotSupportedException{
		IdentifiableMediaEntity<?> result = (IdentifiableMediaEntity<?>)super.clone();
		//Media
		result.media = new HashSet<Media>();
		for(Media media : this.media) {
			result.addMedia(media);
		}
		//no changes to: -
		return result;
	}

}
