/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.IKeyTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermType;

public enum CdmMetaDataPropertyName implements IKeyTerm{
	DB_SCHEMA_VERSION("Schema Version","SCHEMA_VERSION"),
	TERMS_VERSION("Term Version","TERM_VERSION"),
	DB_CREATE_DATE("Created","CREATED"),
	DB_CREATE_NOTE("Create Note","CREATE_NOTE"),
	INSTANCE_NAME("CDM Instance Name","INST_NAME"),
	INSTANCE_ID("CDM Instance ID","INST_ID");

	// **************** END ENUM **********************/

    private String label;
    private String key;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermType.class);

    private CdmMetaDataPropertyName(String label, String key){
        this.label = label;
        this.key = key;
    }

//**************** METHODS ****************************/

    public String getSqlQuery(){
        return String.format(
                "SELECT value FROM CdmMetaData WHERE propertyname='%s'",
                this.key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return getMessage(Language.DEFAULT());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage(Language language) {
        //TODO i18n
        return label;
    }

    public static CdmMetaDataPropertyName getByKey(String key) {
        for (CdmMetaDataPropertyName term : values()){
            if (term.getKey().equals(key)){
                return term;
            }
        }
        return null;
    }

// *************************** DELEGATE **************************************/

 public static void main(String[] var){
     System.out.println(DB_SCHEMA_VERSION.getSqlQuery());
 }

}