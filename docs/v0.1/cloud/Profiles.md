### Message Types

#### Profile
| Field           | Type                           | Description                   |
|-----------------|--------------------------------|-------------------------------|
| `profile_id`    | string                         |                               |
| `display_name`  | string                         |                               |
| `sound_profile` | [SoundProfile](#soundprofile)  | Profile for sound-related settings. |

#### SoundProfile
| Field      | Type                           | Description                              |
|------------|--------------------------------|------------------------------------------|
| `commands` | [Command](#command)<br>*repeated* | List of commands in the sound profile.|

#### Command
| Field         | Type                                   | Description             |
|---------------|----------------------------------------|-------------------------|
| `type`        | [Type](#commandtypeenum)<br>*enum*     |                         |
| `sound_data`  | [SoundData](#sounddata)<br>*repeated*  | List of sound files associated with the command. <br> Robot will play the sound randomly when it runs into a specific situation. |

#### SoundData
| Field          | Type   | Description                              |
|----------------|--------|------------------------------------------|
| `sound_id`     | string |        |
| `display_name` | string |        |
| `url`          | string | The URL to download the sound file from. |

#### CommandTypeEnum
| Name                          | Number | Description                 |
|-------------------------------|--------|-----------------------------|
| TYPE_UNKNOWN                 | 0      |                              |
| TYPE_HELLO                   | 1      |                              |
| TYPE_ARRIVED_HOSTING         | 2      |                              |
| TYPE_LEAVING_HOSTING         | 3      |                              |
| TYPE_TAKE_FOOD               | 4      |                              |
| TYPE_ENJOY                   | 5      | A sound is played when the robot unloads and returns to the return point.                        |
| TYPE_LOAD_DISHES             | 6      |                              |
| TYPE_THANK_YOU               | 7      |                              |
| TYPE_ARRIVED_PATROL_SERVING  | 8      |                              |
| TYPE_LEAVING_PATROL_SERVING  | 9      |                              |
| TYPE_ARRIVED_PATROL_BUSSING  | 10     |                              |
| TYPE_LEAVING_PATROL_BUSSING  | 11     |                              |
| TYPE_ARRIVED_BIRTHDAY        | 12     |                              |
| TYPE_ARRIVED_ANNIVERSARY     | 13     |                              |
| TYPE_ARRIVED_CELEBRATION     | 14     |                              |
| TYPE_ARRIVED_CUSTOM          | 15     | A sound that the user can manually trigger when the robot arrives at a destination.              |
| TYPE_EXCUSE_ME               | 16     |                              |
| TYPE_HAPPY_BIRTHDAY          | 17     | A sound made when arriving at the destination in birthday mode.|
| TYPE_HAPPY_BIRTHDAY_MOVEMENT | 18     | A sound made when moving in birthday mode.                    |
| TYPE_SOUND_ZONE              | 19     | A sound that continues to be made while the robot is within the sound zone until it leaves.      |
| TYPE_DISINFECT_START         | 20     |                              |
| TYPE_DISINFECT_CANCELLED     | 21     |                              |
| TYPE_DISINFECT_FINISHED      | 22     |                              |
| TYPE_DISINFECT_RETURNED      | 23     |                              |
