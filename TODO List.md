
### TODO List ideas:

- refactor the code so that the app path and package is ca.team2706.scouting.mcmergemanager ... this might be hard
- get the Android Back button to work
- Include other statistics to team page:
  - Defensive Power Rating: DPR is calculated just like OPR, except you use the opposing alliance's score instead of your own alliance's score.
  - Calculated Contribution to Winning Margin: CCWM is like OPR & DPR, but instead of the raw scores, you use the point difference (aka winning margin)
  - Projected Seed (based on simulating all future matches)
  - A button to show all matches that team played (including future match predictions, in a different colour)
- on home screen: show Tournament Seedings (current & predicted)
- Schedule search: given a team number, when is their next match?
  - possibly set system alarms w/ notifications for important matches?
- be able to auto-fetch the match data from somewhere on the internet
- use bluetooth to share data between devices running the app (so that as long as one phone in the cluster has internet, they all can get live match results / upload data)
- upload data to a server so that the drive team has live scouting data (possible through Google Drive API?)
- Team Photos:
  - is there a more effective way to load them into the app? Maybe also a Google Drive API?
  - More than one photo per team?
  - pinch zoom
- Easier client side loading(fishing)
- Download and process an excel file of when and who matches to automatically set up teams and game numbers?
- After autonomous start time at 15 instead of 0 in UI but not in background
- Bluetooth sending groups protected by password


UI:
  - Convert to material design(side bar, double tab for scout red scout blue)
  - figure out how to make the "Synced to Drive" label go away when in the Team Info tab (to save screen space)



### Stuff that's done:

- [Mike] change the app name to MC Merge Manager
- [Mike] change the logo to to Merge Conflict logo
- [Mike] Set up settings
