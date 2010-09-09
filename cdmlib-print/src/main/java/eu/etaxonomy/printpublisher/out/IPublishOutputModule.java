// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.printpublisher.out;

import java.io.File;

import org.jdom.Document;

import eu.etaxonomy.printpublisher.NotificationMediator;


/**
 * Performs the publish process defined by the implementing class. Clients should consider extending <code>AbstractPublishOutputModule</code>
 * 
 * @see {@link AbstractPublishOutputModule}
 * @author n.hoffmann
 * @created Apr 8, 2010
 * @version 1.0
 */
public interface IPublishOutputModule {

	/**
	 * Returns the file suffix that should be used for the generated output.
	 * 
	 * @return the file suffix
	 */
	public String getOutputFileSuffix();
	
	/**
	 * The given {@link Document} will be transformed into the output format defined by the implementing class.
	 * The generated output file will be written to the given targetFolder.
	 * 
	 * @param document The {@link Document} that will be used as input for <code>this</code> modules publish process.
	 * @param targetFolder The folder, the generated output should be written to. 
	 * @param notificationMediator the {@link NotificationMediator} to propagate progress of the publish process. Implementors
	 * should allow this to be <code>null</code>.
	 */
	public void output(Document document, File targetFolder, NotificationMediator notificationMediator);
	
}
