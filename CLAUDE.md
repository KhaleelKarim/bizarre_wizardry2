# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Bizarre Wizardry 2** is a NeoForge Minecraft mod (Minecraft 26.1.1, NeoForge 26.1.1.0-beta).

- **Mod ID:** `bizarre_wizardry2_jak`
- **Group:** `dev.ragu_rakkoon.bizarre_wizardry2`
- **Java Version:** 25

## Build Commands

```bash
./gradlew build           # Build the mod JAR (output in build/libs/)
./gradlew runClient       # Launch Minecraft client with the mod loaded
./gradlew runServer       # Launch dedicated server with the mod loaded
./gradlew runData         # Run data generation (generates JSON assets/recipes)
./gradlew clean           # Clean build artifacts
./gradlew --refresh-dependencies  # Force re-resolve dependencies
```

The mod JAR is published locally via `./gradlew publish` to a `repo/` directory.

## Architecture

### Registration Pattern

All game objects (blocks, items, tabs) use NeoForge's `DeferredRegister` system, defined as static fields in `BizarreWizardry2.java` and registered in the constructor via `register(modEventBus)`.

```java
// Pattern for adding new blocks/items
public static final DeferredBlock<X> MY_THING = BLOCKS.register("my_thing", () -> new X(...));
public static final DeferredItem<BlockItem> MY_THING_ITEM = ITEMS.registerSimpleBlockItem("my_thing", MY_THING);
```

### Event Handling

- `@EventBusSubscriber(modid = MOD_ID, bus = Bus.MOD)` — mod lifecycle events (setup, creative tab population)
- `@EventBusSubscriber(modid = MOD_ID, bus = Bus.GAME)` — game events (server starting, etc.)
- Client-only code lives in `ExampleModClient.java` annotated with `@Mod(value = MOD_ID, dist = Dist.CLIENT)`

### Configuration

`Config.java` defines a `ModConfigSpec` with typed entries (`BooleanValue`, `IntValue`, `ConfigValue`). The spec is registered in the main constructor with `ModLoadingContext.get().registerConfig(...)`.

### Resource Templates

Files in `src/main/templates/` are processed by Gradle with property substitution (e.g., `${mod_id}`, `${mod_version}`) and output to `src/main/resources/META-INF/`. Edit templates, not the generated output.

### Data Generation

Run `./gradlew runData` to regenerate JSON assets. Data gen classes go under the `datagen` subpackage and are wired up in the `runData` configuration in `build.gradle`.
