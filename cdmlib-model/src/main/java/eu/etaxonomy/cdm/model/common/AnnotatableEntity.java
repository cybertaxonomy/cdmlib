                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        /**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.EntityCollectionSetterAdapter;
import eu.etaxonomy.cdm.model.EntityCollectionSetterAdapter.SetterAdapterException;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * Abstract superclass implementing human annotations and machine markers to be assigned to CDM objects.
 * @author m.doering
 * @since 08-Nov-2007 13:06:10
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnnotatableEntity", propOrder = {
    "markers",
    "annotations"
})
@Audited
@MappedSuperclass
public abstract class AnnotatableEntity
        extends VersionableEntity
        implements IAnnotatableEntity {

    private static final long serialVersionUID = 9151211842542443102L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	@XmlElementWrapper(name = "Markers", nillable = true)
	@XmlElement(name = "Marker")
    @OneToMany(fetch=FetchType.LAZY, orphanRemoval=true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
	@Merge(MergeMode.ADD_CLONE)
	protected Set<Marker> markers = new HashSet<>();

	@XmlElementWrapper(name = "Annotations", nillable = true)
	@XmlElement(name = "Annotation")
    @OneToMany(fetch=FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
	@Merge(MergeMode.ADD_CLONE)
	protected Set<Annotation> annotations = new HashSet<>();

	protected AnnotatableEntity() {
		super();
	}

//*************** MARKER **********************************************


	@Override
    public Set<Marker> getMarkers(){
		return this.markers;
	}

	@Override
    public void addMarker(Marker marker){
		if (marker != null){
			getMarkers().add(marker);
		}
	}
    public Marker addMarker(MarkerType type, boolean value){
        Marker marker = Marker.NewInstance(type, value);
        addMarker(marker);
        return marker;
    }
	@Override
    public void removeMarker(Marker marker){
		if(getMarkers().contains(marker)) {
			getMarkers().remove(marker);
		}
	}

    @Override
    public void removeMarker(UUID markerTypeUuid){
        for(Marker marker : getMarkers(markerTypeUuid)) {
            removeMarker(marker);
        }
    }

	@Override
    public boolean hasMarker(MarkerType type, boolean value){
		return hasMarker(type.getUuid(), value);
	}

	@Override
    public boolean hasMarker(UUID uuidMarkerType, boolean value){
		for (Marker marker: getMarkers(uuidMarkerType)){
			if (marker.getFlag() == value){
			    return true;
			}
		}
		return false;
	}

    @Override
    public boolean hasAnyMarkerOf(Set<UUID> uuidMarkerTypes, boolean value){
        return this.getMarkers().stream().anyMatch(m->
            m.getMarkerType() != null &&
            uuidMarkerTypes.contains(m.getMarkerType().getUuid())
            && m.getValue() == value);
    }

    @Override
    public Set<Marker> getMarkers(UUID uuidMarkerType){
        Set<Marker> result = new HashSet<>();
        for (Marker marker: getMarkers()){
            if (marker.getMarkerType().getUuid().equals(uuidMarkerType)){
                result.add(marker);
            }
        }
        return result;
    }

    @Override
    public Boolean markerValue(UUID uuidMarkerType){
        for (Marker marker: getMarkers()){
            if (marker.getMarkerType().getUuid().equals(uuidMarkerType)){
                 return marker.getFlag();
            }
        }
        return null;
    }

//*************** ANNOTATIONS **********************************************

	@Override
    public Set<Annotation> getAnnotations(){
		return this.annotations;
	}
	@Override
    public void addAnnotation(Annotation annotation){
		if (annotation != null){
			getAnnotations().add(annotation);
		}
	}
    public Set<Annotation> getAnnotations(UUID uuidAnnotationType){
        Set<Annotation> result = new HashSet<>();
        for (Annotation annotation: getAnnotations()){
            if (annotation.getAnnotationType() != null && annotation.getAnnotationType().getUuid().equals(uuidAnnotationType)){
                result.add(annotation);
            }
        }
        return result;
    }

	@Override
    public void removeAnnotation(Annotation annotation){
		if(getAnnotations().contains(annotation)) {
			getAnnotations().remove(annotation);
		}
	}

	public void setAnnotations(Set<Annotation> annotations) throws SetterAdapterException {
	     new EntityCollectionSetterAdapter<AnnotatableEntity, Annotation>(AnnotatableEntity.class, Annotation.class, "annotations").setCollection(this, annotations);
    }

// **************** EMPTY ************************/

    @Override
    protected boolean checkEmpty(){
       return super.checkEmpty()
            && this.annotations.isEmpty()
            && this.markers.isEmpty()
           ;
    }

//********************** CLONE *****************************************/

    @Override
	public AnnotatableEntity clone() throws CloneNotSupportedException{

	    AnnotatableEntity result = (AnnotatableEntity)super.clone();

		//Annotations
		result.annotations = new HashSet<>();
		for (Annotation annotation : getAnnotations()){
			Annotation newAnnotation = annotation.clone();
			result.addAnnotation(newAnnotation);
		}

		//Markers
		result.markers = new HashSet<>();
		for (Marker marker : getMarkers()){
			Marker newMarker = marker.clone();
			result.addMarker(newMarker);
		}

		//no changes to: -
		return result;
	}
}