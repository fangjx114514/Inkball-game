# Inkball

A Java/Processing arcade puzzle game about redirecting bouncing balls into the right holes before time runs out.

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

## Notice

This project was originally completed for the University of Sydney INFO1113 "Inkball" assignment and is shared after the teaching period ended.

Course materials and assets may belong to the University of Sydney and/or course staff and are not licensed for reuse. Do not copy this project for coursework submission.