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

    @Override
    public void setAsText(String text) {
        String separator = ",";
        List<UUID> uuidList = new ArrayList<>();
        if (text.contains(",")){
            //set the value for more than one UUID
            String[] uuidStringList = text.split(separator);
            for (String element : uuidStringList) {
                    uuidList.add(UUID.fromString(element));
            }
        } else if(text.length() > 0){
            uuidList.add(UUID.fromString(text));
        }
        setValue(uuidList);
    }

}
