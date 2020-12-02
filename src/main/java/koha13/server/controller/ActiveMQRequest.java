package koha13.server.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveMQRequest {
	String jmxUrl;
	String username, password;
	List<Bunch> bunchList;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Bunch{
		String objectName;
		String attribute;
	}
}
