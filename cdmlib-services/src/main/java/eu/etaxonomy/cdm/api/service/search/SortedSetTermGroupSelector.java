/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.grouping.GroupSelector;
import org.apache.lucene.search.grouping.SearchGroup;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefHash;

/**
 * Like Lucene's {@code TermGroupSelector}, but reads {@link SortedSetDocValues}
 * instead of {@code SortedDocValues}. Hibernate Search 6 indexes sortable string
 * fields exclusively as SortedSetDocValues, so the stock TermGroupSelector cannot
 * be used for group-by fields such as {@code groupby_taxon.id__sort}.
 * <p>
 * Only the first value of each document is used as the group key, which matches
 * CDM's single-valued group-by fields.
 */
public class SortedSetTermGroupSelector extends GroupSelector<BytesRef> {

    private final String field;
    private final BytesRefHash values = new BytesRefHash();
    private final Map<Integer, Integer> ordsToGroupIds = new HashMap<>();

    private SortedSetDocValues docValues;
    private int groupId;

    private boolean secondPass;
    private boolean includeEmpty;

    public SortedSetTermGroupSelector(String field) {
        this.field = field;
    }

    @Override
    public void setNextReader(LeafReaderContext readerContext) throws IOException {
        this.docValues = DocValues.getSortedSet(readerContext.reader(), field);
        this.ordsToGroupIds.clear();
        BytesRef scratch = new BytesRef();
        for (int i = 0; i < values.size(); i++) {
            values.get(i, scratch);
            long ord = this.docValues.lookupTerm(scratch);
            if (ord >= 0) {
                ordsToGroupIds.put((int) ord, i);
            }
        }
    }

    @Override
    public void setScorer(Scorable scorer) throws IOException {
    }

    @Override
    public State advanceTo(int doc) throws IOException {
        if (!this.docValues.advanceExact(doc)) {
            groupId = -1;
            return includeEmpty ? State.ACCEPT : State.SKIP;
        }
        long ordLong = docValues.nextOrd();
        if (ordLong == SortedSetDocValues.NO_MORE_ORDS) {
            groupId = -1;
            return includeEmpty ? State.ACCEPT : State.SKIP;
        }
        int ord = (int) ordLong;
        if (ordsToGroupIds.containsKey(ord)) {
            groupId = ordsToGroupIds.get(ord);
            return State.ACCEPT;
        }
        if (secondPass) {
            return State.SKIP;
        }
        groupId = values.add(docValues.lookupOrd(ord));
        ordsToGroupIds.put(ord, groupId);
        return State.ACCEPT;
    }

    private final BytesRef scratch = new BytesRef();

    @Override
    public BytesRef currentValue() {
        if (groupId == -1) {
            return null;
        }
        values.get(groupId, scratch);
        return scratch;
    }

    @Override
    public BytesRef copyValue() {
        if (groupId == -1) {
            return null;
        }
        return BytesRef.deepCopyOf(currentValue());
    }

    @Override
    public void setGroups(Collection<SearchGroup<BytesRef>> searchGroups) {
        this.values.clear();
        this.values.reinit();
        for (SearchGroup<BytesRef> sg : searchGroups) {
            if (sg.groupValue == null) {
                includeEmpty = true;
            } else {
                this.values.add(sg.groupValue);
            }
        }
        this.secondPass = true;
    }
}
