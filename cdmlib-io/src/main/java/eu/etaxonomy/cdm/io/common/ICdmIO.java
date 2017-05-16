/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import eu.etaxonomy.cdm.io.common.events.IIoEvent;


/**
 * @author a.mueller
 * @created 20.06.2008
 */

public interface ICdmIO<STATE extends IoStateBase> extends  IIoObservable {

	final String TEAM_STORE = "team";
	final String REFERENCE_STORE = "reference";
	final String NOMREF_STORE = "nomRef";
	final String TAXONNAME_STORE = "taxonName";
	final String TAXON_STORE = "taxon";
	final String FEATURE_STORE = "feature";
	final String SPECIMEN_STORE = "specimen";

	public boolean check(STATE state);

//	public boolean invoke(T config, Map<String, MapWrapper<? extends CdmBase>> stores);



//	public boolean invoke(IoState<T> state);

	public void updateProgress(STATE state, String message);

	public void updateProgress(STATE state, String message, int worked);

	public void warnProgress(STATE state, String message, Throwable e);

//******************** Observers *********************************************************


	/**
	 * If this object fires an event then notify all of its observers.
	 */
	public void fire(IIoEvent event);

//******************** End Observers *********************************************************

}
