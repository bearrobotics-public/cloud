# Localization

![Robot localization](../assets/localization-pose.jpg)

Localization status is to see whether the robot knows where it is in the map.
This is critical for operation, as the robot needs to have an accurate
understanding of its place in the environment in order to navigate reliably.

Once localized, our robots are able to keep themselves localized as they move
through an environment, using sensor data to compare what it sees to what is on
its map. This means it only needs to figure out where it is once, and can stay
localized from there. A robot may become delocalized, meaning it has lost track
of where it is in the map. This can happen for various reasons, such as:

1. It cannot find walls or landmarks from its map in the sensor data
    - Often because walls are obscured by lots of new obstacles
2. It encounters an area that looks very similar to many other locations in the
   map
    - For example, a long featureless hallway looks the same to the sensors from
      many different places
3. It faces significant slippage of the wheels
    - This is more rare as we have compensation for wheel slip, but if
      significant enough, it can confuse the robot

In the event of delocalizing, the user will need to trigger localization again
to tell the robot where it is, before having it navigate again.

!!! warning
    When a robot becomes **delocalized**, mission commands will fail until the robot is successfully localized again. Monitoring localization status during autonomous operations will be helpful.

Some clear definitions for localization terminology are as follows:

1. **Localized**
    - Robot knows where it is in the environment
2. **Localizing**
    - Robot is currently running a process to figure out where it is
    - Currently our API does this with a hint about its position, given by the
      user
    - Eventually this hint will not be required, and robot can do the initial
      localization on its own
3. **Delocalized**
    - Robot has lost track of where it is in the environment
    - Likely this means it cannot navigate anymore
    - In this event, the user will need to localize the robot again to tell the
      robot its current location

We supply a mechanism for triggering a robot localization. This means the user
tells the robot an estimation of where the robot is in the map (using an x, y coordinate-based Pose),
and the robot uses this guess to more accurately find its place.


