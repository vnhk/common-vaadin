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
import java.util.*;

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
    protected HorizontalLayout autoFiltersRow;

    public AbstractFiltersLayout(Class<T> tClass, Button applyFiltersButton, DefaultFilterValuesContainer defaultFilterValuesContainer, BervanViewConfig bervanViewConfig) {
        this.tClass = tClass;
        this.applyFiltersButton = applyFiltersButton;
        this.defaultFilterValuesContainer = defaultFilterValuesContainer;
        this.bervanViewConfig = bervanViewConfig;

        filterableTextFields.addAll(getFilterableTextFields());

        allFieldsTextSearch = getFilter();
        stringQuerySearch = getFilter();
        Icon questionIcon = VaadinIcon.QUESTION_CIRCLE.create();
        Button helpButton = new Button(questionIcon);
        helpButton.getElement().setAttribute("title", "Click");
        helpButton.addClickListener(e -> showHelpDialog());

        HorizontalLayout queryWithHelp = new HorizontalLayout(stringQuerySearch, helpButton);
        queryWithHelp.setWidth("100%");
        queryWithHelp.setAlignItems(FlexComponent.Alignment.END);

        searchForm = getSearchForm(applyFiltersButton, queryWithHelp);
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
        helpDialog.setWidth("50%");
        helpDialog.setHeaderTitle("Available fields");
        for (Field declaredField : tClass.getDeclaredFields()) {
            helpDialog.add(new H5(declaredField.getName()));
            helpDialog.add(new Hr());
        }
        Button closeButton = new Button("Close", e -> helpDialog.close());
        helpDialog.getFooter().add(closeButton);
        helpDialog.open();
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

        if (Arrays.stream(tClass.getDeclaredFields()).anyMatch(e -> e.getName().equals("deleted"))) {
            value += " & deleted != true";
        }

        return SearchRequestQueryTranslator.translateQuery(value, tClass);
    }

    private void createCriteriaForCheckboxFilters(SearchRequest request) {
        for (Field field : checkboxFiltersMap.keySet()) {
            ClassViewAutoConfigColumn config = buildColumnConfig(field, bervanViewConfig);

            if (config.isDynamicStrValues() && config.getDynamicStrValuesMap() != null && !config.getDynamicStrValuesMap().isEmpty()) {
                //are all checkboxes selected? if so does not make sense create criteria
                createCriteriaForStrValues(field, config, config.getDynamicStrValuesMap(), request);
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
    }    protected final Button removeFiltersButton = new BervanButton("Reset filters", e -> removeFilters());

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
            if (config.isDynamicStrValues() && config.getDynamicStrValuesMap() != null
                    && !config.getDynamicStrValuesMap().isEmpty()) {
                buildStrValuesFilter(config.getDynamicStrValuesMap(), field, fieldLayout);
            } else if (config.getStrValues() != null && !config.getStrValues().isEmpty()) {
                buildStrValuesFilter(config.getStrValues(), field, fieldLayout);
            } else {
                for (Integer val : config.getIntValues()) {
                    Checkbox checkbox = new Checkbox(val.toString());
                    checkbox.setValue(getOrDefaultCheckboxValue(field, val));
                    checkbox.getStyle()
                            .set("margin-top", "20px")
                            .set("min-width", "fit-content")
                            .set("white-space", "nowrap");

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
            checkbox.getStyle()
                    .set("margin-top", "20px")
                    .set("min-width", "fit-content")
                    .set("white-space", "nowrap");

            checkboxFiltersMap.get(field).put(val, checkbox);
            fieldLayout.add(checkbox);
        }
    }

    private Boolean getOrDefaultCheckboxValue(Field field, Object val) {
        return defaultFilterValuesContainer.checkboxFiltersMapDefaultValues.getOrDefault(field, new HashMap<>()).getOrDefault(val, true);
    }

    private Component createDynamicFiltersLayout(List<Component> fieldLayouts) {
        if (fieldLayouts.isEmpty()) {
            return new Div();
        }

        FlexLayout mainContainer = new FlexLayout();
        mainContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        mainContainer.getStyle()
                .set("gap", "0.5rem")
                .set("align-items", "flex-start");
        mainContainer.setWidthFull();

        for (Component fieldLayout : fieldLayouts) {
            fieldLayout.getStyle().set("flex", "0 0 auto");
            mainContainer.add(fieldLayout);
        }

        return mainContainer;
    }

    protected TextField getFilter() {
        TextField searchField = new TextField();
        searchField.setWidth("100%");
        return searchField;
    }




}
