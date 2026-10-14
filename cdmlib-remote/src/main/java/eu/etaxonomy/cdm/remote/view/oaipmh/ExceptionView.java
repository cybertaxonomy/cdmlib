/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.view.oaipmh;

import java.util.Map;

import eu.etaxonomy.cdm.remote.dto.oaipmh.Error;
import eu.etaxonomy.cdm.remote.dto.oaipmh.ErrorCode;
import eu.etaxonomy.cdm.remote.dto.oaipmh.OAIPMH;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Verb;
import eu.etaxonomy.cdm.remote.view.OaiPmhResponseView;

public class ExceptionView extends OaiPmhResponseView {

	@Override
    protected void constructResponse(OAIPMH oaiPmh,Map<String,Object> model) {
    	oaiPmh.getRequest().setVerb((Verb)model.get("verb"));
    	oaiPmh.getRequest().setValue((String)model.get("request"));
        Error error = new Error();
        error.setCode((ErrorCode)model.get("code"));
        error.setValue((String)model.get("message"));
        oaiPmh.getError().add(error);
    }
}
