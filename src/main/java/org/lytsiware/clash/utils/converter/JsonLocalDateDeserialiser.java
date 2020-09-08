package org.lytsiware.clash.utils.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.lytsiware.clash.utils.converter.JsonLocalDateSerialiser.DD_MM_YYYY;

@JsonComponent
public class JsonLocalDateDeserialiser extends JsonDeserializer<LocalDate> {

	@Override
	public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		String dateAsString = jsonParser.getValueAsString();
		if (StringUtils.isEmpty(dateAsString)) {
			return null;
		}
		return LocalDate.parse(dateAsString, DateTimeFormatter.ofPattern(DD_MM_YYYY));

	}
}
