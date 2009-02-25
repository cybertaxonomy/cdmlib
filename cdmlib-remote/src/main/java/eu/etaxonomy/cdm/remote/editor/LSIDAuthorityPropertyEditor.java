package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import eu.etaxonomy.cdm.model.common.LSIDAuthority;
import com.ibm.lsid.MalformedLSIDException;

public class LSIDAuthorityPropertyEditor extends PropertyEditorSupport {
	public void setAsText(String text) {
		try {
			setValue(new LSIDAuthority(text));
		} catch (MalformedLSIDException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
