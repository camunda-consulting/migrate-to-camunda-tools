# Migration to Camunda tools
In this repository you'll find tooling that can assist you in migrating applications from other vendors to Camunda. They are grouped by vendor and then by migration strategy, if there is more than one. To be clear, none of these tools is a 'silver bullet' that will transform applications from one vendor to another at the click of a button. Typically, these tools will generate a BPMN file that attempts to retain the fidelity of the original process diagram. Depending on the vendor you're migrating from, you'll still need to add the components (code, scripts, etc.) to make the process executable. The tools here make extensive use of the [Camunda Model APIs](https://docs.camunda.org/manual/latest/user-guide/model-api/bpmn-model-api/) to create the BPMN file. Having the source code available will enable you to extend the tools, as needed.

## How to use these utilities
Each of the utilities will have its own readme but typically it is composed of a static java method that can be run in your favorite IDE or can be packaged in a jar and executed as such. You'll need to provide two arguments to run any of the tools, the input file that needs to be transformed, and the output BPMN file that you would like to use in Camunda.

Each tool will have a sample input file that can be used as a test. You may find that you'll need to tweak the code as there are many variants or extensions of the standards used among vendors. But having the source code will allow you to accommodate these variants. 

You can just run the main class in your IDE, passing in as arguments the input file and the output file or generate an executable jar file. If you wish to generate an executable jar file issue the following maven command 

```mvn clean compile assembly:single``` 

and execute the following command using the resulting jar file

```java -jar Your-chosen-tool.jar input-file output-file```

## Notes and TODOs
Any and all feedback is welcome! Suggestions for other types of conversions are welcome as well.
