package com.bervan.common.view;

import com.bervan.asynctask.AsyncTask;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BervanLogger;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.SortDirection;

import java.util.UUID;

public class AbstractAsyncTaskList extends AbstractBervanTableView<UUID, AsyncTask> {
    public static final String ROUTE_NAME = "/async/async-task-list";

    public AbstractAsyncTaskList(BaseService<UUID, AsyncTask> service, BervanLogger bervanLogger) {
        super(new AsyncTaskLayout(ROUTE_NAME, false), service, bervanLogger, AsyncTask.class);
        renderCommonComponents();
        addButton.setVisible(false);
    }

    @Override
    protected void customizePreLoad(SearchRequest request) {
        sortField = "modificationDate";
        sortDirection = SortDirection.DESCENDING;
        sortDir = com.bervan.common.search.model.SortDirection.DESC;
    }

    @Override
    protected void preColumnAutoCreation(Grid<AsyncTask> grid) {
        grid.addComponentColumn(entity -> {
                    Button linkButton = new Button(new Icon(VaadinIcon.LINK));
                    linkButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
                    linkButton.getStyle().set("cursor", "pointer");

                    linkButton.addClickListener(event -> {
                        String url = AbstractAsyncTaskDetails.ROUTE_NAME + "/" + entity.getId().toString();
                        getUI().ifPresent(ui -> ui.navigate(url));
                    });

                    return new HorizontalLayout(linkButton);

                }).setKey("link")
                .setWidth("10px")
                .setResizable(false);
    }

    @Override
    protected void newItemButtonClick() {
        throw new UnsupportedOperationException("Adding task manually is not supported!");
    }
}
