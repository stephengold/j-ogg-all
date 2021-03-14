/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: VorbisDecoder.java,v 1.1 2003/03/03 22:06:12 jarnbjo Exp $
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
 * $Log: VorbisDecoder.java,v $
 * Revision 1.1  2003/03/03 22:06:12  jarnbjo
 * no message
 *
 */
 
package de.jarnbjo.jmf;

import java.io.IOException;

import javax.media.*;
import javax.media.format.*;

import de.jarnbjo.vorbis.*;

public class VorbisDecoder implements Codec {

   private static final String CODEC_NAME = "Vorbis decoder";

   private static final Format[] supportedInputFormats=new Format[] {
      new AudioFormat(
         "audio/x-vorbis",
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.byteArray),
      new AudioFormat(
         "audio/vorbis",
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.byteArray),
   };

   private static final Format[] supportedOutputFormats=new Format[] {
      new AudioFormat(
         AudioFormat.LINEAR,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.byteArray)
   };

   private VorbisStream vorbisStream=new VorbisStream();

   public VorbisDecoder() {
   }

   public Format[] getSupportedInputFormats() {
      return supportedInputFormats;
   }

   public Format[] getSupportedOutputFormats(Format input) {
      if(input==null) {
         return supportedOutputFormats;
      }
      else {
         AudioFormat[] res=new AudioFormat[1];
         res[0]=new AudioFormat(
            AudioFormat.LINEAR,
            ((AudioFormat)input).getSampleRate(),
            ((AudioFormat)input).getSampleSizeInBits(),
            ((AudioFormat)input).getChannels(),
            AudioFormat.BIG_ENDIAN,
            AudioFormat.SIGNED,
            Format.NOT_SPECIFIED,
            Format.NOT_SPECIFIED,
            Format.byteArray);
         return res;
      }
   }

   public int process(Buffer in, Buffer out) {
      //System.out.println("process");
      try {
         byte[] res=vorbisStream.processPacket((byte[])in.getData());
         if(res==null) {
            return PlugIn.OUTPUT_BUFFER_NOT_FILLED;
         }
         else {
            byte[] buffer=(byte[])out.getData();
            if(buffer==null || res.length>buffer.length) {
               out.setData(res);
            }
            else {
               System.arraycopy(res, 0, buffer, 0, res.length);
            }
            out.setOffset(0);
            long time=vorbisStream.getCurrentGranulePosition()*1000000000L/vorbisStream.getIdentificationHeader().getSampleRate();
            //System.out.println("s: "+time);
            out.setTimeStamp(time);
            out.setLength(res.length);
         }
         return PlugIn.BUFFER_PROCESSED_OK;
      }
      catch(VorbisFormatException e) {
         return PlugIn.BUFFER_PROCESSED_FAILED;
      }
      catch(IOException e) {
         return PlugIn.BUFFER_PROCESSED_FAILED;
      }
   }

   public Format setInputFormat(Format format) {
      return format;
   }

   public Format setOutputFormat(Format format) {
      return format;
   }

   public void open() {
   }

   public void close() {
   }

   public void reset() {
   }

   public String getName() {
      return CODEC_NAME;
   }

   public Object getControl(String controlType) {
      return null;
   }

   public Object[] getControls() {
      return null;
   }
}