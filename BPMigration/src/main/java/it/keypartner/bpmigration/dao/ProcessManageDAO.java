package it.keypartner.bpmigration.dao;

import it.keypartner.bpmigration.handler.AllProcessInstanceHandler;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.query.ParametrizedQuery;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dao to access AUDIT LOG SERVICE
 */
public class ProcessManageDAO {

	private static final Logger log = LoggerFactory.getLogger(AllProcessInstanceHandler.class);

	/**
	 * Search for defined <b> active process instance</b> with process instance
	 * ID and deploymentID (GAV)
	 * 
	 * @param deploymentID
	 * @param processInstanceId
	 * @return {@link ProcessInstanceLog} is found
	 */
	public ProcessInstanceLog retriveProcessInstance(String deploymentID, String processInstanceId) {
		log.info("RetriveProcessInstance for PROC-INSTANCE-ID [" + processInstanceId + "] DEP-ID [" + deploymentID
				+ "]...");
		ProcessInstanceLog instanceLog = null;
		try {
			JPAAuditLogService auditService = getJPAAuditLogService(deploymentID);
			ProcessInstanceLog instanceLogRetrieved = auditService.findProcessInstance(Long.valueOf(processInstanceId));
			log.info("Found ProcessInstance -> " + instanceLogRetrieved);
			if (instanceLogRetrieved != null && isWithGAV(instanceLogRetrieved, deploymentID)
					&& isWithStatus(instanceLogRetrieved, ProcessInstance.STATE_ACTIVE)) {
				instanceLog = instanceLogRetrieved;
				log.info("Return found processInstance -> " + instanceLog);
				printProcessInstance(instanceLog);
			}
			auditService.dispose();
		} catch (Exception exception) {
			log.error("EXCEPTION IN FIND ACTIVE PROCESS INSTANCE FOR PROC-INSTANCE-ID [" + processInstanceId
					+ "] AND DEPLOYMENTID [" + deploymentID + "]", exception);
		}
		return instanceLog;
	}

	/**
	 * Search for defined <b> active process instance</b> with process
	 * definition ID and deploymentID (GAV)
	 * 
	 * @param deploymentID
	 * @param processId
	 * @return {@link List} of {@link ProcessInstanceLog}
	 */
	public List<ProcessInstanceLog> retriveActiveProcessInstances(String deploymentID, String processId) {
		log.info("RetriveActiveProcessInstance for PROC-ID [" + processId + "] DEP-ID [" + deploymentID + "]...");
		List<ProcessInstanceLog> instanceLogList = new ArrayList<>();
		try {
			JPAAuditLogService auditService = getJPAAuditLogService(deploymentID);
			List<ProcessInstanceLog> foundInstanceLogList = auditService.findActiveProcessInstances(processId);
			if (foundInstanceLogList != null) {
				for (ProcessInstanceLog instanceLog : foundInstanceLogList) {
					if (isWithGAV(instanceLog, deploymentID)) {
						// Add only process instance with deploymentID
						instanceLogList.add(instanceLog);
					}
				}
			}
			auditService.dispose();
		} catch (Exception exception) {
			log.error("EXCEPTION IN FIND ACTIVE PROCESS INSTANCE FOR PROC-ID [" + processId + "] AND DEPLOYMENTID ["
					+ deploymentID + "]", exception);
		}
		log.info("RetriveActiveProcessInstance N." + instanceLogList.size() + " Active Process Instance for PROC-ID ["
				+ processId + "] DEP-ID [" + deploymentID + "]...");
		printProcInstanceLog(instanceLogList);
		return instanceLogList;
	}

	public List<ProcessInstanceLog> retriveActiveProcessInstanceWithVar(String deploymentID, String processID,
			String varName, Object varValue) {
		log.info("RetriveActiveProcessInstanceWithVar for PROC-ID [" + processID + "] DEP-ID [" + deploymentID
				+ "] VAR-NAME [" + varName + "] VAR-VAL [" + varValue + "] ...");
		List<ProcessInstanceLog> instanceLogList = new ArrayList<ProcessInstanceLog>();
		try {
			JPAAuditLogService auditService = getJPAAuditLogService(deploymentID);
			List<ProcessInstanceLog> foundInstanceLogListActiveProc = auditService
					.findActiveProcessInstances(processID);
			for (ProcessInstanceLog processInstanceLog : foundInstanceLogListActiveProc) {
				if (isWithGAV(processInstanceLog, deploymentID)) {
					Long procInstanceId = processInstanceLog.getProcessInstanceId();
					// ParametrizedQuery to search most recent varibiles
					ParametrizedQuery<org.kie.api.runtime.manager.audit.VariableInstanceLog> varQuery = auditService
							.variableInstanceLogQuery().last().intersect().processInstanceId(procInstanceId).build();
					List<org.kie.api.runtime.manager.audit.VariableInstanceLog> varLogsList = new ArrayList<>();
					try {
						varLogsList = varQuery.getResultList();
						for (org.kie.api.runtime.manager.audit.VariableInstanceLog infoLastVar : varLogsList) {
							if (infoLastVar.getVariableId() != null
									&& infoLastVar.getVariableId().compareTo(varName) == 0) {
								log.info("Found var name [" + varName + "] for Process Instance ID [" + procInstanceId
										+ "] With Value [" + varValue + "]");
								if (varValue.equals(infoLastVar.getValue())) {
									instanceLogList.add(processInstanceLog);
									log.info("MATCH OK ");
									break;
								}
							}
						}
					} catch (Exception exception) {
						log.error("EXCEPTION IN FIND VariableInstanceLog FOR Process Instance ID [" + procInstanceId
								+ "] Of Process [" + processID + "]", exception);
					}
				}
			}
			auditService.dispose();

		} catch (Exception exception) {
			log.error("EXCEPTION IN FIND ACTIVE PROCESS INSTANCE FOR DEPLOYMENTID [" + deploymentID + "]", exception);
		}
		log.info("retriveActiveProcessInstanceWithVar N. [" + instanceLogList.size() + "] With Var [" + varName
				+ "] and Value [" + varValue + "]");
		printProcInstanceLog(instanceLogList);
		return instanceLogList;
	}

	/**
	 * Verify that retrieved {@link ProcessInstanceLog} belongs to GAV of
	 * DeploymentID
	 * 
	 * @param processInstanceLog
	 * @param deploymentID
	 * @return
	 */
	private boolean isWithGAV(ProcessInstanceLog processInstanceLog, String deploymentID) {
		if (processInstanceLog != null && processInstanceLog.getExternalId() != null
				&& processInstanceLog.getExternalId().equalsIgnoreCase(deploymentID)) {
			return true;
		}
		return false;
	}

	/**
	 * Verify that retrieved {@link ProcessInstanceLog} has the desired state
	 * 
	 * @param processInstanceLog
	 * @param status
	 * @return
	 */
	private boolean isWithStatus(ProcessInstanceLog processInstanceLog, Integer status) {
		if (processInstanceLog != null && processInstanceLog.getStatus() != null
				&& processInstanceLog.getStatus().compareTo(status) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Get instance of {@link JPAAuditLogService}
	 * 
	 * @param deploymentID
	 * @return
	 */
	private JPAAuditLogService getJPAAuditLogService(String deploymentID) {
		InternalRuntimeManager manager = (InternalRuntimeManager) RuntimeManagerRegistry.get().getManager(deploymentID);
		String auditPu = manager.getDeploymentDescriptor().getAuditPersistenceUnit();
		EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(auditPu);
		JPAAuditLogService auditService = new JPAAuditLogService(emf);
		return auditService;
	}

	private void printProcInstanceLog(List<ProcessInstanceLog> instanceLogList) {
		for (ProcessInstanceLog instanceLog : instanceLogList) {
			printProcessInstance(instanceLog);
		}
	}

	private void printProcessInstance(ProcessInstanceLog instanceLog) {
		log.info("ProcessInstanceLog --> ProcessId " + instanceLog.getProcessId() + "] - ProcessInstanceId ["
				+ instanceLog.getProcessInstanceId() + "] - Status [" + instanceLog.getStatus() + "] - Start ["
				+ instanceLog.getStart() + "] - ExternalId [" + instanceLog.getExternalId() + "] - Duration ["
				+ instanceLog.getDuration() + "] - Outcome [" + instanceLog.getOutcome() + "] - CorrelationKey ["
				+ instanceLog.getCorrelationKey() + "]");
	}
}
