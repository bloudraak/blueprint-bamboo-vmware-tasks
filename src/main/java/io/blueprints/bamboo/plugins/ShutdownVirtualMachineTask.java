package io.blueprints.bamboo.plugins;

import java.net.URL;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
 
public class ShutdownVirtualMachineTask implements TaskType
{
    @Override
    public TaskResult execute(final TaskContext taskContext) throws TaskException
    {
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		final String server = taskContext.getConfigurationMap().get("server");
		final String username = taskContext.getConfigurationMap().get("username");
		final String password = taskContext.getConfigurationMap().get("password");
		final String name = taskContext.getConfigurationMap().get("name");
		buildLogger.addBuildLogEntry("Shutting down the virtual machine '" + name + "' on '" + server + "' using username '" + username + "'");
		try {
			buildLogger.addBuildLogEntry("Connecting to server '" + server + "' using username '" + username + "'.");
			ServiceInstance serviceInstance = new ServiceInstance(new URL(server), username, password, true);
			buildLogger.addBuildLogEntry("Connected to server '" + server + "' using username '" + username + "'.");
			try {
				Folder rootFolder = serviceInstance.getRootFolder();
		    	InventoryNavigator inventory = new InventoryNavigator(rootFolder);
		    	VirtualMachine vm = (VirtualMachine)inventory.searchManagedEntity("VirtualMachine", name);
				if (vm == null) {
					throw new VirtualMachineNotFoundException("The virtußal machine with name '" + name + "' could not be found");
				}

				buildLogger.addBuildLogEntry("Found the virtual machine '" + name + "' on '" + server + "' using username '" + username + "'");
				VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
				if (vmri.getPowerState() == VirtualMachinePowerState.poweredOff) {
					buildLogger.addBuildLogEntry("The virtual machine '" + name + "' is already in a powered off state.");
					return TaskResultBuilder.create(taskContext).success().build();
				}
				
				buildLogger.addBuildLogEntry("Requesting that the guest of virtual machine '" + name + "' shutdown.");
				
				int timeout = 1000 * 60 * 5;
				int ms = timeout;
				vm.shutdownGuest();
				vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
				while (vmri.getPowerState() != VirtualMachinePowerState.poweredOff && ms > 0) {
					buildLogger.addBuildLogEntry("Waiting for the guest of virtual machine '" + name + "' to shutdown and power off the machine.");
					Thread.sleep(5000);
					ms -= 5000;
					vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
				}
					
				buildLogger.addBuildLogEntry("The virtual machine '" + name + "' has was succesfully shutdown and powered off.");
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
}