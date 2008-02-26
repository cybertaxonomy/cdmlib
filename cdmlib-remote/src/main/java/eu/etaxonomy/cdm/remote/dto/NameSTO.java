/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple Transfer Object version of {@link NameTO}
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 11.12.2007 14:40:26
 */
public class NameSTO extends BaseSTO {
	
	private String fullname;
	private ReferenceSTO nomenclaturalReference;
	private List<TaggedText> taggedName = new ArrayList();
	
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public ReferenceSTO getNomenclaturalReference() {
		return nomenclaturalReference;
	}
	public void setNomenclaturalReference(ReferenceSTO nomenclaturalReference) {
		this.nomenclaturalReference = nomenclaturalReference;
	}
	public List<TaggedText> getTaggedName() {
		return taggedName;
	}
	public void setTaggedName(List<TaggedText> taggedName) {
		this.taggedName = taggedName;
	}
	public void addNameToken(TaggedText token){
		this.taggedName.add(token);
	}
	
}
