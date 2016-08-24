package it.keypartner.bpmigration;

import java.util.Date;

public class ProcessVarToMigrate extends ProcessToMigrate {

	private static final long serialVersionUID = 1L;
	private String varName;
	private String varValue;
	private Date modifiedData;

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getVarValue() {
		return varValue;
	}

	public void setVarValue(String varValue) {
		this.varValue = varValue;
	}

	public Date getModifiedData() {
		return modifiedData;
	}

	public void setModifiedData(Date modifiedData) {
		this.modifiedData = modifiedData;
	}

	public ProcessVarToMigrate(long processInstanceId, String toProcessId, String fromProcessId,
			String fromDeploymentId, String toDeploymentId, Date processInstanceStartDate, String processName,
			String varName, String varValue, Date modifiedData) {
		super(processInstanceId, toProcessId, fromProcessId, fromDeploymentId, toDeploymentId,
				processInstanceStartDate, processName);
		this.varName = varName;
		this.varValue = varValue;
		this.modifiedData = modifiedData;
	}

	@Override
	public String toString() {
		return "ProcessVarToMigrate [varName=" + varName + ", varValue=" + varValue + ", modifiedData=" + modifiedData
				+ "]";
	}

}
