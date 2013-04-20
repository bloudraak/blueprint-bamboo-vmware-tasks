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
 
public class RevertVirtualMachineTask implements TaskType
{
    @Override
    public TaskResult execute(final TaskContext taskContext) throws TaskException
    {
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		final String server = taskContext.getConfigurationMap().get("server");
		final String username = taskContext.getConfigurationMap().get("username");
		final String password = taskContext.getConfigurationMap().get("password");
		final String name = taskContext.getConfigurationMap().get("name");
		final String snapshotname = taskContext.getConfigurationMap().get("snapshotName");
		
		buildLogger.addBuildLogEntry("Starting the virtual machine '" + name + "' on '" + server + "' using username '" + username + "'");
		try {
			buildLogger.addBuildLogEntry("Connecting to server '" + server + "' using username '" + username + "'.");
			ServiceInstance serviceInstance = new ServiceInstance(new URL(server), username, password, true);
			buildLogger.addBuildLogEntry("Connected to server '" + server + "' using username '" + username + "'.");
			try {
				Folder rootFolder = serviceInstance.getRootFolder();
		    	InventoryNavigator inventory = new InventoryNavigator(rootFolder);
		    	VirtualMachine vm = (VirtualMachine)inventory.searchManagedEntity("VirtualMachine", name);
				if (vm == null) {
					throw new VirtualMachineNotFoundException("The virtual machine with name '" + name + "' could not be found");
				}
				buildLogger.addBuildLogEntry("Found the virtual machine '" + name + "' on '" + server + "' using username '" + username + "'");
				
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
			}
			finally {
				serviceInstance.getServerConnection().logout();
			}
		}
		catch(Exception exception) {
			throw new TaskException("Failed to start virtual machine '" + name + "' on '" + server + "' using username '" + username + "'", exception);
		}
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