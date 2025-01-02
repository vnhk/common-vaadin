package com.bervan.common.search.model;

import jakarta.persistence.criteria.Order;

import jakarta.persistence.criteria.Expression;

public class OrderImpl implements Order {
    private final Expression<?> expression;
    private final boolean ascending;

    public OrderImpl(Expression<?> expression, boolean ascending) {
        this.expression = expression;
        this.ascending = ascending;
    }

    @Override
    public Order reverse() {
        return new OrderImpl(this.expression, !this.ascending);
    }

    @Override
    public boolean isAscending() {
        return this.ascending;
    }

    @Override
    public Expression<?> getExpression() {
        return this.expression;
    }

    @Override
    public String toString() {
        return (ascending ? "ASC" : "DESC") + " " + expression.toString();
    }
}