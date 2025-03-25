# Status

Status is defined as current realtime state of a set of values. This can range
from anything like connectivitiy to the cloud or what the charging condition is
on a given robot.

## Format

Endpoints that provide status are generally named with a pattern that consists
of `Subscribe` as a prefix and ends in `Status` e.g.`SubscribeFooStatus`). There
is a subtle but distinct difference in usage for Status vs State.

Status represents the current snapshot of a state on the robot. This is why it
is only associated with the Subscribe RPC endpoint.

State is a snapshot of a set of a set of values at _some_ point in time.
Messages that carry these values are named state as the message itself.
