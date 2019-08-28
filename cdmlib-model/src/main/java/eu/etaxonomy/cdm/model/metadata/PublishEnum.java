/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

/**
 * @author k.luther
 * @since 19.11.2018
 *
 */
public enum PublishEnum implements IKeyLabel{

    Publish("Publish", "Publish"),
    NotPublish("NotPublish", "Don't publish"),
    InheritFromParent("InheritFromParent", "Inherit from parent");


    String label;
    String key;

    private PublishEnum(String key, String label){
        this.label = label;
        this.key = key;
    }

    @Override
    public String getLabel(){
        return label;
    }

    @Override
    public String getKey(){
        return key;
    }

    @Override
    public String toString(){
        return key;
    }
}
