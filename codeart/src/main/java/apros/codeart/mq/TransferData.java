package apros.codeart.mq;

import java.nio.ByteBuffer;

import apros.codeart.dto.DTObject;
import apros.codeart.io.BytesReader;
import apros.codeart.util.ListUtil;

public class TransferData {

	private String _language;

	public String language() {
		return _language;
	}

	private DTObject _info;

	public DTObject info() {
		return _info;
	}

//	#region 二进制传输（如果需要的话）

	private int _binaryDataLength;

	/**
	 * 二进制数据的总长度（有可能是分段传输，该值是总数据的长度，而不是本次传输的长度）
	 * 
	 * @return
	 */
	public int binaryDataLength() {
		return _binaryDataLength;
	}

	private ByteBuffer _binaryData;

	/**
	 * 二进制数据
	 * 
	 * @return
	 */
	public ByteBuffer binaryData() {
		return _binaryData;
	}

//	#endregion

	public TransferData(String language, DTObject info, int binaryDataLength, ByteBuffer binaryData) {
		_language = language;
		_info = info;
		_binaryDataLength = binaryDataLength;
		_binaryData = binaryData;
	}

	public TransferData(String language, DTObject info) {
		_language = language;
		_info = info;
		_binaryDataLength = 0;
		_binaryData = null;
	}

	public static TransferData deserialize(byte[] content) {

		var reader = new BytesReader(content);

		var language = reader.readString();
		var dtoCode = reader.readString();

		DTObject dto = DTObject.readonly(dtoCode);

		int binaryLength = 0;
		ByteBuffer binaryData = null;

		if (reader.hasRemaining()) {
			binaryLength = reader.readInt();
			var thisTimeLength = reader.readInt();
			binaryData = reader.readBuffer(thisTimeLength);
		}

		return new TransferData(language, dto, binaryLength, binaryData);
	}

	public static byte[] serialize(TransferData result)
	 {
	     var size = result.DataLength == 0 ? SegmentSize.Byte512.Value : result.Buffer.Length; 

	     using (var temp = ByteBuffer.Borrow(size))
	     {
	         var target = temp.Item;

	         target.Write(result.Language);


	         var dtoData = result.Info.ToData();


	         target.Write(dtoData.Length);
	         target.Write(dtoData);

	         if (result.DataLength > 0)
	         {
	             target.Write(result.DataLength);
	             target.Write(result.Buffer.Length);
	             target.Write(result.Buffer);
	         }

	         return target.ToArray();
	     }
	 }

	public static TransferData CreateEmpty() {
		return new TransferData(string.Empty, DTObject.Empty);
	}
}
