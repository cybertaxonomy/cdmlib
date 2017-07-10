/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.terms;

import java.io.InputStreamReader;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.SimpleImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @date 14.03.2017
 *
 */
public class RepresentationCsvImportConfigurator
        extends SimpleImportConfiguratorBase<InputStreamReader>{

    private static final long serialVersionUID = -3602889553095677715L;

    private boolean overrideWithEmpty;
    private UUID languageUuid;


    /**
     * @param source
     * @param cdmDestination
     * @return
     */
    public static RepresentationCsvImportConfigurator NewInstance(InputStreamReader file,
            ICdmDataSource cdmDestination) {
        return new RepresentationCsvImportConfigurator(file, cdmDestination);
    }

// ****************** CONSTRUCTOR *****************************/

    private RepresentationCsvImportConfigurator(InputStreamReader file,
            ICdmDataSource cdmDestination){
        super(file, cdmDestination, null);
    }

// *************************************


    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList(){
        ioClassList = new Class[]{
            RepresentationCsvImport.class,
        };
    }

    public boolean isOverrideWithEmpty() {
        return overrideWithEmpty;
    }
    public void setOverrideWithEmpty(boolean overrideWithEmpty) {
        this.overrideWithEmpty = overrideWithEmpty;
    }


    public UUID getLanguageUuid() {
        return languageUuid;
    }

    public void setLanguageUuid(UUID languageUuid) {
        this.languageUuid = languageUuid;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Reference getSourceReference() {
        if (this.sourceReference == null){
            sourceReference = ReferenceFactory.newGeneric();
            if (this.getSource() == null){
                sourceReference.setTitleCache("Term Representation Import " + getDateString(), true);
            }else{
                sourceReference.setTitleCache(getSource().toString(), true);
            }
        }
        return sourceReference;
    }

}
