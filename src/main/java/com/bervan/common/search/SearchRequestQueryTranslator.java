package com.bervan.common.search;

import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translates JQL-like query strings into SearchRequest objects.
 *
 * Supported operators:
 * - = (equals)
 * - != (not equals)
 * - ~ (contains/like)
 * - !~ (not contains/not like)
 * - > (greater than)
 * - < (less than)
 * - >= (greater than or equal)
 * - <= (less than or equal)
 * - IS NULL
 * - IS NOT NULL
 * - IN (value1, value2, ...)
 * - NOT IN (value1, value2, ...)
 *
 * Logical operators:
 * - & (AND)
 * - | (OR)
 * - Parentheses for grouping
 *
 * Examples:
 * - name = 'John'
 * - name ~ 'test'
 * - price >= 100
 * - status != 'inactive' & createdDate > '2024-01-01'
 * - (name ~ 'test' | name ~ 'demo') & active = true
 */
public class SearchRequestQueryTranslator {

    // Pattern to match operators in order of precedence (longer operators first)
    private static final List<OperatorMapping> OPERATOR_MAPPINGS = List.of(
            new OperatorMapping("!~", SearchOperation.NOT_LIKE_OPERATION),
            new OperatorMapping("!=", SearchOperation.NOT_EQUALS_OPERATION),
            new OperatorMapping(">=", SearchOperation.GREATER_EQUAL_OPERATION),
            new OperatorMapping("<=", SearchOperation.LESS_EQUAL_OPERATION),
            new OperatorMapping("~", SearchOperation.LIKE_OPERATION),
            new OperatorMapping("=", SearchOperation.EQUALS_OPERATION),
            new OperatorMapping(">", SearchOperation.GREATER_OPERATION),
            new OperatorMapping("<", SearchOperation.LESS_OPERATION)
    );

    // Pattern for IN and NOT IN operators
    private static final Pattern IN_PATTERN = Pattern.compile(
            "^\\s*([\\w.\\[\\]]+)\\s+(NOT\\s+)?IN\\s*\\(([^)]+)\\)\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern for IS NULL and IS NOT NULL
    private static final Pattern IS_NULL_PATTERN = Pattern.compile(
            "^\\s*([\\w.\\[\\]]+)\\s+IS\\s+(NOT\\s+)?NULL\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Translates a query string into a SearchRequest.
     *
     * @param query The JQL-like query string
     * @param entityToFind The entity class to query
     * @return A SearchRequest object
     * @throws QuerySyntaxException if the query syntax is invalid
     */
    public static SearchRequest translateQuery(String query, Class<?> entityToFind) {
        if (query == null || query.trim().isEmpty()) {
            throw new QuerySyntaxException("Query cannot be empty");
        }

        SearchRequest searchRequest = new SearchRequest();
        AtomicInteger criterionCounter = new AtomicInteger(1);
        AtomicInteger groupCounter = new AtomicInteger(1);

        try {
            Expression parsedExpression = parseExpression(query.trim());
            String rootGroupId = buildSearchRequestFromExpression(parsedExpression, entityToFind, searchRequest, criterionCounter, groupCounter);

            if (!SearchRequest.FINAL_GROUP_CONSTANT.equals(rootGroupId)) {
                searchRequest.renameMergeGroup(rootGroupId, SearchRequest.FINAL_GROUP_CONSTANT);
            }
        } catch (QuerySyntaxException e) {
            throw e;
        } catch (Exception e) {
            throw new QuerySyntaxException("Failed to parse query: " + e.getMessage() + ". Query: " + query, e);
        }

        return searchRequest;
    }

    /**
     * Validates a query string without executing it.
     *
     * @param query The query string to validate
     * @param entityClass The entity class to validate against
     * @return ValidationResult with success status and any error messages
     */
    public static ValidationResult validateQuery(String query, Class<?> entityClass) {
        if (query == null || query.trim().isEmpty()) {
            return new ValidationResult(true, null); // Empty query is valid (no filter)
        }

        try {
            Expression expr = parseExpression(query.trim());
            List<String> warnings = validateExpression(expr, entityClass);
            return new ValidationResult(true, warnings.isEmpty() ? null : String.join("; ", warnings));
        } catch (QuerySyntaxException e) {
            return new ValidationResult(false, e.getMessage());
        } catch (Exception e) {
            return new ValidationResult(false, "Parse error: " + e.getMessage());
        }
    }

    /**
     * Returns a list of available field names for an entity class.
     */
    public static List<String> getAvailableFields(Class<?> entityClass) {
        List<String> fields = new ArrayList<>();
        collectFields(entityClass, "", fields, new HashSet<>());
        return fields;
    }

    private static void collectFields(Class<?> clazz, String prefix, List<String> fields, Set<Class<?>> visited) {
        if (clazz == null || visited.contains(clazz) || clazz == Object.class) {
            return;
        }
        visited.add(clazz);

        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();

            // Skip synthetic and static fields
            if (field.isSynthetic() || java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            fields.add(fieldName);
        }

        // Also check superclass
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            collectFields(clazz.getSuperclass(), prefix, fields, visited);
        }
    }

    /**
     * Returns supported operators with their descriptions.
     */
    public static List<OperatorInfo> getSupportedOperators() {
        return List.of(
                new OperatorInfo("=", "Equals", "name = 'John'"),
                new OperatorInfo("!=", "Not equals", "status != 'inactive'"),
                new OperatorInfo("~", "Contains (LIKE)", "name ~ 'test'"),
                new OperatorInfo("!~", "Not contains (NOT LIKE)", "name !~ 'spam'"),
                new OperatorInfo(">", "Greater than", "price > 100"),
                new OperatorInfo("<", "Less than", "price < 50"),
                new OperatorInfo(">=", "Greater than or equal", "price >= 100"),
                new OperatorInfo("<=", "Less than or equal", "price <= 200"),
                new OperatorInfo("IS NULL", "Is null", "deletedDate IS NULL"),
                new OperatorInfo("IS NOT NULL", "Is not null", "email IS NOT NULL"),
                new OperatorInfo("IN", "In list", "status IN ('active', 'pending')"),
                new OperatorInfo("NOT IN", "Not in list", "type NOT IN ('spam', 'deleted')")
        );
    }

    /**
     * Returns logical operators.
     */
    public static List<OperatorInfo> getLogicalOperators() {
        return List.of(
                new OperatorInfo("&", "AND", "name = 'test' & active = true"),
                new OperatorInfo("|", "OR", "status = 'active' | status = 'pending'"),
                new OperatorInfo("( )", "Grouping", "(name ~ 'a' | name ~ 'b') & active = true")
        );
    }

    private static boolean isWrappingParentheses(String input) {
        int parens = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') parens++;
            else if (c == ')') parens--;
            if (parens == 0 && i < input.length() - 1) {
                return false;
            }
        }
        return parens == 0;
    }

    private static Expression parseExpression(String input) {
        input = input.trim();

        // Remove wrapping parentheses
        while (input.startsWith("(") && input.endsWith(")") && isWrappingParentheses(input)) {
            input = input.substring(1, input.length() - 1).trim();
        }

        int parens = 0;
        boolean inQuotes = false;
        char quoteChar = 0;
        List<String> tokens = new ArrayList<>();
        List<Operator> ops = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // Track quote state
            if ((c == '\'' || c == '"') && (i == 0 || input.charAt(i - 1) != '\\')) {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                }
            }

            if (!inQuotes) {
                if (c == '(') parens++;
                if (c == ')') parens--;

                if ((c == '&' || c == '|') && parens == 0) {
                    tokens.add(current.toString().trim());
                    current.setLength(0);
                    ops.add(c == '&' ? Operator.AND_OPERATOR : Operator.OR_OPERATOR);
                    continue;
                }
            }

            current.append(c);
        }
        tokens.add(current.toString().trim());

        // Validate balanced parentheses
        if (parens != 0) {
            throw new QuerySyntaxException("Unbalanced parentheses in query");
        }

        if (inQuotes) {
            throw new QuerySyntaxException("Unclosed quote in query");
        }

        if (tokens.size() == 1) {
            return parseCondition(tokens.get(0));
        }

        // Check all ops are the same type (AND/OR) at this level
        Operator op = ops.get(0);
        List<Expression> expressions = new ArrayList<>();
        for (String token : tokens) {
            if (token.isEmpty()) {
                throw new QuerySyntaxException("Empty condition found - check for missing operands around & or |");
            }
            expressions.add(parseExpression(token));
        }
        return new GroupExpression(op, expressions);
    }

    private static Expression parseCondition(String input) {
        input = input.trim();

        if (input.isEmpty()) {
            throw new QuerySyntaxException("Empty condition");
        }

        // Try IS NULL / IS NOT NULL first
        Matcher isNullMatcher = IS_NULL_PATTERN.matcher(input);
        if (isNullMatcher.matches()) {
            String attr = isNullMatcher.group(1).trim();
            boolean isNotNull = isNullMatcher.group(2) != null;
            SearchOperation op = isNotNull ? SearchOperation.IS_NOT_NULL_OPERATION : SearchOperation.IS_NULL_OPERATION;
            return new Condition(attr, op, null);
        }

        // Try IN / NOT IN
        Matcher inMatcher = IN_PATTERN.matcher(input);
        if (inMatcher.matches()) {
            String attr = inMatcher.group(1).trim();
            boolean isNotIn = inMatcher.group(2) != null;
            String valuesPart = inMatcher.group(3).trim();
            List<String> values = parseInValues(valuesPart);
            SearchOperation op = isNotIn ? SearchOperation.NOT_IN_OPERATION : SearchOperation.IN_OPERATION;
            return new Condition(attr, op, values);
        }

        // Try standard operators (order matters - longer operators first)
        for (OperatorMapping mapping : OPERATOR_MAPPINGS) {
            int opIndex = findOperatorIndex(input, mapping.symbol);
            if (opIndex > 0) {
                String attr = input.substring(0, opIndex).trim();
                String valueStr = input.substring(opIndex + mapping.symbol.length()).trim();
                Object value = parseValue(valueStr, mapping.operation);

                if (attr.isEmpty()) {
                    throw new QuerySyntaxException("Missing field name before operator '" + mapping.symbol + "'");
                }

                return new Condition(attr, mapping.operation, value);
            }
        }

        throw new QuerySyntaxException("Invalid condition: '" + input + "'. Expected format: field operator value. " +
                "Supported operators: =, !=, ~, !~, >, <, >=, <=, IS NULL, IS NOT NULL, IN, NOT IN");
    }

    /**
     * Find operator index, making sure we're not inside quotes.
     */
    private static int findOperatorIndex(String input, String operator) {
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i <= input.length() - operator.length(); i++) {
            char c = input.charAt(i);

            // Track quote state
            if ((c == '\'' || c == '"') && (i == 0 || input.charAt(i - 1) != '\\')) {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                }
            }

            if (!inQuotes && input.substring(i).startsWith(operator)) {
                // Make sure we're not matching a longer operator
                // e.g., when looking for '=' we shouldn't match '!=' or '>=' or '<='
                if (operator.equals("=") && i > 0) {
                    char prev = input.charAt(i - 1);
                    if (prev == '!' || prev == '>' || prev == '<') {
                        continue;
                    }
                }
                if (operator.equals("~") && i > 0 && input.charAt(i - 1) == '!') {
                    continue;
                }
                if (operator.equals(">") && i + 1 < input.length() && input.charAt(i + 1) == '=') {
                    continue;
                }
                if (operator.equals("<") && i + 1 < input.length() && input.charAt(i + 1) == '=') {
                    continue;
                }
                return i;
            }
        }
        return -1;
    }

    private static List<String> parseInValues(String valuesPart) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < valuesPart.length(); i++) {
            char c = valuesPart.charAt(i);

            if ((c == '\'' || c == '"') && (i == 0 || valuesPart.charAt(i - 1) != '\\')) {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                }
                continue; // Don't include quote chars in value
            }

            if (c == ',' && !inQuotes) {
                String val = current.toString().trim();
                if (!val.isEmpty()) {
                    values.add(val);
                }
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        // Add last value
        String val = current.toString().trim();
        if (!val.isEmpty()) {
            values.add(val);
        }

        if (values.isEmpty()) {
            throw new QuerySyntaxException("IN clause requires at least one value");
        }

        return values;
    }

    private static Object parseValue(String valueStr, SearchOperation operation) {
        if (valueStr.isEmpty()) {
            throw new QuerySyntaxException("Missing value after operator");
        }

        // Remove surrounding quotes
        String value = valueStr;
        if ((value.startsWith("'") && value.endsWith("'")) ||
            (value.startsWith("\"") && value.endsWith("\""))) {
            value = value.substring(1, value.length() - 1);
        }

        // For LIKE operations, wrap with % if not already present
        if (operation == SearchOperation.LIKE_OPERATION || operation == SearchOperation.NOT_LIKE_OPERATION) {
            if (!value.contains("%")) {
                value = "%" + value + "%";
            }
        }

        // Handle special values
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        if ("null".equalsIgnoreCase(value)) {
            return null;
        }

        // Try to parse as number if no quotes were used
        if (!valueStr.startsWith("'") && !valueStr.startsWith("\"")) {
            try {
                if (value.contains(".")) {
                    return Double.parseDouble(value);
                } else {
                    return Long.parseLong(value);
                }
            } catch (NumberFormatException e) {
                // Not a number, use as string
            }
        }

        return value;
    }

    private static List<String> validateExpression(Expression expr, Class<?> entityClass) {
        List<String> warnings = new ArrayList<>();
        Set<String> availableFields = new HashSet<>(getAvailableFields(entityClass));

        validateExpressionRecursive(expr, availableFields, warnings);
        return warnings;
    }

    private static void validateExpressionRecursive(Expression expr, Set<String> availableFields, List<String> warnings) {
        if (expr instanceof Condition cond) {
            // Check if field exists (basic check - doesn't handle nested paths perfectly)
            String baseField = cond.attribute.split("\\.")[0].replace("[", "").replace("]", "");
            if (!availableFields.contains(baseField) && !availableFields.contains(cond.attribute)) {
                warnings.add("Field '" + cond.attribute + "' may not exist in entity");
            }
        } else if (expr instanceof GroupExpression group) {
            for (Expression subExpr : group.expressions) {
                validateExpressionRecursive(subExpr, availableFields, warnings);
            }
        }
    }

    private static String buildSearchRequestFromExpression(
            Expression expr,
            Class<?> entityClass,
            SearchRequest request,
            AtomicInteger criterionIdCounter,
            AtomicInteger groupIdCounter
    ) {
        if (expr instanceof Condition cond) {
            String cid = "C" + criterionIdCounter.getAndIncrement();
            Criterion criterion = new Criterion(cid, entityClass.getSimpleName(), cond.attribute, cond.operation, cond.value);
            String gid = "G" + groupIdCounter.getAndIncrement();
            request.addCriterion(gid, Operator.AND_OPERATOR, criterion);
            return gid;
        }

        GroupExpression groupExpr = (GroupExpression) expr;
        List<String> innerGroupIds = new ArrayList<>();

        for (Expression subExpr : groupExpr.expressions) {
            String gid = buildSearchRequestFromExpression(subExpr, entityClass, request, criterionIdCounter, groupIdCounter);
            innerGroupIds.add(gid);
        }

        String thisGroupId = "G" + groupIdCounter.getAndIncrement();
        request.mergeGroup(thisGroupId, groupExpr.operator, innerGroupIds.toArray(new String[0]));
        return thisGroupId;
    }

    // ==================== Inner Classes ====================

    public interface Expression {
    }

    public static class Condition implements Expression {
        public final String attribute;
        public final SearchOperation operation;
        public final Object value;

        public Condition(String attribute, SearchOperation operation, Object value) {
            this.attribute = attribute;
            this.operation = operation;
            this.value = value;
        }
    }

    public static class GroupExpression implements Expression {
        public final Operator operator;
        public final List<Expression> expressions;

        public GroupExpression(Operator operator, List<Expression> expressions) {
            this.operator = operator;
            this.expressions = expressions;
        }
    }

    private static class OperatorMapping {
        final String symbol;
        final SearchOperation operation;

        OperatorMapping(String symbol, SearchOperation operation) {
            this.symbol = symbol;
            this.operation = operation;
        }
    }

    /**
     * Information about a supported operator.
     */
    public static class OperatorInfo {
        public final String symbol;
        public final String description;
        public final String example;

        public OperatorInfo(String symbol, String description, String example) {
            this.symbol = symbol;
            this.description = description;
            this.example = example;
        }
    }

    /**
     * Result of query validation.
     */
    public static class ValidationResult {
        public final boolean valid;
        public final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }

    /**
     * Exception thrown when query syntax is invalid.
     */
    public static class QuerySyntaxException extends RuntimeException {
        public QuerySyntaxException(String message) {
            super(message);
        }

        public QuerySyntaxException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
