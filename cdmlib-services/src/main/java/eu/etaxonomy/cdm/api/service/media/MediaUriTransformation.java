/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.media;

/**
 * Defines a transformation rule to create volatile media representation on base of existing ones.
 * <p>
 * All defined {@link SearchReplace} rules must match otherwise the transformations is omitted.
 * A {@link SearchReplace} rule can be mediate an identity transformation of is URI part
 * in which case the rule just acts as a filter.
 * <p>
 * <b>CHANGING THIS CLASS MAY BREAK DESERIALIZATION OF EXISTING CDM PREFERENCES!</b>
 *
 * @author a.kohlbecker
 * @since Jul 8, 2020
 */
public class MediaUriTransformation {

    SearchReplace scheme = null;
    SearchReplace host = null;
    SearchReplace pathQueryFragment = null;

    String mimeType = null;

    Integer width = null;
    Integer height = null;
    boolean isMaxExtend = false;

    /**
     *
     */
    public MediaUriTransformation() {

    }

    public SearchReplace getScheme() {
        return scheme;
    }

    /**
     * Replacement rule for the scheme part of the URI.
     * <p>
     * In case the regex pattern {@link SearchReplace#getSearch()} does not match the URI part
     * the whole <code>MediaUriTransformation</code> must be omitted.
     *
     * @param scheme The search replace rule for the URI scheme
     */
    public void setScheme(SearchReplace scheme) {
        this.scheme = scheme;
    }


    public SearchReplace getHost() {
        return host;
    }

    /**
     * Replacement rule for the host part of the URI.
     * <p>
     * In case the regex pattern {@link SearchReplace#getSearch()} does not match the URI part
     * the whole <code>MediaUriTransformation</code> must be omitted.
     *
     * @param scheme The search replace rule for the URI host
     */
    public void setHost(SearchReplace host) {
        this.host = host;
    }


    public SearchReplace getPathQueryFragment() {
        return pathQueryFragment;
    }


    /**
     * Replacement rule for the combined {@code path + "?" + query + "#" + fragment} parts of the URI.
     * <p>
     * In case the regex pattern {@link SearchReplace#getSearch()} does not match this string
     * the whole <code>MediaUriTransformation</code> must be omitted.
     *
     * @param scheme The search replace rule for combined {@code path + "?" + query + "#" + fragment} parts of the URI.
     */
    public void setPathQueryFragment(SearchReplace pathQueryFragment) {
        this.pathQueryFragment = pathQueryFragment;
    }

    /**
     * The mime type of the target media
     *
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * The mime type of the target media
     *
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * The pixel width of the target media, usually of an image.
     * The resulting image size also depends on {@link #isMaxExtend()}.
     *
     * @return the width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * The pixel width of the target media, usually of an image.
     * The resulting image size also depends on {@link #isMaxExtend()}.
     *
     * @param width the width to set
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * The pixel height of the target media, usually of an image.
     * The resulting image size also depends on {@link #isMaxExtend()}.
     *
     * @return the height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * The pixel height of the target media, usually of an image.
     * The resulting image size also depends on {@link #isMaxExtend()}.
     *
     * @param height the height to set
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     * When true, the {@link #getWidth() width} and {@link #getHeight() height} are interpreted as max-extend. Otherwise
     * it is is assumed that the image will be cropped to the specified size.
     *
     * @return the isMaxExtend
     */
    public boolean isMaxExtend() {
        return isMaxExtend;
    }

    /**
     * See {@link #isMaxExtend()}
     *
     * @param isMaxExtend the isMaxExtend to set
     */
    public void setMaxExtend(boolean isMaxExtend) {
        this.isMaxExtend = isMaxExtend;
    }


}