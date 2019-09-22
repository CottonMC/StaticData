package io.github.cottonmc.staticdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableSet;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;

@ParametersAreNonnullByDefault
public class StaticData {
	public static final String GLOBAL_DATA_NAMESPACE = "g";
			//"global_static_data_with_a_long_name_to_eliminate_name_collisions";
			// ^ This long namespace was a fantastic idea that looks terrible in log statements and
			// debug strings. It will not be used, but is archived here for posterity.
	
	/**
	 * Gets all data available. All of it. You should probably use a different method instead. The data
	 * is returned in no particular order.
	 */
	/* The data within each mod is returned in depth-first order at the moment but that can change at any time */
	@Nonnull
	public static ImmutableSet<StaticDataItem> getAll() {
		ImmutableSet.Builder<StaticDataItem> builder = ImmutableSet.builder();
		for(ModContainer container : FabricLoader.getInstance().getAllMods()) {
			Path staticDataPath = container.getRootPath().resolve("static_data");
			if (Files.isDirectory(staticDataPath)) {
				try(Stream<Path> files = Files.walk(staticDataPath)) {
					files.forEach((it)->{
						if (Files.isDirectory(it)) return;
						builder.add(new StaticDataItem(toIdentifier(container, it), it));
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		Path globalStaticDataFolder = new File(FabricLoader.getInstance().getGameDirectory(), "static_data").toPath();
		if (Files.isDirectory(globalStaticDataFolder)) {
			try(Stream<Path> files = Files.walk(globalStaticDataFolder)) {
				files.forEach((it)->{
					if (Files.isDirectory(it)) return;
					builder.add(new StaticDataItem(toGlobalIdentifier(globalStaticDataFolder, it), it));
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return builder.build();
	}
	
	/**
	 * Gets all data with the specified name from any mod that offers it. The data is returned in no particular order.
	 */
	@Nonnull
	public static ImmutableSet<StaticDataItem> getAll(String name) {
		ImmutableSet.Builder<StaticDataItem> builder = ImmutableSet.builder();
		for(ModContainer container : FabricLoader.getInstance().getAllMods()) {
			Path staticDataPath = container.getRootPath().resolve("static_data");
			if (Files.isDirectory(staticDataPath)) {
				Path data = staticDataPath.resolve(name);
				if (Files.exists(data) && !Files.isDirectory(data)) {
					builder.add(new StaticDataItem(toIdentifier(container,data), data));
				}
			}
		}
		
		Path globalStaticDataFolder = new File(FabricLoader.getInstance().getGameDirectory(), "static_data").toPath();
		if (Files.isDirectory(globalStaticDataFolder)) {
			Path data = globalStaticDataFolder.resolve(name);
			if (Files.exists(data) && !Files.isDirectory(data)) {
				builder.add(new StaticDataItem(toGlobalIdentifier(globalStaticDataFolder, data), data));
			}
		}
		
		return builder.build();
	}
	
	/**
	 * Lists all data in the specified directory from any mod that offers it.
	 * @param dirname the name of a directory within the "static_data" folder. Can be nested, e.g. "definitions/blocks"
	 */
	@Nonnull
	public static ImmutableSet<StaticDataItem> getAllInDirectory(String dirname) {
		ImmutableSet.Builder<StaticDataItem> builder = ImmutableSet.builder();
		for(ModContainer container : FabricLoader.getInstance().getAllMods()) {
			Path staticDataPath = container.getRootPath().resolve("static_data");
			if (Files.isDirectory(staticDataPath)) {
				Path datadir = staticDataPath.resolve(dirname);
				if (Files.isDirectory(datadir)) {
					try(Stream<Path> files = Files.walk(datadir)) {
						files.forEach((it)->{
							if (Files.isDirectory(it)) return;
							builder.add(new StaticDataItem(toIdentifier(container, it), it));
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		Path globalStaticDataFolder = new File(FabricLoader.getInstance().getGameDirectory(), "static_data").toPath();
		if (Files.isDirectory(globalStaticDataFolder)) {
			Path datadir = globalStaticDataFolder.resolve(dirname);
			if (Files.isDirectory(datadir)) {
				try(Stream<Path> files = Files.walk(datadir)) {
					files.forEach((it)->{
						if (Files.isDirectory(it)) return;
						builder.add(new StaticDataItem(toGlobalIdentifier(globalStaticDataFolder, it), it));
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return builder.build();
	}
	
	/**
	 * Gets a single DataItem from the specified mod
	 * @param modid the mod to search for data
	 * @param name the exact filename (e.g. "test/test.json") of the file inside the static_data folder
	 * @return the DataItem, or Optional.EMPTY if the data was not found.
	 */
	@Nonnull
	public static Optional<StaticDataItem> get(String modid, String name) {
		if (modid.equals(GLOBAL_DATA_NAMESPACE)) {
			Path globalStaticDataFolder = new File(FabricLoader.getInstance().getGameDirectory(), "static_data").toPath();
			if (Files.isDirectory(globalStaticDataFolder)) {
				Path data = globalStaticDataFolder.resolve(name);
				if (Files.exists(data) && !Files.isDirectory(data)) {
					return Optional.of(new StaticDataItem(new Identifier(GLOBAL_DATA_NAMESPACE, name), data));
				}
			}
			
			
			return Optional.empty();
		}
		
		Optional<ModContainer> containerOpt = FabricLoader.getInstance().getModContainer(modid);
		if (!containerOpt.isPresent()) return Optional.empty();
		
		return containerOpt.map(container->{
			Path staticDataPath = container.getRootPath().resolve("static_data");
			if (Files.isDirectory(staticDataPath)) {
				Path data = staticDataPath.resolve(name);
				if (Files.exists(data)) {
					return new StaticDataItem(toIdentifier(container,data), data);
				}
			}
			return null; //gets optional-wrapped by map()
		});
	}
	
	/** Identifier version of {@link #get(String, String)} */
	@Nonnull
	public static Optional<StaticDataItem> get(Identifier id) {
		return get(id.getNamespace(), id.getPath());
	}
	
	
	/**
	 * Gets all data from the specified mod and directory.
	 * @param modid   the mod to search for data
	 * @param dirname the directory to discover data in
	 * @return a list of data discovered
	 */
	@Nonnull
	public static ImmutableSet<StaticDataItem> getInDirectory(String modid, String dirname) {
		ImmutableSet.Builder<StaticDataItem> builder = ImmutableSet.builder();
		
		if (modid.equals(GLOBAL_DATA_NAMESPACE)) {
			Path globalStaticDataFolder = new File(FabricLoader.getInstance().getGameDirectory(), "static_data").toPath();
			if (Files.isDirectory(globalStaticDataFolder)) {
				Path datadir = globalStaticDataFolder.resolve(dirname);
				if (Files.isDirectory(datadir)) {
					try(Stream<Path> files = Files.walk(datadir)) {
						files.forEach((it)->{
							if (Files.isDirectory(it)) return;
							builder.add(new StaticDataItem(toGlobalIdentifier(globalStaticDataFolder, it), it));
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return builder.build();
		}
		
		Optional<ModContainer> containerOpt = FabricLoader.getInstance().getModContainer(modid);
		if (!containerOpt.isPresent()) return builder.build();
		ModContainer container = containerOpt.get();
		
		Path staticDataPath = container.getRootPath().resolve("static_data");
		if (Files.isDirectory(staticDataPath)) {
			Path datadir = staticDataPath.resolve(dirname);
			if (Files.isDirectory(datadir)) {
				try(Stream<Path> files = Files.walk(datadir)) {
					files.forEach((it)->{
						if (Files.isDirectory(it)) return;
						builder.add(new StaticDataItem(toIdentifier(container, it), it));
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return builder.build();
	}
	
	/** Identifier version of {@link #getInDirectory(String, String)} */
	@Nonnull
	public static ImmutableSet<StaticDataItem> getInDirectory(Identifier id) {
		return getInDirectory(id.getNamespace(), id.getPath());
	}
	
	private static String getRelative(Path parent, Path child) {
		return parent.toAbsolutePath().relativize(child)
				.toString()
				.replace(File.pathSeparatorChar, '/')
				.toLowerCase(Locale.ROOT)
				.replace(' ', '_')
				;
	}
	
	private static Identifier toIdentifier(ModContainer container, Path path) {
		String rel = getRelative(container.getRootPath(), path);
		if (rel.startsWith("static_data/")) { //Should always be true
			rel = rel.substring("static_data/".length());
		}
		return new Identifier(container.getMetadata().getId(), rel);
	}
	
	private static Identifier toGlobalIdentifier(Path root, Path path) {
		return new Identifier(GLOBAL_DATA_NAMESPACE, getRelative(root, path));
	}
}
