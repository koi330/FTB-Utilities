package ftb.utils.config;

import ftb.lib.api.config.ConfigEntryBool;

public class FTBUConfigModules
{
	public static final ConfigEntryBool backups = new ConfigEntryBool("backups", true);
	public static final ConfigEntryBool auto_restart = new ConfigEntryBool("auto_restart", true);
	public static final ConfigEntryBool chunk_claiming = new ConfigEntryBool("chunk_claiming", true);
	public static final ConfigEntryBool chunk_loading = new ConfigEntryBool("chunk_loading", true);
	public static final ConfigEntryBool motd = new ConfigEntryBool("motd", true);
	public static final ConfigEntryBool starting_items = new ConfigEntryBool("starting_items", true);
	//public static final ConfigEntryBool starting_items = new ConfigEntryBool("starting_items", true);
}