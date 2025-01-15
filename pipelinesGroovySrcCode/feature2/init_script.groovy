import com.streamsets.*;
import com.streamsets.pipeline.*;
import com.streamsets.pipeline.api.*;

def branchTable = [

        "tab1"   : { record ->
            return 200 // Status code for tab1
        },

        "tab2"   : { record ->
            def field_new = Field.create(Field.Type.STRING, null);
            field_new.value = "Data created by branchtable logic";
            record.sdcRecord.set("/branch_table_entry_tab2", field_new);
            record.sdcRecord.get("/branch_table_entry_tab2").setAttribute('attr-x','--ATTR-X--')
            record.sdcRecord.getHeader().setAttribute('created.by.branchtable.logic', "XYZ");
            return 201 // Status code for tab2
        },

        "default": { record ->
            return 404 // Status code for default case
        }
]

// Closure cache
def cachedClosure = null
def cachedKey = null

def executeBranch = { key, record ->
    if (cachedKey != key) {
        // Cache the new closure if the key changes
        cachedKey = key
        cachedClosure = branchTable[key] ?: branchTable["default"]
    }
    // Call the cached closure
    return cachedClosure.call(record)
}

sdc.state['executeBranch'] = executeBranch