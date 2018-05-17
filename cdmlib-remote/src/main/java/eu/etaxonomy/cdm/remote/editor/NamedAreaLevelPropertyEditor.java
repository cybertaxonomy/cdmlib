package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * @author f.revilla
 * @since 09.06.2010
 */
public class NamedAreaLevelPropertyEditor extends PropertyEditorSupport {
	@Override
    public void setAsText(String text) {
		NamedAreaLevel value = NamedAreaLevel.NewInstance();
		if (NamedAreaLevel.isTDWG_LEVEL1(text)) {
			value = NamedAreaLevel.TDWG_LEVEL1();
		}else if (NamedAreaLevel.isTDWG_LEVEL2(text)) {
			value = NamedAreaLevel.TDWG_LEVEL2();
		}else if (NamedAreaLevel.isTDWG_LEVEL3(text)) {
			value = NamedAreaLevel.TDWG_LEVEL3();
		}else if (NamedAreaLevel.isTDWG_LEVEL4(text)) {
			value = NamedAreaLevel.TDWG_LEVEL4();
		}
		setValue(value);
	}
}
