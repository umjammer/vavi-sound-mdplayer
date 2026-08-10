package org.eclipse.jdt.internal.jarinjarloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class RsrcURLStreamHandler extends URLStreamHandler {
   private final ClassLoader classLoader;

   public RsrcURLStreamHandler(ClassLoader classLoader) {
      this.classLoader = classLoader;
   }

   @Override
   protected URLConnection openConnection(URL u) throws IOException {
      return new RsrcURLConnection(u, this.classLoader);
   }

   @Override
   protected void parseURL(URL url, String spec, int start, int limit) {
      // spec without the "rsrc:" scheme and without the "#ref" part
      String rest = start < limit ? spec.substring(start, limit) : "";
      String file;
      if (rest.isEmpty()) {
         // a ref only spec: `JarURLConnection` resolves "#runtime" against the jar file url to
         // open multi release jars, keep the file or the nested jar becomes unreadable
         file = url.getFile();
      } else if (spec.startsWith("rsrc:")) {
         file = rest;
      } else if (url.getFile().equals("./")) {
         file = rest;
      } else if (url.getFile().endsWith("/")) {
         file = url.getFile() + rest;
      } else {
         file = rest;
      }

      this.setURL(url, "rsrc", "", -1, null, null, file, null, url.getRef());
   }
}
