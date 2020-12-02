package koha13.server.controller;

import com.google.gson.Gson;
import koha13.server.model.JmxPreviewRequest;
import org.springframework.web.bind.annotation.*;

import koha13.server.model.JmxRequest;
import koha13.server.model.RedisRequest;
import redis.clients.jedis.Jedis;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

@RestController
@CrossOrigin
public class JmxController {
  @PostMapping(value = "/get")
  public Object getJmxAttr(@RequestBody JmxRequest jmxRequest) throws Exception {
    JMXServiceURL url = new JMXServiceURL(jmxRequest.getJmxUrl());
    JMXConnector jmxc = null;
    if (!jmxRequest.getUsername().isEmpty() && !jmxRequest.getPassword().isEmpty()) {
      Map<String, Object> environment = new HashMap();
      String[] credentials = new String[] { jmxRequest.getUsername(), jmxRequest.getPassword() };
      environment.put(JMXConnector.CREDENTIALS, credentials);
      jmxc = JMXConnectorFactory.connect(url, environment);
    } else {
      jmxc = JMXConnectorFactory.connect(url, null);
    }
    MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
    try {
      Object data = mbsc.getAttribute(new ObjectName(jmxRequest.getObjectName()), jmxRequest.getAttribute());
      return data;
    } catch (Exception e) {
      return 0;
    } finally {
      jmxc.close();
    }
  }

  @GetMapping("/activemq")
  public Object getMQattrs(@RequestBody ActiveMQRequest request) throws Exception {
    JMXServiceURL url = new JMXServiceURL(request.getJmxUrl());
    JMXConnector jmxc = null;
    Map<String, Object> rs = new HashMap<>();
    if (!request.getUsername().isEmpty() && !request.getPassword().isEmpty()) {
      Map<String, Object> environment = new HashMap();
      String[] credentials = new String[] { request.getUsername(), request.getPassword() };
      environment.put(JMXConnector.CREDENTIALS, credentials);
      jmxc = JMXConnectorFactory.connect(url, environment);
    } else {
      jmxc = JMXConnectorFactory.connect(url, null);
    }
    MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
    for(ActiveMQRequest.Bunch b : request.bunchList){
      Object data = mbsc.getAttribute(new ObjectName(b.getObjectName()), b.getAttribute());
      rs.put(b.getObjectName()+"-"+b.getAttribute(), data);
    }
    return rs;
  }

  @PostMapping(value = "/previewjmx")
  public Map priview(@RequestBody JmxPreviewRequest request) throws Exception{
    JMXServiceURL url = new JMXServiceURL(request.getUrl());
    JMXConnector jmxc = null;
    if (!request.getUsername().isEmpty() && !request.getPassword().isEmpty()) {
      Map<String, Object> environment = new HashMap();
      String[] credentials = new String[] { request.getUsername(), request.getPassword() };
      environment.put(JMXConnector.CREDENTIALS, credentials);
      jmxc = JMXConnectorFactory.connect(url, environment);
    } else {
      jmxc = JMXConnectorFactory.connect(url, null);
    }
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
    return rs;
  }

  @GetMapping("/redis")
  public Object getRedis(@RequestBody RedisRequest redisRequest) {
    Jedis jedis = new Jedis(redisRequest.getUrl());
    String data = jedis.info(redisRequest.getSection());
    System.out.println(data);
    jedis.close();
    String[] spl = data.split("\r\n");
    for (int i = 1; i < spl.length; i++) {
      String[] spl2 = spl[i].split(":");
      if (spl2[0].equals(redisRequest.getAttr())) {
        Object rs = null;
        try {
          rs = Double.parseDouble(spl2[1]);
          return rs;
        } catch (Exception e) {
          return spl2[1];
        }
      }
    }
    return 0;
  }

}
