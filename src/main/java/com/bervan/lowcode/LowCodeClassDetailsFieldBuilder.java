package com.bervan.lowcode;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.builders.ComponentForFieldBuilder;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.logging.JsonLogger;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class LowCodeClassDetailsFieldBuilder implements ComponentForFieldBuilder {
    private static LowCodeClassDetailsFieldBuilder INSTANCE;
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");
    private final BervanViewConfig bervanViewConfig;
    private final LowCodeClassDetailsService lowCodeDetailsService;

    private LowCodeClassDetailsFieldBuilder(BervanViewConfig bervanViewConfig, LowCodeClassDetailsService lowCodeDetailsService) {
        this.bervanViewConfig = bervanViewConfig;
        this.lowCodeDetailsService = lowCodeDetailsService;
    }

    public synchronized static LowCodeClassDetailsFieldBuilder getInstance(BervanViewConfig bervanViewConfig,
                                                                           LowCodeClassDetailsService lowCodeDetailsService) {
        if (INSTANCE == null) {
            INSTANCE = new LowCodeClassDetailsFieldBuilder(bervanViewConfig,
                    lowCodeDetailsService);
        }
        return INSTANCE;
    }


    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        LowCodeClassDetailsAutoConfigurableField lowCodeClassDetailsAutoConfigurableField = new LowCodeClassDetailsAutoConfigurableField((LowCodeClass) item, lowCodeDetailsService, bervanViewConfig, LowCodeClassDetails.class);
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
        LowCodeClass parent;

        public LowCodeClassDetailsAutoConfigurableField(LowCodeClass parent, BaseService<UUID, LowCodeClassDetails> service, BervanViewConfig bervanViewConfig, Class<LowCodeClassDetails> lowCodeClassDetailsClass) {
            super(null, service, bervanViewConfig, lowCodeClassDetailsClass);
            this.parent = parent;
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
            if (parent != null) {
                newObject.setLowCodeClass(parent);
                parent.getLowCodeClassDetails().add(newObject);
            }
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
