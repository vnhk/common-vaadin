package com.bervan.common;

import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinBervanColumn;
import com.bervan.common.model.VaadinBervanColumnConfig;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchRequestQueryTranslator;
import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import static com.bervan.common.TableClassUtils.buildColumnConfig;

public class AbstractFiltersLayout<ID extends Serializable, T extends PersistableTableData<ID>> extends Div {
    protected final VerticalLayout filtersMenuLayout = new VerticalLayout();
    protected final Button filtersButton = new BervanButton(new Icon(VaadinIcon.FILTER), e -> toggleFiltersMenu());
    protected final Button applyFiltersButton;
    protected final Map<Field, Map<Object, Checkbox>> checkboxFiltersMap = new HashMap<>();
    protected final Button reverseFiltersButton = new BervanButton(new Icon(VaadinIcon.RECYCLE), e -> reverseFilters());
    protected final Map<Field, BervanTextField> textFieldFiltersMap = new HashMap<>();
    protected final Map<Field, Map<String, BervanIntegerField>> integerFieldHashMap = new HashMap<>();
    protected final Map<Field, Map<String, BervanDoubleField>> doubleFieldHashMap = new HashMap<>();
    protected final Map<Field, Map<String, BervanBigDecimalField>> bigDecimalHasMap = new HashMap<>();
    protected final Map<Field, Map<String, BervanDateTimePicker>> dateTimeFiltersMap = new HashMap<>();
    protected final Class<T> tClass;
    protected final TextField allFieldsTextSearch;
    protected final TextField stringQuerySearch;
    protected final Set<String> filterableFields = new HashSet<>();
    public AbstractFiltersLayout(Class<T> tClass, Button applyFiltersButton) {
        this.tClass = tClass;
        filtersMenuLayout.setVisible(false);
        this.applyFiltersButton = applyFiltersButton;

        List<Field> vaadinTableColumns = getVaadinTableColumns();
        filterableFields.addAll(getFilterableFields(vaadinTableColumns)); //later configure in each class example @VaadinColumn filterable=true

        allFieldsTextSearch = getFilter("Search in all field: ");

        stringQuerySearch = getFilter("Custom query (overrides other filters)");
        Icon questionIcon = VaadinIcon.QUESTION_CIRCLE.create();
        Button helpButton = new Button(questionIcon);
        helpButton.getElement().setAttribute("title", "Click");
        helpButton.addClickListener(e -> showHelpDialog());

        HorizontalLayout queryWithHelp = new HorizontalLayout(stringQuerySearch, helpButton);
        queryWithHelp.setWidth("100%");
        queryWithHelp.setAlignItems(FlexComponent.Alignment.END);

        buildFiltersMenu();
        filtersMenuLayout.add(allFieldsTextSearch);
        filtersMenuLayout.add(new Hr(), queryWithHelp);
        filtersMenuLayout.add(new HorizontalLayout(applyFiltersButton, reverseFiltersButton));
        filtersMenuLayout.add(removeFiltersButton);

        removeFiltersButton.setVisible(false);

        add(filtersButton, filtersMenuLayout);
    }    protected final Button removeFiltersButton = new BervanButton("Reset filters", e -> removeFilters());

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

    public Map<Field, BervanTextField> getTextFieldFiltersMap() {
        return textFieldFiltersMap;
    }

    public SearchRequest buildCombinedFilters() {
        removeFiltersButton.setVisible(true);

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
        for (String filterableField : filterableFields) {
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
            VaadinBervanColumnConfig config = buildColumnConfig(field);

            if (config.getStrValues().size() > 0) {
                //are all checkbox selected? if so does not make sense create criteria
                if (checkboxFiltersMap.get(field).entrySet().stream().filter(e -> e.getValue().getValue()).count()
                        == config.getStrValues().size()) {
                    continue;
                }

                for (String key : config.getStrValues()) {
                    createCriteriaForCheckbox(request, field, checkboxFiltersMap.get(field).get(key), key);
                }
            } else if (config.getIntValues().size() > 0) {
                //are all checkbox selected? if so does not make sense create criteria
                if (checkboxFiltersMap.get(field).entrySet().stream().filter(e -> e.getValue().getValue()).count()
                        == config.getIntValues().size()) {
                    continue;
                }

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

    protected List<String> getFilterableFields(List<Field> vaadinTableColumns) {
        return new ArrayList<>(vaadinTableColumns.stream().filter(e -> e.getType().equals(String.class))
                .map(Field::getName)
                .toList());
    }

    protected void removeFilters() {
        checkboxFiltersMap.values().forEach(e -> e.values().forEach(c -> c.setValue(true)));
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
        filtersMenuLayout.setVisible(!filtersMenuLayout.isVisible());
    }

    protected List<Field> getVaadinTableColumns() {
        return Arrays.stream(tClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(VaadinBervanColumn.class))
                .toList();
    }

    protected void buildFiltersMenu() {
        List<Field> fields = getVaadinTableColumns();
        for (Field field : fields) {
            VaadinBervanColumnConfig config = buildColumnConfig(field);
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

        textFieldFiltersMap.putAll(TableClassUtils.buildTextFieldFiltersMenu(Collections.singletonList(tClass),
                filtersMenuLayout));

        integerFieldHashMap.putAll(TableClassUtils.buildIntegerFieldFiltersMenu(Collections.singletonList(tClass),
                filtersMenuLayout));

        doubleFieldHashMap.putAll(TableClassUtils.buildDoubleFieldFiltersMenu(Collections.singletonList(tClass),
                filtersMenuLayout));

        bigDecimalHasMap.putAll(TableClassUtils.buildBigDecimalFieldFiltersMenu(Collections.singletonList(tClass),
                filtersMenuLayout));

        if (fields.isEmpty()) {
            filtersButton.setVisible(false);
        }
    }

    protected TextField getFilter(String label) {
        TextField searchField = new TextField(label);
        searchField.setWidth("100%");
        return searchField;
    }




}
