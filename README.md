= loadbalancer

A sample loadbalancer. 'concept' package contains its basic tested logic.
There are two load balancing 'LoadBalancingStrategy' implementations ('RoundRobin' and 'LoadBased') that select 'TargetInstance' to use for generic 'Payload' handling.

There's also 'ConcurrentBase' 'TargetInstance' implementation which uses a limited size thread pool for generic 'Payload' handling.
It is extended by:

* 'ConcurrentExtendable' which may be further extended to handle various payloads in various ways (favouring inheritance).
* 'ConcurrentTaskAccepting' which accepts payloads as 'tasks' (favouring composition).

'example' package contains 'Example' application that uses 'ClientServer' 'TargetInstance' implementation to connect to 'TestServer'.