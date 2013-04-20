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
				buildLogger.addBuildLogEntry("Disconnecting from server '" + server + "'.");
				serviceInstance.getServerConnection().logout();
				buildLogger.addBuildLogEntry("Disconnected from server '" + server + "'.");
			}
		}
		catch(Exception exception) {
			throw new TaskException("Failed to start virtual machine '" + name + "' on '" + server + "' using username '" + username + "'", exception);
		}
		return TaskResultBuilder.create(taskContext).success().build();
    }
}