# Feature 1 logic

## Init Script

- The StreamSets Groovy evaluator is used to create Groovy code at run-time
- Based on a pipeline parameter the `Init Script` will generate Groovy code in memory as a string
- The in-memory string will be dynamically compiled then stored inside a shared variable `sdc.sate['script']'`

```
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

def script = new StringBuilder()
def config = new CompilerConfiguration()
def customizer = new ImportCustomizer()
def binding = new Binding()
config.addCompilationCustomizers(customizer)
def shell = new GroovyShell(binding, config)

script.append("""
  import com.streamsets.*;
  import com.streamsets.pipeline.*;
  import com.streamsets.pipeline.api.*;""")

switch (sdc.pipelineParameters()['table']) {
// *********************************************************
// *********************************************************
// *********************************************************
    case "tab1": script.append("""
    
  def field = record.sdcRecord.get('/tablename');
  def field_new = Field.create(Field.Type.STRING, null);
  
  field_new.value = field.value + " Data updated by dynamic groovy code generation";
  record.sdcRecord.set("/groovy_dyngen_entry_tab1", field_new);
  
  record.sdcRecord.get("/groovy_dyngen_entry_tab1").setAttribute('attr-1','--ATTR-1--');
  record.sdcRecord.get("/groovy_dyngen_entry_tab1").setAttribute('attr-2','--ATTR-2--');
  record.sdcRecord.get("/groovy_dyngen_entry_tab1").setAttribute('attr-3','--ATTR-3--');  
  
  record.sdcRecord.getHeader().setAttribute('created.by.Dynamic.groovy.code.generation', "xyz"); 
  
  """)
        break
        // *********************************************************
        // *********************************************************
        // *********************************************************
    case "tab2": script.append("""
  record.sdcRecord.set("/output_tablename_2", record.sdcRecord.get('/tablename'));
  """)
        break
        // *********************************************************
        // *********************************************************
        // *********************************************************
    default: script.append("""
  record.sdcRecord.set("/output_tablename_default", record.sdcRecord.get('/tablename'));
  """)
}

sdc.state['binding'] = binding
sdc.state['binding'].setVariable("sdc", sdc)
sdc.state['run'] = shell.parse(script.toString())
```

## Script

- As soon as the `Init Script` is executed then the `Script` will loop to consume and produce output records
- The `sdc.state['binding'].setVariable("record", record)` is used to expose the variable named record to the dynamic
  code
- The `sdc.state['run'].run()` method is activating the dynamic code logic
- Then `sdc.output.write(record)` will write the record for pipeline downstream usage

```
sdc.records.each { record ->
    try {
        sdc.state['binding'].setVariable("record", record)
        sdc.state['run'].run()
        sdc.output.write(record)
    } catch (e) {
        sdc.log.error(e.toString(), e)
        sdc.error.write(record, e.toString())
    }
}
```

## Destroy Script

- Cleanup memory resources

```
sdc.state['binding'] = null
sdc.state['run'] = null
```