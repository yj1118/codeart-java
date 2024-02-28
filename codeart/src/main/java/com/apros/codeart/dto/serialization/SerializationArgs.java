package com.apros.codeart.dto.serialization;

final class SerializationArgs {

	private SerializationArgs() {
	}

	/// <summary>
	/// instance在serialize方法中的参数索引
	/// </summary>
	public static final int InstanceIndex = 0;
	/// <summary>
	/// writer在serialize方法中的参数索引
	/// </summary>
	public static final int WriterIndex = 1;
	/// <summary>
	/// serializer在serialize方法中的参数索引
	/// </summary>
	public static final int SerializerIndex = 2;

	public static final int SerializerTypeNameTableIndex = 3;

	public static final int ReaderIndex = 1;
	public static final int DeserializerIndex = 1;
	public static final int DeserializerTypeNameTableIndex = 2;

	public static final String InstanceName = "instance";

	public static final String VersionName = "version";

	public static final String StreamPosition = "streamPosition";
	public static final String StreamLength = "streamLength";
}
