# GDX-Normal-Light – A Dynamic 2D Lighting Engine for LibGDX

**GDX-2D-Light** is a 2D lighting engine for LibGDX that supports real-time dynamic lighting, normal mapping, and Box2D-based shadows. It is designed for easy integration into various project structures — offering a high-level API for `scene2d` users and a low-level API for ECS-based architectures.

The engine allows you to use dynamic lighting to add depth and atmosphere to your 2D sprites.

---

## Features

- **Dynamic Per-Pixel Lighting**  
  Lights are calculated for every pixel, allowing for smooth gradients and realistic effects.

- **Normal Mapping**  
  Add surface detail and depth to your 2D sprites using normal maps.

- **Specular Mapping**  
  Add material properties to your sprites, allowing for shiny surfaces and highlights.
- 
- **Multiple Light Types**  
  - **Point Lights**: Emit light in all directions from a single point, with configurable falloff.  
  - **Spot Lights**: Emit light in a cone, perfect for flashlights or focused effects.  
  - **Directional Lights**: Simulate a distant light source like the sun or moon, affecting the entire scene from a specific angle.

- **Box2D Integration**  
  Leverages box2dlights for accurate, physics-based shadow casting.

- **Flexible API Design**  
  - `Scene2dLightEngine`: A high-level, "plug-and-play" engine for developers using the scene2d framework.  
  - `LightEngine`: A low-level, data-driven engine perfect for ECS-based architectures (like Fleks or Artemis) or custom render loops.

- **Efficient & Robust**  
  The engine is designed to minimize render state changes by intelligently flushing the SpriteBatch when necessary, ensuring both correctness and performance.

- **Optional Lighting**  
  Use a single "uber-shader" that can render objects with normal maps or with simple, flat lighting, allowing you to mix and match lit and unlit sprites in the same render pass.

---

## Limitations

- **Desktop Only**  
  The engine is currently designed for desktop platforms and may not perform well on mobile devices due to the complexity of per-pixel lighting calculations.

## Demo Usage

This project includes two demo implementations that show how to use the lighting engine with both the low-level and high-level APIs:

- `LightDemo`: Demonstrates the low-level API (`LightEngine`) for ECS-based projects.
- `Scene2dLightDemo`: Demonstrates the high-level API (`Scene2dLightEngine`) for `scene2d`-based LibGDX projects.

To run a demo, open the file `GgdxNormalMapExample.kt` and modify the following line to point to the desired demo class:

```kotlin
addScreen(LightDemo())
setScreen<LightDemo>()
// or
addScreen(Scene2dLightDemo())
setScreen<Scene2dLightDemo>()
```

Then you can run the application using:

```bash
./gradlew run
```
## Key Bindings

The following key bindings are supported in the `LightDemo` application:

- `1`, `2`: Switch between point light and spot light
- `BACKSPACE`: Toggle directional light on/off
- `SPACE`: Toggle diffuse lighting
- `N`: Toggle normal map lighting on/off
- `Q`, `A`: Increase/decrease active light's shader intensity
- `W`, `S`: Increase/decrease active light's distance
- `E`, `D`: Increase/decrease active light's shader balance
- `R`, `F`: Increase/decrease spotlight cone angle (only for spot light)
- `T`, `G`: Rotate spotlight cone (only for spot light)
- `I`, `K`: Increase/decrease directional light intensity
- `O`, `L`: Rotate directional light
- `Y`, `H`: Increase/decrease specular intensity
- `Mouse Wheel`: Adjust hue (color) of the active light
- `Mouse Movement`: Move the active light
---

## Dependencies & Setup

- **Language**: Kotlin  
- **Dependencies**: LibGDX, Box2D, Box2DLights  
- **Based on**: The great work of https://gist.github.com/mattdesl/4653464
- **Thanks to**: Quillraven for the inspiring tutorials https://github.com/Quillraven , [YouTube](https://www.youtube.com/@Quillraven)
- **Optional**: LibKTX (used in the demo, but not required)

### Gradle

```kotlin
// In core/build.gradle.kts
dependencies {
    // Example – replace VERSION with the actual version
    implementation("com.github.YourUsername:gdx-2d-light:VERSION")
}
```

> Make sure the shaders `light.vert` and `light.frag` are located in your `assets/shader` directory.

---

## Core Concepts

The library is based on two main engine classes depending on your preferred level of abstraction:

### `LightEngine` (Low-Level / ECS)

- Core renderer  
- Knows nothing about game structure (Actors, Entities, etc.)  
- Ideal for ECS frameworks such as Fleks

### `Scene2dLightEngine` (High-Level / scene2d)

- Convenience wrapper around `LightEngine`  
- Works directly with `Actor` objects  
- Ideal for LibGDX projects using a `Stage`-based architecture

---

## Usage

### 1. Low-Level API (ECS)

#### Step 1: Initialize LightEngine

```kotlin
lateinit var lightEngine: LightEngine
lateinit var rayHandler: RayHandler
lateinit var world: World
lateinit var cam: OrthographicCamera
lateinit var viewport: Viewport
lateinit var batch: SpriteBatch

// In show() / create()
rayHandler = RayHandler(world)
lightEngine = LightEngine(rayHandler, cam, batch, viewport)

// add light
val pointLight = lightEngine.addPointLight(
    position = Vector2(5f, 5f),
    color = Color.WHITE,
    b2dDistance = 15f
)
```

#### Step 2: Render Loop

```kotlin
lightEngine.update()

lightEngine.renderLights { engine ->
    engine.draw(
        diffuse = wallTexture,
        normals = wallNormals,
        x = 0f,
        y = 0f,
        width = 10f,
        height = 10f
    )
    engine.draw(
        diffuse = playerTexture,
        normals = playerNormals,
        x = player.x,
        y = player.y,
        width = player.width,
        height = player.height
    )
    engine.draw(
        diffuse = coinTexture,
        x = coin.x,
        y = coin.y,
        width = 1f,
        height = 1f
    )
}

debugRenderer.render(world, cam.combined)
```

---

### 2. High-Level API (scene2d)

#### Step 1: NormalMapped Actor

```kotlin
class NormalMappedActor(
    override val diffuseTexture: Texture,
    override val normalMapTexture: Texture
) : Actor(), NormalMapped {
    init {
        setSize(diffuseTexture.width.toFloat(), diffuseTexture.height.toFloat())
    }
}
```

#### Step 2: Initialize Engine & Stage

```kotlin
lateinit var scene2dLightEngine: Scene2dLightEngine
lateinit var stage: Stage

stage = Stage(viewport, batch)
rayHandler = RayHandler(world)
scene2dLightEngine = Scene2dLightEngine(rayHandler, cam, batch, viewport, stage)

val wall = NormalMappedActor(wallTexture, wallNormals)
stage.addActor(wall)

val unlitSprite = Image(coinTexture).apply {
    setPosition(5f, 2f)
}
stage.addActor(unlitSprite)
```

#### Step 3: Render Loop

```kotlin
stage.act(delta)
scene2dLightEngine.update()

scene2dLightEngine.renderLights { engine ->
    for (actor in stage.actors) {
        engine.draw(actor)
    }
}

debugRenderer.render(world, cam.combined)
```
