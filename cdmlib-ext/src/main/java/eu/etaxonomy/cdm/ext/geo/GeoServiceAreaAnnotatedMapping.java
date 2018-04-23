/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * This class implements {@link IGeoServiceAreaMapping}. The mapping
 * is stored as a technical annotation of the area.
 * The area is saved while the mapping is set.
 * @author a.mueller
 \* @since 15.08.2011
 *
 */
@Component
public class GeoServiceAreaAnnotatedMapping implements IGeoServiceAreaMapping {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(GeoServiceAreaAnnotatedMapping.class);

    @Autowired
    private ITermService termService;

    @Override
    public GeoServiceArea valueOf(NamedArea area) {
        for (Annotation annotation : area.getAnnotations()){
            if (AnnotationType.TECHNICAL().equals(annotation.getAnnotationType())){
                GeoServiceArea areas = GeoServiceArea.valueOf(annotation.getText());
                return areas;
            }
        }

        return null;
    }


    @Override
    public void set(NamedArea area, GeoServiceArea geoServiceArea) {
        String xml = null;
        try {
            xml = geoServiceArea.toXml();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        Annotation annotation =null;
        for (Annotation existingAnnotation : area.getAnnotations()){
            if (AnnotationType.TECHNICAL().equals(existingAnnotation.getAnnotationType())){
                if (GeoServiceArea.isAreaMapping(existingAnnotation.getText())){
                    //FIXME test mapping type. There may be a mapping for each map service
                    annotation = existingAnnotation;
                }
            }
        }
        if (annotation == null){
            AnnotationType type = AnnotationType.TECHNICAL();
            annotation = Annotation.NewInstance(xml, type, Language.DEFAULT());
        }
        area.addAnnotation(annotation);

        termService.saveOrUpdate(area);

    }

    @Override
    public void clear(NamedArea area){

        Set<Annotation> removeCandidates = new HashSet<Annotation>();
        for (Annotation annotation : area.getAnnotations()){
            if (AnnotationType.TECHNICAL().equals(annotation.getAnnotationType())){
                if (GeoServiceArea.isAreaMapping(annotation.getText())){
                    removeCandidates.add(annotation);
                }
            }
        }

        for(Annotation  remove : removeCandidates){
            area.removeAnnotation(remove);
        }
        termService.saveOrUpdate(area);
    }


}
