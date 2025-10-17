package com.bervan.common.view;

import com.bervan.asynctask.AsyncTask;
import com.bervan.asynctask.HistoryAsyncTask;
import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BervanLogger;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

public class AbstractAsyncTaskDetails extends AbstractBervanEntityView<UUID, AsyncTask> implements HasUrlParameter<String> {
    public static final String ROUTE_NAME = "/async/async-task";
    private final BaseService<UUID, HistoryAsyncTask> historyService;
    private final BervanLogger logger;

    public AbstractAsyncTaskDetails(BaseService<UUID, AsyncTask> service, BaseService<UUID, HistoryAsyncTask> historyService, BervanLogger logger, BervanViewConfig bervanViewConfig) {
        super(new AsyncTaskLayout(ROUTE_NAME, true), service, bervanViewConfig, AsyncTask.class);
        this.historyService = historyService;
        this.logger = logger;
    }

    @Override
    public void setParameter(BeforeEvent event, String s) {
        String taskId = event.getRouteParameters().get("___url_parameter").orElse(UUID.randomUUID().toString());
        this.item = service.loadById(UUID.fromString(taskId)).get();
        renderCommonComponents();
        addButton.setVisible(false);
        editButton.setVisible(false);

        AbstractBervanTableView<UUID, HistoryAsyncTask> historyOwnerCriteria = new AbstractBervanTableView<>(pageLayout, historyService, logger, bervanViewConfig, HistoryAsyncTask.class) {
            @Override
            protected void customizePreLoad(SearchRequest request) {
                pageSize = 10000;
                sortField = "modificationDate";
                sortDirection = SortDirection.DESCENDING;
                sortDir = com.bervan.common.search.model.SortDirection.DESC;
                request.addCriterion("HISTORY_OWNER_CRITERIA", HistoryAsyncTask.class, "asyncTask.id", SearchOperation.EQUALS_OPERATION, taskId);
            }
        };
        historyOwnerCriteria.addButton.setVisible(false);
        add(historyOwnerCriteria);
        historyOwnerCriteria.renderCommonComponents();
    }

    @Override
    protected void customFieldInDetailsLayout(Map<Field, AutoConfigurableField> fieldsHolder, Map<Field, VerticalLayout> fieldsLayoutHolder, VerticalLayout formLayout) {

    }

    @Override
    protected void newItemButtonClick() {
        throw new UnsupportedOperationException("Adding task manually is not supported!");
    }

    @Override
    protected void openEditDialog() {
        throw new UnsupportedOperationException("Editing task manually is not supported!");
    }
}
