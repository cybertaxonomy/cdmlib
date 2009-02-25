package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;
import eu.etaxonomy.cdm.model.common.LSID;

import com.ibm.lsid.MalformedLSIDException;

public class LSIDPropertyEditor extends PropertyEditorSupport {
	public void setAsText(String text) {
		try {
			setValue(new LSID(text));
		} catch (MalformedLSIDException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
