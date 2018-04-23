/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.markup;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Extension;

/**
 * @author a.mueller
 * @since 03.08.2011
 *
 */
public class WriterDataHolder {
	String writer;
	List<FootnoteDataHolder> footnotes;
	Extension extension;
	Annotation annotation;
	
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (StringUtils.isNotBlank(this.writer)){
			String result = "Writer: " + this.writer;
			if (footnotes != null && ! footnotes.isEmpty()){
				result += result + "[" + footnotes.size() + "]";
			}
			return result;
		}else{
			return super.toString();
		}
	}
		
	
}
