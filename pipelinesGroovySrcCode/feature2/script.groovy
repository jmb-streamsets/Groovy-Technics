sdc.records.each { record ->
    try {
        def status = sdc.state['executeBranch']("tab2", record)
        sdc.output.write(record)
    } catch (e) {
        sdc.log.error(e.toString(), e)
        sdc.error.write(record, e.toString())
    }
}