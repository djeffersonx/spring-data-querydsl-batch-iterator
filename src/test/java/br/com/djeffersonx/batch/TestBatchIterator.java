package br.com.djeffersonx.batch;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import br.com.djeffersonx.batch.model.Person;
import br.com.djeffersonx.batchiterator.BatchIterator;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestBatchIterator {

    @MockBean
    private PersonRepository repository;

    private static final Integer TOTAL_RECORDS = 50;
    private static final Supplier<LongStream> MOCK_DATA_IDS = () -> LongStream.range(0, TOTAL_RECORDS);

    private static final PathBuilder<Person> _testProcessControl = new PathBuilder<Person>(
            Person.class, "entity");
    public static final BooleanExpression PREDICATE_LAST_ID_GT_0 = _testProcessControl.getNumber("lastId", Long.class)
            .gt(0L);

    private static final int PAGE_SIZE = 10;

    @Before
    public void before() {

        var mockPersonsRecords = MOCK_DATA_IDS.get()
                .mapToObj(id -> new Person(id, "Registro: " + id)).collect(Collectors.toList());

        IntStream.range(0, (TOTAL_RECORDS / PAGE_SIZE)).forEach(pageIndex -> {
            var indexFirstPageElement = pageIndex * PAGE_SIZE;
            var indexLastPageElement = pageIndex * PAGE_SIZE + PAGE_SIZE;
            var actualPageable = PageRequest.of(pageIndex, PAGE_SIZE);
            var actualPageElements = mockPersonsRecords.subList(indexFirstPageElement, indexLastPageElement);
            when(repository.findAll(PREDICATE_LAST_ID_GT_0, actualPageable))//
                    .thenReturn(new PageImpl<>(actualPageElements, actualPageable, TOTAL_RECORDS));
        });

        PageRequest allDataPage = PageRequest.of(0, 500);
        when(repository.findAll(PREDICATE_LAST_ID_GT_0, allDataPage))//
                .thenReturn(new PageImpl<>(mockPersonsRecords, allDataPage, TOTAL_RECORDS));

    }

    @Test
    public void testIteration() {

        var batchIterator = new BatchIterator<Person>(repository, PREDICATE_LAST_ID_GT_0, 10);
        var recordsFromIteration = new ArrayList<Person>();

        while (batchIterator.hasNext()) {
            Person nextElement = batchIterator.next();
            recordsFromIteration.add(nextElement);
            Assert.assertNotNull("Name is required", nextElement.getName());
        }

        validateListSizeByIteration(recordsFromIteration);

    }

    @Test
    public void testFetchPageGtAllData() {
        var batchIterator = new BatchIterator<Person>(repository, PREDICATE_LAST_ID_GT_0, 500);
        var recordsFromIteration = new ArrayList<Person>();

        while (batchIterator.hasNext()) {
            Person nextElement = batchIterator.next();
            recordsFromIteration.add(nextElement);
            Assert.assertNotNull("Name is required", nextElement.getName());
        }
        validateListSizeByIteration(recordsFromIteration);
    }

    @Test
    public void testForEachRemaining() {
        var batchIterator = new BatchIterator<Person>(repository, PREDICATE_LAST_ID_GT_0, 10);
        var recordsFromIteration = new ArrayList<Person>();
        batchIterator.forEachRemaining(e -> recordsFromIteration.add(e));
        validateListSizeByIteration(recordsFromIteration);
    }


    @Test
    public void testRemove() {
        var batchIterator = new BatchIterator<Person>(repository, PREDICATE_LAST_ID_GT_0, 10);
        Integer itensCount = 0;
        while (batchIterator.hasNext()) {
            batchIterator.remove();
            itensCount++;
        }
        Assert.assertEquals(String.format("%s records must be iterated", TOTAL_RECORDS), TOTAL_RECORDS, itensCount);
    }

    private void validateListSizeByIteration(ArrayList<Person> recordsFromIteration) {
        Assert.assertEquals(String.format("%s records must be iterated", TOTAL_RECORDS), TOTAL_RECORDS,
                (Integer) recordsFromIteration.size());

        var countDifferentIds = (Integer) recordsFromIteration.stream()
                .collect(Collectors.groupingBy(Person::getId)).size();

        Assert.assertEquals(String.format("%s records must be differents", TOTAL_RECORDS), TOTAL_RECORDS,
                countDifferentIds);
    }

}