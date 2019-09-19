package io.github.cottonmc.staticdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
						System.out.println(it.toString());
						builder.add(new StaticDataItem(toIdentifier(container, it), it));
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
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
				if (Files.exists(data)) {
					builder.add(new StaticDataItem(toIdentifier(container,data), data));
				}
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
	
	/**
	 * Gets all data from the specified mod and directory.
	 * @param modid   the mod to search for data
	 * @param dirname the directory to discover data in
	 * @return a list of data discovered
	 */
	@Nonnull
	public static ImmutableSet<StaticDataItem> getInDirectory(String modid, String dirname) {
		ImmutableSet.Builder<StaticDataItem> builder = ImmutableSet.builder();
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
	
	
	private static Identifier toIdentifier(ModContainer container, Path path) {
		String dir = path.toString();
		int start = "/static_data/".length();
		if (dir.length()>=start) dir = dir.substring(start);
		return new Identifier(container.getMetadata().getId(), dir);
	}
}
