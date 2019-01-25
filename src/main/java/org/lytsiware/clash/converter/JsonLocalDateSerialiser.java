package org.lytsiware.clash.converter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


@JsonComponent
public class JsonLocalDateSerialiser extends JsonSerializer<LocalDate> {

	public static final String DD_MM_YYYY = "dd/MM/yyyy";

	@Override
	public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(localDate.format(DateTimeFormatter.ofPattern(DD_MM_YYYY)));
	}
}
