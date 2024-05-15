package apros.codeart.io;

import static apros.codeart.i18n.Language.strings;

import java.nio.ByteBuffer;
import java.util.function.Function;

import apros.codeart.pooling.IPoolItem;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.util.LazyIndexer;

public final class IOBuffer {

	private IOBuffer() {
	}

	public static IPoolItem borrow(int dataLength) {
		var config = getConfig(dataLength);
		var pool = getPool.apply(config);
		return pool.borrow();
	}

	private static Function<SizeConfig, Pool<ByteBuffer>> getPool = LazyIndexer.init((config) -> {
		return new Pool<ByteBuffer>(ByteBuffer.class,
				new PoolConfig(config.minVectorCapacity(), config.maxVectorCapacity(), 60), (isTempItem) -> {
					/*
					 * ByteBuffer.allocateDirect() 直接缓冲区：ByteBuffer.allocateDirect()
					 * 方法创建的是一个直接缓冲区。这种类型的缓冲区在Java堆外分配内存，直接在操作系统的本地内存中进行数据存储。
					 * 性能优势：直接缓冲区主要优势在于减少了在JVM和操作系统之间传输数据时的中间复制。对于大量数据的I/O操作，如文件操作和网络通信，
					 * 直接缓冲区通常能提供更高的性能。这是因为直接缓冲区允许操作系统直接在缓冲区上执行物理I/O操作（零拷贝技术）。
					 * 分配成本：虽然直接缓冲区在执行I/O操作时性能较高，但它们的分配和回收成本相对较高。直接缓冲区的创建和销毁涉及与操作系统的更多交互，
					 * 这在频繁创建和销毁缓冲区的情况下可能导致性能开销。 ByteBuffer.allocate() 堆内缓冲区：ByteBuffer.allocate()
					 * 方法创建的是堆内缓冲区。这种缓冲区在JVM的堆内存中分配，管理起来比较简单，且受到垃圾收集器的管理。
					 * 性能劣势：堆内缓冲区在进行I/O操作时需要一个中间步骤，即JVM需要将数据从堆内缓冲区复制到一个中间缓冲区中，然后操作系统才能从中间缓冲区进行物理I/
					 * O操作。这个额外的复制步骤可能导致性能下降，特别是在处理大量数据时。
					 * 分配效率：堆内缓冲区的创建和销毁通常比直接缓冲区要快，因为它们只涉及常规的Java对象分配，完全在JVM的控制之下。
					 */

					// 如果是超出池空间而创建给外部使用的临时项，那么用allocate分配（这种情况极为少见）
					if (isTempItem)
						return ByteBuffer.allocate(config.value());

					// 由于缓存的字节流是重复使用的，不需要销毁，所以用allocateDirect
					return ByteBuffer.allocateDirect(config.value());
				}, (buffer) -> {
					/*
					 * 1. clear() .clear() 方法重置缓冲区，使其准备好再次被写入。它将 position 设置为0，并将 limit
					 * 设置为缓冲区的容量。注意，.clear() 并不擦除缓冲区中的数据，但它将“忘记”这些数据的存在。 你可以覆盖它们，因为 position 将从0开始。
					 * 
					 * 2. compact() 如果在读取数据后，还有未读的数据需要保留在缓冲区中继续使用，那么可以使用 .compact()
					 * 方法。这个方法将所有未读的数据（即 position 到 limit 之间的数据）复制到缓冲区的开始处，然后将 position
					 * 设置为最后一个未读元素之后的位置，limit 设置为容量，以便可以继续写入更多的数据。
					 * 
					 * 3. rewind() 如果你只想重新读取已经在缓冲区的数据，而不是清除或准备写入新的数据，可以使用 .rewind() 方法。这个方法将
					 * position 设置回0，所以你可以重新读取缓冲区中的所有数据，但保持 limit 不变。
					 * 
					 * 结论 根据你的需求，选择合适的方法来重置缓冲区的状态：
					 * 
					 * 使用 .clear() 如果你想重置整个缓冲区以写入新数据。 使用 .compact() 如果你需要保留缓冲区中未读的数据。 使用 .rewind()
					 * 如果你只想重新读取缓冲区中的数据。
					 * 
					 */
					buffer.clear();
				});
	});

	public static record SizeConfig(int value, int minVectorCapacity, int maxVectorCapacity) {

	}

	/**
	 * 
	 * 根据数据长度，获得缓冲区配置
	 * 
	 * @param dataLength
	 * @return
	 */
	public static SizeConfig getConfig(long dataLength) {
		if (dataLength < SIZES[0].value())
			return Byte128;

		// 超出缓冲区最大支持的大小，这往往是上层程序设计的问题而不是缓冲区的问题，比如，大文件没有分段传输
		if (dataLength > SIZES[SIZES.length - 1].value())
			throw new IllegalStateException(strings("codeart", "MaxSupportedBuffer"));

		for (var size : SIZES) {
			if (size.value() > dataLength)
				return size;
		}

		throw new IllegalStateException(strings("codeart", "MaxSupportedBuffer"));
	}

	private static final int _Byte128 = 128;
	private static final int _Byte256 = 256;
	private static final int _Byte512 = 512;
	private static final int _KB1 = 1024;
	private static final int _KB2 = 1024 * 2;
	private static final int _KB4 = 1024 * 4;
	private static final int _KB8 = 1024 * 8;
	private static final int _KB16 = 1024 * 16;
	private static final int _KB32 = 1024 * 32;
	private static final int _KB64 = 1024 * 64;
	private static final int _KB128 = 1024 * 128;
	private static final int _KB256 = 1024 * 256;
	private static final int _KB512 = 1024 * 512;

	private static final int _MB1 = _KB1 * 1024;
	private static final int _MB2 = _MB1 * 2;
	private static final int _MB4 = _MB1 * 4;
	private static final int _MB8 = _MB1 * 8;
	private static final int _MB16 = _MB1 * 16;
	private static final int _MB32 = _MB1 * 32;

	// 以后这些设置可以从配置文件中设置，不同项目可能对缓冲区要求不一样，比如文件传输里就对MB的需求多：todo,
	private static final SizeConfig Byte128 = new SizeConfig(_Byte128, 4, 64);
	private static final SizeConfig Byte256 = new SizeConfig(_Byte256, 4, 32);
	private static final SizeConfig Byte512 = new SizeConfig(_Byte512, 4, 16);
	private static final SizeConfig KB1 = new SizeConfig(_KB1, 4, 64);
	private static final SizeConfig KB2 = new SizeConfig(_KB2, 4, 32);
	private static final SizeConfig KB4 = new SizeConfig(_KB4, 4, 16);
	private static final SizeConfig KB8 = new SizeConfig(_KB8, 4, 8);
	private static final SizeConfig KB16 = new SizeConfig(_KB16, 4, 4);
	private static final SizeConfig KB32 = new SizeConfig(_KB32, 4, 4);
	private static final SizeConfig KB64 = new SizeConfig(_KB64, 4, 4);
	private static final SizeConfig KB128 = new SizeConfig(_KB128, 4, 4);
	private static final SizeConfig KB256 = new SizeConfig(_KB256, 4, 4);
	private static final SizeConfig KB512 = new SizeConfig(_KB512, 4, 4);
	private static final SizeConfig MB1 = new SizeConfig(_MB1, 2, 4);
	private static final SizeConfig MB2 = new SizeConfig(_MB2, 2, 4);
	private static final SizeConfig MB4 = new SizeConfig(_MB4, 2, 4);
	private static final SizeConfig MB8 = new SizeConfig(_MB8, 2, 4);
	private static final SizeConfig MB16 = new SizeConfig(_MB16, 2, 4);
	private static final SizeConfig MB32 = new SizeConfig(_MB32, 2, 4);

	private static SizeConfig[] SIZES = new SizeConfig[] { Byte128, Byte256, Byte512, KB1, KB2, KB4, KB8, KB16, KB32,
			KB64, KB128, KB256, KB512, MB1, MB2, MB4, MB8, MB16, MB32 };

}
