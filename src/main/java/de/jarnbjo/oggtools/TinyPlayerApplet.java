/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: TinyPlayerApplet.java,v 1.2 2003/04/10 19:48:40 jarnbjo Exp $
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
 * $Log: TinyPlayerApplet.java,v $
 * Revision 1.2  2003/04/10 19:48:40  jarnbjo
 * no message
 *
 * Revision 1.1  2003/03/31 00:22:29  jarnbjo
 * no message
 *
 * Revision 1.1  2003/03/16 01:10:45  jarnbjo
 * no message
 *
 */

package de.jarnbjo.oggtools;

import java.applet.Applet;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.sound.sampled.*;

import de.jarnbjo.ogg.*;
import de.jarnbjo.vorbis.*;

public class TinyPlayerApplet extends Applet implements Runnable {

   private boolean running=false;
   private boolean initialized=false;
   private VorbisStream vStream;
   private LogicalOggStream loStream;

   public void init() {
   }

   public void start() {
      new Thread(this).start();
   }

   public void stop() {
      running=false;
   }

   public void run() {

      try {
         String url=getParameter("url");

         try {
            running=true;

            final UncachedUrlStream os=new UncachedUrlStream(new URL(getCodeBase(), url));
            final LogicalOggStream los=(LogicalOggStream)os.getLogicalStreams().iterator().next();
            final VorbisStream vs=new VorbisStream(los);
            vStream=vs;
            loStream=los;

            initialized=true;

            AudioFormat audioFormat=new AudioFormat(
               (float)vs.getIdentificationHeader().getSampleRate(),
               16,
               vs.getIdentificationHeader().getChannels(),
               true, true);

            DataLine.Info dataLineInfo=new DataLine.Info(SourceDataLine.class, audioFormat);

            SourceDataLine sourceDataLine=(SourceDataLine)AudioSystem.getLine(dataLineInfo);

            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            VorbisInputStream vis=new VorbisInputStream(vs);
            AudioInputStream ais=new AudioInputStream(vis, audioFormat, -1);

            byte[] buffer=new byte[4096];
            int cnt=0, offset=0;
            int total=0;
            long sampleRate=vs.getIdentificationHeader().getSampleRate();
            long oldLt=0;

            while(running) {
               offset=0;
               while(offset<buffer.length && (cnt = ais.read(buffer, offset, buffer.length-offset))>0) {
                  offset+=cnt;
               }
               if(cnt==-1) {
                  running=false;
               }
               if(offset > 0){
                  sourceDataLine.write(buffer, 0, offset);
                  total+=offset;
               }
               offset=0;
               cnt=0;
            }

            sourceDataLine.drain();
            sourceDataLine.close();
         }
         catch(Exception e) {
            e.printStackTrace();
         }
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   public static class VorbisInputStream extends InputStream {

      private VorbisStream source;

      public VorbisInputStream(VorbisStream source) {
         this.source=source;
      }

      public int read() throws IOException {
         return 0;
      }

      public int read(byte[] buffer) throws IOException {
         return read(buffer, 0, buffer.length);
      }

      private static int cnt=0;

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