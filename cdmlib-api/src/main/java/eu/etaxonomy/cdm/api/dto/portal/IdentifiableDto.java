/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

/**
 * @author a.mueller
 * @date 19.01.2023
 */
public class IdentifiableDto extends SourcedDto {

    private String label;
//    private ContainerDto<SourceDto> sources;


    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
//    @Override
//    public ContainerDto<SourceDto> getSources() {
//        return sources;
//    }
//    @Override
//    public void addSource(SourceDto source) {
//        if (sources == null) {
//            sources = new ContainerDto<>();
//        }
//        sources.addItem(source);
//    }

}
