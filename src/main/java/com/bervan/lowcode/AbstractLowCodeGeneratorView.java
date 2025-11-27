package com.bervan.lowcode;

import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.AbstractBervanTableView;

import java.util.List;
import java.util.UUID;

public class AbstractLowCodeGeneratorView extends AbstractBervanTableView<UUID, LowCodeClass> {
    public static final String ROUTE_NAME = "/lowcode-generator";

    public AbstractLowCodeGeneratorView(BaseService<UUID, LowCodeClass> service, LowCodeClassDetailsService lowCodeClassDetailsService, BervanViewConfig bervanViewConfig) {
        super(null, service, bervanViewConfig, LowCodeClass.class);
        this.componentHelper = new LowCodeComponentHelper();
        AbstractBervanTableView.addColumnForGridBuilder(LowCodeClassDetailsColumnBuilder.getInstance());
        CommonComponentUtils.addComponentBuilder(LowCodeClassDetailsFieldBuilder.getInstance(bervanViewConfig, lowCodeClassDetailsService));
        renderCommonComponents();
    }

    @Override
    protected void buildToolbarActionBar() {
        tableToolbarActions = new LowCodeGeneratorToolbar(gridActionService, checkboxes, data, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange, bervanViewConfig)
                .withRunGenerator()
                .withDeleteButton()
                .withEditButton(service)
                .build();
    }

    @Override
    protected List<LowCodeClass> loadData() {
        List<LowCodeClass> lowCodeClasses = super.loadData();
        for (LowCodeClass lowCodeClass : lowCodeClasses) {
            if (lowCodeClass.getLowCodeClassDetails() == null || lowCodeClass.getLowCodeClassDetails().isEmpty()) {
                ((LowCodeClassService) service).loadDetails(lowCodeClass);
            }
        }
        return lowCodeClasses;
    }

    @Override
    protected LowCodeClass preSaveActions(LowCodeClass toBeSaved) {
        for (LowCodeClassDetails lowCodeClassDetail : toBeSaved.getLowCodeClassDetails()) {
            lowCodeClassDetail.setLowCodeClass(toBeSaved);
        }
        return toBeSaved;
    }
}
