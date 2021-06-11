/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.format.AbstractEllypsisFormatter;
import eu.etaxonomy.cdm.format.AbstractEllypsisFormatter.EllipsisData;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

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
            "type",
            "authorship.teamMembers",
            "inReference.type",
            "inReference.authorship.teamMembers",
            "inReference.inReference.type",
            "inReference.inReference.authorship.teamMembers",
            "inReference.inReference.inReference.type",
            "inReference.inReference.inReference.authorship.teamMembers");

    public enum LabelType {
        NOMENCLATURAL,
        BIBLIOGRAPHIC;
    }

    private EnumSet<ReferenceType> sectionTypes = EnumSet.of(ReferenceType.Section, ReferenceType.BookSection);

    private LabelType labelType;
    private int maxCharsVisible = 20;
    private int minNumOfWords = 1;
    private int numOfPreservedEndWords = 3;
    /**
     * see https://dev.e-taxonomy.eu/redmine/issues/8252#note-14
     */
    private int maxAutorsVisibleInSection = 4;


    public ReferenceEllypsisFormatter(LabelType labelType){
        this.labelType = labelType;
    }

    /**
     * @param entity
     * @param filterString
     * @return
     */
    @Override
    protected EllipsisData entityEllypsis(Reference entity, String preserveString) {

        String label = "";
        String authorsCache = null;
        List<String> authorTeamCaches = null;
        Team team = null;
        String title = null;
        String titleCache;

        boolean isSection = sectionTypes.contains(entity.getType());
        boolean isProtectedAuthorsCache = false;

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
                if(entity.getAuthorship() != null){
                    if(entity.getAuthorship() instanceof Team){
                        team = (Team)entity.getAuthorship();
                        authorTeamCaches = team.getTeamMembers().stream().map(topb -> topb.getNomenclaturalTitleCache()).collect(Collectors.toList());
                        isProtectedAuthorsCache = team.isProtectedNomenclaturalTitleCache();
                    }
                    authorsCache = entity.getAuthorship().getNomenclaturalTitleCache();
                }
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
                if(entity.getAuthorship() != null){
                    if(entity.getAuthorship() instanceof Team){
                        team = (Team)entity.getAuthorship();
                        authorTeamCaches = team.getTeamMembers().stream().map(topb -> topb.getTitleCache()).collect(Collectors.toList());
                    } else {
                        authorTeamCaches = new ArrayList<String>();
                        authorTeamCaches.add(entity.getAuthorship().getTitleCache());
                    }
                    authorsCache = entity.getAuthorship().getTitleCache();
                    isProtectedAuthorsCache = entity.getAuthorship().isProtectedTitleCache();
                }
                break;
        }

        Pattern pattern = Pattern.compile("(" + preserveString +")", Pattern.CASE_INSENSITIVE);

        LinkedList<EllipsisData> edList = new LinkedList<EllipsisData>();
        // the titleCache as initial element
        edList.add(new EllipsisData(titleCache, null));

        String authorsEllipsed = null;
        if(isSection){
            // for Sections of articles it is crucial to show as much as possible of the author team to
            // allow the user identify the section by the names of the team members
            if(isProtectedAuthorsCache || authorTeamCaches == null){
                // maxAutorsVisibleInSection * 2 is only a vague approximization to the real number of words per author
                if(!StringUtils.isEmpty(authorsCache)){
                    authorsEllipsed = stringEllypsis(authorsCache, maxCharsVisible, maxAutorsVisibleInSection * 2);
                }
            } else {
                authorsEllipsed = authorTeamCaches.stream().limit(maxAutorsVisibleInSection).collect(Collectors.joining(", "));
            }
        } else {
            if(!StringUtils.isEmpty(authorsCache)){
                authorsEllipsed = stringEllypsis(authorsCache, maxCharsVisible, minNumOfWords);
            }
        }
        if(authorsEllipsed != null){
            authorsEllipsed = preserveString(preserveString, authorsCache, pattern, authorsEllipsed);
            applyAndSplit(edList, authorsCache, authorsEllipsed);
        }

        int titleCompensation = 1 +
                (StringUtils.isEmpty(authorsCache) ? 1 : 0)
                + (entity.getInReference() == null? 1 : 0)
                + (EnumSet.of(ReferenceType.Journal, ReferenceType.PrintSeries, ReferenceType.Proceedings).contains(entity.getType()) ? 2 : 0);

        if(!StringUtils.isEmpty(title)){
            // the titleCompensation helps in cases like journals and when the reference has not much additional information than a title.
            String titleEllipsed = stringEllypsis(title, maxCharsVisible * titleCompensation, minNumOfWords * titleCompensation);
            titleEllipsed = preserveString(preserveString, title, pattern, titleEllipsed);
            applyAndSplit(edList, title, titleEllipsed);
        }

        if(entity.getInReference() != null){
            EllipsisData inRefEd = entityEllypsis(entity.getInReference(), preserveString);
            inRefEd.original = " in " + inRefEd.original;
            inRefEd.truncated = " in " + inRefEd.truncated;
            applyAndSplit(edList, inRefEd.original, inRefEd.truncated);
        }

        // ellypsis for all text parts which haven not been processed yet
        for(EllipsisData ed : edList){
            if(ed.truncated == null){
                if(edList.getLast().equals(ed)){
                    // special handling for last one
                    List<String> tokens = Arrays.asList(ed.original.split(" "));
                    if(tokens.size() > numOfPreservedEndWords){
                        // unpreservedParts part may need ellipsis
                        String unpreservedPart = String.join(" ", tokens.subList(0, tokens.size() - numOfPreservedEndWords));
                        String unpreservedPartEllypsis = stringEllypsis(unpreservedPart, maxCharsVisible * titleCompensation, minNumOfWords * titleCompensation);
                        unpreservedPartEllypsis = preserveString(preserveString, unpreservedPart, pattern, unpreservedPartEllypsis);
                        String preservedPart = String.join(" ", tokens.subList(tokens.size() - numOfPreservedEndWords, tokens.size()));
                        ed.truncated = unpreservedPartEllypsis + " " + preservedPart;
                    } else {
                        // only preserved words -> do not change
                        ed.truncated = ed.original;
                    }
                } else {
                    ed.truncated = stringEllypsis(ed.original, maxCharsVisible, minNumOfWords);
                    ed.truncated = preserveString(preserveString, ed.original, pattern, ed.truncated);
                }
            }
            label += ed.truncated;
        }


        EllipsisData ed = new EllipsisData(titleCache, label);
        return ed;
    }


    public int getMaxCharsVisible() {
        return maxCharsVisible;
    }

    public void setMaxCharsVisible(int maxAuthorCharsVisible) {
        this.maxCharsVisible = maxAuthorCharsVisible;
    }


    public int getMinNumOfWords() {
        return minNumOfWords;
    }

    public void setMinNumOfWords(int minNumOfWords) {
        this.minNumOfWords = minNumOfWords;
    }

    public int getMaxAutorsVisibleInSection() {
        return maxAutorsVisibleInSection;
    }

    public void setMaxAutorsVisibleInSection(int maxAutorsVisibleInSection) {
        this.maxAutorsVisibleInSection = maxAutorsVisibleInSection;
    }


}
