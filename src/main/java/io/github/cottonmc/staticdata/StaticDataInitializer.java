package io.github.cottonmc.staticdata;

import net.fabricmc.api.ModInitializer;

public class StaticDataInitializer implements ModInitializer {
	public static final String MODID = "staticdata";

	//public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize() {
		/*
		for(StaticData.DataItem item : StaticData.getInDirectory("staticdata", "test")) {
			try {
				System.out.println(item.getIdentifier()+">> "+item.getAsString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		/*
		StaticData.get("staticdata", "test/test.json").map(it -> {
			try {
				return it.getAsString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "";
		}).ifPresent(it->System.out.println(it));*/
	}
}
