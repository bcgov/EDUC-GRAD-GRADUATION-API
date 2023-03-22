package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonPropertyOrder({ "options", "data", "dataMap"})
public class ReportRequest implements Serializable {

	private static final long serialVersionUID = 2L;

	private ReportData data;
	private Map<String, ReportData> dataMap;
	private ReportOptions options;

	@JsonProperty("data")
	public ReportData getData() {
		return data;
	}
	public void setData(ReportData data) {
		this.data = data;
	}

	@JsonProperty("dataMap")
	public Map<String, ReportData> getDataMap() {
		return dataMap;
	}
	public void setDataMap(Map<String, ReportData> dataMap) {
		this.dataMap = dataMap;
	}

	@JsonProperty("options")
	public ReportOptions getOptions() {
		return options;
	}
	public void setOptions(ReportOptions options) {
		this.options = options;
	}

	@Override
	@SneakyThrows
	public String toString() {
		return new ObjectMapper().writeValueAsString(this);
	}
}
