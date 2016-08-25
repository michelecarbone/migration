package it.keypartner.bpmigration.handler;

import it.keypartner.bpmigration.BasicParamSearchProcessInstance;
import it.keypartner.bpmigration.ProcessToMigrate;
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

public class AllProcessInstanceHandler implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(AllProcessInstanceHandler.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager wkManager) {
		log.info("Calling search ...");

		BasicParamSearchProcessInstance searchProcessInstance = (BasicParamSearchProcessInstance) workItem
				.getParameter("in_search");
		log.info("Params -> " + searchProcessInstance.toString());

		ProcessManageDAO processManageDAO = new ProcessManageDAO();
		List<ProcessInstanceLog> processFound = processManageDAO.retriveActiveProcessInstance(
				searchProcessInstance.getFromDeploymentId(), searchProcessInstance.getFromProcessId());

		// Invoco il builder per costruire le info da estrarre
		List<ProcessToMigrate> processToMigrates = ProcessToMigrateBuilder.build(processFound, searchProcessInstance);
		log.info("Process To Migrate [" + processToMigrates.size() + "]");

		Map<String, Object> results = new HashMap<String, Object>();
		results.put("out_processList", processToMigrates);

		wkManager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {

	}

}
