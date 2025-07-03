# GDX Normal-Mapped Lighting Engine

A powerful 2D lighting engine for **LibGDX**, combining dynamic per-pixel lighting, **normal mapping**, and **Box2D shadows**. Supports both `scene2d` and ECS-based architectures like Fleks, with a focus on flexibility and real-time performance.

## ✨ Features

- **Dynamic Per-Pixel Lighting**  
  Smooth, realistic lighting effects calculated per pixel.

- **Normal Mapping**  
  Adds depth and surface detail to 2D sprites using normal maps.

- **Specular Mapping**  
  Enables shiny, reflective surfaces using specular maps.

- **Multiple Light Types**  
  - **Point Lights**: Omnidirectional, with customizable falloff.  
  - **Spot Lights**: Cone-shaped, for flashlights or beams.  
  - **Directional Lights**: Global light from a direction, like sunlight.

- **Box2D Integration**  
  Uses `box2dlights` for real-time, physics-based shadow casting.

- **Scene2d Integration**  
  Seamless lighting for `scene2d` actors and stages.

- **Effect System**  
  Lights can have built-in effects like:
  - `FIRE`
  - `PULSE`
  - `FAULTY_LAMP`
  - `LIGHTNING`
  - `COLOR_CYCLE`
  - `NONE`

- **Flexible API Design**  
  - `LightEngine`: Low-level, ECS-compatible engine.  
  - `Scene2dLightEngine`: High-level, plug-and-play engine for scene2d users.

---

## ⚙️ Engine Setup & Usage

### Low-Level API (`LightEngine`)

#### Initialization

```kotlin
val rayHandler = RayHandler(world)
val lightEngine = LightEngine(rayHandler, cam, batch, viewport)
```

#### Adding Lights

```kotlin
val pointLight = lightEngine.addPointLight(
    position = Vector2(5f, 5f),
    color = Color(1f, 0.5f, 0.2f, 1f),
    b2dDistance = 15f
)
pointLight.effect = LightEffectType.FIRE
```

#### Rendering Loop

```kotlin
lightEngine.update()

lightEngine.renderLights { engine ->
    engine.draw(diffuseTexture, normalMapTexture, specularTexture, x, y, width, height)
    engine.draw(diffuseTexture, normalMapTexture, x, y, width, height)
    engine.draw(diffuseTexture, x, y, width, height) // Unlit
}
```

---

### High-Level API (`Scene2dLightEngine`)

#### Initialization

```kotlin
val stage = Stage(viewport, batch)
val lightEngine = Scene2dLightEngine(rayHandler, cam, batch, viewport, stage)
```

#### NormalMappedActor

```kotlin
val myActor = NormalMappedActor(diffuseTexture, normalMapTexture)
stage.addActor(myActor)
```

#### LightActor

```kotlin
val pointLight = lightEngine.addPointLight(...)
val lightActor = LightActor(pointLight)
stage.addActor(lightActor)
```

#### Rendering Loop

```kotlin
stage.act(delta)
lightEngine.update()

lightEngine.renderLights { engine ->
    for (actor in stage.actors) {
        engine.draw(actor)
    }
}
```

---

## 🧪 Demo Projects

The repo includes two demos:

- `LightDemo`: Uses `LightEngine` with ECS-style rendering
- `Scene2dLightDemo`: Uses `Scene2dLightEngine` with actors

To run a demo, open `GgdxNormalMapExample.kt` and modify the demo class:

```kotlin
addScreen(LightDemo())
setScreen<LightDemo>()
// or
addScreen(Scene2dLightDemo())
setScreen<Scene2dLightDemo>()
```

Then execute:

```bash
./gradlew run
```

---

## ⌨️ Key Bindings (Demo)

| Key           | Action                                      |
|---------------|---------------------------------------------|
| `1`, `2`      | Switch between Point and Spot lights        |
| `BACKSPACE`   | Toggle directional light                    |
| `SPACE`       | Toggle diffuse lighting                     |
| `N`           | Toggle normal map lighting                  |
| `Q` / `A`     | Increase / decrease shader intensity        |
| `W` / `S`     | Increase / decrease light distance          |
| `E` / `D`     | Adjust shader balance                       |
| `R` / `F`     | Adjust spot cone angle                      |
| `T` / `G`     | Rotate spotlight cone                       |
| `I` / `K`     | Adjust directional light intensity          |
| `O` / `L`     | Rotate directional light                    |
| `Y` / `H`     | Adjust specular intensity                   |
| Mouse Wheel   | Change light hue                            |
| Mouse Move    | Move active light                           |

---

## 📦 Dependencies

- **Language**: Kotlin
- **Framework**: LibGDX
- **Physics**: Box2D
- **Lighting**: box2dlights
- **Optional**: LibKTX (for demos)

### Gradle Setup

```kotlin
// In core/build.gradle.kts
dependencies {
    implementation("com.github.YourUsername:gdx-2d-light:VERSION")
}
```

Make sure the shaders `light.vert` and `light.frag` are in `assets/shader`.

---

## 🧠 Core Concepts

| Engine Type           | Description                                                         |
|-----------------------|---------------------------------------------------------------------|
| `LightEngine`         | Low-level core renderer, no assumptions about actors/entities       |
| `Scene2dLightEngine`  | Wraps `LightEngine`, works with `scene2d` stages and actors         |

---

## 🙏 Credits

- Based on [mattdesl's shader gist](https://gist.github.com/mattdesl/4653464)
- Thanks to [Quillraven](https://github.com/Quillraven) for inspiration [YouTube Tutorials](https://www.youtube.com/@Quillraven)
