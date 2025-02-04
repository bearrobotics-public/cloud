
## February 5, 2025

<div class="tag-container">
  <div class="tag">Release</div>
  <div class="tag feature">Beta</div>
</div>

#### Added New Endpoints
  - [**GetCurrentMapContent**](v0.1/cloud/CloudApiService.md#getcurrentmapcontent) 
  - [**GetLocation**](v0.1/cloud/CloudApiService.md#getlocation) 
  - [**GetMap**](v0.1/cloud/CloudApiService.md#getmap) 
  - [**GetMapData**](v0.1/cloud/CloudApiService.md#getmapdata) 
  - [**SwitchMap**](v0.1/cloud/CloudApiService.md#switchmap) 
    <p class="changelog">Available only with multiple floor maps. </p>
  - [**AppendMission**](v0.1/cloud/CloudApiService.md#appendmission)
  - [**UpdateMission**](v0.1/cloud/CloudApiService.md#updatemission) 
  - [**GetRobotSystemInfo**](v0.1/cloud/CloudApiService.md#getrobotsysteminfo)

#### Extended Existing Endpoints
The following endpoints were enhanced with backward-compatible features compared to the POC version.

  - [**CreateMission**](v0.1/cloud/CloudApiService.md#createmission)
    <p class="changelog">Added support for [new mission types](v0.1/robot/Mission.md#missiontypeenum) in the request. </p>
  - [**SubscribeMissionStatus**](v0.1/cloud/CloudApiService.md#subscribemissionstatus), [**SubscribeBatteryStatus**](v0.1/cloud/CloudApiService.md#subscribebatterystatus), [**SubscribeRobotPose**](v0.1/cloud/CloudApiService.md#subscriberobotpose): 
    <p> Added [`EventMetadata`](v0.1/common/Annotations.md#eventmetadata) to the response payloads. </p>

#### Unchanged Endpoints
These endpoints remain the same as in the POC version.

  - [**ChargeRobot**](v0.1/cloud/CloudApiService.md#chargerobot) </br>
  - [**ListRobotIDs**](v0.1/cloud/CloudApiService.md#listprofileids) </br> 

