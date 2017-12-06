package de.pdbm.janki.core;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Logger {

	static final String LOG_FILE_NAME = "janki.log";

	private static final PrintStream logger;
	
	private static final Map<LogType, Boolean> logTypeSwitches = new ConcurrentHashMap<>();
	
	static {
		Stream.of(LogType.values()).forEach(value -> logTypeSwitches.put(value, Boolean.FALSE));
		PrintStream tmp = null;
		try {
			tmp= new PrintStream(LOG_FILE_NAME);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		logger = tmp;
	}
	
	public static void switchLogTypeOn(LogType... types) {
		Stream.of(types).forEach(value -> logTypeSwitches.put(value, Boolean.TRUE));
	}
	
	public static void log(LogType logType, String text) {
		if (logTypeSwitches.get(logType)) {
			logger.println(text);
		}
	}
}
