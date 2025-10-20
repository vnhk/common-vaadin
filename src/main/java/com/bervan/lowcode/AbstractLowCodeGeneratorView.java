package com.bervan.lowcode;

import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.core.model.BervanLogger;

import java.util.UUID;

public class AbstractLowCodeGeneratorView extends AbstractBervanTableView<UUID, LowCodeClass> {
    public static final String ROUTE_NAME = "/lowcode-generator";

    public AbstractLowCodeGeneratorView(BaseService<UUID, LowCodeClass> service, LowCodeClassDetailsService lowCodeClassDetailsService, BervanViewConfig bervanViewConfig, BervanLogger bervanLogger) {
        super(null, service, bervanLogger, bervanViewConfig, LowCodeClass.class);
        this.componentHelper = new LowCodeComponentHelper();
        AbstractBervanTableView.addColumnForGridBuilder(LowCodeClassDetailsColumnBuilder.getInstance());
        CommonComponentUtils.addComponentBuilder(LowCodeClassDetailsFieldBuilder.getInstance(bervanViewConfig, lowCodeClassDetailsService, bervanLogger));
        renderCommonComponents();

        tableToolbarActions = new LowCodeGeneratorToolbar(gridActionService, checkboxes, data, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange, bervanViewConfig)
                .withRunGenerator()
                .withDeleteButton()
                .withEditButton(service, bervanLogger);
    }

    @Override
    protected LowCodeClass preSaveActions(LowCodeClass toBeSaved) {
        for (LowCodeClassDetails lowCodeClassDetail : toBeSaved.getLowCodeClassDetails()) {
            lowCodeClassDetail.setLowCodeClass(toBeSaved);
        }
        return toBeSaved;
    }
}
