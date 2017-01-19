/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v36_40;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ITermUpdater;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.TermUpdaterBase;
import eu.etaxonomy.cdm.database.update.v35_36.TermUpdater_35_36;
import eu.etaxonomy.cdm.database.update.v40_41.TermUpdater_40_41;

/**
 * @author a.mueller
 * @date 16.04.2016
 *
 */
public class TermUpdater_36_40 extends TermUpdaterBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_36_40.class);

	public static final String startTermVersion = "3.6.0.0.201527040000";
	private static final String endTermVersion = "4.0.0.0.201604200000";

// *************************** FACTORY **************************************/

	public static TermUpdater_36_40 NewInstance(){
		return new TermUpdater_36_40(startTermVersion, endTermVersion);
	}

// *************************** CONSTRUCTOR ***********************************/

	protected TermUpdater_36_40(String startTermVersion, String endTermVersion) {
		super(startTermVersion, endTermVersion);
	}


	@Override
	protected List<ITermUpdaterStep> getUpdaterList() {
		List<ITermUpdaterStep> list = new ArrayList<ITermUpdaterStep>();

		//there are some more new vocabularies, but we trust that the term initializer will
		//initialize and persist them correctly

		return list;
	}

	@Override
	public ITermUpdater getNextUpdater() {
		return TermUpdater_40_41.NewInstance();
	}

	@Override
	public ITermUpdater getPreviousUpdater() {
		return TermUpdater_35_36.NewInstance();
	}

}
