package it.keypartner.bpmigration.handler;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllProcessInstanceHandler implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(AllProcessInstanceHandler.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		log.info("Calling search...");

		manager.completeWorkItem(workItem.getId(), null);

		log.info("Search end!");

	}

	@Override
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {

	}

}
