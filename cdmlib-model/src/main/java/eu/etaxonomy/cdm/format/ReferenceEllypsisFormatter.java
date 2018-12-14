/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Creates truncated labels for references by applying ellypsis on the reference authors and title for inReferences an
 * ellypsis is also being created.
 * <p>
 * Here are some examples:
 * <table>
 * <th>
 *  <td>original</td>
 *  <td>ellypsis</td></th>
 * <tr>
 * </tr>
 * <td>
 *   <td>Gottschling, M., Tillmann, U., Kusber, W.-H. & al., A Gordian knot: Nomenclature and taxonomy of Heterocapsa triquetra (Peridiniales: Heterocapsaceae) in Taxon 67(1): 179–185. 2018<td>
 *   </td>Gottschling, M.,…Kusber…, A Gordian knot:… in Taxon 67(1): 179–185. 2018</td>
 *   </tr>
 * <td>
 *   <td>Hamilton, P.B., Stachura-Suchoples, K., Kusber, W.-H. & al., Typification of the puzzling large diatom species Neidium iridis Ehrenb. in Cryptog. Algol.<td>
 *   </td>Hamilton, P.B.,…Kusber…, Typification of the… in Cryptog. Algol.</td>
 * </tr>
 * <td>
 * <td>Jahn , R., Kusber, W.-H. & Cocquyt, C., Differentiating Iconella from Surirella (Bacillariophyceae): typifying four Ehrenberg names and a preliminary checklist of the African taxa in PhytoKeys 82: 73-112<td>
 * </td>Jahn , R., Kusber, W.-H. & Cocquyt, C., Differentiating… in PhytoKeys 82: 73-112</td>
 * </tr>
 * <td>
 * <td>Jahn, R., Kusber, W.-H., Skibbe, O. & al., Gomphonella olivacea (Hornemann) Rabenhorst – a new phylogenetic position for a well-known taxon, its typification, new species and combinations in Cryptog. Algol.<td>
 * </td>Jahn, R., Kusber,…, Gomphonella olivacea… in Cryptog. Algol.</td>
 * </td>
 * </table>
 * @author a.kohlbecker
 * @since Dec 12, 2018
 *
 */
public class ReferenceEllypsisFormatter extends AbstractEllypsisFormatter<Reference> {

    /**
     * This init strategy should be used when the ReferenceEllypsisFormatter is being used
     * outside of a hibernate session
     */
    public static List<String> INIT_STRATEGY = Arrays.asList(
            "authorship",
            "inReference.authorship",
            "inReference.inReference.authorship",
            "inReference.inReference.inReference");

    public enum LabelType {
        NOMENCLATURAL,
        BIBLIOGRAPHIC;
    }

    private LabelType labelType;
    private int maxAuthorCharsVisible = 20;
    private int maxTitleCharsVisible = 20;
    private int minNumOfWords = 1;

    public ReferenceEllypsisFormatter(LabelType labelType){
        this.labelType = labelType;
    }

    /**
     * @param entity
     * @param filterString
     * @return
     */
    @Override
    protected EllipsisData entityEllypsis(Reference entity, String filterString) {

        String label = "";
        String authors = entity.getAuthorship() != null ? entity.getAuthorship().getTitleCache() : "";
        String title = null;
        String titleCache;

        switch(labelType){
            case NOMENCLATURAL:
                if(!entity.isProtectedAbbrevTitleCache()){
                    title = entity.getAbbrevTitle();
                    if(title == null) {
                        // fallback to use the title
                        title = entity.getTitle();
                    }
                }
                titleCache = entity.getAbbrevTitleCache();
                break;
            case BIBLIOGRAPHIC:
            default:
                if(!entity.isProtectedTitleCache()){
                    title = entity.getTitle();
                    if(title == null) {
                        // fallback to use the abbreviated title
                        title = entity.getAbbrevTitle();
                    }
                }
                titleCache = entity.getTitleCache();
                break;
        }

        Pattern pattern = Pattern.compile("(" + filterString +")", Pattern.CASE_INSENSITIVE);

        if(authors != null){
            String authorsEllipsed = authors;
            if(authorsEllipsed.length() > maxAuthorCharsVisible) {
                authorsEllipsed = stringEllypsis(authors, maxAuthorCharsVisible, minNumOfWords);
                authorsEllipsed = preserveString(filterString, authors, pattern, authorsEllipsed);
            }
            label = titleCache.replace(authors, authorsEllipsed);
        }

        if(title != null){
            String titleEllipsed = title;
            if(titleEllipsed.length() > maxTitleCharsVisible) {
                titleEllipsed = stringEllypsis(title, maxTitleCharsVisible, minNumOfWords);
                titleEllipsed = preserveString(filterString, title, pattern, titleEllipsed);
            }
            label = label.replace(title, titleEllipsed);
        }


        if(entity.getInReference() != null){
            EllipsisData inRefEd = entityEllypsis(entity.getInReference(), filterString);
            label = label.replace(inRefEd.original, inRefEd.truncated);
        }

        EllipsisData ed = new EllipsisData(titleCache, label);

        return ed;
    }

    public int getMaxAuthorCharsVisible() {
        return maxAuthorCharsVisible;
    }

    public void setMaxAuthorCharsVisible(int maxAuthorCharsVisible) {
        this.maxAuthorCharsVisible = maxAuthorCharsVisible;
    }

    public int getMaxTitleCharsVisible() {
        return maxTitleCharsVisible;
    }

    public void setMaxTitleCharsVisible(int maxTitleCharsVisible) {
        this.maxTitleCharsVisible = maxTitleCharsVisible;
    }

    public int getMinNumOfWords() {
        return minNumOfWords;
    }

    public void setMinNumOfWords(int minNumOfWords) {
        this.minNumOfWords = minNumOfWords;
    }


}
