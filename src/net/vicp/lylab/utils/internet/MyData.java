//package net.vicp.lylab.utils.internet;
//
//import net.vicp.lylab.core.TranscodeProtocol;
//
//public class MyData extends TranscodeProtocol {
//	public static void main(String[] arg)
//	{
//		MyData m = new MyData();
//		m.setValue("我是MyData");
//		Protocol p = m.encode();
//		Protocol p2 = Protocol.fromBytes(p.toBytes());
//		System.out.println(p2);
//		System.out.println(((MyData) MyData.decode(p2)).getValue());
//		byte[] bytes = {76, 89, 76, 97, 98, -15, 0, 0, 0, 12, -15, 110, 101, 116, 46, 118, 105, 99, 112, 46, 108, 121, 108, 97, 98, 46, 117, 116, 105, 108, 115, 46, 105, 110, 116, 101, 114, 110, 101, 116, 46, 77, 121, 68, 97, 116, 97, -15, -26, -120, -111, -26, -104, -81, 77, 121, 68, 97, 116, 97};
//		System.out.println("len=61:\t"+Protocol.validate(bytes, 61));
//		System.out.println("len=60:\t"+Protocol.validate(bytes, 60));
//		System.out.println("len=59:\t"+Protocol.validate(bytes, 59));
//		System.out.println("total is " + bytes.length);
//	}
//
//	String value;
//	
//	public String getValue() {
//		return value;
//	}
//
//	public void setValue(String value) {
//		this.value = value;
//	}
//
//}
