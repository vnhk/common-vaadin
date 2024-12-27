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

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractDataIEView extends AbstractPageView {
    public static final String ROUTE_NAME = "interview-app/import-export-data";
    protected final List<BaseService<? extends Serializable, ? extends PersistableTableData<?>>> dataServices;
    protected final MenuNavigationComponent pageLayout;
    protected final BervanLogger logger;
    protected final List<Class<?>> classesToExport;
    @Value("${file.service.storage.folder}")
    private String pathToFileStorage;
    @Value("${global-tmp-dir.file-storage-relative-path}")
    private String globalTmpDir;

    public AbstractDataIEView(List<BaseService<? extends Serializable, ? extends PersistableTableData<?>>> dataServices,
                              MenuNavigationComponent pageLayout,
                              BervanLogger logger, List<Class<?>> classesToExport) {
        this.logger = logger;
        this.dataServices = dataServices;
        this.pageLayout = pageLayout;
        this.classesToExport = classesToExport;

        add(pageLayout);

        Button prepareExportButton = new Button("Prepare data for export");
        prepareExportButton.addClassName("option-button");
        add(prepareExportButton);

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

        prepareExportButton.addClickListener(buttonClickEvent -> {
            StreamResource resource = prepareDownloadResource();
            Anchor downloadLink = new Anchor(resource, "Export");
            downloadLink.getElement().setAttribute("download", true);

            add(downloadLink);
            remove(prepareExportButton);
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
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        logger.debug("Class that will be imported: " + classesToExport);
        BaseExcelImport baseExcelImport = new BaseExcelImport(classesToExport, logger);
        List<? extends ExcelIEEntity> objects = (List<? extends ExcelIEEntity>) baseExcelImport.importExcel(baseExcelImport.load(file));
        logger.debug("Extracted " + objects.size() + " entities from excel.");

        for (BaseService<? extends Serializable, ? extends PersistableTableData<?>> dataService : dataServices) {
            dataService.saveIfValid(objects);
        }
    }

    public StreamResource prepareDownloadResource() {
        try {
            BaseExcelExport baseExcelExport = new BaseExcelExport();
            Workbook workbook = baseExcelExport.exportExcel(getDataToExport(), null);
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
        for (BaseService<? extends Serializable, ? extends PersistableTableData<?>> dataService : dataServices) {
            Set<?> loaded = dataService.load();
            for (Object t : loaded) {
                if (t instanceof ExcelIEEntity<?>) {
                    result.add((ExcelIEEntity<?>) t);
                } else {
                    logger.warn("Data to be exported is not ExcelIEEntity!: " + t.getClass().getName());
                    break;
                }
            }
        }

        return result;
    }
}
