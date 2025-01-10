import com.streamsets.*;
import com.streamsets.pipeline.*;
import com.streamsets.pipeline.api.*;

def field = record.sdcRecord.get('/tablename');
def field_new = Field.create(Field.Type.STRING, null);
field_new.value = field.value + " Data updated by gpg dynamic groovy code generation";

record.sdcRecord.set("/groovy_dyngen_entry_tab3", field_new);
record.sdcRecord.get("/groovy_dyngen_entry_tab3").setAttribute('attr-1','--ATTR-1--');
record.sdcRecord.get("/groovy_dyngen_entry_tab3").setAttribute('attr-2','--ATTR-2--');
record.sdcRecord.get("/groovy_dyngen_entry_tab3").setAttribute('attr-3','--ATTR-3--');
record.sdcRecord.getHeader().setAttribute('created.by.pgp.dynamic.groovy.code.generation', "xyz");

//*******************************************************************************************
// This groovy code does implement the StreamSets Data Collector Records API
// Data Collector Records API does provide access to field structures like: "field headers"
// Data Collector Records API does provide a better management of the field data types.
//*******************************************************************************************

//*******************************************************************************************
// record object will be made visible to the script using the "Binding" groovy class
//*******************************************************************************************