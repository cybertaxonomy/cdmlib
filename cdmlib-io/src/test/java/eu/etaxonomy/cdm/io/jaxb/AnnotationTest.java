/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStreamReader;
import java.net.URI;

import org.junit.Test;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;

public class AnnotationTest {

	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/AnnotationTest.xml";

	    @Test
	    public void testUnmarshalAnnotations() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

			AnnotatableEntity annotatableEntity = dataSet.getTaxonBases().get(0);
			assertNotNull("annotatableEntity must exist",annotatableEntity);

			assertNotNull("annotatableEntity must have annotations",annotatableEntity.getAnnotations());
			assertEquals("There should be one annotation",1,annotatableEntity.getAnnotations().size());

			Annotation annotation = annotatableEntity.getAnnotations().iterator().next();
//			assertEquals("object must also be the annotated object",annotatableEntity,annotation.getAnnotatedObj());
			assertNotNull("Annotation must have annotations",annotation.getAnnotations());
			assertEquals("There should be one annotation",1,annotation.getAnnotations().size());
			Annotation annotationTwo = annotation.getAnnotations().iterator().next();
//			assertEquals("Annotation should be the annotated object",annotation,annotationTwo.getAnnotatedObj());
	    }
}
