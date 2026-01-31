package com.bervan.common.view;

import com.bervan.asynctask.AsyncTask;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.SortDirection;

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
    protected void preColumnAutoCreation(Grid<AsyncTask> grid) {
        grid.addComponentColumn(entity -> {
                    Icon linkIcon = new Icon(VaadinIcon.LINK);
                    linkIcon.getStyle().set("cursor", "pointer");
                    return new Anchor(ROUTE_NAME + "/" + entity.getId(), new HorizontalLayout(linkIcon));
                }).setKey("link")
                .setWidth("6px")
                .setResizable(false);
    }
}
