package com.bervan.common.view;

import com.bervan.asynctask.AsyncTask;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BervanLogger;

import java.util.UUID;

public class AbstractAsyncTaskList extends AbstractBervanTableView<UUID, AsyncTask> {
    public static final String ROUTE_NAME = "/async/async-task-list";

    public AbstractAsyncTaskList(BaseService<UUID, AsyncTask> service, BervanLogger bervanLogger) {
        super(new AsyncTaskLayout(ROUTE_NAME), service, bervanLogger, AsyncTask.class);
        renderCommonComponents();
        addButton.setVisible(false);
    }

    @Override
    protected void newItemButtonClick() {
        throw new UnsupportedOperationException("Adding task manually is not supported!");
    }
}
