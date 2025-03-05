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


## Schema Validation Issues found in BPMN models produced by jBPM Process Modeler aka Business Central
- SAXException while parsing input stream org.xml.sax.SAXException: Error: URI=null Line=109: cvc-id.2: There are multiple occurrences of ID value '_a7c1ff58-5ee2-31bd-aa7e-28298ca3f397'.
- SAXException while parsing input stream org.xml.sax.SAXException: Error: URI=null Line=276: cvc-complex-type.3.2.2: Attribute 'name' is not allowed to appear in element 'bpmn2:textAnnotation'.
   - Workaround: `xmlstarlet edit --inplace --ps -N bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" --delete "//bpmn2:textAnnotation" *.bpmn`
- SAXException while parsing input stream org.xml.sax.SAXException: Error: URI=null Line=409: cvc-complex-type.2.4.b: The content of element 'bpmn2:ioSpecification' is not complete. One of '{"http://www.omg.org/spec/BPMN/20100524/MODEL":inputSet, "http://www.omg.org/spec/BPMN/20100524/MODEL":outputSet}' is expected.
   - Workaround: `xmlstarlet edit --inplace --ps -N bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" --delete "//bpmn2:ioSpecification" *.bpmn`
      - That leads to: SAXException while parsing input stream org.xml.sax.SAXException: Error: URI=null Line=3403: cvc-id.1: There is no ID/IDREF binding for IDREF '_64C3F5B9-603C-4E44-9452-3ABEE65DA9FF_pNotificationReplyDataOutputX'.
        - `xmlstarlet edit --inplace --ps -N bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" --delete "//bpmn2:dataOutputAssociation" *.bpmn`
        - `xmlstarlet edit --inplace --ps -N bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" --delete "//bpmn2:dataInputAssociation" *.bpmn`
- DOM document is not valid org.xml.sax.SAXParseException; cvc-elt.4.2: Cannot resolve 'bpsim:Scenario' to a type definition for element 'bpsim:Scenario'.
   - `xmlstarlet edit --inplace --ps -N bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" --delete "//bpmn2:relationship" *.bpmn`

## Other issues
- Waypoints are not placed correctly.
    - Deleting them with `xmlstarlet edit --inplace --ps -N bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" --delete "//di:waypoint" *.bpmn` does not help