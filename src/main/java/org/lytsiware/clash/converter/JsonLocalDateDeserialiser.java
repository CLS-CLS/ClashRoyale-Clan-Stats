package org.lytsiware.clash.converter;

import static org.lytsiware.clash.converter.JsonLocalDateSerialiser.DD_MM_YYYY;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.jackson.JsonComponent;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

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
