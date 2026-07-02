# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- WasmJS: Fixed crash due to updates in Promise interop with Kotlin (#310)

## [0.9.1] - 2026-06-17

### Added

- Caching of map tiles for faster loading (#298)

### Changed

- Use last known user location as map default centroid (#300)
- Updated dependencies to latest stable versions (#302)

### Fixed

- Deleting a measurement sometimes wouldn't exit the details screen (#303)
- Fixed incorrect measurement average level due to rounding error (#304)

## [0.9.0] - 2026-05-13

### Added

- Visual hint of GPS accuracy while recording (#278)
- Measurement GeoJson export (#279, #290)
- Onboarding flow (#282, #293)
- Added wiki and issue reporting capabilities (#294)

### Fixed

- Android: switching location on or off from control center didn't trigger a state refresh in the
  app (#278)
- A measurement with empty or single element LAEq sequence would cause the app to crash when opening
  its details page (#288)
- iOS: Sometimes measurement path would not show up on map (#290)

### Changed

- Updated icons set (#277)
- Ensure that version numbers are in sync between platforms (#281)
- Update dependencies to latest stable versions (#283)

## [0.8.0] - 2026-03-04

### Added

- Manually select the input microphone from available input sources (#245)
- Added a disclaimer to the audio player stating that recorded audio is local only, as well as a
  delete button right next to the player. (#256)
- WasmJS: Browser URL bar now shows the current page URL. Navigation on web is now done through
  browser backward/forward actions instead of in-app back buttons. (#257)
- Added changelog to better keep track of changes (#263)
- Microphone calibration from reference device (#266, #275)

### Fixed

- iOS: Pressing the "Go to settings" button in the permission modal now actually opens the app's
  settings page in iOS 18+ (#252)
- Noise levels map legend now shows highest level on top (#255)
- History screen now shows the most recent measurement on top of the list. Measurements are loaded
  on demand for faster initial loading. (#254)
- Fixed flickering when fast scrolling in measurement history list due to reading from disk. (#274)

### Changed

- Improve performance of streaming and processing incoming audio (#250)
- Unify the color of primary and secondary texts (#251)
- Replace deprecated `BackHandler` with new `NavigationBackHandler` (#258)

-----

## [0.7.1] - 2026-02-05

### Fixed

- iOS: Measurement export sheet would not show up in iOS 18 (#239)

-----

## [0.7.0] - 2026-01-27

### Added

- Text or number fields in settings screen now show in a full screen dialog for easier edition
  (#222)
- Show app information at the bottom of the settings screen (app version, OS name and version,
  device information, ...) with an option to copy to clipboard (#223)
- Add export features to export measurement raw data and/or associated audio clip (#227)

### Fixed

- Hide settings options that are currently not in use (#222)
- Force max audio or measurement duration to be above zero in settings (#222)
- Measurement description placeholder now shows "No description" instead of Lorem ipsum (#224)
- Bottom padding is set dynamically based on the presence or not of a bottom pill navigation on the
  device (#225)
- Fixes layout breaks due to adaptive font or content size in accessibility settings (#230)

### Changed

- Removed custom drop shadow implementation in favour of native CMP implementation (#231)
- Update project to AGP 9.0 structure, CMP to 1.10.0 and Kotlin to 2.3.0 (#233)

-----

## [0.6.0] - 2025-12-17

### Added

- Help button on map screen that shows noise level legend (#170)
- Show community map in a tile on home screen (#172)
- Improve recording controls with play/pause and recording time displayed (#177)
- Support multiple layout size classes (phone, tablet, desktop, ...) and improve responsiveness of
  layout components (#179 and #185)
- New plot in the "Details" screen that shows average recorded level per frequency band (#187)
- New app icon (#193)
- New "Repartition of Noise Exposure (RNE)" plot in the "Details" screen (#202)
- Confirmation popup when trying to exit the recording screen while making a measurement (#206)

### Fixed

- Spectrogram plot performance issue (#169)
- WasmJS: fixes location permission popup on Firefox (#169)
- Faster load time on home page by loading measurements on demand (#190)
- iOS: when playing back recorded audio, level was very low (#194)
- When measurement consists of a single point (static recording), show a pin marker on the details
  map (#197)
- Android: Sample rate of `MediaRecorder` was set to 41000 instead of 441000, resulting in glitchy
  recorded audio (#198)
- Fix ticks of spectrogram scale (#199)
- Fix X axis of SPL over time plot (#200)
- Catch potential exceptions when starting or stopping a recording to avoid crashes (#204)

### Changed

- Cleanup class and functions naming to remove redundant "Measurement" prefix (#178)

-----

## [0.5.0] - 2025-10-22

### Added

- Confirmation popup for measurement deletion (#127)
- Integrated permissions flow: screens can declare required or optional permissions, if said
  permissions are not granted, shows a popup that prompts for granting permission or opening the
  associated settings page. Optional permissions can be skipped, required permissions must be
  granted in order to access the screen. (#141)
- New "History" screen with basic measurement layout to browse previous measurements. (#143)
- New "SPL over time" plot in "Details" screen that shows the measured noise level over time (#145)
- Local storage versioning: models have an associated version number. When deserialisation fails,
  check eventual version number mismatch and run a migration block if needed. By default, the
  outdated model is deleted. (#150)
- First map implementation using MapComposeMP (#163 and #165)

### Fixed

- Current noise level should show LAEq instead of LEq (#126)
- Android: adapt required permissions based on Android version (#147)
- Android: fixed a crash when trying to start a recording session while location permission is not
  granted (#149)

### Changed

- New color palette for app theme, and use the "Coloring Noise" scheme for noise levels (#117 and
  #148)
- Use compose to render spectrum plot and increase render performance (#138)
- Use compose to render spectrogram plot and increase render performance (#139)

-----

## [0.4.1] - 2025-07-08

### Fixed

- Disable persistent storage check to avoid restricting access to the app. (#119)

-----

## [0.4.0] - 2025-07-02

### Added

- Show an error message in browser that don't support Kotlin/Wasm inviting users to switch to
  another browser (#86)
- Show min, average and max values while making a recording (#94)
- Add a "Last measurements" section to the home page showing the last two measurements and a
  statistics section with total measurements count and total analysed duration (#98)
- Add a bottom sheet layout for measurement summary showing measurement date, duration,
  average value and a summary view with Min, LA90, LA50, LA10 and Max values (#103)
- Added a multiplatform audio player implementation to be able to listen back to recorded
  audio clips (#105)
- Added controls to delete a measurement, or only its associated audio clip. This view also
  shows the size on disk of each measurement and the size of only the audio file. (#106)
- Use NotoSans as a shared font between all platforms to improve consistency (#108)
- Fix and improve SPL weighted decaying. Values in sound level meter should now appear more
  stable than before, hence more easily readable. (#109)

## Fixed

- WasmJS: Ensure AGC, echo cancellation and noise suppression are disabled (#84)
- Fix various issues with OPFS storage on web (#85)
- Clip acoustic indicator values to -999 to avoid -Inf when serializing to JSON (#88)

## Changed

- Update dependencies (#87)
- Improve ViewModel injection using `koinViewModel` to better manage view models lifecycles (#99)
- Remove `BACKGROUND_LOCATION` permission from manifest in favour of a `ForegroundService` (#100)
- Use lifecycle aware flow collection to reduce energy impact while in background (#107)

-----
