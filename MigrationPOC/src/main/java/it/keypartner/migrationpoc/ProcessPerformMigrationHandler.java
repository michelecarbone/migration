package it.keypartner.migrationpoc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.WorkflowProcessInstanceUpgrader;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.WorkflowProcess;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ProcessPerformMigrationHandler implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(ProcessPerformMigrationHandler.class);

	@Inject
	RuntimeDataService runtimeDataService;

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		log.info("ProcessPerformMigrationHandler ..... CDI??");

		String in_fromDepoymentId = (String) workItem.getParameter("in_fromDepoymentId");
		String in_toDepoymentId = (String) workItem.getParameter("in_toDepoymentId");
		String in_fromProcessInstaceId = (String) workItem.getParameter("in_fromProcessInstaceId");
		String in_toProcessId = (String) workItem.getParameter("in_toProcessId");
		System.out.println(runtimeDataService);
		findAllProcess(in_fromDepoymentId);
		log.info("ProcessPerformMigrationHandler .. IN Action");

		// log.info("Validate ?? "+ validate(in_fromDepoymentId,
		// in_toDepoymentId, in_fromProcessInstaceId, in_toProcessId));
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

			// KieSession current =
			// JPAKnowledgeService.newStatefulKnowledgeSession(currentManager.getEnvironment()
			// .getKieBase(), null,
			// currentManager.getEnvironment().getEnvironment());
			//
			KieSession tobe = JPAKnowledgeService.newStatefulKnowledgeSession(
					toBeManager.getEnvironment().getKieBase(), null, toBeManager.getEnvironment().getEnvironment());

			// KieSession tobe =
			// currentManager.getRuntimeEngine(EmptyContext.get()).getKieSession();

			log.info("MIGRO...");
			WorkflowProcessInstanceUpgrader.upgradeProcessInstance(extractIfNeeded(tobe),
					Long.parseLong(in_fromProcessInstaceId), in_toProcessId, null);

			// upgradeProcessInstance(extractIfNeeded(current),
			// extractIfNeeded(tobe),
			// Long.valueOf(in_fromProcessInstaceId), in_toProcessId, null);

			log.info("Migration From ProcessInstance -> [" + in_fromProcessInstaceId + "] To ProcessID ["
					+ in_toProcessId + "]");

			// current.dispose();

			outcomeBuffer.append("Migration  of process instance (" + in_fromProcessInstaceId
					+ ") completed successfully to process " + in_toProcessId);

		} catch (Exception e) {
			outcomeBuffer.append("Migration of process instance (" + in_fromProcessInstaceId + ") failed due to "
					+ e.getMessage());
			log.error("Migration of process instance ({}) failed", in_fromProcessInstaceId, e);
		}
		return outcomeBuffer.toString();
	}

	private static void upgradeProcessInstance(KieRuntime oldkruntime, KieRuntime kruntime, long processInstanceId,
			String processId, Map<String, String> nodeMapping) {
		if (nodeMapping == null) {
			nodeMapping = new HashMap<String, String>();
		}
		WorkflowProcessInstanceImpl processInstance = (WorkflowProcessInstanceImpl) oldkruntime
				.getProcessInstance(processInstanceId);
		if (processInstance == null) {
			throw new IllegalArgumentException("Could not find process instance " + processInstanceId);
		}
		if (processId == null) {
			throw new IllegalArgumentException("Null process id");
		}
		WorkflowProcess process = (WorkflowProcess) kruntime.getKieBase().getProcess(processId);
		if (process == null) {
			throw new IllegalArgumentException("Could not find process " + processId);
		}
		if (processInstance.getProcessId().equals(processId)) {
			return;
		}
		synchronized (processInstance) {
			org.kie.api.definition.process.Process oldProcess = processInstance.getProcess();
			processInstance.disconnect();
			processInstance.setProcess(oldProcess);
			updateNodeInstances(processInstance, nodeMapping, (NodeContainer) process);
			processInstance.setKnowledgeRuntime((InternalKnowledgeRuntime) kruntime);
			processInstance.setProcess(process);
			processInstance.reconnect();
		}
	}

	private KieRuntime extractIfNeeded(KieSession ksession) {
		if (ksession instanceof CommandBasedStatefulKnowledgeSession) {
			return ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession).getCommandService()
					.getContext()).getKieSession();
		}

		return ksession;
	}

	private static void updateNodeInstances(NodeInstanceContainer nodeInstanceContainer,
			Map<String, String> nodeMapping, NodeContainer nodeContainer) {
		if (nodeMapping == null || nodeMapping.isEmpty()) {
			return;
		}
		for (NodeInstance nodeInstance : nodeInstanceContainer.getNodeInstances()) {
			String oldNodeId = (String) ((NodeImpl) ((org.jbpm.workflow.instance.NodeInstance) nodeInstance).getNode())
					.getMetaData().get("UniqueId");
			String newNodeId = nodeMapping.get(oldNodeId);
			if (newNodeId == null) {
				continue;
			}
			Long upgradedNodeId = findNodeIfByUniqueId(newNodeId, nodeContainer);
			if (upgradedNodeId == null) {
				try {
					upgradedNodeId = Long.parseLong(newNodeId);
				} catch (NumberFormatException e) {
					continue;
				}
			}

			((NodeInstanceImpl) nodeInstance).setNodeId(upgradedNodeId);
			if (nodeInstance instanceof NodeInstanceContainer) {
				updateNodeInstances((NodeInstanceContainer) nodeInstance, nodeMapping, nodeContainer);
			}
		}

	}

	private static Long findNodeIfByUniqueId(String uniqueId, NodeContainer nodeContainer) {
		Long result = null;

		for (Node node : nodeContainer.getNodes()) {
			if (uniqueId.equals(node.getMetaData().get("UniqueId"))) {
				return node.getId();
			}
			if (node instanceof NodeContainer) {
				result = findNodeIfByUniqueId(uniqueId, (NodeContainer) node);
				if (result != null) {
					return result;
				}
			}
		}

		return result;
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

	private void findAllProcess(String in_fromDepoymentId) {
		InternalRuntimeManager manager = (InternalRuntimeManager) RuntimeManagerRegistry.get().getManager(
				in_fromDepoymentId);

		String auditPu = manager.getDeploymentDescriptor().getAuditPersistenceUnit();

		EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(auditPu);

		JPAAuditLogService auditService = new JPAAuditLogService(emf);
		List<ProcessInstanceLog> listProcInstance = auditService.findActiveProcessInstances();
		log.info("Found " + listProcInstance.size());
		for (ProcessInstanceLog logProc : listProcInstance) {
			log.info(logProc.toString());
		}

		auditService.dispose();
	}
}
