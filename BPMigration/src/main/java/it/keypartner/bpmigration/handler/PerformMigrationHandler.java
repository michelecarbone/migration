package it.keypartner.bpmigration.handler;

import it.keypartner.bpmigration.ProcessToMigrate;
import it.keypartner.bpmigration.dao.MigrationManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformMigrationHandler implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(MigrationManager.class);

	@SuppressWarnings("unchecked")
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		List<ProcessToMigrate> processToMigrateList = (List<ProcessToMigrate>) workItem
				.getParameter("in_processToMigrateList");
		StringBuilder stringBuilder = new StringBuilder();
		for (ProcessToMigrate processToMigrate : processToMigrateList) {
			log.info("Start Migration for -> " + processToMigrate);
			String outcome = MigrationManager.migrate(processToMigrate);
			log.info("End Migration for -> " + processToMigrate);
			stringBuilder.append(outcome + "\n");
		}
		Map<String, Object> results = new HashMap<String, Object>();
		log.info(stringBuilder.toString());

		results.put("out_outcome", stringBuilder.toString());

		manager.completeWorkItem(workItem.getId(), results);
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// no-op

	}

}
