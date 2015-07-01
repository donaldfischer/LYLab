package net.vicp.lylab.core;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.interfaces.Transcode;
import flexjson.JSONDeserializer;

/**
 * Extends TranscodeProtocol means it could be encoding into protocol.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.03.17
 * @version 1.0.0
 * 
 */
public abstract class TranscodeObject extends CloneableBaseObject implements Transcode {
	public abstract Protocol encode();
	public abstract String[] exclude(String ... excludeRule);
	public Object decode(Protocol protocol)
	{
		if(protocol == null)
			throw new LYException("Parameter protocol is null");
		if(protocol.getInfo() == null)
			throw new LYException("Inner info is null");
		if(protocol.getData() == null)
			throw new LYException("Inner data is null");
		try {
			return new JSONDeserializer<Object>().use(null, Class.forName(new String(protocol.getInfo()))).deserialize(new String(protocol.getData(), CoreDef.CHARSET));
		} catch (Exception e) {
			throw new LYException("Failed to convert data into specific class:" + new String(protocol.getInfo()) + ". Maybe the data isn't json?", e);
		}
	}
	
}