/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.service;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.IProgressMonitorService;
import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RemotingProgressMonitorThread;
import eu.etaxonomy.cdm.io.common.CacheUpdaterConfigurator;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.SOURCE_TYPE;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.common.SetSecundumForSubtreeConfigurator;
import eu.etaxonomy.cdm.io.common.SortIndexUpdaterConfigurator;
import eu.etaxonomy.cdm.io.distribution.excelupdate.ExcelDistributionUpdateConfigurator;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.io.reference.ris.in.RisReferenceImportConfigurator;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportConfiguratorBase;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportConfigurator;

/**
 * @author cmathew
 * @date 31 Jul 2015
 *
 */
@Service
@Transactional(readOnly = false)
public class IOServiceImpl implements IIOService {

    @Autowired
    CdmApplicationAwareDefaultExport cdmExport;

    @Autowired
    @Qualifier("defaultImport")
    CdmApplicationAwareDefaultImport<?> cdmImport;

    @Autowired
    IProgressMonitorService progressMonitorService;
//
//    @Autowired
//    @Qualifier("defaultUpdate")
//    CdmApplicationAwareDefaultUpdate cdmUpdate;


    @Override
    public ExportResult export(IExportConfigurator config) {
        config.setTarget(TARGET.EXPORT_DATA);
        return cdmExport.execute(config);
    }


    @Override
    public UUID monitImportData(final IImportConfigurator configurator, final byte[] importData, final SOURCE_TYPE type) {
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {

                configurator.setProgressMonitor(monitor);
                ImportResult result = importData(configurator, importData, type);
                for(byte[] report : result.getReports()) {
                    monitor.addReport(new String(report));
                }
                return result;
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(3);
        monitorThread.start();
        return uuid;
    }

    @Override
    public UUID monitExportData(@SuppressWarnings("rawtypes") final IExportConfigurator configurator) {
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {

                configurator.setProgressMonitor(monitor);
                ExportResult result = export(configurator);
//                for(byte[] report : result.getReports()) {
//                    monitor.addReport(new String(report));
//                }
                return result;
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(3);
        monitorThread.start();
        return uuid;
    }

    @Override
    public UUID monitUpdateData(final IImportConfigurator configurator) {
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {

                configurator.setProgressMonitor(monitor);
                ImportResult result =updateData((SetSecundumForSubtreeConfigurator)configurator);

                return result;
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(3);
        monitorThread.start();
        return uuid;
    }

    @Override
    public ImportResult importData(IImportConfigurator configurator, byte[] importData, SOURCE_TYPE type) {
        ImportResult result;
        switch(type) {
        case URI:
            if (importData.equals(new byte[1])){
                result = cdmImport.invoke(configurator);
                return result;
            }
            return importDataFromUri(configurator, importData);
        case INPUTSTREAM:
            return importDataFromInputStream(configurator,importData);
        default :
            throw new RuntimeException("Source type is not recongnised");
        }
    }

    @Override
    public ImportResult updateData(SetSecundumForSubtreeConfigurator configurator) {
        ImportResult result;

        result = cdmImport.invoke(configurator);
        return result;
    }

    @Override
    public ImportResult importDataFromUri(IImportConfigurator configurator, byte[] importData) {
        ImportResult result;

        ImportConfiguratorBase config = (ImportConfiguratorBase)configurator;
        String suffix = ".import";
        String prefix = "cdm-";
        FileOutputStream stream = null;

        try {
            Path tempFilePath = Files.createTempFile(prefix, suffix);
            stream = new FileOutputStream(tempFilePath.toFile());
            stream.write(importData);
            config.setSource(tempFilePath.toUri());
            result = cdmImport.invoke(config);
     //       Files.delete(tempFilePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    @Override
    public ImportResult importDataFromInputStream(IImportConfigurator configurator, byte[] importData) {
        ImportConfiguratorBase config = (ImportConfiguratorBase)configurator;
        ImportResult result;
        try {
            if (config instanceof ExcelImportConfiguratorBase){
                ExcelImportConfiguratorBase excelConfig = (ExcelImportConfiguratorBase)config;
                //excelConfig.setStream(importData);
            }else{
                config.setSource(new ByteArrayInputStream(importData));
            }
            result = cdmImport.invoke(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    @Override
    public ImportResult importDataFromStream(SpecimenImportConfiguratorBase configurator) {
        ImportResult result = new ImportResult();
            result = cdmImport.invoke(configurator);
            return result;
    }

    @Override
    public ImportResult importDataFromStream(List<Abcd206ImportConfigurator> configurators) {
        ImportResult result = new ImportResult();

            for (SpecimenImportConfiguratorBase configurator:configurators){
                result = cdmImport.invoke(configurator);
            }
            return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ImportResult updateSortIndex(SortIndexUpdaterConfigurator config) {
        ImportResult result = new ImportResult();

        result = cdmImport.invoke(config);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImportResult updateCaches(CacheUpdaterConfigurator config) {
        ImportResult result = new ImportResult();

        result = cdmImport.invoke(config);
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ImportResult updateDistributionData(ExcelDistributionUpdateConfigurator configurator) {
        ImportResult result = new ImportResult();
        result = cdmImport.invoke(configurator);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImportResult importRISData(RisReferenceImportConfigurator configurator) {
        ImportResult result = new ImportResult();
        result = cdmImport.invoke(configurator);
        return result;
    }





}
