package com.bervan.common;

import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinTableColumn;
import com.bervan.common.model.VaadinTableColumnConfig;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import static com.bervan.common.TableClassUtils.buildColumnConfig;

public class AbstractFiltersLayout<ID extends Serializable, T extends PersistableTableData<ID>> extends Div {
    protected final Button filtersButton = new BervanButton(new Icon(VaadinIcon.FILTER), e -> toggleFiltersMenu());
    protected final VerticalLayout filtersMenuLayout = new VerticalLayout();
    protected final Button applyFiltersButton;
    protected final Button reverseFiltersButton = new BervanButton(new Icon(VaadinIcon.RECYCLE), e -> reverseFilters());
    protected final Button removeFiltersButton = new BervanButton("Reset filters", e -> removeFilters());
    protected final Map<Field, Map<Object, Checkbox>> checkboxFiltersMap = new HashMap<>();
    protected final Map<Field, Map<String, BervanDateTimePicker>> dateTimeFiltersMap = new HashMap<>();
    protected final Class<T> tClass;
    protected final TextField allFieldsTextSearch;
    protected final Set<String> filterableFields = new HashSet<>();

    public AbstractFiltersLayout(Class<T> tClass, Button applyFiltersButton) {
        this.tClass = tClass;
        filtersMenuLayout.setVisible(false);
        this.applyFiltersButton = applyFiltersButton;

        List<Field> vaadinTableColumns = getVaadinTableColumns();
        filterableFields.addAll(getFilterableFields(vaadinTableColumns)); //later configure in each class example @VaadinColumn filterable=true

        allFieldsTextSearch = getFilter();
        buildFiltersMenu();
        filtersMenuLayout.add(allFieldsTextSearch);
        filtersMenuLayout.add(new HorizontalLayout(applyFiltersButton, reverseFiltersButton));
        filtersMenuLayout.add(removeFiltersButton);

        removeFiltersButton.setVisible(false);

        add(filtersButton, filtersMenuLayout);
    }

    public void addFilterableFields(String fieldName) {
        filterableFields.add(fieldName);
    }

    public void removeFilterableFields(String fieldName) {
        filterableFields.remove(fieldName);
    }

    protected void reverseFilters() {
        for (Map<Object, Checkbox> value : checkboxFiltersMap.values()) {
            value.values().forEach(e -> e.setValue(!e.getValue()));
        }
    }

    public Map<Field, Map<Object, Checkbox>> getCheckboxFiltersMap() {
        return checkboxFiltersMap;
    }

    public Map<Field, Map<String, BervanDateTimePicker>> getDateTimeFiltersMap() {
        return dateTimeFiltersMap;
    }

    public SearchRequest buildCombinedFilters() {
        removeFiltersButton.setVisible(true);

        SearchRequest request = new SearchRequest();
        createCriteriaForCheckboxFilters(request);
        createCriteriaForDateTimeFilters(request);
        createCriteriaForTextInputs(request);

        return request;
    }

    private void createCriteriaForTextInputs(SearchRequest request) {
        for (String filterableField : filterableFields) {
            String value = allFieldsTextSearch.getValue();
            if (!value.isBlank()) {
                request.addCriterion("TEXT_FILTER_GROUP", Operator.OR_OPERATOR, tClass, filterableField, SearchOperation.LIKE_OPERATION, "%" + value + "%");
            }
        }
    }

    private void createCriteriaForCheckboxFilters(SearchRequest request) {
        for (Field field : checkboxFiltersMap.keySet()) {
            VaadinTableColumnConfig config = buildColumnConfig(field);
            if (config.getStrValues().size() > 0) {
                for (String key : config.getStrValues()) {
                    createCriteriaForCheckbox(request, field, checkboxFiltersMap.get(field).get(key), key);
                }
            } else if (config.getIntValues().size() > 0) {
                for (Integer key : config.getIntValues()) {
                    createCriteriaForCheckbox(request, field, checkboxFiltersMap.get(field).get(key), key);
                }
            }
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

    protected List<String> getFilterableFields(List<Field> vaadinTableColumns) {
        return new ArrayList<>(vaadinTableColumns.stream().filter(e -> e.getType().equals(String.class))
                .map(Field::getName)
                .toList());
    }

    protected void removeFilters() {
        checkboxFiltersMap.values().forEach(e -> e.values().forEach(c -> c.setValue(true)));
        dateTimeFiltersMap.values().forEach(e -> e.values().forEach(c -> c.setValue(null)));
        allFieldsTextSearch.setValue("");
        removeFiltersButton.setVisible(false);
    }


    protected void toggleFiltersMenu() {
        filtersMenuLayout.setVisible(!filtersMenuLayout.isVisible());
    }

    protected List<Field> getVaadinTableColumns() {
        return Arrays.stream(tClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(VaadinTableColumn.class))
                .toList();
    }

    protected void buildFiltersMenu() {
        List<Field> fields = getVaadinTableColumns();
        for (Field field : fields) {
            VaadinTableColumnConfig config = buildColumnConfig(field);
            if (!config.getStrValues().isEmpty() || !config.getIntValues().isEmpty()) {
                VerticalLayout fieldLayout = new VerticalLayout();
                fieldLayout.setWidthFull();
                checkboxFiltersMap.putIfAbsent(field, new HashMap<>());
                if (!config.getStrValues().isEmpty()) {
                    H4 label = new H4(config.getDisplayName() + ":");
                    fieldLayout.add(label);
                    for (String val : config.getStrValues()) {
                        Checkbox checkbox = new Checkbox(val);
                        checkbox.setValue(true);
                        checkboxFiltersMap.get(field).put(val, checkbox);
                        fieldLayout.add(checkbox);
                    }
                } else {
                    H4 label = new H4(config.getDisplayName() + ":");
                    fieldLayout.add(label);
                    for (Integer val : config.getIntValues()) {
                        Checkbox checkbox = new Checkbox(val.toString());
                        checkbox.setValue(true);
                        checkboxFiltersMap.get(field).put(val, checkbox);
                        fieldLayout.add(checkbox);
                    }
                }
                filtersMenuLayout.add(fieldLayout);
            }
        }

        dateTimeFiltersMap.putAll(TableClassUtils.buildDateTimeFiltersMenu(Collections.singletonList(tClass),
                filtersMenuLayout));

        if (fields.isEmpty()) {
            filtersButton.setVisible(false);
        }
    }

    protected TextField getFilter() {
        TextField searchField = new TextField("Search in all field: ");
        searchField.setWidth("100%");
        return searchField;
    }
}
