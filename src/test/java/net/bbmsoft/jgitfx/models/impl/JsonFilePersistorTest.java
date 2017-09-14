package net.bbmsoft.jgitfx.models.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class JsonFilePersistorTest {

	@Test
	public void testPersist() throws IOException {

		StringBuilder sb = new StringBuilder();

		JsonFilePersistor persistor = new JsonFilePersistor(new File("."), false, file -> sb);

		List<File> data = Arrays.asList(new File("file1"), new File("file2"), new File("file3"));

		persistor.persist(data);

		String result = sb.toString();
		System.out.println(result);

		assertEquals("[{\"path\":\"file1\"},{\"path\":\"file2\"},{\"path\":\"file3\"}]", result);
	}

	@Test
	public void testLoad() throws IOException {

		File file = new File(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID());
		file.createNewFile();
		file.deleteOnExit();

		JsonFilePersistor persistor = new JsonFilePersistor(file,
				f -> new StringReader("[{\"path\":\"file1\"},{\"path\":\"file2\"},{\"path\":\"file3\"}]"));

		persistor.load(list -> {
			assertEquals(3, list.size());
			assertEquals("file1", list.get(0).getName());
			assertEquals("file2", list.get(1).getName());
			assertEquals("file3", list.get(2).getName());
		});

	}
}
