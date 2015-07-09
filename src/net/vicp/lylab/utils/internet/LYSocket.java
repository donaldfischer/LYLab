package net.vicp.lylab.utils.internet;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Callback;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Transmission;
import net.vicp.lylab.core.interfaces.recycle.Recyclable;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;
import net.vicp.lylab.utils.tq.Task;

/**
 * A raw socket can be used for communicating with server, you need close socket after using it.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class LYSocket extends Task implements Recyclable, Transmission {
	private static final long serialVersionUID = 883892527805494627L;
	
	protected Socket socket;

	protected Callback beforeConnect = null;
	protected Callback afterClose = null;
	protected Callback beforeTransmission = null;
	protected Callback afterTransmission = null;
	
	protected InputStream in;
	protected OutputStream out;
	
	private boolean isServer;
	
	protected AtomicInteger socketRetry = new AtomicInteger();
	protected int socketMaxRetry = Integer.MAX_VALUE;
	protected String host;
	protected int port;

	private byte[] readBuffer = new byte[CoreDef.SOCKET_MAX_BUFFER];
	
	public LYSocket(ServerSocket serverSocket) {
		if(serverSocket == null) throw new LYException("Parameter serverSocket is null");
		try {
			this.socket = serverSocket.accept();
			this.host = socket.getInetAddress().getHostAddress();
			this.port = socket.getPort();
			in = socket.getInputStream();
			out = socket.getOutputStream();
			isServer = true;
			setSoTimeout(CoreDef.DEFAULT_SOCKET_TTIMEOUT);
		} catch (Exception e) {
			throw new LYException("Can not establish connection to socket", e);
		}
	}
	
	public LYSocket(String host, Integer port) {
		this.host = host;
		this.port = port;
		isServer = false;
	}

	@Override
	public void exec() {
		try {
			if (isServer()) {
				byte[] bytes = receive();
				if(bytes == null) return;
				send(doResponse(bytes));
				close();
			} else {
				doRequest(null);
			}
		} catch (Exception e) {
			throw new LYException("Connect break", e);
		}
	}

	@Override
	public byte[] request(byte[] request) {
		// do something
		return null;
	}

	@Override
	public byte[] response(byte[] request) {
		// do something
		return null;
	}
	
	public byte[] doRequest(byte[] request) {
		if(beforeTransmission != null)
			beforeTransmission.callback();
		if(isServer()) throw new LYException("Do request is forbidden to a server socket");
		byte[] ret = request(request);
		if(afterTransmission != null)
			afterTransmission.callback(ret);
		return ret;
	}

	public byte[] doResponse(byte[] request) {
		if(beforeTransmission != null)
			beforeTransmission.callback();
		if(!isServer()) throw new LYException("Do response is forbidden to a client socket");
		if(afterTransmission != null)
			afterTransmission.callback();
		byte[] ret = response(request);
		if(afterTransmission != null)
			afterTransmission.callback(ret);
		return ret;
	}
	
	public void connect()
	{
		if(isServer()) return;
		try {
			if(beforeConnect != null)
				beforeConnect.callback();
			socket = new Socket(host, port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
			setSoTimeout(CoreDef.DEFAULT_SOCKET_TTIMEOUT);
		} catch (Exception e) {
			throw new LYException("Can not establish connection to server", e);
		}
	}
	
	public boolean send(byte[] msg) throws Exception {
		if(isClosed()) return false;
		out.write(msg);
		out.flush();
		return true;
	}
	
	public byte[] receive() throws Exception {
		if(isClosed()) throw new LYException("Connection closed");
		Protocol rawProtocol = null;
		if (in != null) {
			int totalRecv = 0, getLen = 0;
			while (true) {
				getLen = 0;
				try {
					if(totalRecv == readBuffer.length)
						readBuffer = Arrays.copyOf(readBuffer, readBuffer.length*10);
					getLen = in.read(readBuffer, totalRecv, readBuffer.length - totalRecv);
				} catch (Exception e) {
					throw new LYException(e);
				}
				if (getLen == -1)
					return null;
				if (getLen == 0)
					throw new LYException("Impossible");
				totalRecv += getLen;
				if(rawProtocol == null)
					rawProtocol = ProtocolUtils.rawProtocol(ProtocolUtils.pairToProtocol(readBuffer));
				int result = ProtocolUtils.validate(rawProtocol, readBuffer, totalRecv);
				if (result == -1)
					throw new LYException("Bad data package");
				if (result == 0)
					break;
				if (result == 1)
					continue;
			}
		}
		return readBuffer;
	}

	@Override
	public void close() throws Exception {
		if (socket != null) {
			socket.shutdownInput();
			socket.shutdownOutput();
			if(!socket.isClosed())
				socket.close();
			socket = null;
			in = null;
			out = null;
		}
		if(afterClose != null)
			afterClose.callback();
	}

	public boolean isClosed() {
		return socket == null || socket.isClosed()
				|| in == null || out == null;
	}

	@Override
	public boolean isRecyclable() {
		return (socketRetry.get() < socketMaxRetry) && !isServer() && isClosed();
	}

	@Override
	public void recycle() {
		if (socketRetry.incrementAndGet() > socketMaxRetry)
			throw new LYException("Socket recycled for too many times");
		recycle(host, port);
	}

	protected void recycle(String host, int port) {
		if(isServer()) return;
		try {
			socket = new Socket(host, port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (Exception e) {
			throw new LYException("Recycle connection failed");
		}
	}

	public void setSoTimeout(int timeout)
	{
		try {
			socket.setSoTimeout(timeout);
		} catch (Exception e) {
            throw new LYException("Set timeout failed", e);
		}
	}
	
	public int getSoTimeout()
	{
		try {
			return socket.getSoTimeout();
		} catch (Exception e) {
			throw new LYException("Socket is closed");
		}
	}

	// getters & setters below
	public int getSocketMaxRetry() {
		return socketMaxRetry;
	}

	public void setSocketMaxRetry(int socketMaxRetry) {
		this.socketMaxRetry = socketMaxRetry;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isServer() {
		return isServer;
	}

	public int getSocketRetry() {
		return socketRetry.get();
	}

	public Callback getBeforeConnect() {
		return beforeConnect;
	}

	public void setBeforeConnect(Callback beforeConnect) {
		this.beforeConnect = beforeConnect;
	}

	public Callback getAfterClose() {
		return afterClose;
	}

	public void setAfterClose(Callback afterClose) {
		this.afterClose = afterClose;
	}

	public Callback getBeforeTransmission() {
		return beforeTransmission;
	}

	public void setBeforeTransmission(Callback beforeTransmission) {
		this.beforeTransmission = beforeTransmission;
	}

	public Callback getAfterTransmission() {
		return afterTransmission;
	}

	public void setAfterTransmission(Callback afterTransmission) {
		this.afterTransmission = afterTransmission;
	}

}
