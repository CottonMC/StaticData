package io.github.cottonmc.staticdata;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import net.minecraft.util.Identifier;

/**
 * Represents one file retrieved from static_data
 */
public class StaticDataItem {
	private final Identifier id;
	private final Path path;
	private volatile String readCache = null;
	
	public StaticDataItem(Identifier id, Path path) {
		this.id = id;
		this.path = path;
	}
	
	public InputStream createInputStream() throws IOException {
		if (!Files.exists(path)) throw new FileNotFoundException(); //Should never happen
		return Files.newInputStream(path, StandardOpenOption.READ);
	}
	
	/**
	 * Reads this data in as a UTF-8 String and returns it. Caches the data for later accesses.
	 * 
	 * <p>This method is threadsafe.
	 * @return a String representing the contents of this static data item
	 * @throws IOException
	 */
	public String getAsString() throws IOException {
		if (readCache!=null) return readCache; //DCL
		synchronized(this) {
			if (readCache!=null) return readCache;
			
			if (!Files.exists(path)) throw new FileNotFoundException(); //Should never happen
			readCache = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
			return readCache;
		}
	}
	
	public Identifier getIdentifier() {
		return id;
	}
}