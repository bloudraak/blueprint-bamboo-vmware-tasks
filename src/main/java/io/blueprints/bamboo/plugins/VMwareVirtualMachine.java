package io.blueprints.bamboo.plugins;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.SnapshotFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.ToolsUnavailable;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VirtualMachineSnapshot;

public class VMwareVirtualMachine {

	private ServiceInstance _serviceInstance;
	private VirtualMachine _inner;

	public VMwareVirtualMachine(URL uri, String name, String username,
			String password) throws RemoteException, MalformedURLException {

		ServiceInstance serviceInstance = new ServiceInstance(uri, username,
				password, true);

		VirtualMachine vm = null;
		try {

			Folder rootFolder = serviceInstance.getRootFolder();
			ManagedEntity[] mes = rootFolder.getChildEntity();

			for (int i = 0; i < mes.length; i++) {
				if (!(mes[i] instanceof Datacenter)) {
					continue;
				}

				Datacenter dc = (Datacenter) mes[i];
				Folder vmFolder = dc.getVmFolder();
				ManagedEntity[] vms = vmFolder.getChildEntity();

				for (int j = 0; j < vms.length; j++) {
					if (!(vms[j] instanceof VirtualMachine)) {
						continue;
					}

					vm = (VirtualMachine) vms[j];
					if (!name.equalsIgnoreCase(name)) {
						continue;
					}
				}
			}
		} catch (RemoteException e) {
			serviceInstance.getServerConnection().logout();
			throw e;
		}

		_serviceInstance = serviceInstance;
		_inner = vm;

	}

	public void revertToSnapshot(String snapshotName) throws VmConfigFault,
			TaskInProgress, FileFault, InvalidState,
			InsufficientResourcesFault, RuntimeFault, RemoteException {
		VirtualMachineSnapshotTree[] rootSnapshotList = _inner.getSnapshot()
				.getRootSnapshotList();
		VirtualMachineSnapshotTree snapshotTree = findInSnapshotTree(
				snapshotName, rootSnapshotList);
		if (snapshotTree != null) {
			VirtualMachineSnapshot snapshot = new VirtualMachineSnapshot(
					_serviceInstance.getServerConnection(),
					snapshotTree.getSnapshot());
			Task task = snapshot.revertToSnapshot_Task(null);
			task.waitForMe();
		} else {
			// throw SnapshotNotFoundException
		}
	}

	private static VirtualMachineSnapshotTree findInSnapshotTree(
			String snapshotName, VirtualMachineSnapshotTree[] rootSnapshotList) {
		if (rootSnapshotList != null) {
			for (int i = 0; i < rootSnapshotList.length; i++) {
				VirtualMachineSnapshotTree virtualMachineSnapshotTree = rootSnapshotList[i];
				String name = virtualMachineSnapshotTree.getName();
				if (snapshotName.equals(name)) {
					return virtualMachineSnapshotTree;
				} else {
					VirtualMachineSnapshotTree snapshot = findInSnapshotTree(
							snapshotName,
							virtualMachineSnapshotTree.childSnapshotList);
					if (snapshot != null) {
						return snapshot;
					}
				}
			}
		}
		return null;
	}

	public void disconnect() {
		_serviceInstance.getServerConnection().logout();
	}

	public void start() throws VmConfigFault, TaskInProgress, FileFault,
			InvalidState, InsufficientResourcesFault, RuntimeFault,
			RemoteException {

		VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) this._inner
				.getRuntime();
		if (vmri.getPowerState() == VirtualMachinePowerState.poweredOff) {
			Task task = this._inner.powerOnVM_Task(null);
			task.waitForMe();
		}
	}

	public void waitForGuest() throws InvalidProperty, RuntimeFault,
			RemoteException, InterruptedException {
		// wait for 5 minutes
		waitForGuest(60 * 1000 * 5, true);
	}

	public void waitForGuest(long timeout, boolean waitForGuestHostname)
			throws InterruptedException, InvalidProperty, RuntimeFault,
			RemoteException {
		VirtualMachine vm = this._inner;
		while (!isGuestToolsRunning() && timeout > 0) {
			Thread.sleep(500);
			timeout -= 500;
		}

		if (timeout <= 0) {
			throw new InterruptedException(
					"The guest tools did not startup in '" + timeout + "ms'");
		}

		if (waitForGuestHostname) {
			vm.getResourcePool();
			String guestHostName = null;
			do {
				guestHostName = vm.getGuest().getHostName();
				if ("".equals(guestHostName)) {
					Thread.sleep(1000);
					timeout -= 1000;
				}
			} while ((guestHostName == null || "".equals(guestHostName)) && timeout > 0);
		}
	}
	
	public String getGuestIpAddress() {
		VirtualMachine vm = this._inner;
		return vm.getGuest().getIpAddress();
	}
	
	public boolean isInteractiveGuestOperationsReady() {
		VirtualMachine vm = this._inner;
		return vm.getGuest().getInteractiveGuestOperationsReady();
	}
	
	public String getGuestHostName() {
		VirtualMachine vm = this._inner;
		return vm.getGuest().getHostName();
	}

	public boolean isGuestToolsRunning() {
		VirtualMachine vm = this._inner;
		return vm.getGuest().getToolsRunningStatus()
				.equals("guestToolsRunning");
	}
	
	public void takeSnapshot(String name) throws InvalidName, VmConfigFault, SnapshotFault, TaskInProgress, FileFault, InvalidState, RuntimeFault, RemoteException {
		takeSnapshot(name, null);
	}

	public void takeSnapshot(String name, String description) throws InvalidName, VmConfigFault, SnapshotFault, TaskInProgress, FileFault, InvalidState, RuntimeFault, RemoteException {
		System.out.println(name);	
		VirtualMachine vm = this._inner;
		Task task = vm.createSnapshot_Task(name, description, true, true);
		task.waitForMe();	
	}

	public void shutdown() throws TaskInProgress, InvalidState, ToolsUnavailable, RuntimeFault, RemoteException, InterruptedException {
		shutdown(5*60*1000);
	}
	
	public void shutdown(long timeout) throws TaskInProgress, InvalidState, ToolsUnavailable, RuntimeFault, RemoteException, InterruptedException {
		VirtualMachine vm = this._inner;
		
		vm.shutdownGuest();
		VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) this._inner.getRuntime();
		while (vmri.getPowerState() != VirtualMachinePowerState.poweredOff) {
			Thread.sleep(1000);
			timeout -= 1000;
			 vmri = (VirtualMachineRuntimeInfo) this._inner.getRuntime();
		}
	}
	
	public void powerOff() throws TaskInProgress, InvalidState, RuntimeFault, RemoteException {
		VirtualMachine vm = this._inner;
		Task task = vm.powerOffVM_Task();
		task.waitForMe();	
	}
}
