package kr.co.cjdashboard.api.dashboard.model.res;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CommonResult {

	// 응답 성공여부 : true/false
	private boolean success;

	// 응답 코드 번호 : > 0 정상, < 0 비정상
	private int code;

	// 응답 메시지
	private String msg;

	// 에러 메시지
	private List<FieldErrorDetail> errors = new ArrayList<>();
}
