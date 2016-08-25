package it.keypartner.bpmigration.handler;

import it.keypartner.bpmigration.BasicParamSearchProcessInstance;
import it.keypartner.bpmigration.ProcessVarToMigrate;
import it.keypartner.bpmigration.VarParamSearchProcessInstance;
import it.keypartner.bpmigration.builder.ProcessToMigrateBuilder;
import it.keypartner.bpmigration.dao.ProcessManageDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInstanceWithVarHandler implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(ProcessInstanceWithVarHandler.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager wkManager) {
		log.info("Calling search process with var...");

		BasicParamSearchProcessInstance basicParamSearchProcessInstance = (BasicParamSearchProcessInstance) workItem
				.getParameter("in_basicParamSearch");
		VarParamSearchProcessInstance varParamSearchProcessInstance = (VarParamSearchProcessInstance) workItem
				.getParameter("in_varParamSearch");

		log.info("Variabiles -> " + varParamSearchProcessInstance.toString());

		log.info("Basic -> " + basicParamSearchProcessInstance.toString());

		ProcessManageDAO processManageDAO = new ProcessManageDAO();
		List<ProcessInstanceLog> processFound = processManageDAO.retriveActiveProcessInstanceWithVar(
				basicParamSearchProcessInstance.getFromDeploymentId(),
				basicParamSearchProcessInstance.getFromProcessId(),
				varParamSearchProcessInstance.getSearchProcessVarName(),
				varParamSearchProcessInstance.getSearchProcessVarValue());
		List<org.kie.api.runtime.manager.audit.VariableInstanceLog> variableInstanceLogs = processManageDAO
				.getVariableInstanceLogsList();

		// Invoco il builder per costruire le info da estrarre
		List<ProcessVarToMigrate> processToMigrates = ProcessToMigrateBuilder.build(processFound, variableInstanceLogs,
				varParamSearchProcessInstance, basicParamSearchProcessInstance);
		log.info("Process To Migrate [" + processToMigrates.size() + "]");

		Map<String, Object> results = new HashMap<String, Object>();
		results.put("out_processList", processToMigrates);

		wkManager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {

	}

}
