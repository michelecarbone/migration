package it.keypartner.migrationpoc;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class ProcessPerformMigrationHandler implements WorkItemHandler {

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		System.out.println("ProcessPerformMigrationHandler INIT");
		String in_fromProcessInstaceId = (String) workItem.getParameter("in_fromProcessInstaceId");
		String in_toProcessId = (String) workItem.getParameter("in_toProcessId");

		String outcome = "ESITO OK";

		Map<String, Object> results = new HashMap<String, Object>();
		results.put("out_outcome", outcome);
		System.out.println("ProcessPerformMigrationHandler OKKKKKKK");
		manager.completeWorkItem(workItem.getId(), results);

	}

	@Override
	public void executeWorkItem(WorkItem arg0, WorkItemManager arg1) {
		// TODO Auto-generated method stub

	}

}
