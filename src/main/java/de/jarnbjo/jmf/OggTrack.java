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
import javax.media.format.*;
import javax.media.protocol.*;

import de.jarnbjo.ogg.*;
import de.jarnbjo.util.io.*;


public class OggTrack implements Track {

   private TrackListener listener;
   private LogicalOggStream source;
   private boolean enabled=true;
   private int channels, sampleRate;
   private long currentBytePos;

   private Format format;

   private final static long VORBIS_HEADER = 0x736962726f7601L;

   protected OggTrack(LogicalOggStream source) {
      this.source=source;
      /*
      format=new AudioFormat(
         "application/octet-stream",
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.byteArray);
         */
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
      /*
      long agp=oggChannel.getCurrentGranulePosition();
      return agp==-1?
         Time.TIME_UNKNOWN:
         new Time(agp*1000000000L/((long)vorbisChannel.getSampleRate()));
         */
   }

   public Format getFormat() {
      return format;
   }

   public Time getStartTime() {
      return Time.TIME_UNKNOWN;
      //return new Time(0);
   }

   public boolean isEnabled() {
      return enabled;
   }

   //public void setNextPage(int nextPage) throws IOException {
   //   vorbisChannel.setNextPage(nextPage);
   //}

   public Time mapFrameToTime(int frameNumber) {
      return Time.TIME_UNKNOWN;
      /*
      long[] agp=oggChannel.getAbsoluteGranulePositions();
      if(agp==null) {
         return Time.TIME_UNKNOWN;
      }
      else {
         //frameNumber+=3;
         long six=oggChannel.getAbsoluteGranulePositions()[frameNumber];
         return new Time(six*1000000000L/((long)vorbisChannel.getSampleRate()));
      }
      */
   }

   public int mapTimeToFrame(Time t) {
      /*
      long[] agp=oggChannel.getAbsoluteGranulePositions();
      if(agp!=null) {
         long six=t.getNanoseconds()*((long)vorbisChannel.getSampleRate())/1000000000L;
         for(int i=0; i<agp.length; i++) {
            if(agp[i]>six) {
               return i;//-3;
            }
         }
      }*/
      return FRAME_UNKNOWN;
   }


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
         /*
         try {
            //vorbisChannel.setNextPage(0);
         }
         catch(IOException ex) {}
         */
      }
      catch(IOException e) {
         /** @todo find a way to signal an error condition */
      }
   }

   public void setEnabled(boolean t) {
      enabled=t;
   }

   public void setTrackListener(TrackListener listener) {
      this.listener=listener;
   }

   public Time getDuration() {
      return Duration.DURATION_UNKNOWN;
      //long nos=oggChannel.getNumberOfSamples();
      //return nos==-1?
      //   Duration.DURATION_UNKNOWN:
      //   new Time(nos*1000000000L/((long)vorbisChannel.getSampleRate()));
   }

}