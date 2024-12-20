# How to run Technical Requirements

There are three tasks to prepare the technical requirements for the review.

- downloadBsiSpecs
- generateTechnicalRequirements
- extractRequirements

## Download Bsi Specs

- The task is run with the command

```sh
./gradlew :downloadBsiSpecs -Ptoken=YOUR_PRIVATE_TOKEN
```

#### NOTE: If this file is already downloaded, this task is not required to be run again.

- This token is required to download the specs from the shared repository. Please generate your own
  token.
- Once this is downloaded, please check if the file `bsi-requirements.html` is available in
  the `requirements` folder.

## Generate Technical Requirements

- This task is run with the command

```sh
./gradlew :generateTechnicalRequirements
```

Running this task creates the `requirements-report.html` at the root level.

## Prepare requirements for the Gutachter

- This task is run with the command

```sh
./gradlew :extractRequirements
```

#### NOTE: `./gradlew :generateTechnicalRequirements` calls this task once it ends. So this might not be required to call.

- This copies the `requirements-report.html` and its required css and scripts to a folder
  names `gutachter` at root level. This can be presented for review,

- The files under `gutachter` folder are auto-generated. Please do not modify them.
