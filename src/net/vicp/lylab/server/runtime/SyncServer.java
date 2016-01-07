package net.vicp.lylab.server.runtime;

import java.net.ServerSocket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.internet.BaseSocket;
import net.vicp.lylab.utils.internet.ToClientLongSocket;
import net.vicp.lylab.utils.internet.ToClientSocket;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.Task;

/**
 * A raw socket can be used for communicating with server, you need close socket after using it.
 * Actually, this class is not as useful as I thought
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class SyncServer extends Task implements LifeCycle {
	private static final long serialVersionUID = 883892527805494627L;
	protected AtomicBoolean isClosed = new AtomicBoolean(true);
	protected ServerSocket serverSocket;
	protected LYTaskQueue lyTaskQueue = null;
	protected Aop aop;
	protected Integer port = null;
	protected boolean isLongServer = false;

	public SyncServer() {
		aop = null;
	}

	public SyncServer(Aop aop) {
		this.aop = aop;
	}
	
	@Override
	public void initialize() {
		if(!isClosed.compareAndSet(true, false))
			return;
		this.begin("Sync Server - Main Thread");
	}
	
	@Override
	public void close() throws Exception {
		if(!isClosed.compareAndSet(false, true))
			return;
		serverSocket.close();
		this.callStop();
	}

	@Override
	public void exec() {
		try {
			if(port == null) throw new NullPointerException("Server port not defined");
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			throw new LYException("Server start failed", e);
		}
		while (!isClosed.get()) {
			try {
				BaseSocket bs = null;
				if(isLongServer)
					bs = new ToClientLongSocket(serverSocket, new SimpleHeartBeat()).setAopLogic(aop);
				else
					bs = new ToClientSocket(serverSocket).setAopLogic(aop);
				if(lyTaskQueue.addTask(bs) == null)
					await(CoreDef.WAITING_SHORT);
			} catch (Exception e) {
				log.error(Utils.getStringFromException(e));
			}
		}
	}

	public void setLyTaskQueue(LYTaskQueue lyTaskQueue) {
		this.lyTaskQueue = lyTaskQueue;
	}

	public void setAop(Aop aop) {
		this.aop = aop;
	}

	public boolean isClosed() {
		return isClosed.get();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isLongServer() {
		return isLongServer;
	}

	public void setIsLongServer(boolean isLongServer) {
		this.isLongServer = isLongServer;
	}

}
