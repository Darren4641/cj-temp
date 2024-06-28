package kr.co.cjdashboard.exception.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import kr.co.cjdashboard.api.dashboard.model.res.FieldErrorDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;


@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Builder
public final class ExceptionMsg implements Serializable {
	
	private static final long serialVersionUID = 1L;

	
	@JsonInclude(value = Include.NON_NULL)
	private Throwable throwable;
	
	private final String msg;

	private final int code;

	private final  boolean success;

	private List<FieldErrorDetail> errors;

}










