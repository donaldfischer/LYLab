package net.vicp.lylab.server.filter;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.utils.internet.async.BaseSocket;
import net.vicp.lylab.utils.internet.impl.Message;

public interface Filter {
	public Message doFilter(BaseSocket socket, Message request) throws LYException;
	
}
