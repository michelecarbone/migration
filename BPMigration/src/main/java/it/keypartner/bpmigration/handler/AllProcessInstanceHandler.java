package it.keypartner.bpmigration.handler;

import it.keypartner.bpmigration.SearchProcessInstance;
import it.keypartner.bpmigration.dao.ProcessManageDAO;

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

		SearchProcessInstance searchProcessInstance = (SearchProcessInstance) workItem.getParameter("in_search");
		log.info("Params -> " + searchProcessInstance.toString());

		ProcessManageDAO processManageDAO = new ProcessManageDAO();
		processManageDAO.retriveActiveProcessInstance(searchProcessInstance.getFromDeploymentId());

		wkManager.completeWorkItem(workItem.getId(), null);
	}

	@Override
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {

	}

}
