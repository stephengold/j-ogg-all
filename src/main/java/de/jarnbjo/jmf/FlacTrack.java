/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: FlacTrack.java,v 1.1 2003/03/03 22:06:12 jarnbjo Exp $
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
 * $Log: FlacTrack.java,v $
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
import de.jarnbjo.flac.*;
import de.jarnbjo.util.io.*;


public class FlacTrack extends OggTrack {

   private LogicalOggStream oggStream;
   private StreamInfo streamInfo;
   private AudioFormat format;

   public FlacTrack(LogicalOggStream source, byte[] siHeaderData) throws FlacFormatException, IOException {
      super(source);
      oggStream=source;
      BitInputStream bd=new ByteArrayBitInputStream(siHeaderData, ByteArrayBitInputStream.BIG_ENDIAN);
      streamInfo=(StreamInfo)MetadataBlock.createInstance(bd);

      format=new AudioFormat(
         "audio/x-flac",
         (double)streamInfo.getSampleRate(),
         16,
         streamInfo.getChannels(),
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.NOT_SPECIFIED,
         Format.byteArray);
   }

   public Format getFormat() {
      return format;
   }

   public Time getDuration() {
      long nos=oggStream.getMaximumGranulePosition();
      return nos==-1?
         Duration.DURATION_UNKNOWN:
         new Time(nos*1000000000L/((long)streamInfo.getSampleRate()));
   }

   protected int getSampleRate() {
      return streamInfo.getSampleRate();
   }

}