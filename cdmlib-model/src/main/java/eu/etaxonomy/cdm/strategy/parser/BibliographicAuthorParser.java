/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

/**
 * @author a.mueller
 * @date 05.03.2022
 */
public class BibliographicAuthorParser {

    private static final String etAl = "\\set\\s+al\\.?";
    private static final String team = ".+\\s*(&.+|"+etAl+")";
    private static final Pattern teamRe = Pattern.compile(team);

    private static BibliographicAuthorParser singleton;
    public static final BibliographicAuthorParser Instance() {
        if (singleton == null) {
            singleton = new BibliographicAuthorParser();
        }
        return singleton;
    }

    public TeamOrPersonBase<?> parse(String authorStr) {
        TeamOrPersonBase<?> result;
        if (StringUtils.isBlank(authorStr)) {
            return null;
        }
        Matcher matcher = teamRe.matcher(authorStr);
        if (matcher.matches()) {
            Team team = Team.NewInstance();
            result = team;
            String bracketPart = matcher.group(1);
            List<Person> members = getMembers(authorStr.substring(0, authorStr.replace(bracketPart, "").length()));
            members.stream().forEach(m->team.addTeamMember(m));
            if (bracketPart.matches(etAl) || bracketPart.matches("\\s*&\\s*al\\.?")) {
                team.setHasMoreMembers(true);
            }else {
                bracketPart = bracketPart.substring(1).trim();
                members = getMembers(bracketPart);
                //TODO this should be only 1 Person so we may call single person directly
                members.stream().forEach(m->team.addTeamMember(m));
            }
        }else {
            List<Person> members = getMembers(authorStr);
            if (members.size() == 1) {
                result = members.get(0);
            }else {
                Team team = Team.NewInstance();
                result = team;
                members.stream().forEach(m->team.addTeamMember(m));
            }
        }
        return result;
    }

    private List<Person> getMembers(String membersStr) {
        String initChar = "[A-Z"+UTF8.CAPITAL_A_ACUTE
                + UTF8.CAPITAL_E_ACUTE
                + UTF8.CAPITAL_I_ACUTE
                + UTF8.CAPITAL_O_ACUTE
                + UTF8.CAPITAL_U_ACUTE
                + "]";

        List<Person> result = new ArrayList<>();
        String[] split = membersStr.split(",");
        String initialsRe = "("+initChar+"\\.?\\s?|(de|de la|de los)){1,4}";
        boolean isLast = false;
//        boolean lastWasFamily;
        for (int i = 0; i<split.length; i++) {
            Person person = Person.NewInstance();
            isLast = i >= split.length-1;
            String str = split[i];
            String regex = "((?!"+initialsRe+"\\s).)*\\s+("+initialsRe+")";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(str);
            if (matcher.matches()) {
                //initials not separated by comma
                String initials = matcher.group(4);
                String family = str.replaceAll(initials + "$", "").trim();
                person.setFamilyName(family);
                person.setInitials(initials.trim());
            }else {
                if (isLast) {
                    person.setTitleCache(str.trim(), true);
                }else {
                    String next = split[i+1].trim();
                    if (next.matches(initialsRe)) {
                        person.setFamilyName(str.trim());
                        person.setInitials(next.trim());
                        i++;
                        while(i+1 < split.length && split[i+1].trim().matches(initialsRe)) {
                             next = split[i+1].trim();
                             person.setInitials(next.trim());
                             i++;
                        }
                    }else {
                        person.setTitleCache(str.trim(), true);
                    }
                }
            }
            result.add(person);
        }
        return result;
    }
}
