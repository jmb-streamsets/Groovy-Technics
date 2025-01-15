# Feature 2 logic

## Init Script

- A `Branch Table` logic is used to organize the code logic by using a `Map`
  made of closures wrapping 1 to n Groovy or Java statements
- Each closure is implementing an input object name `record` that is in fact and
  SDC record object propagated by reference
- A Groovy lambda function **`executeBranch`** is necessary for the main logic
  to execute a `branch`
- The lambda function does accept two parameters: the branch to execute and the
  SDC record
- The lambda function is using a basic cache
- The lambda function is stored inside a shared variable
  `sdc.sate['executeBranch']'`

```
import com.streamsets.*;
import com.streamsets.pipeline.*;
import com.streamsets.pipeline.api.*;

def branchTable = [
  
        "tab1"   : { record ->
            def field_new = Field.create(Field.Type.STRING, null)
            field_new.value = "Data created by branchtable logic"
            record.sdcRecord.set("/branch_table_entry_tab1", field_new)
            record.sdcRecord.get("/branch_table_entry_tab1").setAttribute('attr-x','--ATTR-X--')
            record.sdcRecord.getHeader().setAttribute('created.by.branchtable.logic', "XYZ")               
            return 200 // Status code for tab1
        },
  
        "tab2"   : { record ->
            def field_new = Field.create(Field.Type.STRING, null)
            field_new.value = "Data created by branchtable logic"
            record.sdcRecord.set("/branch_table_entry_tab2", field_new);
            record.sdcRecord.get("/branch_table_entry_tab2").setAttribute('attr-x','--ATTR-X--')
            record.sdcRecord.getHeader().setAttribute('created.by.branchtable.logic', "XYZ")         
            return 200 // Status code for tab2
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
```

## Script

- As soon as the `Init Script` is executed then the `Script` will loop to
  consume and produce output records
- The
  `def status = sdc.state['executeBranch'](sdc.pipelineParameters()['table'], record)`
  is used to call the lambda function `executeBranch`
- A status code is returned by the lambda function to indicate a possible
  success or failure
- Then `sdc.output.write(record)` will write the record for pipeline downstream
  usage

```
sdc.records.each { record ->
    try {
      def status = sdc.state['executeBranch'](sdc.pipelineParameters()['table'], record)
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
sdc.state['executeBranch'] = null
```