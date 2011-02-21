/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.cyprus;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.model.common.User;

/**
 * @author a.babadshanjan
 * @created 08.01.2009
 * @version 1.0
 */

@Component
public class CyprusUserImport extends CdmImportBase<CyprusImportConfigurator, CyprusImportState> {
	private static final Logger logger = Logger.getLogger(CyprusUserImport.class);
	
	@Override
	protected boolean isIgnore(CyprusImportState state) {
		return ! state.getConfig().isDoTaxa();
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(CyprusImportState state) {
		logger.warn("DoCheck not yet implemented for CyprusExcelImport");
		return true;
	}


	@Override
	protected boolean doInvoke(CyprusImportState state) {
		this.getAuthenticationManager();
		
		String username = "cmi";
		String password = "fasfowjo0490";
		String personTitle = "Cyprus initial import";
		User user = User.NewInstance(personTitle, username, password);
//		user.setAccountNonLocked(false);
//		user.setEnabled(false);
		getUserService().save(user);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		Authentication authentication = getAuthenticationManager().authenticate(token);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		
		username = "zypern";
		password = "r4i6wpo";
		personTitle = "Cyprus editor";
		User userZypern = User.NewInstance(personTitle, username, password);
		getUserService().save(userZypern);
		
		

		return true;
	}


	
}
