package kr.co.cjdashboard.api.dashboard.model.res;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListResult<T> extends CommonResult {
	private List<T> list;
}
