/****************************************************************
 * 1) Field Class
 ****************************************************************/
class Field {
    enum Type {
        STRING, INTEGER, FLOAT, BOOLEAN
    }

    Type type
    Object value
    Map<String, String> attributes = [:]

    /**
     * Factory-style create method
     */
    static Field create(Type type, Object value) {
        return new Field(type: type, value: value)
    }

    /**
     * Add or update an attribute in the attributes map
     */
    void setAttribute(String name, String value) {
        attributes[name] = value
    }

    @Override
    String toString() {
        return "Field[${type}:${value}]"
    }
}

/****************************************************************
 * 2) Header Class
 *
 * A simple class that holds header attributes and provides
 * setAttribute/getAttribute methods.
 ****************************************************************/
class Header {
    private Map<String, String> delegate

    Header(Map<String, String> delegate) {
        this.delegate = delegate
    }

    void setAttribute(String attr, String value) {
        delegate[attr] = value
        println("Set header attribute ${attr} with ${value}")
    }

    String getAttribute(String attr) {
        return delegate[attr]
    }

    @Override
    String toString() {
        return delegate.toString()
    }
}

/****************************************************************
 * 3) SdcRecord Class
 *
 * - Stores Fields by path (String -> Field).
 * - Has a Header instance for storing header-level attributes.
 ****************************************************************/
class SdcRecord {
    /**
     * We store only Field objects by path, so you can do:
     *   def field = record.get('/somePath')
     *   field.value = "..."
     *   field.setAttribute(..., ...)
     */
    private Map<String, Field> store = [:]

    /**
     * Header-level attributes
     */
    private Map<String, String> headerMap = [:]

    /**
     * Provide direct access to a header object
     */
    private Header headerHelper

    SdcRecord() {
        // Initialize our Header instance and connect it to headerMap
        this.headerHelper = new Header(headerMap)
    }

    /**
     * Set a Field at a specific path
     */
    void set(String path, Field field) {
        store[path] = field
        println("Set ${path} with ${field}")
    }

    /**
     * Get the Field stored at a specific path
     */
    Field get(String path) {
        return store[path]
    }

    /**
     * Access the Header object
     */
    Header getHeader() {
        return headerHelper
    }

    @Override
    String toString() {
        return "SdcRecord(store=$store, header=$headerMap)"
    }
}

/****************************************************************
 * 4) Branch Logic
 *
 * (Unchanged, but now uses typed Fields & typed Header.)
 ****************************************************************/
def branchTable = [
        "tab1"   : { SdcRecord sdcRecord ->
            // Possibly do something with sdcRecord here...
            return 200
        },
        "tab2"   : { SdcRecord sdcRecord ->
            // Create a new Field and store it at /branch_table_entry_tab2
            def fieldNew = Field.create(Field.Type.STRING, null)
            fieldNew.value = " --- UPDATED ---"
            sdcRecord.set("/column_01", fieldNew)
            sdcRecord.set("/column_02", fieldNew)
            sdcRecord.set("/column_03", fieldNew)

            // Retrieve the Field and set an attribute
            sdcRecord.get("/column_01")?.setAttribute('attr-x', '--ATTR-X--')
            sdcRecord.getHeader().setAttribute('created.by.branchtable.logic_1', "001")

            sdcRecord.get("/column_02")?.setAttribute('attr-x', '--ATTR-X--')
            sdcRecord.getHeader().setAttribute('created.by.branchtable.logic_2', "002")

            sdcRecord.get("/column_03")?.setAttribute('attr-x', '--ATTR-X--')
            sdcRecord.getHeader().setAttribute('created.by.branchtable.logic_3', "003")

            return 201
        },
        "default": { SdcRecord sdcRecord ->
            return 404
        }
]

def executeBranch = { String key, SdcRecord sdcRecord ->
    try {
        def closure = branchTable[key] ?: branchTable["default"]
        return closure.call(sdcRecord)
    } catch (Exception e) {
        println("Error processing branch '${key}': ${e.message}")
        return 500 // Internal server error
    }
}

/****************************************************************
 * 5) Demonstration & Testing
 ****************************************************************/
println "========== Single Record Tests =========="
def record1 = new SdcRecord()
println "Result for tab1: " + executeBranch("tab1", record1) // => 200

def record2 = new SdcRecord()
println "Result for tab2: " + executeBranch("tab2", record2) // => 201

def record3 = new SdcRecord()
println "Result for unknown: " + executeBranch("unknown", record3) // => 404

// Print the resulting SdcRecord states
println "Record1 = ${record1}"
println "Record2 = ${record2}"
println "Record3 = ${record3}"

println "\n========== Creating Multiple Records in a List =========="
def recordList = []

(1..10).eachWithIndex { int entry, int i ->
    def rec = new SdcRecord()
    // For demonstration, set a field and a header attribute
    def field = Field.create(Field.Type.STRING, "Record #$i data")
    rec.set("/field$i", field)
    rec.getHeader().setAttribute("batchId", "Batch-$i")
    // Run a branch on every even record, for example
    executeBranch("tab2", rec)
    recordList << rec
}

// Print all records in the list
recordList.eachWithIndex { rec, idx ->
    println "Record #${idx + 1} in list: $rec"
}
