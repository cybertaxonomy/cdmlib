/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author f.revilla
 * @since 09.06.2010
 */
public class UUIDListPropertyEditor extends PropertyEditorSupport {

    private String nullRepresentation;

    public UUIDListPropertyEditor(){
        super();
    }

    public UUIDListPropertyEditor(String nullRepresentation){
        this.nullRepresentation = nullRepresentation;
    }

    @Override
    public void setAsText(String text) {
        String separator = ",";
        List<UUID> uuidList = new ArrayList<>();
        if(nullRepresentation != null && nullRepresentation.equals(text)){
            uuidList.add(null);
        } else if (text.contains(",")){
            //set the value for more than one UUID
            String[] uuidStringList = text.split(separator);
            for (String element : uuidStringList) {
                    if(nullRepresentation != null && nullRepresentation.equals(element) && !uuidList.contains(null)){
                        uuidList.add(null);
                    } else {
                        uuidList.add(UUID.fromString(element));
                    }
            }
        } else if(text.length() > 0){
            uuidList.add(UUID.fromString(text));
        }
        setValue(uuidList);
    }
}