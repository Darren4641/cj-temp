package kr.co.cjdashboard.api.dashboard.model.res;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class FieldErrorDetail {
	private String field;
	private String message;
}
