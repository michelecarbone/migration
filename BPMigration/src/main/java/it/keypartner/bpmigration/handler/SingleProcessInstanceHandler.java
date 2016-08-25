package it.keypartner.bpmigration.handler;

import it.keypartner.bpmigration.BasicParamSearchProcessInstance;
import it.keypartner.bpmigration.ProcessToMigrate;
import it.keypartner.bpmigration.builder.ProcessToMigrateBuilder;
import it.keypartner.bpmigration.dao.ProcessManageDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleProcessInstanceHandler implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(SingleProcessInstanceHandler.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager wkManager) {
		log.info("Calling single search ...");

		List<ProcessToMigrate> processToMigrates = new ArrayList<>();
		BasicParamSearchProcessInstance searchProcessInstance = (BasicParamSearchProcessInstance) workItem
				.getParameter("in_search");
		String processInstanceId = (String) workItem.getParameter("in_singleProcessInstanceId");

		log.info("Params -> " + searchProcessInstance.toString() + " AND ProcessInstanceID " + processInstanceId);

		ProcessManageDAO processManageDAO = new ProcessManageDAO();
		ProcessInstanceLog processFound = processManageDAO.retriveProcessInstance(
				searchProcessInstance.getFromDeploymentId(), processInstanceId);
		if (processFound != null) {
			if (processFound.getStatus().compareTo(ProcessInstance.STATE_ACTIVE) == 0) {
				List<ProcessInstanceLog> processInstanceLogs = new ArrayList<ProcessInstanceLog>();
				processInstanceLogs.add(processFound);
				// Invoco il builder per costruire le info da estrarre
				processToMigrates = ProcessToMigrateBuilder.build(processInstanceLogs, searchProcessInstance);
			}
		} else {
			log.info("PROCESS NOT FOUND");
		}
		log.info("Single Process To Migrate [" + processToMigrates.size() + "]");
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("out_processList", processToMigrates);

		wkManager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {

	}

}
