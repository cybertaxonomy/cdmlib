package eu.etaxonomy.cdm.io.print.compare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.print.PrintPubExportConfigurator.FactSortMode;

@Component
public class PrintPubFactOrderStrategyResolver {

    @Autowired
    private FactPortalLikeOrderStrategy portalLikeFactOrderStrategy;

    @Autowired
    private FactAlphabeticalOrderStrategy alphabeticalFactOrderStrategy;

    public IPrintPubFactOrderStrategy resolve(FactSortMode mode) {

        if (mode == FactSortMode.ALPHABETICAL) {
            return alphabeticalFactOrderStrategy;
        }

        return portalLikeFactOrderStrategy;
    }
}