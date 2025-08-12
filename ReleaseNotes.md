# Release 1.32.0

### fixed (3 changes)

- Password lockout align with ios app
- Fix 3-dot menu for archived digas
- Talkback adjustments

### ⚡ enhancements (4 changes):

- Blur background on showing dialogs
- Help GID PKV Screen information
- PKV Onboarding Drawer is cleaner
- Profile screen has been updated

### ✨ features (3 changes):

- Redeem process made easy, access to redeem screen is faster than before
- Redeem screen with reworked design
- Digas show information from Bfarm

### improvements (2 changes)

- Add database module for next version of database
- Add new module for eu-rezept feature

---

# Release 1.31.0

### fixed (4 changes)

- Save cache pharmacies from fhirvzd
- Show dual pharmacy times
- Paging for prescriptions
- Fix nfc being always on read mode

### ⚡ enhancements (4 changes):

- KBV parsing & show damaged bundles
- Add Gutachter Documentation for Finding A.06
- Add accessibility for maps
- Add link to accessibility

### ✨ features (4 changes):

- Update Onboarding screens
- Add password timeout
- Fallback when insurer information missing
- Offline language translations

---

# Release 1.30.0

### fixed (1 change)

- Bug fixes

### ⚡ enhancements (1 change)

- Infrastructure improvements

### ✨ features (2 change)

- DiGA Integration:
    - Added full support for DiGA (Digitale Gesundheitsanwendungen) prescriptions
    - View and manage your active DiGA prescriptions directly in the app
    - Check real-time redemption status and receive dispense information
    - Securely redeem and delete DiGA prescriptions
- Make FhirVZD as default for pharmacies

### reworked (2 change)

- Parsing FHIR data for Medication dispense and Audit events
- Add new base module app-core to handle all base functionality of the app

---

# Release 1.29.0

### changed (3 changes)

- Send communication dispense requests as 1.4 version
- Update accessibility in settings screen
- Update info-text reply message color

### reworked (1 change)

- Parsing FHIR data for KBV data and communications

### fixed (2 changes)

- Store crashes and ANR issues
- Invoice message preview now shown in message overview

### improvements (2 changes)

- Modularize FHIR parsing, navigation, and trackers
- Infrastructure changes and Fastlane migration

### pre-work (1 change)

- Added FHIR VZD and DiGA download groundwork

---

# Release 1.28.0

### changed (4 changes)

- Message detail and message overview show the latest message
- Sort languages
- Prescription state is checked for ready state before redeeming
- Parsing of prescriptions

### added (2 changes)

- Support for communication reply version 1.4
- Detecting phone number in message detail screen

### internal (1 change)

- Infrastructure / project improvements

### fixed (1 change)

- Bug fixes

---

# Release 1.27.0

### changed (2 changes)

- Main color of the app became darker to allow for more contrast
- Optimized messages

### added (1 change)

- Spanish and Irish are now supported

### internal (1 change)

- Infrastructure / project improvements

### fixed (1 change)

- Bug fixes

### other (1 change)

- UX improvements

---

# Release 1.26.0

### added (3 changes)

- Link to organ donation register in settings
- In-app notification about the latest changes
- Option to have both appPassword and device credentials as login method

### changed (1 change)

- Accessibility overhaul on many screens

### internal (1 change)

- Infrastructure / project improvements

### fixed (1 change)

- Bug fixes

### other (1 change)

- UX improvements

---

# Release 1.25.1

### added (2 changes)

- Saving the credentials of the user when logging in with gID
- No internet hint

### reworked (1 change)

- App login

### fixed (1 change)

- Bug fixes

### internal (1 change)

- Infrastructure / project improvements

### other (1 change)

- UX improvements

---

# Release 1.24.0

### fixed (1 change)

- Bug fixes

### internal (1 change)

- Infrastructure / project improvements

### other (1 change)

- UX improvements

---

# Release 1.23.0

### added (3 changes)

- Help-section for GID usage
- In-app language change for Android 13 and higher
- Profile picture enhancements: stickers, emojis, bitmojis

### fixed (1 change)

- Bug fixes

### internal (1 change)

- Infrastructure / project improvements

### other (1 change)

- UX improvements

---

# Release 1.22.0

### added (2 changes)

- Saving credentials for all devices with hardware-backed keystore
- Redeem from detail view

### reworked (2 changes)

- Refactoring of pharmacy feature
- Refactoring of redeem feature

### fixed (1 change)

- Bug fixes

### other (2 changes)

- URL updates
- UX improvements

---

# Release 1.21.0

### other (1 change)

- Skipped

---

# Release 1.20.0

### reworked (2 changes)

- Refactor of egk card feature
- Partial refactoring of pharmacy feature

### added (2 changes)

- Multi-language support for privacy policy
- Update health insurance contacts

### fixed (1 change)

- Bug fixes

---

# Release 1.19.1

### added (2 changes)

- gID (HealthID) authentication function
- Language settings can now be changed within the app

### other (2 changes)

- UX improvements
- Bug fixes

---

# Release 1.18.1

### changed (1 change)

- Optimized performance

### other (2 changes)

- UX improvements
- Bug fixes

---

# Release 1.17.2

### added (2 changes)

- Demo mode function
- Function to change the name of scanned prescriptions

### changed (1 change)

- Increased minimum SDK version to 26 and build version to 34

### other (2 changes)

- UX improvements
- Bug fixes

---

# Release 1.16.1

### added (1 change)

- Invoice correction function for private health insurance customers

### changed (1 change)

- Optimized performance

### other (2 changes)

- UX improvements
- Bug fixes

---

# Release 1.14.0

### added (1 change)

- Allow devices without NFC to authenticate using 3rd party health insurance apps

### fixed (1 change)

- Lots of bugfixes

---

# Release 1.13.0-RC1

### fixed (1 change)

- Fasttrack authentication with external insurance apps works again

### other (1 change)

- Bugfixes

---

# Release 1.12.1

### added (1 change)

- Redeem prescriptions directly without being authenticated with TI

### fixed (1 change)

- Lots of bugfixes

---

# Release 1.11.0-RC5

### added (1 change)

- Direct redemption of prescription without TI

### fixed (1 change)

- Bugfixes

---

# Release 1.10.0

### added (1 change)

- Began implementation for support of private insurances

### fixed (1 change)

- Bugfixes

---

# Release 1.9.0

### changed (1 change)

- Small refactorings

### fixed (1 change)

- Bugfixes

---

# Release 1.8.0

### changed (1 change)

- Switched to new analytics tool

### fixed (1 change)

- Bugfixes

---

# Release 1.7.0

### added (2 changes)

- New wizard for ordering a healthcard
- Support for new FHIR profile version

### fixed (1 change)

- Lots of bugfixes

---

# Release 1.6.1

### added (3 changes)

- Tooltips
- Maps in pharmacy search
- New order onboarding and new local redeem screen

### changed (3 changes)

- Prescription details
- Switched from SafetyNet to Play Integrity API
- New ML-Kit info screen

### fixed (1 change)

- Several bugfixes

---

# Release 1.4.9

### added (2 changes)

- Pharmacies can be searched for on a map
- New users now get a better onboarding in the app

### fixed (1 change)

- Several bugfixes

---

# Release 1.2.1-SRC

### other (1 change)

- Codebase for SRC review  
