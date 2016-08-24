package it.keypartner.bpmigration.dao;

import it.keypartner.bpmigration.handler.AllProcessInstanceHandler;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.internal.query.ParametrizedQuery;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessManageDAO {

	private static final Logger log = LoggerFactory.getLogger(AllProcessInstanceHandler.class);

	public List<ProcessInstanceLog> retriveActiveProcessInstance(String deploymentID, String processId) {
		log.info("retriveActiveProcessInstance for PROC-ID [" + processId + "] DEP-ID [" + deploymentID + "]...");
		List<ProcessInstanceLog> instanceLogList = new ArrayList<>();
		try {
			JPAAuditLogService auditService = getJPAAuditLogService(deploymentID);
			instanceLogList = auditService.findActiveProcessInstances(processId);
			log.info("retriveActiveProcessInstance N." + instanceLogList.size()
					+ " Active Process Instance for PROC-ID [" + processId + "] DEP-ID [" + deploymentID + "]...");
			printProcInstanceLog(instanceLogList);
			auditService.dispose();
		} catch (Exception exception) {
			log.error("EXCEPTION IN FIND ACTIVE PROCESS INSTANCE FOR PROC-ID [" + processId + "] AND DEPLOYMENTID ["
					+ deploymentID + "]", exception);
		}
		return instanceLogList;
	}

	public List<ProcessInstanceLog> retriveActiveProcessInstanceWithVar(String deploymentID, String processID,
			String varName, Object varValue) {
		log.info("retriveActiveProcessInstance for PROC-ID [" + processID + "]...");
		List<ProcessInstanceLog> instanceLogList = new ArrayList<>();
		List<ProcessInstanceLog> instanceLogListWithValue = new ArrayList<>();
		try {
			JPAAuditLogService auditService = getJPAAuditLogService(deploymentID);
			instanceLogList = auditService.findActiveProcessInstances(processID);

			for (ProcessInstanceLog processInstanceLog : instanceLogList) {
				Long procInstanceId = processInstanceLog.getProcessInstanceId();
				// Query parametrica per cercare le variabili (piu' recentemente
				// aggiornate) per deifinito Process Instance ID
				ParametrizedQuery<org.kie.api.runtime.manager.audit.VariableInstanceLog> varQuery = auditService
						.variableInstanceLogQuery().last().intersect().processInstanceId(procInstanceId).build();
				List<org.kie.api.runtime.manager.audit.VariableInstanceLog> varLogsList = new ArrayList<>();
				try {
					varLogsList = varQuery.getResultList();
					for (org.kie.api.runtime.manager.audit.VariableInstanceLog infoLastVar : varLogsList) {
						if (infoLastVar.getVariableId() != null && infoLastVar.getVariableId().compareTo(varName) == 0) {
							log.info("Found var name [" + varName + "] for Process Instance ID [" + procInstanceId
									+ "] With Value [" + varValue + "]");
							if (varValue.equals(infoLastVar.getValue())) {
								instanceLogListWithValue.add(processInstanceLog);
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

		} catch (Exception exception) {
			log.error("EXCEPTION IN FIND ACTIVE PROCESS INSTANCE FOR DEPLOYMENTID [" + deploymentID + "]", exception);
		}
		log.info("Found N. " + instanceLogListWithValue.size() + " With Var [" + varName + "] and Value [" + varValue
				+ "]");
		printProcInstanceLog(instanceLogListWithValue);
		return instanceLogListWithValue;
	}

	/*
	 * UTILITY
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
			log.info("ProcessId [" + instanceLog.getProcessId() + "] - ProcessInstanceId ["
					+ instanceLog.getProcessInstanceId() + "] - Status [" + instanceLog.getStatus() + "] - Start ["
					+ instanceLog.getStart() + "] - ExternalId [" + instanceLog.getExternalId() + "] - Duration ["
					+ instanceLog.getDuration() + "] - Outcome [" + instanceLog.getOutcome() + "] - CorrelationKey ["
					+ instanceLog.getCorrelationKey() + "]");
		}
	}
}
