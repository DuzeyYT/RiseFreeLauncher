package cc.fish.rfl.api.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InputStreamUtil {

  public byte[] readAllBytes(InputStream inputStream) {
    try {
      int bufferSize = 1024;
      byte[] buffer = new byte[bufferSize];
      int bytesRead;
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        byteArrayOutputStream.write(buffer, 0, bytesRead);
      }
      byteArrayOutputStream.flush();
      return byteArrayOutputStream.toByteArray();
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    return null;
  }
}