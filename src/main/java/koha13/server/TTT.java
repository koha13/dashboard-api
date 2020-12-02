package koha13.server;

import com.google.gson.Gson;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TTT {
	public static void main(String[] args) throws Exception {
		Gson gson = new Gson();
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
		JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
		Map<String, Object> rs = new HashMap<>();
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
		Set<ObjectName> objectNames = mbsc.queryNames(new ObjectName("org.apache.activemq:*"), null);

		for (ObjectName o : objectNames) {
			String[] spl = o.toString().split("\\:|\\,");
			Map<String, Object> aa = rs;
			for (int i = 0; i < spl.length; i++) {
				if (aa.containsKey(spl[i])) {
					aa = (Map<String, Object>) aa.get(spl[i]);
				} else {
					Map<String, Object> newMap = new HashMap<>();
					aa.put(spl[i], newMap);
					aa = (Map<String, Object>) aa.get(spl[i]);
				}
				if (i == spl.length - 1) {
					MBeanInfo mInfo = mbsc.getMBeanInfo(o);
					Map<String, String> attributeMap = new HashMap<>();
					MBeanAttributeInfo[] attrInfo = mInfo.getAttributes();
					for (MBeanAttributeInfo attr : attrInfo) {
						attributeMap.put(attr.getName(), attr.getDescription());
					}
					aa.put("attribute", attributeMap);
				}
			}
		}
		String aaaaa = gson.toJson(rs).replaceAll("\\\\u003d", "=");
//		aaaaa = aaaaa.replaceAll("\\\\","");
		System.out.println(aaaaa);
	}
}
