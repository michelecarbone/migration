package it.keypartner.bpmigration.builder;

import it.keypartner.bpmigration.BasicParamSearchProcessInstance;
import it.keypartner.bpmigration.ProcessToMigrate;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.process.audit.ProcessInstanceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builders to convert {@link ProcessInstanceLog} to {@link ProcessToMigrate}
 *
 */
public class ProcessToMigrateBuilder {

	private static final Logger log = LoggerFactory.getLogger(ProcessToMigrateBuilder.class);

	public static List<ProcessToMigrate> build(List<ProcessInstanceLog> processInstanceLogList,
			BasicParamSearchProcessInstance searchProcessInstance) {
		List<ProcessToMigrate> listProcessToMigrate = new ArrayList<>();
		for (ProcessInstanceLog processInstanceLog : processInstanceLogList) {
			listProcessToMigrate.add(ProcessToMigrateBuilder.build(processInstanceLog, searchProcessInstance));
		}
		return listProcessToMigrate;
	}

	public static ProcessToMigrate build(ProcessInstanceLog processInstanceLog,
			BasicParamSearchProcessInstance searchProcessInstance) {
		ProcessToMigrate processToMigrate = new ProcessToMigrate(processInstanceLog.getProcessInstanceId(),
				searchProcessInstance.getToProcessId(), searchProcessInstance.getFromProcessId(),
				searchProcessInstance.getFromDeploymentId(), searchProcessInstance.getToDeploymentId(),
				processInstanceLog.getStart(), processInstanceLog.getProcessName());
		return processToMigrate;
	}

}
