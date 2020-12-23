package br.com.djeffersonx.batchiterator;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class BatchIterator<T> implements Iterator<T> {

    private final QuerydslPredicateExecutor<T> repository;
    private final Predicate predicate;
    private final Integer batchSize;

    private Queue<T> actualPageQueue;
    private Integer actualPageIndex = 0;
    private Integer totalPages;

    private static final BooleanExpression EMPTY_PREDICATE = Expressions.asBoolean(true).isTrue();

    public BatchIterator(QuerydslPredicateExecutor<T> repository, Integer batchSize) {
        this(repository, null, batchSize);
    }

    public BatchIterator(QuerydslPredicateExecutor<T> repository, Predicate predicate, Integer batchSize) {
        this.repository = repository;
        this.predicate = predicate == null ? EMPTY_PREDICATE : predicate;
        this.batchSize = batchSize;
        nextPage();
    }

    private void nextPage() {
        Page<T> page = repository.findAll(predicate == null ? new BooleanBuilder() : predicate, PageRequest.of(actualPageIndex, batchSize));
        this.actualPageIndex++;
        this.actualPageQueue = new LinkedList<>(page.toList());
        if (totalPages == null) {
            totalPages = page.getTotalPages();
        }
    }

    @Override
    public boolean hasNext() {
        return !actualPageQueue.isEmpty() || hasMorePages();
    }

    @Override
    public T next() {
        if (!actualPageQueue.isEmpty()) {
            return actualPageQueue.poll();
        } else if (hasMorePages()) {
            nextPage();
            return next();
        }
        throw new RuntimeException("No more elements");
    }

    private boolean hasMorePages() {
        return actualPageIndex < totalPages;
    }

    @Override
    public void remove() {
        if (hasNext()) {
            next();
        } else {
            throw new RuntimeException("No more elements");
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        while (hasNext()) {
            action.accept(next());
        }
    }

}