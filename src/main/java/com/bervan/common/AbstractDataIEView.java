package com.bervan.common;


import com.bervan.common.model.PersistableTableData;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BervanLogger;
import com.bervan.ieentities.BaseExcelExport;
import com.bervan.ieentities.BaseExcelImport;
import com.bervan.ieentities.ExcelIEEntity;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AbstractDataIEView<ID extends Serializable, T extends PersistableTableData<ID>> extends AbstractPageView {
    protected final BaseService<ID, T> dataService;
    protected final MenuNavigationComponent pageLayout;
    protected final BervanLogger logger;
    private final Class<T> classToExport;
    @Value("${file.service.storage.folder}")
    private String pathToFileStorage;
    @Value("${global-tmp-dir.file-storage-relative-path}")
    private String globalTmpDir;
    protected final AbstractFiltersLayout<ID, T> filtersLayout;
    protected final Button exportButton = new BervanButton("Prepare data for export");

    public AbstractDataIEView(BaseService<ID, T> dataService,
                              MenuNavigationComponent pageLayout,
                              BervanLogger logger, Class<T> classToExport) {
        this.logger = logger;
        this.dataService = dataService;
        this.pageLayout = pageLayout;
        this.classToExport = classToExport;
        this.filtersLayout = new AbstractFiltersLayout<>(classToExport, exportButton);

        add(pageLayout);

        add(filtersLayout);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            InputStream inputStream = buffer.getInputStream();
            try {
                importData(inputStream, fileName);
                Notification.show("File uploaded successfully: " + fileName);
            } catch (Exception e) {
                logger.error("Failed to upload file: " + fileName, e);
                Notification.show("Failed to upload file: " + fileName);
            }
        });

        upload.addFailedListener(event ->
                Notification.show("Upload failed"));

        exportButton.addClickListener(buttonClickEvent -> {
            StreamResource resource = prepareDownloadResource();
            Anchor downloadLink = new Anchor(resource, "Export");
            downloadLink.getElement().setAttribute("download", true);

            add(downloadLink);
            remove(exportButton);
            remove(upload);
            add(upload);
        });

        add(upload);
    }

    private void importData(InputStream inputStream, String fileName) throws IOException {
        File uploadFolder = new File(pathToFileStorage + globalTmpDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
        File file = new File(pathToFileStorage + globalTmpDir + File.separator + fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[10240];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        BaseExcelImport baseExcelImport = new BaseExcelImport(Collections.singletonList(classToExport), logger);
        List<? extends ExcelIEEntity<ID>> objects = (List<? extends ExcelIEEntity<ID>>) baseExcelImport.importExcel(baseExcelImport.load(file));
        logger.debug("Extracted " + objects.size() + " entities from excel.");

        dataService.saveIfValid(objects);
    }

    public StreamResource prepareDownloadResource() {
        try {
            BaseExcelExport baseExcelExport = new BaseExcelExport();
            List<ExcelIEEntity<?>> dataToExport = getDataToExport();
            logger.info("Found " + dataToExport.size() + " to be exported!");
            if (dataToExport.isEmpty()) {
                showWarningNotification("No data to be exported!");
                return null;
            }
            Workbook workbook = baseExcelExport.exportExcel(dataToExport, null);
            File saved = baseExcelExport.save(workbook, pathToFileStorage + globalTmpDir, "export" + LocalDateTime.now() + ".xlsx");
            String filename = saved.getName();

            return new StreamResource(filename, () -> {
                try {
                    return new FileInputStream(saved);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            logger.error("Could not prepare export data.", e);
            showErrorNotification("Could not prepare export data.");
        }

        return null;
    }

    private List<ExcelIEEntity<?>> getDataToExport() {
        List<ExcelIEEntity<?>> result = new ArrayList<>();

        try {
            Set<T> loaded = dataService.load(filtersLayout.buildCombinedFilters(), Pageable.ofSize(100000));
            for (T t : loaded) {
                if (t instanceof ExcelIEEntity<?>) {
                    result.add((ExcelIEEntity<?>) t);
                }
            }
        } catch (OutOfMemoryError e) {
            logger.error("To much data to be exported!", e);
            showErrorNotification("To much data to be exported!");
        }

        return result;
    }
}

