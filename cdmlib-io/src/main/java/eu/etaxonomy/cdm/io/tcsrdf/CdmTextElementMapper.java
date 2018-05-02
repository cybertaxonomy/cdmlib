/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author a.mueller
 * @since 29.07.2008
 */

public class CdmTextElementMapper extends CdmSingleAttributeRDFMapperBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmTextElementMapper.class);

	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmTextElementMapper(String sourceElementString, String sourceNamespace, String cdmAttributeString) {
		super(sourceElementString, cdmAttributeString);
		this.sourceNameSpace = sourceNamespace;
	}

	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmTextElementMapper(String dbAttributString, String cdmAttributeString) {
		super(dbAttributString, cdmAttributeString);
	}

	@Override
    public String getSourceNamespace(){
		return sourceNameSpace;
	}

	@Override
    public Class getTypeClass(){
		return String.class;
	}

	@Override
    public boolean mapsSource(Resource content, Statement parentElement){
		return super.mapsSource(content, parentElement);
	}






}
