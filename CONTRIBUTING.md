# Welcome to the gematik contributing guide

Thank you for investing your time in contributing to our projects!

Read our [Code of Conduct](./CODE_OF_CONDUCT.md) to keep our community approachable and respectable. 

In this guide you will get an overview how you can contribute to our projects by opening an issue, creating, reviewing and merging a pull request.

Use the table of contents icon on the top left corner of this document to get to a specific section of this guide quickly.

## Reporting a security vulnerability

Please do not report vulnerabilities and security incidents as GitHub issues. Please contact us by sending an E-Mail to TODO or report them using the contact form at https://fachportal.gematik.de/kontaktformular.

## New contributor guide

To get an overview of the project, read the [README](./README.md).

## Getting started

### Issues

#### Create a new issue

If you spot a problem or have a feature request, search if an issue already exists.
If a related issue doesn't exist, you can open a new issue.

#### Solve an issue

Scan through our existing issues to find one that interests you. If you find an issue to work on, you are welcome to open a PR with a fix.

### Coding Style

gematik projects follow the kotlin style guide conventions, see [kotlin style guide android](https://developer.android.com/kotlin/style-guide) or [kotlin lang style guide](https://kotlinlang.org/docs/coding-conventions.html). Please follow them when working on your contributions.

### Code Coverage, Sonars, OWASP, Code format, etc.
 
- CodeFormat & Style: Please check your code with Ktlint and Detekt. commands: "./gradlew ktlintformat" & "./gradlew detekt"
- Code Coverage: If you add UseCases or ViewModels please add corresponding UnitTests as well. 
- Code Coverage: If you add or change UI make sure these changes are covered by screenshotstests "./gradlew verifyPaparazziDebug".


### Commit your update

Commit the changes once you are happy with them.

### Pull Request Process

- When you're finished with the changes, create a pull request, also known as a PR.
- Fill the pull request template so that we can review your PR. This template helps reviewers to understand your changes as well as the purpose of your pull request.
- Don't forget to [link the PR to the issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue) if you are solving one.
- Once you submit your PR, a project team member will review your proposal. We may ask questions or request additional information.
- We may ask for changes to be made before a PR can be merged, either using [suggested changes](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/incorporating-feedback-in-your-pull-request)
  or pull request comments. You can apply suggested changes directly through the UI. You can make any other changes in your fork, then commit them to your branch.
- As you update your PR and apply changes, mark each conversation as [resolved](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/commenting-on-a-pull-request#resolving-conversations).
- If your pull request is approved by our developers, we may merge it into the project.

### Your PR is merged!

Congratulations: The gematik team thanks you.

Once your PR is merged, your contributions will be publicly visible on the [gematik github page](https://github.com/gematik/).
