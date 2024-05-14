package apros.codeart.dto;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 各种时间类型的测试
 */
class TimeTest {

	private static class LocalDateTimeAdapter
			implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
		private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

		@Override
		public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
			// 转换 LocalDateTime 到 OffsetDateTime
			OffsetDateTime odt = src.atOffset(ZoneOffset.UTC); // 使用 UTC 时区
			return new JsonPrimitive(formatter.format(odt)); // 使用 ISO 8601 格式
		}

		@Override
		public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return LocalDateTime.parse(json.getAsString(), formatter);
		}
	}

	public static class Person {
		private String name;
		private int age;
		private String email;
		private LocalDateTime birthDate;

		public Person(String name, int age, String email, LocalDateTime birthDate) {
			this.name = name;
			this.age = age;
			this.email = email;
			this.birthDate = birthDate;
		}

		// Getter和Setter
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public LocalDateTime getBirthDate() {
			return birthDate;
		}

		public void setBirthDate(LocalDateTime birthDate) {
			this.birthDate = birthDate;
		}
	}

	private static String getPersonCode() {
		// 创建一个Person对象
		Person person = new Person("John Doe", 30, "john.doe@example.com", Birthday);

		// 创建Gson实例，设置日期格式
		Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

		// 序列化
		String json = gson.toJson(person);

		return json;
	}

	private static LocalDateTime Birthday = LocalDateTime.of(1984, 11, 18, 2, 30, 50);

	@Test
	public void writeLocalDateTime() {
		// 用gson测试浏览器是否可以识别
		DTObject dto = DTObject.editable();
		dto.setLocalDateTime("birthday", Birthday);

		var code = dto.getCode(false, false);

		JsonElement element = JsonParser.parseString(code);
		Assertions.assertTrue(element.isJsonObject());
	}

	@Test
	public void readLocalDateTime() {

		var code = getPersonCode();
		var dto = DTObject.readonly(code);
		var birthday = dto.getLocalDateTime("birthDate");

		Assertions.assertTrue(birthday.equals(Birthday));

	}

	@Test
	public void writeZonedDateTime() {
//		// 用gson测试浏览器是否可以识别
//		DTObject dto = DTObject.editable();
//		dto.setLocalDateTime("birthday", Birthday);
//
//		var code = dto.getCode(false, false);
//
//		JsonElement element = JsonParser.parseString(code);
//		Assertions.assertTrue(element.isJsonObject());
	}
}
