package it.keypartner.migrationpoc;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.workflow.instance.WorkflowProcessInstanceUpgrader;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessPerformMigrationHandler implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(ProcessPerformMigrationHandler.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		log.info("ProcessPerformMigrationHandler .. IN Action");
		String in_fromDepoymentId = (String) workItem.getParameter("in_fromDepoymentId");
		String in_toDepoymentId = (String) workItem.getParameter("in_toDepoymentId");
		String in_fromProcessInstaceId = (String) workItem.getParameter("in_fromProcessInstaceId");
		String in_toProcessId = (String) workItem.getParameter("in_toProcessId");

		log.info("Validate ?? "
				+ validate(in_fromDepoymentId, in_toDepoymentId, in_fromProcessInstaceId, in_toProcessId));
		String outcome = migrate(in_fromDepoymentId, in_toDepoymentId, in_fromProcessInstaceId, in_toProcessId);

		Map<String, Object> results = new HashMap<String, Object>();
		results.put("out_outcome", outcome);
		manager.completeWorkItem(workItem.getId(), results);

		log.info("ProcessPerformMigrationHandler .. End");

	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
	}

	/**
	 * -- Migration --
	 * 
	 * @param in_fromDepoymentId
	 * @param in_toDepoymentId
	 * @param in_fromProcessInstaceId
	 * @param in_toProcessId
	 * @return
	 */
	private String migrate(String in_fromDepoymentId, String in_toDepoymentId, String in_fromProcessInstaceId,
			String in_toProcessId) {
		StringBuffer outcomeBuffer = new StringBuffer();

		try {
			InternalRuntimeManager currentManager = (InternalRuntimeManager) RuntimeManagerRegistry.get().getManager(
					in_fromDepoymentId);
			InternalRuntimeManager toBeManager = (InternalRuntimeManager) RuntimeManagerRegistry.get().getManager(
					in_toDepoymentId);

			org.kie.api.definition.process.Process toBeProcess = toBeManager.getEnvironment().getKieBase()
					.getProcess(in_toProcessId);

			KieSession current = JPAKnowledgeService.newStatefulKnowledgeSession(currentManager.getEnvironment()
					.getKieBase(), null, currentManager.getEnvironment().getEnvironment());

			KieSession tobe = JPAKnowledgeService.newStatefulKnowledgeSession(
					toBeManager.getEnvironment().getKieBase(), null, toBeManager.getEnvironment().getEnvironment());

			WorkflowProcessInstanceUpgrader.upgradeProcessInstance(tobe, Long.parseLong(in_fromProcessInstaceId),
					in_toProcessId, null);

			log.info("Migration From ProcessInstance [" + in_fromProcessInstaceId + "] To ProcessID [" + in_toProcessId
					+ "]");
			current.destroy();
			tobe.destroy();

		} catch (Exception e) {
			outcomeBuffer.append("Migration of process instance (" + in_fromProcessInstaceId + ") failed due to "
					+ e.getMessage());
			log.error("Migration of process instance ({}) failed", in_fromProcessInstaceId, e);
		}
		return outcomeBuffer.toString();
	}

	private KieRuntime extractIfNeeded(KieSession ksession) {
		if (ksession instanceof CommandBasedStatefulKnowledgeSession) {
			return ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession).getCommandService()
					.getContext()).getKieSession();
		}

		return ksession;
	}

	public boolean validate(String in_fromDepoymentId, String in_toDepoymentId, String in_fromProcessInstaceId,
			String in_toProcessId) {

		if (!RuntimeManagerRegistry.get().isRegistered(in_fromDepoymentId)) {

			return false;
		}

		if (!RuntimeManagerRegistry.get().isRegistered(in_toDepoymentId)) {

			return false;
		}

		InternalRuntimeManager manager = (InternalRuntimeManager) RuntimeManagerRegistry.get().getManager(
				in_toDepoymentId);
		if (manager.getEnvironment().getKieBase().getProcess(in_toProcessId) == null) {
			log.error("No process found for {} in deployment {}", in_toProcessId, in_toDepoymentId);

			return false;
		}

		String auditPu = manager.getDeploymentDescriptor().getAuditPersistenceUnit();

		EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(auditPu);

		JPAAuditLogService auditService = new JPAAuditLogService(emf);
		ProcessInstanceLog procLog = auditService.findProcessInstance(Long.valueOf(in_fromProcessInstaceId));
		if (procLog == null || procLog.getStatus() != ProcessInstance.STATE_ACTIVE) {
			log.error("No process instance found or it is not active (id {} in status {}",
					Long.valueOf(in_fromProcessInstaceId), (procLog == null ? "-1" : procLog.getStatus()));

			return false;
		}
		auditService.dispose();

		return true;
	}
}
