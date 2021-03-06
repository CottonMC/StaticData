package io.github.cottonmc.staticdata;

import java.io.File;
import java.io.IOException;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class StaticDataInitializer implements ModInitializer {
	public static final String MODID = "staticdata";

	//public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize() {
		new File(FabricLoader.getInstance().getGameDirectory(), "static_data").mkdirs();
		/*
		for(StaticDataItem item : StaticData.getInDirectory("g", "test2")) {
			try {
				System.out.println(item.getIdentifier()+">> "+item.getAsString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		/*
		StaticData.get("g", "test2/test2.md").map(it -> {
			try {
				return it.getAsString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "";
		}).ifPresent(it->System.out.println(it));*/
	}
}
