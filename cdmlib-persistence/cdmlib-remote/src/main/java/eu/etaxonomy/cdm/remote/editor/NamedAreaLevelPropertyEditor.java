package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * @author f.revilla
 * @date 09.06.2010
 */
@Component
public class NamedAreaLevelPropertyEditor extends PropertyEditorSupport {
	public void setAsText(String text) {
		NamedAreaLevel value = new NamedAreaLevel();
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
