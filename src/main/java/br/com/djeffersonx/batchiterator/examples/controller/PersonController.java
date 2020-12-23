package br.com.djeffersonx.batchiterator.examples.controller;

import br.com.djeffersonx.batchiterator.BatchIterator;
import br.com.djeffersonx.batchiterator.examples.model.Person;
import br.com.djeffersonx.batchiterator.examples.model.QPerson;
import br.com.djeffersonx.batchiterator.examples.repository.PersonRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@RestController
@Validated
@RequestMapping("/person")
public class PersonController {

    private final PersonRepository repository;

    public PersonController(final PersonRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void postConstruct() {
        repository.saveAll(
                LongStream.range(0, 10000).mapToObj(
                        id -> new Person(id, "Person " + id)
                ).collect(Collectors.toList()));
    }

    @GetMapping("/alldata/print")
    public void printAllData() {
        //QPerson.person.id.gt(0L)
        var batchIterator = new BatchIterator<>(repository, null, 500);
        while (batchIterator.hasNext()) {
            Person person = batchIterator.next();
            System.out.println(person.getName());
        }
    }

}