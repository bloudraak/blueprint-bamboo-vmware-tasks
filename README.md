Bamboo VMware Tasks
=============================

Contains tasks for Atlassian Bamboo to start, revert, shutdown and power off a virtual machine.

Runtime Requirements

1. VMware vSphere 5.1 host. The free edition will not work.
2. Atlassian Bamboo

Development Requirements
2. Atlassian Plugin SDK installed and configured 

Development Tasks

In order to run and test the

	atlas-run

Bamboo Tasks

- Start VMware Virtual Machine. Powers on a virtual machine and waits for the guest to respond
- Power Off VMware Virtual Machine. Powers off a virtual machine without shutting down the guest.
- Shutdown VMware Virtual Machine. Shuts down the guest of the virtual machine. The task waits for the machine to be in powered off state before it begins.
- Revert VMware Virtual Machine. Reverts a virtual machine a given snapshot, or the current snapshot if none is specified.

Passwords are encrypted in the database.
 