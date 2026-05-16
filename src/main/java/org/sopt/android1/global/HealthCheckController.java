package org.sopt.android1.global;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
	@GetMapping("/health")
	public String healthCheck() {
		return "already";
	}
}
