# Release 1.2.4
### Added

- New empty state screen

### Changed

- Improved health-card order process

### Fixed

- Several demomode fixes
- Adjusted layout of prescriptions
- Removed duplicate password request
- Adjusted login hints

### Internal

- Adjusted   integration of upcoming fasttrack feature

# Release 1.2.3
### Added

- New empty state screen
- New tabbed layout on main screen
- Contact section in prescription redemption

### Changed

- Changed app auth appearance
- Improved NFC handling in Cardwall

### Fixed

- Handling of invalid prescriptions
- Properly display redeemed prescriptions
- Various fixes in demo mode (prescriptions)
- Error handling for bearer tokens
- Minor theming issues
    
### Internal

- IDP integration tests
- Support for virtual health cards


# Release 1.2.1
# Release 1.2.1

### Added

- Support for multiple profiles/health cards (Not enabled yet)
- NFC troubleshooting
- Display differences in data terms since accepting it originally
- Konnektathon build-type

### Changed

- Refactoring of audit events processing
- Update health insurances list
- Updated Readme
- Moved audit events to profile details
- Moved loading of audit events to background 

### Fixed

- Token-Display in Demo Mode
- SafetyNet handling on devices without Google services

### Internal

- Restructured database-schemes

# Release 1.1.0
- Insurance list is now up to date.
- Onboarding enforces a password strength of at least two (yellow indicator)
- Multiprofile handling is currently hidden behind a feature flag.
  - The settings menu is now located at the bottom right.
  - Enables to add/edit/delete profiles.
- This repo is now a kotlin multiplatform project including the android and the desktop app. This is currently work in progress and the code between both variants is not yet aligned to each other.
- Fasttrack (authentication without a health card) is currently behind a feature flag.
- The NFC reading is now blocked after an error occurs (e.g. PIN & CAN wrong).
- Add precise & coarse location support for Android 12.
- Fixes db migration.
- Several other bug fixes.

# Release 1.0.13
1.0.13

# Release 1.0.12
1.0.12

# Release 1.0.9
Release 1.0.9

# Release 1.0.7
Initial public release

