<img src="icon.png" align="right" width="180px"/>

# Static Data

This mod gives you access to an additional data pool alongside `assets` and `data`, called `static_data`. This pool is available immediately in your ModInitializer, and cannot be overridden or reloaded, so you can use it to bootstrap blocks and items. Additionally, you can request static data from *other* or *all* mods, offering a new route for cooperative data synthesis.

**This mod is open source and under a permissive license.** As such, it can be included in any modpack on any platform without prior permission. We appreciate hearing about people using our mods, but you do not need to ask to use them. See the [LICENSE file](LICENSE) for more details.

## Importing

gradle:
```groovy
repositories {
	maven { url "http://server.bbkr.space:8081/artifactory/libs-release" }
}

dependencies {
	modImplementation "io.github.cottonmc:StaticData:1.1.2"
	include "io.github.cottonmc:StaticData:1.1.2"
}
```