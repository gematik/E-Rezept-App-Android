# E-Rezept App
## Introduction

Prescriptions for medicines that are only available in pharmacies can be issued as electronic prescriptions (e-prescriptions resp. E-Rezepte) for people with public health insurance from 1 July 2021.
The official gematik E-Rezept App (electronic prescription app) is available to receive and redeem prescriptions digitally. Anyone can download the app for free:

[![Download E-Rezept on the App Store](https://user-images.githubusercontent.com/52454541/126137060-cb8c7ceb-6a72-423d-9079-f3e1a98b2638.png)](https://apps.apple.com/de/app/das-e-rezept/id1511792179)[![Download E-Rezept on the PlayStore](https://user-images.githubusercontent.com/52454541/126138350-a52e1d84-1588-4e8a-86df-189ee4df8bc8.png)](https://play.google.com/store/apps/details?id=de.gematik.ti.erp.app)[![Download E-Rezept on the App Gallery](https://user-images.githubusercontent.com/52454541/126158983-15d73f12-36c6-41ce-8de5-29d10baaed04.png)](https://appgallery.huawei.com/#/app/C104463531)

and login with the health card of the public health insurance. In July 2021, the e-prescription started with a test phase, initially in the focus region Berlin-Brandenburg. The nationwide rollout started three month later in September 2022.

The e-prescriptions are stored in the telematics infrastructure, for which gematik is responsible.

Visit our [FAQ page](https://www.das-e-rezept-fuer-deutschland.de/faq) for more information about the e-prescription.

### Support & Feedback

For endusers and insurant:

[![E-Rezept Webseite](https://img.shields.io/badge/web-E%20Rezept%20Webseite-green?logo=web.ru&style=flat-square&logoColor=white)](https://www.das-e-rezept-fuer-deutschland.de/)
[![eMail E-Rezept](https://img.shields.io/badge/email-E%20Rezept%20team-green?logo=mail.ru&style=flat-square&logoColor=white)](mailto:app-feedback@gematik.de)
[![E-Rezept Support Telephone](https://img.shields.io/badge/phone-E%20Rezept%20Service-green?logo=phone.ru&style=flat-square&logoColor=white)](tel:+498002773777)

Members of the health-industry with functional questions

[![eMail E-Rezept Team](https://img.shields.io/badge/web-E%20Rezept%20Industrie-green?logo=web.ru&style=flat-square&logoColor=white)](https://www.gematik.de/hilfe-kontakt/hersteller/)

IT specialists

[![eMail E-Rezept Fachportal](https://img.shields.io/badge/web-E%20Rezept%20Fachportal-green?logo=web.ru&style=flat-square&logoColor=white)](https://fachportal.gematik.de/anwendungen/elektronisches-rezept)
[![eMail E-Rezept Team](https://img.shields.io/badge/email-E%20Rezept%20team-green?logo=mail.ru&style=flat-square&logoColor=white)](mailto:app-feedback@gematik.de)

### Data Privacy

You can find the privacy policy for the app at: [https://www.das-e-rezept-fuer-deutschland.de/app/datenschutz](https://www.das-e-rezept-fuer-deutschland.de/app/datenschutz)

### Contributors

We plan to enable contribution to the E-Rezept App in the near future.

### Licensing

The E-Rezept App is licensed under the European Union Public Licence (EUPL); every use of the E-Rezept App Sourcecode must be in compliance with the EUPL.

You will find more details about the EUPL here: [https://joinup.ec.europa.eu/collection/eupl](https://joinup.ec.europa.eu/collection/eupl)

Unless required by applicable law or agreed to in writing, software distributed under the EUPL is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the EUPL for the specific language governing permissions and limitations under the License.

## Development

### Getting started

To get started, build one of the \*Pu\* variants. Currently, the Google and Huawei variants differ only in configuration. The code is identical. This is likely to change soon.

This repository is an [Kotlin Multiplatform Project](https://kotlinlang.org/docs/multiplatform.html) unifying the upcoming E-Rezept App for desktop and the Android App.

### Structure

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
|   `-- shared-test
|       `-- src
|           |-- main  
|-- common
|   `-- src
|       |-- androidMain
|       |-- androidTest
|       |-- commonMain
|       |-- commonTest
|       |-- desktopMain
|       `-- desktopTest
|-- desktop
|   `-- src
|       |-- jvmMain
|       `-- jvmTest
`-- plugins
    `-- dependencies
```

`plugins/dependencies` is a [composed build](https://docs.gradle.org/current/userguide/composite_builds.html) required by any of the other modules (android, common and desktop) managing the dependencies in one place.

The `gradle.properties` file contains all pre-defined properties required to communicate with the FD (**F**ach**D**ienst), IDP (**ID**entity **P**rovider) and the pharmacy lookup service.
Unfortunately the actual values are not meant to be public.

### Android

To build the Android App choose one variant (e.g. `gradle :android:assembleGooglePuExternalDebug -Pbuildkonfig.flavor=googlePuExternal`):

```shell
gradle :android:assemble(Google|Huawei)Pu(External|Internal)(Debug|Release) -Pbuildkonfig.flavor=(google|huawei)Pu(External|Internal)
```

*Note: Currently the android build variant is derived from the `buildkonfig.flavor` property.*

#### APK

The resulting `.apk` can be found in e.g. `app/android/build/outputs/apk/googlePuExternal/debug/`.

Additionally, you can find the latest apk [here](https://github.com/gematik/E-Rezept-App-Android/releases/latest)

#### Visualize Test Tags

See [Visualize Test Tags](documentation/test-tags.md)


### Links Sourcecode

- [E-Rezept iOS implementation](https://github.com/gematik/E-Rezept-App-iOS)
- Reference implementation of the [IDP (**ID**entity **P**rovider)](https://github.com/gematik/ref-idp-server)
- Reference implementation of the [FD (**F**ach**D**ienst)](https://github.com/gematik/ref-eRp-FD-Server)
