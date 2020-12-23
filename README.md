### The problem

You have a really huge database, and for some reason you need to load all the data from a table for processing in memory.

To solve this we must do the batch processing, for example the following case:

You want to select the records from the Person table created after the date: 2015-01-01 (this select results in 150 million records), and perform a memory processing in batches of 10,000 records at a time.

### The solution:

``` java
BooleanExpression createdAtPredicate = QPerson.createdAt.gt(LocalDate.of(2010, 01, 01));
BatchIterator<Person> batchIterator = new BatchIterator<Person>(personRepository, createdAtPredicate, 10000);
while(batchIterator.hasNext()) {
    Person person = batchIterator.next();
    businessLogicWith(person);
}
```