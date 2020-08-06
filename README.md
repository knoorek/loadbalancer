# loadbalancer

A sample loadbalancer. 'concept' package contains its basic tested logic.
There are two load balancing 'LoadBalancingStrategy' implementations ('RoundRobin' and 'LoadBased') that select 'TargetInstance' to use for 'Payload' handling.

There's also 'AbstractThreadPoolBased' 'TargetInstance' implementation which uses a limited size thread pool for 'Payload' handling.

'example' package contains 'Example' application that uses 'ClientServer' 'TargetInstance' implementation to connect to 'TestServer'.
