package apros.codeart.mq;

import apros.codeart.dto.DTObject;
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

	private byte[] _binaryData;

	/**
	 * 二进制数据
	 * 
	 * @return
	 */
	public byte[] binaryData() {
		return _binaryData;
	}

//	#endregion

	public TransferData(String language, DTObject info, int binaryDataLength, byte[] binaryData) {
		_language = language;
		_info = info;
		_binaryDataLength = binaryDataLength;
		_binaryData = binaryData;
	}

	public TransferData(String language, DTObject info) {
		_language = language;
		_info = info;
		_binaryDataLength = 0;
		_binaryData = ListUtil.emptyByts();
	}

	public static TransferData Deserialize(byte[] content)
	 {
	     using (var temp = ByteBuffer.Borrow(content.Length))
	     {
	         var source = temp.Item;
	         source.Write(content);

	         var language = source.ReadString();

	         var dtoLength = source.ReadInt32();
	         var dtoData = source.ReadBytes(dtoLength);

	         DTObject dto = DTObject.Create(dtoData);

	         int binaryLength = 0;
	         byte[] binaryData = Array.Empty<byte>();

	         if (source.ReadPosition < source.Length)
	         {
	             binaryLength = source.ReadInt32();
	             var thisTimeLength = source.ReadInt32();
	             binaryData = source.ReadBytes(thisTimeLength);
	         }

	         return new TransferData(language, dto, binaryLength, binaryData);
	     }
	 }

	public static byte[] Serialize(TransferData result)
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
