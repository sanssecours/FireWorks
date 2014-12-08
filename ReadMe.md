# FireWorks

A simple simulation of a rocket factory. There are four different type of people responsible for the creation of a rocket:

- Supplier
- Worker
- Quality Tester
- Logistician

Suppliers can be created by using the Graphical User Interface `FireWorks`. Workers, Quality Testers, and Logistician are all simple command line programs.

## Requirements

- [Maven](http://maven.apache.org)
- [MozartSpaces](http://www.mozartspaces.org)

## Compilation

```bash
mvn compile
```

## Execution

### FireWorks GUI

```bash
mvn exec:java -PFireWorks
```

To add suppliers use the button `New Order`. You can change the different attributes for a supplier by double clicking the cell for the corresponding attribute. If you are happy with the current selection of suppliers you can use the button `Start Suppliers` to activate them.

###  Worker

```bash
mvn exec:java -PWorker
```

###  Quality Tester

```bash
mvn exec:java -PTester
```

###  Logistician

```bash
mvn exec:java -PLogistic
```

