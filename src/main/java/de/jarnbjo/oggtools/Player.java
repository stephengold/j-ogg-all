/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: Player.java,v 1.4 2003/04/10 19:48:40 jarnbjo Exp $
 * -----------------------------------------------------------
 *
 * $Author: jarnbjo $
 *
 * Description:
 *
 * Copyright 2002-2003 Tor-Einar Jarnbjo
 * -----------------------------------------------------------
 *
 * Change History
 * -----------------------------------------------------------
 * $Log: Player.java,v $
 * Revision 1.4  2003/04/10 19:48:40  jarnbjo
 * no message
 *
 * Revision 1.3  2003/04/04 08:32:54  jarnbjo
 * no message
 *
 * Revision 1.2  2003/03/31 00:22:29  jarnbjo
 * no message
 *
 * Revision 1.1  2003/03/16 01:10:45  jarnbjo
 * no message
 *
 */

package de.jarnbjo.oggtools;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

import de.jarnbjo.ogg.*;
import de.jarnbjo.vorbis.*;
import de.jarnbjo.flac.*;

public class Player {

   private static FlacStream fs;

   public static void main(String[] args) {

      System.out.println("JOggPlayer v1.0");
      System.out.println("Please send bug reports to Tor-Einar@Jarnbjo.de");
      System.out.println("");

      if(args.length!=1) {
         System.out.println("Usage: java -jar JOggPlayer.jar <URL>");
         System.exit(0);
      }

      try {
         final UncachedUrlStream os=new UncachedUrlStream(new URL(args[0]));
         
         // get the first logical Ogg stream
         final LogicalOggStream los=(LogicalOggStream)os.getLogicalStreams().iterator().next();

         if(los.getFormat()!=LogicalOggStream.FORMAT_VORBIS) {
            System.err.println("Not a plain Ogg/Vorbis-file. Unable to play.");
            System.exit(1);
         }

         final VorbisStream vs=new VorbisStream(los);

         System.out.println("Title:  "+vs.getCommentHeader().getTitle());
         System.out.println("Artist: "+vs.getCommentHeader().getArtist());

         VorbisInputStream vis=new VorbisInputStream(vs);

         AudioFormat audioFormat=new AudioFormat(
            (float)vs.getIdentificationHeader().getSampleRate(),
            16,
            vs.getIdentificationHeader().getChannels(),
            true, true);

         AudioInputStream ais=new AudioInputStream(vis, audioFormat, -1);
         DataLine.Info dataLineInfo=new DataLine.Info(SourceDataLine.class, audioFormat);

         SourceDataLine sourceDataLine=(SourceDataLine)AudioSystem.getLine(dataLineInfo);

         FloatControl gainControl=null;

         sourceDataLine.open(audioFormat);
         sourceDataLine.start();

         byte[] buffer=new byte[8192];
         int cnt=0;

         while((cnt = ais.read(buffer, 0, buffer.length)) != -1) {
            if(cnt > 0){
               sourceDataLine.write(buffer, 0, cnt);
            } //end if
         } //end while

         sourceDataLine.drain();
         sourceDataLine.close();

         System.exit(0);
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   public static class VorbisInputStream extends InputStream {

      private VorbisStream source;
      private byte[] buffer=new byte[8192];

      public VorbisInputStream(VorbisStream source) {
         this.source=source;
      }

      public int read() throws IOException {
         return 0;
      }

      public int read(byte[] buffer) throws IOException {
         return read(buffer, 0, buffer.length);
      }

      public int read(byte[] buffer, int offset, int length) throws IOException {
         try {
            return source.readPcm(buffer, offset, length);
         }
         catch(EndOfOggStreamException e) {
            return -1;
         }
      }
   }

}