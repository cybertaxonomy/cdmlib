/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.media;

import java.io.IOException;
import java.util.Collection;

import org.apache.http.HttpException;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.media.CdmImageInfo;

/**
 * @author a.kohlbecker
 * @since May 7, 2021
 */
public abstract class AbstactMediaMetadataReader {

    protected CdmImageInfo cdmImageInfo;
    protected URI metadataUri;

    protected AbstactMediaMetadataReader(URI imageUri, URI metadataUri) {
        this.cdmImageInfo = new CdmImageInfo(imageUri);
        this.metadataUri = metadataUri;
    }

    public abstract AbstactMediaMetadataReader read() throws IOException, HttpException;

    public CdmImageInfo getCdmImageInfo() {
        return cdmImageInfo;
    }

    protected void setCdmImageInfo(CdmImageInfo cdmImageInfo) {
        this.cdmImageInfo = cdmImageInfo;
    }

    /**
     * Wrapper for the Item.getText() method which applies cleaning of the text representation.
     * <ol>
     * <li>Strings are surrounded by single quotes, these must be removed</li>
     * </ol>
     * @param item
     */
    protected String text(String  text) {
        if(text.startsWith("'") && text.endsWith("'")) {
            text = text.substring(1 , text.length() - 1);
        }
        return text;
    }

    protected void processPutMetadataEntry(String key, Collection<String> values) {
        values.stream().forEach(v -> processPutMetadataEntry(key, v));
    }

    protected void processPutMetadataEntry(String key, String value) {

        String text = text(value);
        if ("Keywords".equals(key)){
            String[] customKeyVal = text.split(":");
            if (customKeyVal.length == 2){
                //convention used e.g. for Flora of cyprus (#9137)
                appendMetadataEntry(customKeyVal[0].trim(), customKeyVal[1].trim());
            }else{
                appendMetadataEntry(key, text);
            }
        }else if (key.contains("/")){
            //TODO: not sure where this syntax is used originally
            //key.replace("/", "");
            int index = key.indexOf("/");
            key = key.substring(0, index);
            appendMetadataEntry(key, text);
        } else {
            appendMetadataEntry(key, text);
        }
    }

    public void appendMetadataEntry(String key, String text) {
        if(cdmImageInfo.getMetaData().containsKey(key)) {
            cdmImageInfo.getMetaData().put(key, cdmImageInfo.getMetaData().get(key).concat("; ").concat(text));
        } else {
            cdmImageInfo.getMetaData().put(key, text);
        }
    }


}
