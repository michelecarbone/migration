package it.keypartner.bpmigration.builder;

import it.keypartner.bpmigration.ProcessToMigrate;
import it.keypartner.bpmigration.ProcessVarToMigrate;
import it.keypartner.bpmigration.SearchProcessInstance;

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
			SearchProcessInstance searchProcessInstance) {
		List<ProcessToMigrate> listProcessToMigrate = new ArrayList<>();
		for (ProcessInstanceLog processInstanceLog : processInstanceLogList) {
			listProcessToMigrate.add(ProcessToMigrateBuilder.build(processInstanceLog, searchProcessInstance));
		}
		return listProcessToMigrate;
	}

	public static List<ProcessVarToMigrate> build(List<ProcessInstanceLog> processInstanceLogList,
			List<org.kie.api.runtime.manager.audit.VariableInstanceLog> variableInstanceLogsList,
			SearchProcessInstance searchProcessInstance) {
		List<ProcessVarToMigrate> listProcessToMigrate = new ArrayList<>();
		for (ProcessInstanceLog processInstanceLog : processInstanceLogList) {
			for (org.kie.api.runtime.manager.audit.VariableInstanceLog variableInstanceLog : variableInstanceLogsList) {
				if (variableInstanceLog.getProcessInstanceId().compareTo(processInstanceLog.getProcessInstanceId()) == 0) {
					log.info("Found same processId and VariabileProcessID");
					listProcessToMigrate.add(ProcessToMigrateBuilder.build(processInstanceLog, variableInstanceLog,
							searchProcessInstance));
					break;
				}
			}
		}
		return listProcessToMigrate;
	}

	public static ProcessToMigrate build(ProcessInstanceLog processInstanceLog,
			SearchProcessInstance searchProcessInstance) {
		ProcessToMigrate processToMigrate = new ProcessToMigrate(processInstanceLog.getProcessInstanceId(),
				searchProcessInstance.getToProcessId(), searchProcessInstance.getFromProcessId(),
				searchProcessInstance.getFromDeploymentId(), searchProcessInstance.getToDeploymentId(),
				processInstanceLog.getStart(), processInstanceLog.getProcessName());
		return processToMigrate;
	}

	public static ProcessVarToMigrate build(ProcessInstanceLog processInstanceLog,
			org.kie.api.runtime.manager.audit.VariableInstanceLog variableInstanceLog,
			SearchProcessInstance searchProcessInstance) {
		ProcessVarToMigrate processToMigrate = new ProcessVarToMigrate(processInstanceLog.getProcessInstanceId(),
				searchProcessInstance.getToProcessId(), searchProcessInstance.getFromProcessId(),
				searchProcessInstance.getFromDeploymentId(), searchProcessInstance.getToDeploymentId(),
				processInstanceLog.getStart(), processInstanceLog.getProcessName(),
				variableInstanceLog.getVariableId(), variableInstanceLog.getValue(), variableInstanceLog.getDate());
		return processToMigrate;
	}
}
