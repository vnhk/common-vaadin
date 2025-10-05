package com.bervan.common.view;

import com.bervan.asynctask.AsyncTask;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BervanLogger;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.UUID;

public class AbstractAsyncTaskList extends AbstractBervanTableView<UUID, AsyncTask> {
    public static final String ROUTE_NAME = "/async/async-task-list";

    public AbstractAsyncTaskList(BaseService<UUID, AsyncTask> service, BervanLogger bervanLogger) {
        super(new AsyncTaskLayout(ROUTE_NAME), service, bervanLogger, AsyncTask.class);
        renderCommonComponents();
        addButton.setVisible(false);
    }

    @Override
    protected void preColumnAutoCreation(Grid<AsyncTask> grid) {
        grid.addComponentColumn(entity -> {
                    Icon linkIcon = new Icon(VaadinIcon.LINK);
                    linkIcon.getStyle().set("cursor", "pointer");
                    return new Anchor(AbstractAsyncTaskDetails.ROUTE_NAME + entity.getId(), new HorizontalLayout(linkIcon));
                }).setKey("link")
                .setWidth("10px")
                .setResizable(false);
    }

    @Override
    protected void newItemButtonClick() {
        throw new UnsupportedOperationException("Adding task manually is not supported!");
    }
}
