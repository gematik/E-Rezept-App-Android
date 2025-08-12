# .snyk.d/ignore.yaml
snyk:policy:
  ignore:
    '*':
      - reason: "Ignore local Gradle plugin module"
        path: plugins/technical-requirements-plugin
        expires: 2099-01-01T00:00:00.000Z