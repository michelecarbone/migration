package it.keypartner.bpmigration.dao;

import it.keypartner.bpmigration.handler.AllProcessInstanceHandler;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessManageDAO {

	private static final Logger log = LoggerFactory.getLogger(AllProcessInstanceHandler.class);

	public List<ProcessInstanceLog> retriveActiveProcessInstance(String deploymentID) {
		log.info("retriveActiveProcessInstance for DEP-ID [" + deploymentID + "]...");
		List<ProcessInstanceLog> instanceLogList = new ArrayList<>();
		try {
			InternalRuntimeManager manager = (InternalRuntimeManager) RuntimeManagerRegistry.get().getManager(
					deploymentID);
			String auditPu = manager.getDeploymentDescriptor().getAuditPersistenceUnit();
			EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(auditPu);
			JPAAuditLogService auditService = new JPAAuditLogService(emf);
			instanceLogList = auditService.findActiveProcessInstances();
			log.info("retriveActiveProcessInstance N." + instanceLogList.size()
					+ " Active Process Instance for DEP-ID [" + deploymentID + "]...");
			auditService.dispose();
		} catch (Exception exception) {
			log.error("EXCEPTION IN FIND ACTIVE PROCESS INSTANCE FOR DEPLOYMENTID [" + deploymentID + "]", exception);
		}
		return instanceLogList;
	}

}
