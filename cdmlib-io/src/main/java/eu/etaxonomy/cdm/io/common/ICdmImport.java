/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;


/**
 * Interface for all import classes.
 *
 * @author a.babadshanjan
 * @since 17.11.2008
 */
public interface ICdmImport<CONFIG extends IImportConfigurator, STATE extends ImportStateBase>
            extends ICdmIO<STATE>{


    public byte[] getByteArray();

    public abstract void invoke(STATE state);

}
