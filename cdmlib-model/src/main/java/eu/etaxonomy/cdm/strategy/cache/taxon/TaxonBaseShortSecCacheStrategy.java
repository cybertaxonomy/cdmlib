package eu.etaxonomy.cdm.strategy.cache.taxon;


/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/



import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;

public class TaxonBaseShortSecCacheStrategy<T extends TaxonBase>
        extends StrategyBase
        implements ITaxonCacheStrategy<T> {

    private static final long serialVersionUID = -2831618484053675222L;
    final static UUID uuid = UUID.fromString("931e48f0-2033-11de-8c30-0800200c9a66");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

//	@Override
//    public String getTitleCache(T taxonBase) {
//	    return getTitleCache(taxonBase, null);
//    }

	@Override
    public String getTitleCache(T taxonBase) {
		String title;
		if (taxonBase.getName() != null && taxonBase.getName().getTitleCache() != null){
			String namePart = getNamePart(taxonBase);

			title = namePart + " sec. ";  //TODO check if separator is required before, e.g. for nom. status. see TaxonBaseDefaultCacheStrategy
			title += getSecundumPart(taxonBase);
		}else{
			title = taxonBase.toString();
		}
		if (taxonBase.isDoubtful()){
			title = "?" + title;
		}
		return title;
	}

	/**
	 * @param taxonBase
	 * @param title
	 * @return
	 */
	private String getSecundumPart(T taxonBase) {
		String result = "???";
		Reference<?> sec = taxonBase.getSec();
		sec = HibernateProxyHelper.deproxy(sec, Reference.class);
		if (sec != null){
			if (sec.isProtectedTitleCache()){
				return sec.getTitleCache();
			}
			if (sec.getAuthorship() != null){

				if (sec.getAuthorship().isInstanceOf(Team.class)){
					Team authorTeam = HibernateProxyHelper.deproxy(sec.getAuthorship(), Team.class);
					if (authorTeam.getTeamMembers().size() > 2){
						if (authorTeam.getTeamMembers().get(0).getLastname() != null){
					        result = authorTeam.getTeamMembers().get(0).getLastname() + " & al.";
					    } else {
					        result = authorTeam.getTeamMembers().get(0).getTitleCache();
					        result = result + " & al.";
					    }
					} else if (authorTeam.getTeamMembers().size() == 2){
						String firstAuthor;
						if (authorTeam.getTeamMembers().get(0).getLastname() != null){
							firstAuthor = authorTeam.getTeamMembers().get(0).getLastname();
						}else{
							firstAuthor = authorTeam.getTeamMembers().get(0).getTitleCache();
						}
						String secondAuthor;
						if (authorTeam.getTeamMembers().get(1).getLastname() != null){
							secondAuthor = authorTeam.getTeamMembers().get(1).getLastname();
						}else{
							secondAuthor = authorTeam.getTeamMembers().get(1).getTitleCache();
						}
						result = firstAuthor + " & " + secondAuthor;

					} else{
						if (authorTeam.getTeamMembers().get(0).getLastname() != null){
					        result = authorTeam.getTeamMembers().get(0).getLastname();
					    } else {
					        result = authorTeam.getTeamMembers().get(0).getTitleCache();
					    }
					}

				} else {
					Person author = HibernateProxyHelper.deproxy(sec.getAuthorship(), Person.class);
					if (author.getLastname() != null){
						result = author.getLastname();
					} else{
						result = author.getTitleCache();
					}
				}
				if (result != null){
					result = result.replaceAll("[A-Z]\\.", "");
				}
				if (sec.getYear() != null && result != null){
					result = result.concat(" (" + sec.getYear()+")");
				}
			}else{
				result = taxonBase.getSec().getTitleCache();
			}
		}
		return result;
	}

	/**
	 * @param name
	 */
	private String getNamePart(TaxonBase<?> taxonBase) {
		TaxonNameBase<?,?> nameBase = taxonBase.getName();
		String result = nameBase.getTitleCache();
		//use name cache instead of title cache if required
		if (taxonBase.isUseNameCache() && nameBase.isInstanceOf(NonViralName.class)){
			NonViralName<?> nvn = HibernateProxyHelper.deproxy(nameBase, NonViralName.class);
			result = nvn.getNameCache();
		}
		if (StringUtils.isNotBlank(taxonBase.getAppendedPhrase())){
			result = result.trim() + " " +  taxonBase.getAppendedPhrase().trim();
		}
		return result;
	}

    @Override
    public List<TaggedText> getTaggedTitle(T taxonBase) {
        if (taxonBase == null){
            return null;
        }

        List<TaggedText> tags = new ArrayList<TaggedText>();

        if (taxonBase.isProtectedTitleCache()){
            //protected title cache
            tags.add(new TaggedText(TagEnum.name, taxonBase.getTitleCache()));
            return tags;
        }else{
            //name
            TaxonNameBase<?,INameCacheStrategy<TaxonNameBase>> name = taxonBase.getName();
            if (name != null){
                //TODO
                List<TaggedText> nameTags = name.getCacheStrategy().getTaggedTitle(name);
                tags.addAll(nameTags);
            }

            //ref.
            List<TaggedText> secTags;
            Reference<?> ref = taxonBase.getSec();
            ref = HibernateProxyHelper.deproxy(ref, Reference.class);
            if (ref != null){
                secTags = getSecReferenceTags(ref);
            }else{
                secTags = new ArrayList<TaggedText>();
                if (isBlank(taxonBase.getAppendedPhrase())){
                    secTags.add(new TaggedText(TagEnum.reference, "???"));
                }
            }
            if(! secTags.isEmpty()){
                //sec.
                tags.add(new TaggedText(TagEnum.separator, "sec."));
                tags.addAll(secTags);
            }
        }
        return tags;
    }

    /**
     * @param ref
     */
    private List<TaggedText> getSecReferenceTags(Reference<?> sec) {
        List<TaggedText> tags = new ArrayList<TaggedText>();

        if (sec.isProtectedTitleCache()){
            tags.add(new TaggedText(TagEnum.reference, sec.getTitleCache()));
        }else{
            if (sec.getAuthorship() != null){
                List<TaggedText> authorTags;
                if (sec.getAuthorship().isInstanceOf(Team.class)){
                    authorTags = handleTeam(sec);
                } else {
                    authorTags = handlePerson(sec);
                }
                tags.addAll(authorTags);

                //FIXME why did we have this normalization? For removing first names??
//                if (result != null){
//                    result = result.replaceAll("[A-Z]\\.", "");
//                }

                //year
                String year = sec.getYear();
                if (StringUtils.isNotBlank(year) && ! authorTags.isEmpty()){
                    tags.add(new TaggedText(TagEnum.separator, "("));
                    tags.add(new TaggedText(TagEnum.year, year));
                    tags.add(new TaggedText(TagEnum.separator, ")"));
                }
            }else{

            }
        }

        return tags;
    }

    private List<TaggedText>  handlePerson(Reference<?> sec) {
        List<TaggedText> tags = new ArrayList<TaggedText>();

        Person author = HibernateProxyHelper.deproxy(sec.getAuthorship(), Person.class);
        String authorStr;
        if (author.getLastname() != null){
            authorStr = author.getLastname();
        } else{
            authorStr = author.getTitleCache();
        }
        tags.add(new TaggedText(TagEnum.authors, authorStr));
        return tags;
    }

    private List<TaggedText> handleTeam(Reference<?> sec) {
        List<TaggedText> tags = new ArrayList<TaggedText>();

        Team authorTeam = HibernateProxyHelper.deproxy(sec.getAuthorship(), Team.class);
        if (authorTeam.isProtectedTitleCache() || authorTeam.getTeamMembers().isEmpty()){
            String authorStr = authorTeam.getTitleCache();
            tags.add(new TaggedText(TagEnum.authors, authorStr));
        }else if (authorTeam.getTeamMembers().size() > 2){
            //>2 members
            if (authorTeam.getTeamMembers().get(0).getLastname() != null){
                String authorStr = authorTeam.getTeamMembers().get(0).getLastname() + " & al.";
                tags.add(new TaggedText(TagEnum.authors, authorStr));
            } else {
                String authorStr = authorTeam.getTeamMembers().get(0).getTitleCache();
                authorStr = authorStr + " & al.";
                tags.add(new TaggedText(TagEnum.authors, authorStr));
            }
        } else if (authorTeam.getTeamMembers().size() == 2){
            //2 members
            String firstAuthor;
            if (authorTeam.getTeamMembers().get(0).getLastname() != null){
                firstAuthor = authorTeam.getTeamMembers().get(0).getLastname();
            }else{
                firstAuthor = authorTeam.getTeamMembers().get(0).getTitleCache();
            }
            String secondAuthor;
            if (authorTeam.getTeamMembers().get(1).getLastname() != null){
                secondAuthor = authorTeam.getTeamMembers().get(1).getLastname();
            }else{
                secondAuthor = authorTeam.getTeamMembers().get(1).getTitleCache();
            }
            String authorStr = firstAuthor + " & " + secondAuthor;
            tags.add(new TaggedText(TagEnum.authors, authorStr));
        } else{
            //1 member
            String authorStr;
            if (authorTeam.getTeamMembers().get(0).getLastname() != null){
                authorStr = authorTeam.getTeamMembers().get(0).getLastname();
            } else {
                authorStr = authorTeam.getTeamMembers().get(0).getTitleCache();
            }
            tags.add(new TaggedText(TagEnum.authors, authorStr));
        }
        return tags;
    }

    @Override
    public String getTitleCache(T taxonBase, HTMLTagRules htmlTagRules) {
        List<TaggedText> tags = getTaggedTitle(taxonBase);
        if (tags == null){
            return null;
        }else{
            String result = TaggedCacheHelper.createString(tags, htmlTagRules);
            return result;
        }
    }

}