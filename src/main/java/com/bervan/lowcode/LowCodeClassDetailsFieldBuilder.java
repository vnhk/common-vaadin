package com.bervan.lowcode;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.builders.ComponentForFieldBuilder;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.core.model.BervanLogger;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

@Slf4j
public class LowCodeClassDetailsFieldBuilder implements ComponentForFieldBuilder {
    private static LowCodeClassDetailsFieldBuilder INSTANCE;
    private final BervanViewConfig bervanViewConfig;
    private final BervanLogger bervanLogger;
    private final LowCodeClassDetailsService lowCodeDetailsService;

    private LowCodeClassDetailsFieldBuilder(BervanViewConfig bervanViewConfig, BervanLogger bervanLogger, LowCodeClassDetailsService lowCodeDetailsService) {
        this.bervanViewConfig = bervanViewConfig;
        this.bervanLogger = bervanLogger;
        this.lowCodeDetailsService = lowCodeDetailsService;
    }

    public synchronized static LowCodeClassDetailsFieldBuilder getInstance(BervanViewConfig bervanViewConfig,
                                                                           LowCodeClassDetailsService lowCodeDetailsService,
                                                                           BervanLogger bervanLogger) {
        if (INSTANCE == null) {
            INSTANCE = new LowCodeClassDetailsFieldBuilder(bervanViewConfig, bervanLogger,
                    lowCodeDetailsService);
        }
        return INSTANCE;
    }


    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        LowCodeClassDetailsAutoConfigurableField lowCodeClassDetailsAutoConfigurableField = new LowCodeClassDetailsAutoConfigurableField(lowCodeDetailsService, bervanLogger, bervanViewConfig, LowCodeClassDetails.class);
        lowCodeClassDetailsAutoConfigurableField.setValue(item == null ? null : ((LowCodeClass) item).getLowCodeClassDetails());
        return lowCodeClassDetailsAutoConfigurableField;
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return VaadinLowCodeClassDetailsColumn.class.getSimpleName().equals(config.getExtension());
    }

    //If works use this logic for autogeneration with default extension that will build it automatically, but there will be a problem with service
    //because it is not generic, so at least build abstract core class with logic and then override it for service and code generation
    //will generate it automatically with throw RuntimeException and comment where to override and use it.
    //or just use cascade and service is not needed at all.
    private class LowCodeClassDetailsAutoConfigurableField extends AbstractBervanTableView<UUID, LowCodeClassDetails> implements AutoConfigurableField<List<LowCodeClassDetails>> {

        public LowCodeClassDetailsAutoConfigurableField(BaseService<UUID, LowCodeClassDetails> service, BervanLogger bervanLogger, BervanViewConfig bervanViewConfig, Class<LowCodeClassDetails> lowCodeClassDetailsClass) {
            super(null, service, bervanLogger, bervanViewConfig, lowCodeClassDetailsClass);
            pageSize = 10000;
            filtersLayout.setVisible(false);
            renderCommonComponents();
            paginationBar.setVisible(false);
        }

        @Override
        public void setWidthFull() {
            super.setWidthFull();
        }

        @Override
        protected LowCodeClassDetails save(LowCodeClassDetails newObject) {
            newObject.setId(UUID.randomUUID());
            this.data.add(newObject); //delayed save it might be different for new item and for editing (if parent entity already exists)
            this.grid.getDataProvider().refreshAll();
            return newObject;
        }

        @Override
        protected void refreshData() {

        }

        @Override
        public List<LowCodeClassDetails> getValue() {
            return data;
        }

        @Override
        public void setValue(List<LowCodeClassDetails> obj) {
            if (obj == null) {
                return;
            }
            this.data.clear();
            this.data.addAll(obj);
        }

        @Override
        public void validate() {

        }

        @Override
        public boolean isInvalid() {
            return false;
        }

        @Override
        public void setReadOnly(boolean readOnly) {

        }
    }

}
