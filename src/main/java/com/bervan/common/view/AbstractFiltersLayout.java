package com.bervan.common.view;

import com.bervan.common.TableClassUtils;
import com.bervan.common.component.*;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchRequestQueryTranslator;
import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

import static com.bervan.common.TableClassUtils.buildColumnConfig;

public class AbstractFiltersLayout<ID extends Serializable, T extends PersistableTableData<ID>> extends AbstractPageView {
    protected final Button applyFiltersButton;
    @Getter
    protected final Map<Field, Map<Object, Checkbox>> checkboxFiltersMap = new HashMap<>();
    protected final DefaultFilterValuesContainer defaultFilterValuesContainer;
    protected final BervanViewConfig bervanViewConfig;
    protected final Button reverseFiltersButton = new BervanButton(new Icon(VaadinIcon.RECYCLE), e -> reverseFilters());
    @Getter
    protected final Map<Field, BervanTextField> textFieldFiltersMap = new HashMap<>();
    protected final Map<Field, Map<String, BervanIntegerField>> integerFieldHashMap = new HashMap<>();
    protected final Map<Field, Map<String, BervanDoubleField>> doubleFieldHashMap = new HashMap<>();
    protected final Map<Field, Map<String, BervanBigDecimalField>> bigDecimalHasMap = new HashMap<>();
    @Getter
    protected final Map<Field, Map<String, BervanDateTimePicker>> dateTimeFiltersMap = new HashMap<>();
    protected final Class<T> tClass;
    protected final TextField allFieldsTextSearch;
    protected final TextField stringQuerySearch;
    protected final Set<String> filterableTextFields = new HashSet<>();
    protected final Div searchForm;
    protected final Button filtersButton = new BervanButton(new Icon(VaadinIcon.FILTER), e -> toggleFiltersMenu());
    {
        filtersButton.addClassName("bervan-icon-btn");
        filtersButton.addClassName("primary");
        filtersButton.getElement().setAttribute("title", "Toggle filters");
    }
    protected HorizontalLayout autoFiltersRow;

    // Quick filter support for column headers
    @Getter
    protected final Map<String, QuickFilter> quickFiltersMap = new HashMap<>();
    protected Consumer<SearchRequest> onQuickFilterChange;

    /**
     * Quick filter configuration for column headers.
     */
    public static class QuickFilter {
        private final String columnKey;
        private final String filterType;
        private String textValue;
        private Object minValue;
        private Object maxValue;

        public QuickFilter(String columnKey, String filterType) {
            this.columnKey = columnKey;
            this.filterType = filterType;
        }

        public String getColumnKey() {
            return columnKey;
        }

        public String getFilterType() {
            return filterType;
        }

        public String getTextValue() {
            return textValue;
        }

        public void setTextValue(String textValue) {
            this.textValue = textValue;
        }

        public Object getMinValue() {
            return minValue;
        }

        public void setMinValue(Object minValue) {
            this.minValue = minValue;
        }

        public Object getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(Object maxValue) {
            this.maxValue = maxValue;
        }

        public boolean hasValue() {
            return (textValue != null && !textValue.isBlank()) ||
                   minValue != null || maxValue != null;
        }

        public void clear() {
            textValue = null;
            minValue = null;
            maxValue = null;
        }
    }

    public AbstractFiltersLayout(Class<T> tClass, Button applyFiltersButton, DefaultFilterValuesContainer defaultFilterValuesContainer, BervanViewConfig bervanViewConfig) {
        this.tClass = tClass;
        this.applyFiltersButton = applyFiltersButton;
        this.defaultFilterValuesContainer = defaultFilterValuesContainer;
        this.bervanViewConfig = bervanViewConfig;

        filterableTextFields.addAll(getFilterableTextFields());

        allFieldsTextSearch = getFilter();
        allFieldsTextSearch.setPlaceholder("Search all fields...");
        allFieldsTextSearch.addClassName("bervan-filter-input");

        stringQuerySearch = getFilter();
        stringQuerySearch.setPlaceholder("e.g. name ~ 'test' & status = 'active'");
        stringQuerySearch.addClassName("bervan-query-input");

        Icon questionIcon = VaadinIcon.QUESTION_CIRCLE.create();
        Button helpButton = new Button(questionIcon);
        helpButton.addClassName("bervan-icon-btn");
        helpButton.addClassName("info");
        helpButton.getElement().setAttribute("title", "Show query syntax help");
        helpButton.addClickListener(e -> showHelpDialog());

        HorizontalLayout queryWithHelp = new HorizontalLayout(stringQuerySearch, helpButton);
        queryWithHelp.setWidth("100%");
        queryWithHelp.setAlignItems(FlexComponent.Alignment.END);

        searchForm = getSearchForm(applyFiltersButton, queryWithHelp);
        allFieldsTextSearch.addKeyPressListener(keyPressEvent -> {
            if (keyPressEvent.getKey().equals(Key.ENTER)) {
                applyFiltersButton.click();
            }
        });
        add(filtersButton, searchForm);
        removeFiltersButton.setVisible(false);
    }

    private Div getSearchForm(Button applyFiltersButton, HorizontalLayout queryWithHelp) {
        buildFiltersMenu();

        Div allFieldsFilter = createSearchSection("All fields filter", allFieldsTextSearch);
        Div queryWithHelpFilter = createSearchSection("Custom query (overrides other filters)", queryWithHelp);
        HorizontalLayout searchSectionRow = createSearchSectionRow(allFieldsFilter, queryWithHelpFilter);

        HorizontalLayout searchActionButtonsLayout = getSearchActionButtonsLayout(applyFiltersButton, reverseFiltersButton, removeFiltersButton);
        final Div searchForm = getSearchForm(null, searchActionButtonsLayout, autoFiltersRow, searchSectionRow);
        searchForm.setVisible(false);

        return searchForm;
    }

    private void showHelpDialog() {
        Dialog helpDialog = new Dialog();
        helpDialog.setWidth("70%");
        helpDialog.setMaxWidth("800px");
        helpDialog.setHeaderTitle("Query Syntax Help");
        helpDialog.addClassName("bervan-help-dialog");

        // Create content layout
        Div content = new Div();
        content.getStyle().set("max-height", "60vh");
        content.getStyle().set("overflow-y", "auto");
        content.getStyle().set("padding", "var(--bervan-spacing-md)");

        // Operators section
        Div operatorsSection = new Div();
        operatorsSection.add(new H5("Comparison Operators"));
        operatorsSection.add(new Hr());

        FlexLayout operatorsGrid = new FlexLayout();
        operatorsGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        operatorsGrid.getStyle().set("gap", "var(--bervan-spacing-sm)");
        operatorsGrid.getStyle().set("margin-bottom", "var(--bervan-spacing-md)");

        for (SearchRequestQueryTranslator.OperatorInfo op : SearchRequestQueryTranslator.getSupportedOperators()) {
            Div opCard = createOperatorCard(op.symbol, op.description, op.example);
            operatorsGrid.add(opCard);
        }
        operatorsSection.add(operatorsGrid);
        content.add(operatorsSection);

        // Logical operators section
        Div logicalSection = new Div();
        logicalSection.add(new H5("Logical Operators"));
        logicalSection.add(new Hr());

        FlexLayout logicalGrid = new FlexLayout();
        logicalGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        logicalGrid.getStyle().set("gap", "var(--bervan-spacing-sm)");
        logicalGrid.getStyle().set("margin-bottom", "var(--bervan-spacing-md)");

        for (SearchRequestQueryTranslator.OperatorInfo op : SearchRequestQueryTranslator.getLogicalOperators()) {
            Div opCard = createOperatorCard(op.symbol, op.description, op.example);
            logicalGrid.add(opCard);
        }
        logicalSection.add(logicalGrid);
        content.add(logicalSection);

        // Available fields section
        Div fieldsSection = new Div();
        fieldsSection.add(new H5("Available Fields"));
        fieldsSection.add(new Hr());

        FlexLayout fieldsGrid = new FlexLayout();
        fieldsGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        fieldsGrid.getStyle().set("gap", "var(--bervan-spacing-xs)");
        fieldsGrid.getStyle().set("margin-bottom", "var(--bervan-spacing-md)");

        List<String> availableFields = SearchRequestQueryTranslator.getAvailableFields(tClass);
        for (String fieldName : availableFields) {
            Div fieldChip = new Div();
            fieldChip.setText(fieldName);
            fieldChip.addClassName("bervan-field-chip");
            fieldChip.getStyle()
                    .set("background", "var(--bervan-surface-2)")
                    .set("padding", "var(--bervan-spacing-xs) var(--bervan-spacing-sm)")
                    .set("border-radius", "var(--bervan-border-radius-sm)")
                    .set("font-family", "var(--bervan-font-mono)")
                    .set("font-size", "var(--bervan-font-size-sm)")
                    .set("color", "var(--bervan-accent)");
            fieldsGrid.add(fieldChip);
        }
        fieldsSection.add(fieldsGrid);
        content.add(fieldsSection);

        // Examples section
        Div examplesSection = new Div();
        examplesSection.add(new H5("Example Queries"));
        examplesSection.add(new Hr());

        Div examplesList = new Div();
        examplesList.getStyle().set("font-family", "var(--bervan-font-mono)");
        examplesList.getStyle().set("font-size", "var(--bervan-font-size-sm)");
        examplesList.getStyle().set("background", "var(--bervan-surface-2)");
        examplesList.getStyle().set("padding", "var(--bervan-spacing-md)");
        examplesList.getStyle().set("border-radius", "var(--bervan-border-radius-md)");

        String[] examples = {
                "name = 'John'",
                "name ~ 'test'",
                "price >= 100 & price <= 500",
                "status != 'inactive'",
                "(name ~ 'a' | name ~ 'b') & active = true",
                "deletedDate IS NULL",
                "status IN ('active', 'pending')"
        };
        for (String example : examples) {
            Div exampleLine = new Div();
            exampleLine.setText(example);
            exampleLine.getStyle().set("margin-bottom", "var(--bervan-spacing-xs)");
            exampleLine.getStyle().set("color", "var(--bervan-text-primary)");
            examplesList.add(exampleLine);
        }
        examplesSection.add(examplesList);
        content.add(examplesSection);

        helpDialog.add(content);

        Button closeButton = new BervanButton("Close", e -> helpDialog.close());
        closeButton.addClassName("bervan-icon-btn");
        closeButton.addClassName("primary");
        helpDialog.getFooter().add(closeButton);
        helpDialog.open();
    }

    private Div createOperatorCard(String symbol, String description, String example) {
        Div card = new Div();
        card.getStyle()
                .set("background", "var(--bervan-surface-1)")
                .set("padding", "var(--bervan-spacing-sm)")
                .set("border-radius", "var(--bervan-border-radius-md)")
                .set("border", "1px solid var(--bervan-border-color)")
                .set("min-width", "180px")
                .set("flex", "1 1 auto");

        Div symbolDiv = new Div();
        symbolDiv.setText(symbol);
        symbolDiv.getStyle()
                .set("font-family", "var(--bervan-font-mono)")
                .set("font-weight", "bold")
                .set("color", "var(--bervan-primary)")
                .set("font-size", "var(--bervan-font-size-md)");

        Div descDiv = new Div();
        descDiv.setText(description);
        descDiv.getStyle()
                .set("color", "var(--bervan-text-secondary)")
                .set("font-size", "var(--bervan-font-size-sm)");

        Div exampleDiv = new Div();
        exampleDiv.setText(example);
        exampleDiv.getStyle()
                .set("font-family", "var(--bervan-font-mono)")
                .set("font-size", "var(--bervan-font-size-xs)")
                .set("color", "var(--bervan-text-tertiary)")
                .set("margin-top", "var(--bervan-spacing-xs)");

        card.add(symbolDiv, descDiv, exampleDiv);
        return card;
    }

    public void addFilterableFields(String fieldName) {
        filterableTextFields.add(fieldName);
    }

    public void removeFilterableFields(String fieldName) {
        filterableTextFields.remove(fieldName);
    }

    protected void reverseFilters() {
        for (Map<Object, Checkbox> value : checkboxFiltersMap.values()) {
            value.values().forEach(e -> e.setValue(!e.getValue()));
        }
    }

    public SearchRequest buildCombinedFilters() {
        SearchRequest request = stringQuerySearch();
        if (request != null) {
            return request;
        }

        request = new SearchRequest();
        createCriteriaForCheckboxFilters(request);
        createCriteriaForDateTimeFilters(request);
        createCriteriaForTextInputs(request);
        createCriteriaForIntegerFilters(request);
        createCriteriaForDoubleFilters(request);
        createCriteriaForBigDecimalFilters(request);

        return request;
    }

    private void createCriteriaForTextInputs(SearchRequest request) {
        for (String filterableField : filterableTextFields) {
            String value = allFieldsTextSearch.getValue();
            if (!value.isBlank()) {
                request.addCriterion("TEXT_FILTER_GROUP", Operator.OR_OPERATOR, tClass, filterableField, SearchOperation.LIKE_OPERATION, "%" + value + "%");
            }
        }

        createCriteriaTextFilters(request);
    }

    private SearchRequest stringQuerySearch() {
        String value = stringQuerySearch.getValue();
        if (value == null || value.isBlank()) {
            return null;
        }

        // Don't add deleted != true here - BaseService.buildLoadSearchRequestData
        // already handles this properly with (deleted = false OR deleted IS NULL)
        // Adding deleted != true here would incorrectly exclude NULL deleted rows
        // because in SQL: NULL != true returns NULL (falsy), not TRUE

        return SearchRequestQueryTranslator.translateQuery(value, tClass);
    }

    private void createCriteriaForCheckboxFilters(SearchRequest request) {
        for (Field field : checkboxFiltersMap.keySet()) {
            ClassViewAutoConfigColumn config = buildColumnConfig(field, bervanViewConfig);

            if (config.isDynamicStrValues() && config.getDynamicStrValuesList() != null && !config.getDynamicStrValuesList().isEmpty()) {
                //are all checkboxes selected? if so does not make sense create criteria
                createCriteriaForStrValues(field, config, config.getDynamicStrValuesList(), request);
            } else if (config.getStrValues() != null && !config.getStrValues().isEmpty()) {
                //are all checkboxes selected? if so does not make sense create criteria
                createCriteriaForStrValues(field, config, config.getStrValues(), request);
            } else if (config.getIntValues() != null && !config.getIntValues().isEmpty()) {
                long selectedCount = checkboxFiltersMap.get(field).entrySet().stream()
                        .filter(e -> e.getValue().getValue()).count();

                if (selectedCount == config.getIntValues().size()) {
                    continue;
                }

                for (Integer key : config.getIntValues()) {
                    createCriteriaForCheckbox(request, field, checkboxFiltersMap.get(field).get(key), key);
                }
            }
        }
    }

    private void createCriteriaForStrValues(Field field, ClassViewAutoConfigColumn config, List<String> values, SearchRequest request) {
        long selectedCount = checkboxFiltersMap.get(field).entrySet().stream()
                .filter(e -> e.getValue().getValue()).count();


        if (selectedCount == values.size()) {
            return;
        }

        for (String key : values) {
            createCriteriaForCheckbox(request, field, checkboxFiltersMap.get(field).get(key), key);
        }
    }

    private void createCriteriaForDateTimeFilters(SearchRequest request) {
        for (Field field : dateTimeFiltersMap.keySet()) {
            BervanDateTimePicker from = dateTimeFiltersMap.get(field).get("FROM");
            BervanDateTimePicker to = dateTimeFiltersMap.get(field).get("TO");
            if (from.getValue() != null) {
                createCriteriaForDateGreaterEqual(field.getName().toUpperCase() + "_DATE_CRITERIA_GROUP", request, field, from);
            }

            if (to.getValue() != null) {
                createCriteriaForDateLessEqual(field.getName().toUpperCase() + "_DATE_CRITERIA_GROUP", request, field, to);
            }
        }
    }

    private void createCriteriaTextFilters(SearchRequest request) {
        for (Field field : textFieldFiltersMap.keySet()) {
            BervanTextField textField = textFieldFiltersMap.get(field);
            if (textField.getValue() != null && !textField.getValue().isBlank()) {
                createCriteriaForTextContaining(field.getName().toUpperCase() + "_TEXT_FIELD_GROUP", request, field, textField);
            }
        }
    }

    protected void createCriteriaForTextContaining(String groupId, SearchRequest request, Field field, BervanTextField textField) {
        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass, field.getName(), SearchOperation.LIKE_OPERATION, "%" + textField.getValue() + "%");
    }

    private void createCriteriaForIntegerFilters(SearchRequest request) {
        for (Field field : integerFieldHashMap.keySet()) {
            BervanIntegerField bervanFieldFrom = integerFieldHashMap.get(field).get("FROM");
            BervanIntegerField bervanFieldTo = integerFieldHashMap.get(field).get("TO");
            if (bervanFieldFrom.getValue() != null) {
                createCriteriaIntegerGreaterOrEqual(field.getName().toUpperCase() + "_INTEGER_FIELD_GROUP", request, field, bervanFieldFrom);
            }

            if (bervanFieldTo.getValue() != null) {
                createCriteriaIntegerLessOrEqual(field.getName().toUpperCase() + "_INTEGER_FIELD_GROUP", request, field, bervanFieldTo);
            }
        }
    }

    private void createCriteriaForDoubleFilters(SearchRequest request) {
        for (Field field : doubleFieldHashMap.keySet()) {
            BervanDoubleField bervanFieldFrom = doubleFieldHashMap.get(field).get("FROM");
            BervanDoubleField bervanFieldTo = doubleFieldHashMap.get(field).get("TO");
            if (bervanFieldFrom.getValue() != null) {
                createCriteriaDoubleGreaterOrEqual(field.getName().toUpperCase() + "_DOUBLE_FIELD_GROUP", request, field, bervanFieldFrom);
            }

            if (bervanFieldTo.getValue() != null) {
                createCriteriaDoubleLessOrEqual(field.getName().toUpperCase() + "_DOUBLE_FIELD_GROUP", request, field, bervanFieldTo);
            }
        }
    }

    private void createCriteriaForBigDecimalFilters(SearchRequest request) {
        for (Field field : bigDecimalHasMap.keySet()) {
            BervanBigDecimalField bervanFieldFrom = bigDecimalHasMap.get(field).get("FROM");
            BervanBigDecimalField bervanFieldTo = bigDecimalHasMap.get(field).get("TO");
            if (bervanFieldFrom.getValue() != null) {
                createCriteriaBigDecimalGreaterOrEqual(field.getName().toUpperCase() + "_BIG_DECIMAL_FIELD_GROUP", request, field, bervanFieldFrom);
            }

            if (bervanFieldTo.getValue() != null) {
                createCriteriaBigDecimalLessOrEqual(field.getName().toUpperCase() + "_BIG_DECIMAL_FIELD_GROUP", request, field, bervanFieldTo);
            }
        }
    }

    protected void createCriteriaBigDecimalGreaterOrEqual(String groupId, SearchRequest request, Field field, BervanBigDecimalField bervanField) {
        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass, field.getName(), SearchOperation.GREATER_EQUAL_OPERATION, bervanField.getValue());
    }

    protected void createCriteriaBigDecimalLessOrEqual(String groupId, SearchRequest request, Field field, BervanBigDecimalField bervanField) {
        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass, field.getName(), SearchOperation.LESS_EQUAL_OPERATION, bervanField.getValue());
    }

    protected void createCriteriaDoubleGreaterOrEqual(String groupId, SearchRequest request, Field field, BervanDoubleField bervanField) {
        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass, field.getName(), SearchOperation.GREATER_EQUAL_OPERATION, bervanField.getValue());
    }

    protected void createCriteriaDoubleLessOrEqual(String groupId, SearchRequest request, Field field, BervanDoubleField bervanField) {
        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass, field.getName(), SearchOperation.LESS_EQUAL_OPERATION, bervanField.getValue());
    }

    protected void createCriteriaIntegerGreaterOrEqual(String groupId, SearchRequest request, Field field, BervanIntegerField bervanField) {
        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass, field.getName(), SearchOperation.GREATER_EQUAL_OPERATION, bervanField.getValue());
    }

    protected void createCriteriaIntegerLessOrEqual(String groupId, SearchRequest request, Field field, BervanIntegerField bervanField) {
        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass, field.getName(), SearchOperation.LESS_EQUAL_OPERATION, bervanField.getValue());
    }

    protected void createCriteriaForCheckbox(SearchRequest request, Field field, Checkbox checkbox, Object key) {
        SearchOperation operator;
        if (checkbox.getValue()) {
            operator = SearchOperation.EQUALS_OPERATION;
            request.addCriterion("TABLE_FILTER_CHECKBOXES_FOR_" + field.getName().toUpperCase() + "_GROUP", Operator.OR_OPERATOR, tClass, field.getName(), operator, key);
        }
    }

    protected void createCriteriaForDateGreaterEqual(String groupId, SearchRequest request, Field field, BervanDateTimePicker date) {
        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass, field.getName(), SearchOperation.GREATER_EQUAL_OPERATION, date.getValue());
    }

    protected void createCriteriaForDateLessEqual(String groupId, SearchRequest request, Field field, BervanDateTimePicker date) {
        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass, field.getName(), SearchOperation.LESS_EQUAL_OPERATION, date.getValue());
    }

    protected Set<String> getFilterableTextFields() {
        return bervanViewConfig.getFilterableFieldNames(tClass, String.class);
    }

    public void removeFilters() {
        for (Map.Entry<Field, Map<Object, Checkbox>> fieldMapEntry : checkboxFiltersMap.entrySet()) {
            Map<Object, Boolean> defaultValuesForField = defaultFilterValuesContainer.checkboxFiltersMapDefaultValues.getOrDefault(fieldMapEntry.getKey(), new HashMap<>());
            for (Map.Entry<Object, Checkbox> checkboxEntry : fieldMapEntry.getValue().entrySet()) {
                checkboxEntry.getValue().setValue(defaultValuesForField.getOrDefault(checkboxEntry.getKey(), true));
            }
        }

        dateTimeFiltersMap.values().forEach(e -> e.values().forEach(c -> c.setNullValue()));
        allFieldsTextSearch.setValue("");
        stringQuerySearch.setValue("");
        textFieldFiltersMap.values().forEach(e -> e.setValue(""));
        integerFieldHashMap.values().forEach(e -> e.values().forEach(c -> c.setValue(null)));
        doubleFieldHashMap.values().forEach(e -> e.values().forEach(c -> c.setValue(null)));
        bigDecimalHasMap.values().forEach(e -> e.values().forEach(c -> c.setValue(null)));
        removeFiltersButton.setVisible(false);
    }

    protected void toggleFiltersMenu() {
        searchForm.setVisible(!searchForm.isVisible());
    }

    protected List<Field> getVaadinTableColumns() {
        Set<String> fieldNames = bervanViewConfig.getFieldNames(tClass);
        return Arrays.stream(tClass.getDeclaredFields())
                .filter(e -> fieldNames.contains(e.getName()))
                .toList();
    }

    protected void buildFiltersMenu() {
        List<Component> fieldLayouts = new ArrayList<>();

        List<Field> fields = getVaadinTableColumns();
        for (Field field : fields) {
            ClassViewAutoConfigColumn config = buildColumnConfig(field, bervanViewConfig);
            if (!config.isFilterable()) {
                continue;
            }

            if (config.getStrValues() == null && config.getIntValues() == null && !config.isDynamicStrValues()) {
                continue;
            }

            FlexLayout fieldLayout = new FlexLayout();
            fieldLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
            fieldLayout.getStyle()
                    .set("gap", "0.5rem")
                    .set("align-items", "flex-start");
            fieldLayout.setWidthFull();

            checkboxFiltersMap.putIfAbsent(field, new HashMap<>());
            if (config.isDynamicStrValues() && config.getDynamicStrValuesList() != null
                    && !config.getDynamicStrValuesList().isEmpty()) {
                buildStrValuesFilter(config.getDynamicStrValuesList(), field, fieldLayout);
            } else if (config.getStrValues() != null && !config.getStrValues().isEmpty()) {
                buildStrValuesFilter(config.getStrValues(), field, fieldLayout);
            } else if (config.getIntValues() != null && !config.getIntValues().isEmpty()) {
                for (Integer val : config.getIntValues()) {
                    Checkbox checkbox = new Checkbox(val.toString());
                    checkbox.setValue(getOrDefaultCheckboxValue(field, val));
                    checkbox.addClassName("bervan-filter-checkbox");

                    checkboxFiltersMap.get(field).put(val, checkbox);
                    fieldLayout.add(checkbox);
                }
            }

            fieldLayouts.add(createSearchSection(config.getDisplayName(), fieldLayout));
        }


        dateTimeFiltersMap.putAll(TableClassUtils.buildDateTimeFiltersMenu(Collections.singletonList(tClass),
                fieldLayouts, bervanViewConfig));

        textFieldFiltersMap.putAll(TableClassUtils.buildTextFieldFiltersMenu(Collections.singletonList(tClass),
                fieldLayouts, bervanViewConfig));

        integerFieldHashMap.putAll(TableClassUtils.buildIntegerFieldFiltersMenu(Collections.singletonList(tClass),
                fieldLayouts, bervanViewConfig));

        doubleFieldHashMap.putAll(TableClassUtils.buildDoubleFieldFiltersMenu(Collections.singletonList(tClass),
                fieldLayouts, bervanViewConfig));

        bigDecimalHasMap.putAll(TableClassUtils.buildBigDecimalFieldFiltersMenu(Collections.singletonList(tClass),
                fieldLayouts, bervanViewConfig));

        if (fields.isEmpty()) {
            filtersButton.setVisible(false);
        }

        Div filtersSection = createSearchSection("Filters",
                createDynamicFiltersLayout(fieldLayouts));
        autoFiltersRow = createSearchSectionRow(filtersSection);
    }

    private void buildStrValuesFilter(List<String> config, Field field, FlexLayout fieldLayout) {
        for (String val : config) {
            Checkbox checkbox = new Checkbox(val);
            checkbox.setValue(getOrDefaultCheckboxValue(field, val));
            checkbox.addClassName("bervan-filter-checkbox");

            checkboxFiltersMap.get(field).put(val, checkbox);
            fieldLayout.add(checkbox);
        }
    }    protected final Button removeFiltersButton = new BervanButton("Reset filters", e -> removeFilters());

    private Boolean getOrDefaultCheckboxValue(Field field, Object val) {
        return defaultFilterValuesContainer.checkboxFiltersMapDefaultValues.getOrDefault(field, new HashMap<>()).getOrDefault(val, true);
    }

    private Component createDynamicFiltersLayout(List<Component> fieldLayouts) {
        if (fieldLayouts.isEmpty()) {
            return new Div();
        }

        // Use CSS Grid for uniform sizing - 2 columns on desktop, 1 on mobile
        Div mainContainer = new Div();
        mainContainer.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(2, 1fr)")
                .set("gap", "var(--bervan-spacing-md)")
                .set("width", "100%");

        for (Component fieldLayout : fieldLayouts) {
            // Each filter section takes equal width
            fieldLayout.getStyle()
                    .set("min-width", "0")  // Prevent overflow
                    .set("width", "100%");
            mainContainer.add(fieldLayout);
        }

        return mainContainer;
    }

    protected TextField getFilter() {
        TextField searchField = new TextField();
        searchField.setWidth("100%");
        return searchField;
    }

    // ==================== Quick Filter API ====================

    /**
     * Sets the callback to be invoked when quick filters change.
     */
    public void setOnQuickFilterChange(Consumer<SearchRequest> callback) {
        this.onQuickFilterChange = callback;
    }

    /**
     * Registers a quick filter for a column.
     *
     * @param columnKey The column key
     * @param filterType The filter type: "text", "number", "date", "boolean", "select"
     */
    public QuickFilter registerQuickFilter(String columnKey, String filterType) {
        QuickFilter filter = new QuickFilter(columnKey, filterType);
        quickFiltersMap.put(columnKey, filter);
        return filter;
    }

    /**
     * Updates a quick filter value programmatically.
     */
    public void setQuickFilterValue(String columnKey, String value) {
        QuickFilter filter = quickFiltersMap.get(columnKey);
        if (filter != null) {
            filter.setTextValue(value);
            notifyQuickFilterChange();
        }
    }

    /**
     * Updates a quick filter range value programmatically.
     */
    public void setQuickFilterRange(String columnKey, Object minValue, Object maxValue) {
        QuickFilter filter = quickFiltersMap.get(columnKey);
        if (filter != null) {
            filter.setMinValue(minValue);
            filter.setMaxValue(maxValue);
            notifyQuickFilterChange();
        }
    }

    /**
     * Clears a specific quick filter.
     */
    public void clearQuickFilter(String columnKey) {
        QuickFilter filter = quickFiltersMap.get(columnKey);
        if (filter != null) {
            filter.clear();
            notifyQuickFilterChange();
        }
    }

    /**
     * Clears all quick filters.
     */
    public void clearAllQuickFilters() {
        quickFiltersMap.values().forEach(QuickFilter::clear);
        notifyQuickFilterChange();
    }

    /**
     * Builds combined filters including quick filters.
     */
    public SearchRequest buildCombinedFiltersWithQuickFilters() {
        SearchRequest request = buildCombinedFilters();
        applyQuickFilters(request);
        return request;
    }

    /**
     * Applies quick filters to a search request.
     */
    protected void applyQuickFilters(SearchRequest request) {
        for (QuickFilter filter : quickFiltersMap.values()) {
            if (!filter.hasValue()) {
                continue;
            }

            String columnKey = filter.getColumnKey();
            String groupId = "QUICK_FILTER_" + columnKey.toUpperCase();

            switch (filter.getFilterType()) {
                case "text":
                    if (filter.getTextValue() != null && !filter.getTextValue().isBlank()) {
                        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass,
                                columnKey, SearchOperation.LIKE_OPERATION,
                                "%" + filter.getTextValue() + "%");
                    }
                    break;

                case "number":
                case "date":
                    if (filter.getMinValue() != null) {
                        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass,
                                columnKey, SearchOperation.GREATER_EQUAL_OPERATION,
                                filter.getMinValue());
                    }
                    if (filter.getMaxValue() != null) {
                        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass,
                                columnKey, SearchOperation.LESS_EQUAL_OPERATION,
                                filter.getMaxValue());
                    }
                    break;

                case "boolean":
                    if (filter.getTextValue() != null && !filter.getTextValue().isBlank()) {
                        boolean boolValue = "true".equalsIgnoreCase(filter.getTextValue());
                        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass,
                                columnKey, SearchOperation.EQUALS_OPERATION,
                                boolValue);
                    }
                    break;

                case "select":
                    if (filter.getTextValue() != null && !filter.getTextValue().isBlank()) {
                        request.addCriterion(groupId, Operator.AND_OPERATOR, tClass,
                                columnKey, SearchOperation.EQUALS_OPERATION,
                                filter.getTextValue());
                    }
                    break;
            }
        }
    }

    /**
     * Notifies listeners that quick filters have changed.
     */
    protected void notifyQuickFilterChange() {
        if (onQuickFilterChange != null) {
            SearchRequest request = buildCombinedFiltersWithQuickFilters();
            onQuickFilterChange.accept(request);
        }
    }

    /**
     * Gets the filter type for a field based on its Java type.
     */
    public String getFilterTypeForField(Field field) {
        Class<?> type = field.getType();

        if (type == String.class) {
            return "text";
        } else if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        } else if (type == Integer.class || type == int.class ||
                   type == Long.class || type == long.class ||
                   type == Double.class || type == double.class ||
                   type == Float.class || type == float.class ||
                   Number.class.isAssignableFrom(type)) {
            return "number";
        } else if (type == LocalDateTime.class ||
                   type == java.time.LocalDate.class ||
                   type == java.util.Date.class) {
            return "date";
        } else if (type.isEnum()) {
            return "select";
        }

        return "text";
    }

    /**
     * Checks if any quick filter has a value set.
     */
    public boolean hasActiveQuickFilters() {
        return quickFiltersMap.values().stream().anyMatch(QuickFilter::hasValue);
    }
}
