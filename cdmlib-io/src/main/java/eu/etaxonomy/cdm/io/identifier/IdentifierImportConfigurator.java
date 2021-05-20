/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.identifier;

import java.io.InputStreamReader;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.SimpleImportConfiguratorBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Configurator for importing Identifier via csv for a given class.
 *
 * @author a.mueller
 * @since 14.03.2017
 */
public class IdentifierImportConfigurator
        extends SimpleImportConfiguratorBase<InputStreamReader>{

    private static final long serialVersionUID = 2399625330102810465L;

    private Class<? extends IdentifiableEntity> cdmClass;
    private UUID uuidIdentifierType;

    private boolean ignoreEmptyIdentifier = true;
    private boolean updateExisting = true;
    private boolean warnAndDoNotOverrideIfExists = false;


    public static IdentifierImportConfigurator NewInstance(InputStreamReader file,
            ICdmDataSource cdmDestination) {
        return new IdentifierImportConfigurator(file, cdmDestination);
    }

// ****************** CONSTRUCTOR *****************************/

    private IdentifierImportConfigurator(InputStreamReader file,
            ICdmDataSource cdmDestination){
        super(file, cdmDestination, null);
    }

// *************************************

    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList(){
        ioClassList = new Class[]{
            IdentifierImport.class,
        };
    }

    public Class<? extends IdentifiableEntity> getCdmClass() {
        return this.cdmClass;
    }
    public void setCdmClass(Class<? extends IdentifiableEntity> cdmClass) {
        this.cdmClass = cdmClass;
    }

    public UUID getIdentifierTypeUuid() {
        return uuidIdentifierType;
    }
    public void setIdentifierTypeUuid(UUID uuidIdentifierType) {
        this.uuidIdentifierType = uuidIdentifierType;
    }

    @Override
    public Reference getSourceReference() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isIgnoreEmptyIdentifier() {
        return ignoreEmptyIdentifier;
    }
    public void setIgnoreEmptyIdentifier(boolean ignoreEmptyIdentifier) {
        this.ignoreEmptyIdentifier = ignoreEmptyIdentifier;
    }

    public boolean isUpdateExisting() {
        return updateExisting;
    }
    public void setUpdateExisting(boolean updateExisting) {
        this.updateExisting = updateExisting;
    }

    public boolean isWarnAndDoNotOverrideIfExists() {
        return warnAndDoNotOverrideIfExists;
    }
    public void setWarnAndDoNotOverrideIfExists(boolean warnAndDoNotOverrideIfExists) {
        this.warnAndDoNotOverrideIfExists = warnAndDoNotOverrideIfExists;
    }
}