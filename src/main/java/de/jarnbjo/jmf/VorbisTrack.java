package de.jarnbjo.jmf;

import java.io.*;

import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;

import de.jarnbjo.ogg.*;
import de.jarnbjo.vorbis.*;
import de.jarnbjo.util.io.*;

/**
 * <p>Überschrift: JOgg</p>
 * <p>Beschreibung: Java Ogg implementation</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Organisation: </p>
 * @author Tor-Einar Jarnbjo
 * @version 1.0
 */

public class VorbisTrack extends OggTrack {

   private LogicalOggStream oggStream;
   private IdentificationHeader identificationHeader;
   private AudioFormat format;

   public VorbisTrack(LogicalOggStream source, byte[] idHeaderData) throws VorbisFormatException, IOException {
      super(source);
      oggStream=source;
      BitInputStream bd=new ByteArrayBitInputStream(idHeaderData);
      bd.getInt(8);
      identificationHeader=new IdentificationHeader(bd);
      format=new AudioFormat(
         "audio/x-vorbis",
         (double)identificationHeader.getSampleRate(),
         16,
         identificationHeader.getChannels(),
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
         new Time(nos*1000000000L/((long)identificationHeader.getSampleRate()));
   }

   protected int getSampleRate() {
      return identificationHeader.getSampleRate();
   }

}