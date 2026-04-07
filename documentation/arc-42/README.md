# das e-Rezept (Android) Arc42 Documentation

[![pipeline status](https://gitlab.prod.ccs.gematik.solutions/dinesh.gangatharan/arc-42-documentation/badges/main/pipeline.svg)](https://jenkins.prod.ccs.gematik.solutions/view/e-Rezept-Android/job/e-Rezept-Android-App-Doc/)

This repository contains the Arc42 architecture documentation for the **das e-Rezept** Android application, served via Docsify and published on GitHub Pages.

The documentation provides a comprehensive overview of the architecture, design decisions, and implementation details of the application. It is organized into the following sections:

- **Introduction and Goals**: Overview of the project, its goals, and the intended audience.
- **Building Block View**: High-level and mid-level views of the application architecture, including module dependencies and interactions.
- **Runtime View**: Detailed runtime architecture, including sequence diagrams and component interactions.
- **Deployment View**: Overview of the deployment architecture, including server and client interactions.
- **Cross-Cutting Concepts**: Description of cross-cutting concerns, including security, logging, and error handling.
- **Architectural Decisions**: Key architectural decisions made during the development of the application, including rationale and alternatives considered.
- **Quality Requirements**: Overview of quality requirements and how they are addressed in the architecture.
- **Glossary**: Definitions of key terms and concepts used in the documentation.
- **Appendix**: Additional information, including references and links to related documentation.
- **Diagrams**: PUML sources and generated SVG/PNG diagrams used in the documentation.
- **CI/CD**: Overview of the CI/CD pipeline used for building and deploying the application, including build steps and deployment targets.

## Local Preview

```bash
# Install dependencies
npm ci

# Serve the Docsify site locally
npm run docs:serve
```

This will start a local server (usually at http://localhost:3000) where you can browse the documentation.

### Project Structure
/
├── diagrams/            # PUML sources  
├── docs/                # Docsify site content  
│   ├── index.html  
│   ├── _sidebar.md  
│   ├── 01_introduction_and_goals.md  
│   ├── … other .md files …  
│   └── diagrams/        # PUML generated SVG/PNG diagrams  
│       ├── high_level.png  
│       ├── … other .png files …  
├── public/              # Built static site (for deployment)  
├── package.json         # Project configuration  
└── README.md            # This file  