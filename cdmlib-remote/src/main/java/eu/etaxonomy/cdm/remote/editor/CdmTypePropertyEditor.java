/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import org.apache.log4j.Logger;

/**
 * @author a.kohlbecker
 * @date 30.06.2009
 */
public class CdmTypePropertyEditor extends PropertyEditorSupport  {

    public static final Logger logger = Logger.getLogger(CdmTypePropertyEditor.class);

    @Override
    public void setAsText(String text) {

            Class<?> clazz = null;
            try {
                clazz = Class.forName(text);
            } catch (ClassNotFoundException e) { /* IGNORE */ }
            try {
                clazz = Class.forName("eu.etaxonomy.cdm.model.agent."+text);
            } catch (ClassNotFoundException e) { /* IGNORE */ }
            try {
                clazz = Class.forName("eu.etaxonomy.cdm.model.common."+text);
            } catch (ClassNotFoundException e) { /* IGNORE */ }
            try {
                clazz = Class.forName("eu.etaxonomy.cdm.model.description."+text);
            } catch (ClassNotFoundException e) { /* IGNORE */ }
            try {
                clazz = Class.forName("eu.etaxonomy.cdm.model.location."+text);
            } catch (ClassNotFoundException e) { /* IGNORE */ }
            try {
                clazz = Class.forName("eu.etaxonomy.cdm.model.media."+text);
            } catch (ClassNotFoundException e) { /* IGNORE */ }
            try {
                clazz = Class.forName("eu.etaxonomy.cdm.model.molecular."+text);
            } catch (ClassNotFoundException e) { /* IGNORE */ }
            try {
                clazz = Class.forName("eu.etaxonomy.cdm.model.name."+text);
            } catch (ClassNotFoundException e) { /* IGNORE */ }
            try {
                clazz = Class.forName("eu.etaxonomy.cdm.model.occurrence."+text);
            } catch (ClassNotFoundException e) { /* IGNORE */ }
            try {
                clazz = Class.forName("eu.etaxonomy.cdm.model.reference."+text);
            } catch (ClassNotFoundException e) { /* IGNORE */ }
            try {
                clazz = Class.forName("eu.etaxonomy.cdm.model.taxon."+text);
            } catch (ClassNotFoundException e) { /* IGNORE */ }

            if(clazz != null){
                setValue(clazz);
            } else {
                logger.error("Cannot find class for " + text);
            }
    }
}
