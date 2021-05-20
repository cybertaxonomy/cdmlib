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
import java.io.InputStream;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.cybertaxonomy.media.info.model.MediaInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import com.github.dozermapper.core.loader.api.BeanMappingBuilder;
import com.github.dozermapper.core.loader.api.FieldsMappingOptions;
import com.github.dozermapper.core.loader.api.TypeMappingOptions;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.media.CdmImageInfo;

/**
 * Reads media metadata and file information from a
 * <a href="https://github.com/cybertaxonomy/MediaInfoService">MediaInfoService</a>
 *
 * @author a.kohlbecker
 * @since May 7, 2021
 */
public class MediaInfoServiceReader extends AbstactMediaMetadataReader {

    private static final Logger logger = Logger.getLogger(MediaInfoServiceReader.class);

    static private Mapper dozerMapper;

    static {
        // FIXME provide dozerMapper as bean via spring configuration
        //dozerMapper = DozerBeanMapperBuilder.buildDefault();
        dozerMapper = DozerBeanMapperBuilder.create().withMappingBuilder(new BeanMappingBuilder() {

            @Override
            protected void configure() {
                mapping(type(MediaInfo.class).mapEmptyString(true),
                        type(CdmImageInfo.class),
                        TypeMappingOptions.wildcardCaseInsensitive(true)
                ).fields(
                        field("size"),
                        field("length"),
                        FieldsMappingOptions.oneWay()

                ).fields(
                        field("extension"),
                        field("suffix"),
                        FieldsMappingOptions.oneWay()
                        );
            }
        }).build();

    }
    protected MediaInfoServiceReader(URI imageUri, URI metadataUri) {
        super(imageUri, metadataUri);
    }


    @Override
    public AbstactMediaMetadataReader read() throws IOException, HttpException {
        logger.info("reading metadata from " + metadataUri);
        InputStream jsonStream = UriUtils.getInputStream(metadataUri);
        ObjectMapper mapper = new ObjectMapper();
        MediaInfo mediaInfo = mapper.readValue(jsonStream, MediaInfo.class);
        dozerMapper.map(mediaInfo, getCdmImageInfo());
        // TODO how to do this with Dozer?:
        mediaInfo.getMetaData().entrySet().forEach(e -> processPutMetadataEntry(e.getKey(), e.getValue()));
        return this;
    }

}
