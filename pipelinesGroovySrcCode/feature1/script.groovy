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