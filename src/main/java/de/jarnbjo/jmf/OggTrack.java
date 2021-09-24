/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: OggTrack.java,v 1.1 2003/03/03 22:06:12 jarnbjo Exp $
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
 * $Log: OggTrack.java,v $
 * Revision 1.1  2003/03/03 22:06:12  jarnbjo
 * no message
 *
 */
 
package de.jarnbjo.jmf;

import java.io.*;
import javax.media.*;
import de.jarnbjo.ogg.*;


public class OggTrack implements Track {

   private TrackListener listener;
   final private LogicalOggStream source;
   private boolean enabled=true;
   private int channels, sampleRate;
   private long currentBytePos;

   private Format format;

   private final static long VORBIS_HEADER = 0x736962726f7601L;

   protected OggTrack(LogicalOggStream source) {
      this.source=source;
   }


   public static OggTrack createInstance(LogicalOggStream source) throws IOException {
      if(source.getFormat()==LogicalOggStream.FORMAT_VORBIS) {
         byte[] data=source.getNextOggPacket();
         source.reset();
         System.out.println("VorbisTrack");
         return new VorbisTrack(source, data);
      }
      else if(source.getFormat()==LogicalOggStream.FORMAT_THEORA) {
         byte[] data=source.getNextOggPacket();
         source.reset();
         System.out.println("TheoraTrack");
         return new TheoraTrack(source, data);
      }
      else if(source.getFormat()==LogicalOggStream.FORMAT_FLAC) {
         byte[] data=source.getNextOggPacket();
         data=source.getNextOggPacket();
         source.reset();
         System.out.println("FlacTrack");
         return new FlacTrack(source, data);
      }
      System.out.println("OggTrack");
      return new OggTrack(source);
   }

   public Time getMediaTime() {
      return Time.TIME_UNKNOWN;
   }

   @Override
   public Format getFormat() {
      return format;
   }

   @Override
   public Time getStartTime() {
      return Time.TIME_UNKNOWN;
      //return new Time(0);
   }

   @Override
   public boolean isEnabled() {
      return enabled;
   }

   @Override
   public Time mapFrameToTime(int frameNumber) {
      return Time.TIME_UNKNOWN;
   }

   @Override
   public int mapTimeToFrame(Time t) {
      return FRAME_UNKNOWN;
   }


   @Override
   public synchronized void readFrame(javax.media.Buffer buffer) {

      try {
         byte[] packet=source.getNextOggPacket();
         byte[] byteArray=(byte[])buffer.getData();
         if(byteArray==null || packet.length>byteArray.length) {
            buffer.setData(packet);
         }
         else {
            System.arraycopy(packet, 0, byteArray, 0, packet.length);
         }
         buffer.setOffset(0);
         buffer.setLength(packet.length);
      }
      catch(EndOfOggStreamException e) {
         buffer.setEOM(true);
         buffer.setOffset(0);
         buffer.setLength(0);
      }
      catch(IOException e) {
         /** @todo find a way to signal an error condition */
      }
   }

   @Override
   public void setEnabled(boolean t) {
      enabled=t;
   }

   @Override
   public void setTrackListener(TrackListener listener) {
      this.listener=listener;
   }

   @Override
   public Time getDuration() {
      return Duration.DURATION_UNKNOWN;
   }

}