/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: TheoraTrack.java,v 1.1 2003/03/03 22:06:12 jarnbjo Exp $
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
 * $Log: TheoraTrack.java,v $
 * Revision 1.1  2003/03/03 22:06:12  jarnbjo
 * no message
 *
 */
 
package de.jarnbjo.jmf;

import java.io.*;

import java.awt.Dimension;

import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;

import de.jarnbjo.ogg.*;
import de.jarnbjo.theora.*;
import de.jarnbjo.util.io.*;

public class TheoraTrack extends OggTrack {

   private LogicalOggStream oggStream;
   private VideoFormat format;

   private Header header;

   public TheoraTrack(LogicalOggStream source, byte[] idHeaderData) throws IOException {
      super(source);
      oggStream=source;
      BitInputStream bd=new ByteArrayBitInputStream(idHeaderData, ByteArrayBitInputStream.BIG_ENDIAN);
      header=new Header(bd);

      format=new VideoFormat("THEORA",
         new Dimension(header.getWidth(), header.getHeight()),
         -1,
         Format.byteArray,
         (float)header.getFrameRate());
   }

   public Format getFormat() {
      return format;
   }

   public Time getDuration() {
      long frame=oggStream.getMaximumGranulePosition()>>6;
      return frame==-1?
         Duration.DURATION_UNKNOWN:
         new Time(((double)frame)/header.getFrameRate());
   }

   /*
   public synchronized void readFrame(javax.media.Buffer buffer) {

      try {
         buffer.setData(oggStream.getNextOggPage());
      }
      catch(EndOfOggStreamException e) {
         buffer.setEOM(true);
         buffer.setOffset(0);
         buffer.setLength(0);
      }
      catch(IOException e) {
         /** @todo find a way to signal an error condition
      }
   }
   */
}