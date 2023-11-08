# Local Queue

The queue provides for a durable local queue usable by the process itself.
It allows for actions to be executed and ensures they are complete even through a service restart.
It is not intended for high-velocity calls, but rather maintenance threads.

It's a simple utility for the local instance and should not be used in any distributed fashion.