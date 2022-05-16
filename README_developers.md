# TestGenie

## Description

Here you can find the overall structure of TestGenie plugin. The classes are listed and their purpose is described.

## Plugin Configuration File

The plugin configuration file is `plugin.xml` which can be found in `src/main/resources/META-INF` directory. All declarations (like actions, services, listeners) are in this file.


## Classes

All the classes can be found in `src/main/kotlin/nl/tudelft/ewi/se/ciselab/testgenie` directory.

### Actions

All the action classes can be found in `actions` directory.

### Coverage
All the classes related to the coverage visualisation can be found in `coverage` directory.

### EvoSuite
All the classes that interact with EvoSuite can be found in `evosuite` directory.

### Helpers
All the helper classes can be found in `helpers` directory.

### Listeners
All the listener classes can be found in `listener` directory.

### Services
All the service classes can be found in `services` directory.

### Settings
All the classes related to TestGenie `Settings/Preferences` page can be found in `settings` directory.

### Tool Window
All the classes related to TestGenie Tool Window (on the right side) can be found in `toolwindow` directory.


## Tests

The tests for TestGenie can be found in `src/test` directory.

- `resources` directory contains a dummy project(s) used for testing the plugin
- `kotlin/nl/tudelft/ewi/se/ciselab/testgenie` directory contains the actual tests
    - `helpers` directory contains non-UI tests for the logic of our plugin
    - `uiTest` directory contains the tests related to UI.
      - `pages` directory has the frames used for UI testsing.
      - `utils` directory contains utils files that are helpful for UI testing.
      - `tests` directory contains the actual UI tests