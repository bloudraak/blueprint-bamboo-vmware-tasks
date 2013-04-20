/*
 * Copyright (c) 2013, Werner Strydom
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Werner Strydom nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL WERNER STRYDOM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.blueprints.bamboo.plugins;

import java.net.URL;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.security.EncryptionService;
 
public class RevertVirtualMachineTask extends VirtualMachineTaskType
{
	public RevertVirtualMachineTask(EncryptionService encryptionService)
    {
        super(encryptionService);
    }

    @Override
	protected TaskResult execute(final TaskContext taskContext, final VirtualMachine vm) throws Throwable
	{
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		final String name = vm.getName();
		final String snapshotname = taskContext.getConfigurationMap().get("snapshotName");
		
		if (snapshotname == null || "".equals(snapshotname)) {
			buildLogger.addBuildLogEntry("No snapshot name was specified. The virtual machine '" + name + "' will be reverted to the current snapshot");
			Task task = vm.revertToCurrentSnapshot_Task(null);
			if(task.waitForMe()==Task.SUCCESS)
			{
				buildLogger.addBuildLogEntry("Reverted the virtual machine '" + name + "' to the current snapshot");
			}
			else {
				buildLogger.addBuildLogEntry("Failed to revert the virtual machine '" + name + "' to the current snapshot");
			}
		}
		else {
			VirtualMachineSnapshot vmsnap = this.getSnapshotInTree(vm, snapshotname);
			if(vmsnap != null) {
				Task task = vmsnap.revertToSnapshot_Task(null);
				if(task.waitForMe()==Task.SUCCESS) {
					buildLogger.addBuildLogEntry("Reverted the virtual machine '" + name + "' to the snapshot '" + snapshotname + "'");
				}
				else {
					buildLogger.addBuildLogEntry("Failed to revert the virtual machine '" + name + "' to the snapshot '" + snapshotname + "'");
				}
			}
		}
		buildLogger.addBuildLogEntry("The guest of the virtual machine '" + name + "' is ready to be used.");
		return TaskResultBuilder.create(taskContext).success().build();
	}
	
	private VirtualMachineSnapshot getSnapshotInTree(VirtualMachine vm, String snapName)
	{
		if (vm == null || snapName == null) 
		{
			return null;
		}

		VirtualMachineSnapshotTree[] snapTree = vm.getSnapshot().getRootSnapshotList();
		if(snapTree==null)
		{
			return null;
		}

		ManagedObjectReference mor = findSnapshotInTree(snapTree, snapName);
		if(mor == null) {
			return null;
		}
			
		return new VirtualMachineSnapshot(vm.getServerConnection(), mor);
	}
	
	private ManagedObjectReference findSnapshotInTree(VirtualMachineSnapshotTree[] snapTree, String snapName)
	{
		for(int i=0; i <snapTree.length; i++) 
		{
			VirtualMachineSnapshotTree node = snapTree[i];
			if(snapName.equals(node.getName())) {
				return node.getSnapshot();
			} 
			
			VirtualMachineSnapshotTree[] childTree = node.getChildSnapshotList();
			if (childTree == null) {
				continue;
			}
			
			ManagedObjectReference mor = findSnapshotInTree(childTree, snapName);
			if (mor != null) {
				return mor;
			}
		}
		return null;
	}
}