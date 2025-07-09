<img alt="gematik logo" align="right" width="250" height="47" src="app/features/src/main/res/drawable/gematik_logo_flag_with_background.png"/> <br/> 

# E-Rezept App 

## Table Of Contents

- [About The Project](#about-the-project)
    - [Release Notes](#release-notes)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Structure](#structure)
    - [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [Security And Privacy](#security-and-privacy)
- [License](#license)
- [Contact](#contact)
- [Additional Links And Sourcecode](#additional-links-and-sourcecode)
 
## About The Project
Prescriptions for medicines that are only available in pharmacies can be issued as electronic prescriptions (e-prescriptions resp. E-Rezepte) for people with public health insurance from 1 July 2021.
The official gematik E-Rezept App (electronic prescription app) is available to receive and redeem prescriptions digitally. Anyone can download the app for free:

[![Download E-Rezept on the App Store](https://user-images.githubusercontent.com/52454541/126137060-cb8c7ceb-6a72-423d-9079-f3e1a98b2638.png)](https://apps.apple.com/de/app/das-e-rezept/id1511792179)[![Download E-Rezept on the PlayStore](https://user-images.githubusercontent.com/52454541/126138350-a52e1d84-1588-4e8a-86df-189ee4df8bc8.png)](https://play.google.com/store/apps/details?id=de.gematik.ti.erp.app)[![Download E-Rezept on the App Gallery](https://user-images.githubusercontent.com/52454541/126158983-15d73f12-36c6-41ce-8de5-29d10baaed04.png)](https://appgallery.huawei.com/#/app/C104463531)

or as an apk here in GitHub: [Releases](https://github.com/gematik/E-Rezept-App-Android/releases)

Login is possible with the health card or the app of the users public health insurance company. In July 2021, the e-prescription started with a test phase, initially in the focus region Berlin-Brandenburg. The nationwide rollout started three month later in September 2022.

The e-prescriptions are stored in the telematics infrastructure, for which gematik is responsible.

Visit our [FAQ page](https://www.das-e-rezept-fuer-deutschland.de/faq) for more information about the e-prescription.

### Release Notes
See [ReleaseNotes.md](./ReleaseNotes.md) for all information regarding the (newest) releases.

## Getting Started
This section provides instructions on how to get started with the project, 
including setting up the development environment and building the application.

### Prerequisites
Before you can build and run the application, ensure you have the following prerequisites installed on your system:

- **Android Studio:** The official IDE for Android app development.
- **Kotlin:** The primary programming language used in this project. Android Studio comes bundled with Kotlin support.
- **Git:** A distributed version control system used to manage the project's source code.

**Getting the Project Code**

To begin, clone the project's repository from GitHub using the following command in your terminal:
"git clone https://github.com/gematik/E-Rezept-App-Android.git" or by using the version control tool of Android Studio.

### Structure
The following graphic provides an overview over the more important parts of the kotlin multiplatform project:
```text
|-- app
|   `-- android
|       `-- src
|           |-- androidTest
|           |-- main
|           `-- test
|   `-- android-mock
|       `-- src
|           |-- androidTest
|           |-- main
|           `-- test
|   `-- demo-mode
|       `-- src
|           |-- main
|   `-- features
|       `-- src
|           |-- debug
|           |-- release
|           |-- androidTest
|           |-- main
|           `-- test
|   `-- test-actions
|       `-- src
|           |-- main  
|-- common
|   `-- src
|       |-- androidMain
|       |-- commonMain
|       |-- commonTest
`-- ui-components
    `-- src
```

- **app/android/src/main:** includes build parameters for the android app.
- **app/android-mock/src/main:** includes build parameters for the android app-mock (mostly used for testing).
- **app/features/src/main:** includes the core logic of the android app. Including Screens, UseCases and Navigation.
- **app/demo-mode/src/main:** includes the core logic of the demo-mode of the android app. Including all overwritten Datasources and UseCases
- **common/src/commonMain:** includes the general logic of app. Most important the localDatasource and it's entities.

### Installation
To create and install an unsigned debug.apk on your connected android device, run:
- "./gradlew :app:android:assembleGoogleTuInternalDebug" in your terminal. 
  - This will create an apk in the following path "app/android/build/outputs/apk/googleTuInternal/debug", which you can then install on your device
  - run "adb install app/android/build/outputs/apk/googleTuInternal/debug/android-googleTuInternal-debug.apk"
- or click on the green "run"-Button in the top right of android studio.

## Usage
The installed debug.apk can't communicate to connected servers. It's purpose is for local testing only. To show some data, you can enter the demo-mode via the settings. 
If you want or need an active app, please download a live version from the sources mentioned in the [About The Project](#about-the-project) section.

## Contributing
See [Contributing.md](./CONTRIBUTING.md) for all information regarding the the contributing process in this project.

## Security And Privacy
See [Security.md](./SECURITY.md) for all information regarding the used security and privacy guidelines in this project.

## License
Copyright 2021-2025 gematik GmbH

EUROPEAN UNION PUBLIC LICENCE v. 1.2

EUPL © the European Union 2007, 2016

See the [License.md](./LICENSE.md) for the specific language governing permissions and limitations under the License

## Additional Notes and Disclaimer from gematik GmbH
1. Copyright notice: Each published work result is accompanied by an explicit statement of the license conditions for use. These are regularly typical conditions in connection with open source or free software. Programs described/provided/linked here are free software, unless otherwise stated.
2. Permission notice: Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    1. The copyright notice (Item 1) and the permission notice (Item 2) shall be included in all copies or substantial portions of the Software.
    2. The software is provided "as is" without warranty of any kind, either express or implied, including, but not limited to, the warranties of fitness for a particular purpose, merchantability, and/or non-infringement. The authors or copyright holders shall not be liable in any manner whatsoever for any damages or other claims arising from, out of or in connection with the software or the use or other dealings with the software, whether in an action of contract, tort, or otherwise.
    3. We take open source license compliance very seriously. We are always striving to achieve compliance at all times and to improve our processes. If you find any issues or have any suggestions or comments, or if you see any other ways in which we can improve, please reach out to: ospo@gematik.de
3. Please note: Parts of this code may have been generated using AI-supported technology. Please take this into account, especially when troubleshooting, for security analyses and possible adjustments.

## Contact
For endusers and insurant:

[![E-Rezept Webseite](https://img.shields.io/badge/web-E%20Rezept%20Webseite-green?logo=web.ru&style=flat-square&logoColor=white)](https://www.das-e-rezept-fuer-deutschland.de/)
[![eMail E-Rezept](https://img.shields.io/badge/email-E%20Rezept%20team-green?logo=mail.ru&style=flat-square&logoColor=white)](mailto:app-feedback@gematik.de)
[![E-Rezept Support Telephone](https://img.shields.io/badge/phone-E%20Rezept%20Service-green?logo=phone.ru&style=flat-square&logoColor=white)](tel:+498002773777)

Members of the health-industry with functional questions

[![eMail E-Rezept Team](https://img.shields.io/badge/web-E%20Rezept%20Industrie-green?logo=web.ru&style=flat-square&logoColor=white)](https://www.gematik.de/hilfe-kontakt/hersteller/)

IT specialists

[![eMail E-Rezept Fachportal](https://img.shields.io/badge/web-E%20Rezept%20Fachportal-green?logo=web.ru&style=flat-square&logoColor=white)](https://fachportal.gematik.de/anwendungen/elektronisches-rezept)
[![eMail E-Rezept Team](https://img.shields.io/badge/email-E%20Rezept%20team-green?logo=mail.ru&style=flat-square&logoColor=white)](mailto:app-feedback@gematik.de)

### Additional Links And Sourcecode

- [E-Rezept iOS implementation](https://github.com/gematik/E-Rezept-App-iOS)
- Reference implementation of the [IDP (**ID**entity **P**rovider)](https://github.com/gematik/ref-idp-server)
- Reference implementation of the [FD (**F**ach**D**ienst)](https://github.com/gematik/ref-eRp-FD-Server)
