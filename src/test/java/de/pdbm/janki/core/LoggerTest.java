package de.pdbm.janki.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import de.pdbm.janki.core.LogType;
import de.pdbm.janki.core.Logger;

public class LoggerTest {

	@Test
	public void test() throws IOException {
		Logger.switchLogTypeOn(LogType.DEVICE_INITIALIZATION);
		Path path = Paths.get(Logger.LOG_FILE_NAME);
		long numberOfLines1= Files.lines(path).count();
		Logger.log(LogType.DEVICE_DISCOVERY, "write some text");
		long numberOfLines2= Files.lines(path).count();
		Assert.assertSame("must be same number of lines", numberOfLines1, numberOfLines2);
		Logger.log(LogType.DEVICE_INITIALIZATION, "write some text");
		long numberOfLines3= Files.lines(path).count();
		Assert.assertSame("must be one more line", numberOfLines2 + 1, numberOfLines3);
	}
}
