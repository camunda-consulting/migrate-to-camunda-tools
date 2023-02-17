# JBPM  Business Process Management BPMN Generator
The JBPM file must be improve to be able to work in Camunda.
The generator does this transformations.


## How to use this utility
After cloning the repository and performing the necessary maven commands, either run the

```org.camunda.bpmn.generator.BPMNFromJbpm```

is the main class for your IDE.
Arguments are:
* input directory. All files ended by .bpmn will be processed
* output directory, to produce the result

To build an executable Jar file, execute:

```mvn clean compile assembly:single```


Execute the following command using the resulting jar file

```
cd target
java -jar JPBM-1.0-SNAPSHOT-jar-with-dependencies.jar <inputDirectory> <outputDirectory>
```

For example

```
cd target
java -jar JPBM-1.0-SNAPSHOT-jar-with-dependencies.jar "../src/test/resources" .
```

Produce this
`````
SLF4J: No SLF4J providers were found.
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See https://www.slf4j.org/codes.html#noProviders for further details.
INFO  Migrate JPMB process in [../src/test/resources] to Camunda 7 process saved in [.]
INFO  Found  2 files in [D:\dev\intellij\consulting\migrate-to-camunda-tools\JBPM\target\..\src\test\resources]
INFO  ----------------------------------------------- Manage[BoundaryEvent.bpmn]
INFO       [ReadBpmnModel       ] : true -  in 359 ms
INFO    -- End pre verification  in 364 ms
INFO       [BpmnDefinition      ] : Added xmlns:xsi, xmlns:camunda in 4 ms
INFO       [Feel                ] : Feel Expression replaced 0 ignored 0 in 5 ms
INFO       [DeleteAssignment    ] : Assignement deleted 0 in 0 ms
INFO       [UserTaskInput       ] : iospecification deleted 0 in 0 ms

...

`````
