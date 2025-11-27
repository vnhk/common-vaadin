package com.bervan.common.view;

import com.bervan.asynctask.AsyncTask;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;

import java.util.UUID;

public class AbstractAsyncTaskList extends AbstractBervanTableView<UUID, AsyncTask> {
    public static final String ROUTE_NAME = "/async/async-task-list";

    public AbstractAsyncTaskList(BaseService<UUID, AsyncTask> service, BervanViewConfig bervanViewConfig) {
        super(new AsyncTaskLayout(ROUTE_NAME, false), service, bervanViewConfig, AsyncTask.class);
        renderCommonComponents();
        addButton.setVisible(false);
    }
}
