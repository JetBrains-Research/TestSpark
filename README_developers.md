# TestGenie

## Description

Here you can find the overall structure of TestGenie plugin. The classes are listed and their purpose is described.

## Plugin Configuration File

The plugin configuration file is `plugin.xml` which can be found in `src/main/resources/META-INF` directory. All declarations (like actions, services, listeners) are in this file.

## Classes

## Tests

The tests for TestGenie can be found in `src/test` directory.

- `resources` directory contains a dummy project(s) used for testing the plugin
- `kotlin/nl/tudelft/ewi/se/ciselab/testgenie` directory contains the actual tests
    - `helpers` directory contains non-UI tests for the logic of our plugin
    - `uiTest` directory contains the tests related to UI.
      - `pages` directory has the frames used for UI testsing.
      - `utils` directory contains utils files that are helpful for UI testing.
      - `tests` directory contains the actual UI tests