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