# Inkball

Draws temporary lines to guide moving balls into matching holes while avoiding incorrect captures.

## Features

- Multi-level gameplay loaded from `config.json`
- Player-drawn collision lines
- Ball spawning, wall recolouring, hole attraction, scoring, pausing, restart, and level transitions
- Sprite-based board rendering with Processing
- JUnit test scaffold and JaCoCo coverage configuration

## Project Structure

```text
src/main/java/inkball/       Game source code
src/main/resources/inkball/  Sprites and image assets
src/test/java/inkball/       Tests and test fixtures
config.json                  Level and scoring configuration
level1.txt, level2.txt       Playable level layouts
```

## Requirements

- Java 8
- Gradle 8.10

## Run

```bash
gradle run
```

## Test

```bash
gradle test
```

Test coverage report:

```bash
gradle test jacocoTestReport
```

The HTML report is generated under `build/reports/jacoco/test/html/`.

## Controls

- Draw line: left mouse drag
- Remove line: right click near a line
- Pause/resume: space
- Restart: `R`

## Notes

This project was originally built as a first-year university assignment and later cleaned up for GitHub release.