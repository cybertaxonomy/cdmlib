/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 *
 */
public class MappedCdmBase<CDMBASE extends CdmBase> {


	private String namespace;
	private String sourceId;

	private CDMBASE cdmBase;

	public MappedCdmBase(String namespace, String sourceId, CDMBASE cdmBase) {
		super();
		this.namespace = namespace;
		this.sourceId = sourceId;
		this.cdmBase = cdmBase;
	}

	public MappedCdmBase(TermUri termUri, String sourceId, CDMBASE cdmBase) {
		super();
		this.namespace = termUri.toString();
		this.sourceId = sourceId;
		this.cdmBase = cdmBase;
	}

	public MappedCdmBase(String sourceId, CDMBASE cdmBase) {
		super();
		this.namespace = null;
		this.sourceId = sourceId;
		this.cdmBase = cdmBase;
	}

	public MappedCdmBase(CDMBASE cdmBase) {
		super();
		this.namespace = null;
		this.sourceId = null;
		this.cdmBase = cdmBase;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getSourceId() {
		return sourceId;
	}

	public CDMBASE getCdmBase() {
		return cdmBase;
	}

	public boolean isMappable(){
		return (this.namespace != null && this.sourceId != null && this.cdmBase != null);
	}

	@Override
    public String toString(){
		String result = CdmUtils.concat("@", new String[]{namespace, sourceId, cdmBase.toString()});
		return result;
	}


}
