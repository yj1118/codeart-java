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
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.util.Strings;

public final class IOUtil {
	private IOUtil() {
	}

	public static Path firstFile(String dir) {
		Path path = Paths.get(dir);
		return firstFile(path);
	}

	public static Path firstFile(Path dir) {
		try (var stream = Files.list(dir)) {
			var p = stream.filter(Files::isRegularFile) // 筛选出文件
					.findFirst(); // 返回第一个文件

			if (p.isPresent())
				return p.get();
			return null;

		} catch (IOException e) {
			throw propagate(e);
		}
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

	public static Path combine(Path folder, String... paths) {

		if (paths.length == 0)
			return folder;

		var current = folder;

		for (var i = 0; i < paths.length; i++) {
			Path filePath = Paths.get(paths[i]);
			current = current.resolve(filePath);
		}

		return current;
	}

	public static Path combine(String... paths) {

		if (paths.length == 0)
			return null;
		if (paths.length == 1)
			return Paths.get(paths[0]);

		var current = Paths.get(paths[0]);

		for (var i = 1; i < paths.length; i++) {
			Path filePath = Paths.get(paths[i]);
			current = current.resolve(filePath);
		}

		return current;
	}

	public static void delete(String path) {
		Path dir = Paths.get(path);
		delete(dir);
	}

	/**
	 * 
	 * 删除文件或文件夹
	 * 
	 * @param path
	 */
	public static void delete(Path dir) {

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

	/**
	 * 
	 * 查找文件
	 * 
	 * @param path
	 * @param glob   glob 参数用来指定过滤条件。例如，"*.{txt}" 表示过滤所有扩展名为 .txt
	 *               的文件。可以通过逗号分隔在大括号内添加多个后缀来扩展过滤条件，如 *.{txt,jpg,png}。
	 * @param action
	 */
	public static void search(String path, String glob, Consumer<Path> action) {
		Path dir = Paths.get(path);

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob)) {
			for (Path file : stream) {
				action.accept(file);
			}
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	public static Iterable<Path> search(String path, String glob) {

		var items = new ArrayList<Path>();

		Path dir = Paths.get(path);

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob)) {
			for (Path file : stream) {
				items.add(file);
			}
		} catch (IOException e) {
			throw propagate(e);
		}

		return items;
	}

	public static void eachFolder(String path, Consumer<Path> action) {
		Path dir = Paths.get(path);
		eachFolder(dir, (item) -> {
			action.accept(item);
			return true;
		});
	}

	public static void eachFolder(String path, Function<Path, Boolean> action) {
		Path dir = Paths.get(path);
		eachFolder(dir, action);
	}

	public static void eachFolder(Path dir, Function<Path, Boolean> action) {

		// 使用 try-with-resources 语句确保 DirectoryStream 被正确关闭
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					// 返回false表示终止读取
					if (!action.apply(entry))
						break;
				}
			}
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	public static String readString(String path) {
		Path file = Paths.get(path);
		return readString(file);
	}

	public static String readString(Path file) {

		if (!Files.exists(file))
			return null;

		try {
			return Files.readString(file);
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	/**
	 * 
	 * 给定的路径下是否有子目录
	 * 
	 * @param path
	 * @return
	 */
	public static boolean hasSubdirectory(String path) {
		Path dir = Paths.get(path);

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					return true;
				}
			}
		} catch (IOException e) {
			throw propagate(e);
		}
		return false;
	}

	public static boolean hasSub(String path) {
		Path dir = Paths.get(path);
		return hasSub(dir);
	}

	/**
	 * 
	 * 是否有子目录或子文件
	 * 
	 * @param path
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean hasSub(Path dir) {

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path entry : stream) {
				return true;
			}
		} catch (IOException e) {
			throw propagate(e);
		}
		return false;
	}

}
