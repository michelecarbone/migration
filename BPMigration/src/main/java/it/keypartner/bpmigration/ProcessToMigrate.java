package it.keypartner.bpmigration;

import java.io.Serializable;
import java.util.Date;

public class ProcessToMigrate implements Serializable {

	private static final long serialVersionUID = 1L;
	private long processInstanceId;
	private java.lang.String toProcessId;
	private java.lang.String fromProcessId;
	private java.lang.String fromDeploymentId;
	private java.lang.String toDeploymentId;
	private Date processInstanceStartDate;
	private String processName;

	public java.lang.String getToProcessId() {
		return toProcessId;
	}

	public void setToProcessId(java.lang.String toProcessId) {
		this.toProcessId = toProcessId;
	}

	public java.lang.String getFromProcessId() {
		return fromProcessId;
	}

	public void setFromProcessId(java.lang.String fromProcessId) {
		this.fromProcessId = fromProcessId;
	}

	public java.lang.String getFromDeploymentId() {
		return fromDeploymentId;
	}

	public void setFromDeploymentId(java.lang.String fromDeploymentId) {
		this.fromDeploymentId = fromDeploymentId;
	}

	public java.lang.String getToDeploymentId() {
		return toDeploymentId;
	}

	public void setToDeploymentId(java.lang.String toDeploymentId) {
		this.toDeploymentId = toDeploymentId;
	}

	public Date getProcessInstanceStartDate() {
		return processInstanceStartDate;
	}

	public void setProcessInstanceStartDate(Date processInstanceStartDate) {
		this.processInstanceStartDate = processInstanceStartDate;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public ProcessToMigrate(long processInstanceId, String toProcessId, String fromProcessId, String fromDeploymentId,
			String toDeploymentId, Date processInstanceStartDate, String processName) {
		super();
		this.processInstanceId = processInstanceId;
		this.toProcessId = toProcessId;
		this.fromProcessId = fromProcessId;
		this.fromDeploymentId = fromDeploymentId;
		this.toDeploymentId = toDeploymentId;
		this.processInstanceStartDate = processInstanceStartDate;
		this.processName = processName;
	}

	@Override
	public String toString() {
		return "ProcessToMigrate [processInstanceId=" + processInstanceId + ", toProcessId=" + toProcessId
				+ ", fromProcessId=" + fromProcessId + ", fromDeploymentId=" + fromDeploymentId + ", toDeploymentId="
				+ toDeploymentId + ", processInstanceStartDate=" + processInstanceStartDate + ", processName="
				+ processName + "]";
	}

}
