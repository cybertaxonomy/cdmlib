/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print.out;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.Document;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;


/**
 * Performs the publish process defined by the implementing class. Clients should consider extending <code>AbstractPublishOutputModule</code>
 *
 * @see {@link PublishOutputModuleBase}
 * @author n.hoffmann
 * @since Apr 8, 2010
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
    public void output(Document document, File targetFolder, IProgressMonitor progressMonitor);

    /**
     * Returns all available stylesheets for this output module.
     * Search path will include the users {@link CdmUtils.perUserCdmFolder} directory to allow for custom stylesheets
     *
     * @return a set of xsl files
     * @throws IOException TODO
     */
    public List<File> getStylesheets() throws IOException;

    /**
     * @return the xslt file associated with this output module
     */
    public File getXslt();

    /**
     * Associate an xsl  file to be used by this output module
     *
     * @param xslt a file
     */
    public void setXslt(File xslt);
}
