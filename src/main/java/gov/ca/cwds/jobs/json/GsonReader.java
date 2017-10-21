package gov.ca.cwds.jobs.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import com.google.gson.Gson;

// @Provider
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class GsonReader<T> implements MessageBodyReader<T> {

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] antns, MediaType mt) {
    return true;
  }

  @Override
  public T readFrom(Class<T> type, Type genericType, Annotation[] antns, MediaType mt,
      MultivaluedMap<String, String> mm, InputStream in)
      throws IOException, WebApplicationException {
    return new Gson().fromJson(convertStreamToString(in), type);
  }

  private String convertStreamToString(InputStream inputStream) throws IOException {
    if (inputStream != null) {
      Writer writer = new StringWriter();

      char[] buffer = new char[1024]; // NOTE: make configurable.
      try (Reader reader =
          new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
        int n;
        while ((n = reader.read(buffer)) != -1) {
          writer.write(buffer, 0, n);
        }
      } finally {
        inputStream.close();
      }
      return writer.toString();
    } else {
      return "";
    }
  }

}
