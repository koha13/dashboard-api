package koha13.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JmxRequest {
  String jmxUrl;
  String objectName;
  String username;
  String password;
  String attribute;
}
