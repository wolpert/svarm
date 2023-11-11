# Local Queue

The queue provides for a durable local queue usable by the process itself. It
allows for actions to be executed and ensures they are complete even through a
service restart. It is not intended for high-velocity calls, but rather
maintenance threads.

It's a simple utility for the local instance and should not be used in any
distributed fashion.

## Queue States

* Pending - The message is waiting to be picked up by the queue processor.
* Activating - The message is enqueued by the runtime executor.
* Processing - The message is being processed by the runtime executor.

## How it works

1. A table exists for active requests which is what makes it durable.
2. Message consumers are registered with the queue which handle messages based
   on message type.
3. Messages are sent to the queue which stores the message in the database with
   the Pending state.
4. The queue processor looks for pending messages, changes their state to
   activating, and sends them to the executor.
    * If there is no message consumer for the message type, the executor emits
      an error and deletes the request.
5. When the executor pool queues the message and when its active, changes the
   state tp processing.
6. When the executor completes the message, it deletes the request.

## On start up

The queue processor converts all Activating and Processing messages to Pending.
Then during the normal cycle, will start processing the active messages.
