package org.eclipse.jdt.internal.jarinjarloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


public class RsrcURLConnection extends URLConnection {
   private ClassLoader classLoader;

   public RsrcURLConnection(URL url, ClassLoader classLoader) {
      super(url);
      this.classLoader = classLoader;
   }

   @Override
   public void connect() throws IOException {
   }

   @Override
   public InputStream getInputStream() throws IOException {
      String file = URLDecoder.decode(super.url.getFile(), StandardCharsets.UTF_8);
      InputStream result = this.classLoader.getResourceAsStream(file);
      if (result == null) {
         throw new MalformedURLException("Could not open InputStream for URL '" + super.url + "'");
      } else {
         return result;
      }
   }
}
