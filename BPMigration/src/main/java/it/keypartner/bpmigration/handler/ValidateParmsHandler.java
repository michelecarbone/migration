package it.keypartner.bpmigration.handler;

import it.keypartner.bpmigration.BasicParamSearchProcessInstance;
import it.keypartner.bpmigration.dao.ProcessManageDAO;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Work item handler for validation of search params
 *
 */
public class ValidateParmsHandler implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(ValidateParmsHandler.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager wkManager) {
		log.info("Calling validate single search ...");
		BasicParamSearchProcessInstance searchProcessInstance = (BasicParamSearchProcessInstance) workItem
				.getParameter("in_validParamSearch");

		log.info("Params to validate [" + searchProcessInstance.toString() + "]");

		ProcessManageDAO processManageDAO = new ProcessManageDAO();
		String valid = processManageDAO.validateParams(searchProcessInstance.getFromDeploymentId(),
				searchProcessInstance.getFromProcessId(), searchProcessInstance.getToDeploymentId(),
				searchProcessInstance.getToProcessId());
		log.info("Valid ? [" + valid + "]");
		if (valid == null) {
			valid = "";
		}

		log.info("Result of Validation [" + valid + "]");
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("out_validMsg", valid.toString());
		wkManager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {

	}

}
