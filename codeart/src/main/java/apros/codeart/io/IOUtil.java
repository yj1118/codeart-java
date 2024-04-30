package apros.codeart.io;

import static apros.codeart.runtime.Util.propagate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.util.Strings;

public final class IOUtil {
	private IOUtil() {
	}

	/**
	 * 
	 * 事务性写入：对于关键数据，可以采用写入临时文件然后重命名的方式。
	 * 
	 * 首先将数据写入一个临时文件，完成后再将临时文件重命名为目标文件名。
	 * 
	 * 在多数操作系统中， 重命名操作是原子的，这可以确保要么拥有完整的文件，要么什么都不改变。
	 * 
	 * @param filePath
	 * @param content
	 */
	public static void atomicWrite(String filePath, String content) {
		File targetFile = new File(filePath);
		File tempFile = new File(targetFile.getParentFile(), targetFile.getName() + ".temp");

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
			writer.write(content);
			writer.flush();
		} catch (IOException e) {
			tempFile.delete();
			throw propagate(e);
		}

		try {
			Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			tempFile.delete();
			throw propagate(e);
		}
	}

	public static void atomicNewFile(String filePath) {
		atomicWrite(filePath, Strings.EMPTY);
	}

	/**
	 * 
	 * 创建目录，如果目录存在，那么不创建
	 * 
	 * @param dir
	 */
	public static void createDirectory(String dir) {

		try {
			Path path = Paths.get(dir);

			if (!Files.exists(path)) { // 检查路径是否存在
				Files.createDirectory(path); // 创建文件夹
			}
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	/**
	 * 获取当前程序运行的目录
	 * 
	 * @return
	 */
	public static String getCurrentDirectory() {
		return System.getProperty("user.dir");
	}

	public static String combine(String... paths) {

		if (paths.length == 0)
			return null;
		if (paths.length == 1)
			return paths[0];

		var current = Paths.get(paths[0]);

		for (var i = 1; i < paths.length; i++) {
			Path filePath = Paths.get(paths[i]);
			current = current.resolve(filePath);
		}

		return current.toString();
	}

	/**
	 * 
	 * 删除文件或文件夹
	 * 
	 * @param path
	 */
	public static void delete(String path) {
		Path dir = Paths.get(path);

		try {
			deleteDirectoryRecursively(dir);
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	private static void deleteDirectoryRecursively(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
				for (Path entry : entries) {
					deleteDirectoryRecursively(entry);
				}
			}
		}
		Files.delete(path);
	}

}
