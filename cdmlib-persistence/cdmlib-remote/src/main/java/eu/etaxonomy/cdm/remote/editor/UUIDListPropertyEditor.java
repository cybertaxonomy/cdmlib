package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author f.revilla
 * @date 09.06.2010
 */

public class UUIDListPropertyEditor extends PropertyEditorSupport {

	public void setAsText(String text) {
		String separator = ",";
		List<UUID> uuidList = new ArrayList<UUID>();
		if (text.contains(",")){
			//set the value for more than one UUID
			String[] uuidStringList = text.split(separator);
			for (String element : uuidStringList) {
				uuidList.add(UUID.fromString(element));
			}		
		}else{//set the value of the single UUID
			uuidList.add(UUID.fromString(text));
		}
		setValue(uuidList);
	}

}
