package apros.codeart.io;

import static apros.codeart.runtime.Util.propagate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
	public void atomicWrite(String filePath, String content) {
		File targetFile = new File(filePath);
		File tempFile = new File(targetFile.getParentFile(), "tempfile_" + targetFile.getName());

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

}
