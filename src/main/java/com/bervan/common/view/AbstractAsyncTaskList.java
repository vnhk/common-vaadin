package com.bervan.common.view;

import com.bervan.asynctask.AsyncTask;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import java.util.UUID;

public class AbstractAsyncTaskList extends AbstractBervanTableView<UUID, AsyncTask> {
    public static final String ROUTE_NAME = "/async/async-tasks";

    public AbstractAsyncTaskList(BaseService<UUID, AsyncTask> service, BervanViewConfig bervanViewConfig) {
        super(new AsyncTaskLayout(ROUTE_NAME, false), service, bervanViewConfig, AsyncTask.class);
        sortDirection = SortDirection.DESCENDING;
        sortField = "modificationDate";
        renderCommonComponents();
        newItemButton.setVisible(false);
    }

    @Override
    protected Grid<AsyncTask> getGrid() {
        Grid<AsyncTask> grid = new Grid<>(AsyncTask.class, false);
        buildGridAutomatically(grid);

        if (grid.getColumnByKey("status") != null) {
            grid.getColumnByKey("status").setRenderer(new ComponentRenderer<>(
                    entity -> new Anchor(ROUTE_NAME + "/" + entity.getId(),
                            entity.getStatus() != null ? entity.getStatus() : entity.getId().toString())
            ));
        }

        return grid;
    }
}
