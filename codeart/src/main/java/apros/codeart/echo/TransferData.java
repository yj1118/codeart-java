package apros.codeart.echo;

import apros.codeart.dto.DTObject;
import apros.codeart.io.BytesReader;
import apros.codeart.io.BytesWriter;
import apros.codeart.util.StringUtil;

public class TransferData {

	private String _language;

	public String language() {
		return _language;
	}

	private DTObject _body;

	public DTObject body() {
		return _body;
	}
//
//	public void setInfo(DTObject info) {
//		_info = info;
//	}
//
////	#region 二进制传输（如果需要的话）
//
//	private int _binaryDataTotalLength;
//
//	/**
//	 * 二进制数据的总长度（有可能是分段传输，该值是总数据的长度，而不是本次传输的长度）
//	 * 
//	 * @return
//	 */
//	public int binaryDataTotalLength() {
//		return _binaryDataTotalLength;
//	}
//
//	private ByteBuffer _binaryData;
//
//	/**
//	 * 二进制数据
//	 * 
//	 * @return
//	 */
//	public ByteBuffer binaryData() {
//		return _binaryData;
//	}

//	#endregion

	public TransferData(String language, DTObject body) {
		_language = language;
		_body = body;
//		_binaryDataTotalLength = binaryDataTotalLength;
//		_binaryData = binaryData;
	}

//	public TransferData(String language, DTObject info) {
//		_language = language;
//		_info = info;
//		_binaryDataTotalLength = 0;
//		_binaryData = null;
//	}

	public static TransferData deserialize(byte[] content) {

		var reader = new BytesReader(content);

		var language = reader.readString();
		var dtoCode = reader.readString();

		DTObject dto = DTObject.readonly(dtoCode);

//		int binaryTotalLength = reader.readInt();
//		ByteBuffer binaryData = null;
//
//		if (reader.hasRemaining()) {
//			var thisTimeLength = reader.readInt();
//			binaryData = reader.readBuffer(thisTimeLength);
//		}

//		return new TransferData(language, dto, binaryTotalLength, binaryData);
		return new TransferData(language, dto);
	}

	public static byte[] serialize(TransferData result) {
		// 注意，我们使用估算值快速得到可能的最大占用大小
		int estimateSize = 0;
		// 语言占用字节数
		estimateSize += StringUtil.maxBytesPerChar(result.language());

		// info占用字节数
		var bodyCode = result.body().getCode();

		estimateSize += StringUtil.maxBytesPerChar(bodyCode);

//		estimateSize += 4; // binaryDataTotalLength的记录占用4个字节
//		// 二进制数据占用字节数
//		estimateSize += result.binaryData() == null ? 0 : result.binaryData().capacity();

		try (var writer = new BytesWriter(estimateSize)) {

			writer.write(result.language());
			writer.write(bodyCode);

//			writer.write(result.binaryDataTotalLength());
//
//			if (result.binaryData() != null) {
//				writer.write(result.binaryData());
//			}

			return writer.toBytesAndEnd();
		}
	}

	public static TransferData createEmpty() {
		return new TransferData(StringUtil.empty(), DTObject.Empty);
	}
}
