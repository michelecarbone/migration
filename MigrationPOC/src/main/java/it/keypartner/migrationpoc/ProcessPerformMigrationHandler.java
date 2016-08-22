package it.keypartner.migrationpoc;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class ProcessPerformMigrationHandler implements WorkItemHandler {

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		System.out.println("ProcessPerformMigrationHandler .. IN Action");
		String in_fromProcessInstaceId = (String) workItem.getParameter("in_fromProcessInstaceId");
		String in_toProcessId = (String) workItem.getParameter("in_toProcessId");

		String outcome = "ESITO OK";

		Map<String, Object> results = new HashMap<String, Object>();
		results.put("out_outcome", outcome);
		manager.completeWorkItem(workItem.getId(), results);
		System.out.println("ProcessPerformMigrationHandler .. End");

	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
	}

}
