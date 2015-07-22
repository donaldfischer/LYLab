package net.vicp.lylab.utils.internet.async_test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.HeartBeat;
import net.vicp.lylab.utils.internet.impl.LYLabProtocol;
import net.vicp.lylab.utils.tq.Task;

public class Client_ extends Task {
	private static final long serialVersionUID = -2269033856733289392L;

	Protocol protocol = new LYLabProtocol();
	// 信道选择器
	private Selector selector;
	// 与服务器通信的信道
	SocketChannel socketChannel;
	// 要连接的服务器Ip地址
	private String hostIp;
	// 要连接的远程服务器在监听的端口
	private int hostListenningPort;

	public Client_(String HostIp, int HostListenningPort) throws IOException {
		this.hostIp = HostIp;
		this.hostListenningPort = HostListenningPort;

		initialize();
	}

	/**
	 * 初始化
	 * 
	 * @throws IOException
	 */
	private void initialize() throws IOException {
		// 打开监听信道并设置为非阻塞模式
		socketChannel = SocketChannel.open(new InetSocketAddress(hostIp,
				hostListenningPort));
		socketChannel.configureBlocking(false);

		// 打开并注册选择器到信道
		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_READ);

		// 启动读取线程
		begin();
	}

	/**
	 * 发送字符串到服务器
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void sendMsg(byte[] message) throws IOException {
		ByteBuffer writeBuffer = ByteBuffer.wrap(message);
		socketChannel.write(writeBuffer);
	}

	public static void main(String[] args) throws IOException {
		@SuppressWarnings("unused")
		Client_ client = new Client_("localhost", 8888);
		
//		Protocol protocol= new LYLabProtocol();
//		SimpleMessage msg = new SimpleMessage();
//		client.sendMsg(protocol.encode(msg));
//		client.sendMsg(protocol.encode(msg));
//		client.sendMsg(protocol.encode(msg));
//		client.sendMsg(protocol.encode(msg));
//		client.sendMsg(protocol.encode(msg));
	}
    
	ByteBuffer buffer = ByteBuffer.allocate(1024);
	private Object heartBeat;

	
	private byte[] readTail = new byte[2000];
	private int readTailLen = 0;

//	private void reserve(byte[] last, int start, int len) {
//		readTailLen = len - start;
//		for (int i = start; i < len; i++) {
//			readTail[i - start] = last[i];
//		}
//	}

//	private byte[] useReserved(byte[] bb) {
//		byte[] out = byteCat(readTail, 0, readTailLen, bb, 0, bb.length);
//		readTailLen = 0;
//		return out;
//	}

	private byte[] bytecat(byte[] pre, int preOffset, int preCopyLenth, byte[] suf, int sufOffset, int sufCopyLenth) {
		byte[] out = new byte[preCopyLenth - preOffset - sufOffset + sufCopyLenth];
		int i;
		for (i = preOffset; i < preCopyLenth; i++)
			out[i - preOffset] = pre[i];
		for (int j = sufOffset; j < sufCopyLenth; j++)
			try {
				out[i - preOffset - sufOffset + j] = suf[j];
			} catch (Exception e) {
				System.err.print("ilen/i/preOffset/sufOffset/sufCopyLenth/j/suf.len" + "\t"+(preCopyLenth - preOffset - sufOffset + sufCopyLenth) + "\t" + i + "\t"
						+ preOffset + "\t" + sufOffset + "\t" + sufCopyLenth + "\t" + j + "\t" + suf.length);
				throw new LYException(e);
			}
		return out;
	}

	private void receiveBasedAopDrive(SocketChannel socketChannel) throws Exception {
		if (socketChannel != null) {
			int len;
			boolean notFinished = true;
			int attempts = 0;
			while (notFinished) {
				buffer.clear();
				len = socketChannel.read(buffer);
				if (len == 0) {
					if (attempts > 3) {
						try {
							socketChannel.close();
						} catch (Exception e) {
							throw new LYException(
									"Lost connection to client, and close socket channel failed",
									e);
						}
						throw new LYException("Lost connection to client");
					}
					attempts++;
					continue;
				}
				if (len == -1)
				{
					socketChannel.write(ByteBuffer.wrap(protocol.encode(new SimpleHeartBeat())));
					break;
				}
				len += readTailLen;

				byte[] ret = bytecat(readTail, 0, readTailLen, buffer.array(),
						0, buffer.array().length);
				readTailLen = 0;

				int start = 0, next = 0;
				while ((next = protocol.validate(ret, start, len)) != 0) {
					// <<--------------------------
					// receive-based aop drive
					Message requestMsg = null;
					Message responseMsg = null;
					try {
						Object obj = protocol.decode(ret, start);
						if (obj instanceof HeartBeat) {
							send(socketChannel,
									ByteBuffer.wrap(protocol.encode(heartBeat)));
							continue;
						}
						requestMsg = (Message) obj;
					} catch (Exception e) {
						log.debug(Utils.getStringFromException(e));
					}
					if (requestMsg == null) {
						responseMsg = new Message();
						responseMsg.setCode(0x00001);
						responseMsg.setMessage("Message not found");
					} else {
						// TODO
						responseMsg = new Message();
						responseMsg.setUuid(requestMsg.getUuid());
						id++;
						// response = aop.doAction(null, msg);
					}
					byte[] response = (protocol == null ? null : protocol
							.encode(responseMsg));

					send(socketChannel, ByteBuffer.wrap(response));
					// <<--------------------------
					start = next;
				}
				System.out.println(len + "\t" + start);
				
				if (start == len) {
					break;
				}
				readTailLen = len - start;
				for (int i = start; i < len; i++) {
					readTail[i - start] = ret[i];
				}
			}
//				reserve(ret, start, len);
		}
	}
	int id=0;	//TODO
	private void send(SocketChannel socketChannel, ByteBuffer wrap) {
		// TODO Auto-generated method stub
		
	}

	public void exec() {
		try {
			while (selector.select() > 0) {
				// 遍历每个有可用IO操作Channel对应的SelectionKey
				for (SelectionKey sk : selector.selectedKeys()) {
					// 如果该SelectionKey对应的Channel中有可读的数据
					if (sk.isReadable()) {
						// 使用NIO读取Channel中的数据
						SocketChannel sc = (SocketChannel) sk.channel();
						receiveBasedAopDrive(sc);
					}
					// 删除正在处理的SelectionKey
					selector.selectedKeys().remove(sk);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				selector.close();
			} catch (Exception ex) {
				log.info(Utils.getStringFromException(ex));
			}
		}
		System.out.println("请求总数:"+id);
	}
}