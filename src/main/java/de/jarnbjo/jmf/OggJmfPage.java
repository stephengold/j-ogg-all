/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: OggJmfPage.java,v 1.2 2003/03/31 00:23:18 jarnbjo Exp $
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
 * $Log: OggJmfPage.java,v $
 * Revision 1.2  2003/03/31 00:23:18  jarnbjo
 * no message
 *
 * Revision 1.1  2003/03/03 22:06:12  jarnbjo
 * no message
 *
 */

package de.jarnbjo.jmf;

import java.io.*;
import javax.media.protocol.PullSourceStream;

import de.jarnbjo.util.io.*;

import de.jarnbjo.ogg.*;

/**
 * <p>An instance of this class represents an ogg page read from an ogg file
 * or network stream. It has no public constructor, but instances can be
 * created by the <code>create</code> methods, supplying a JMF stream or
 * a <code>RandomAccessFile</code>
 * which is positioned at the beginning of an Ogg page.</p>
 *
 * <p>Furthermore, the class provides methods for accessing the raw page data,
 * as well as data attributes like segmenting information, sequence number,
 * stream serial number, checksum and whether this page is the beginning or
 * end of a logical bitstream (BOS, EOS) and if the page data starts with a
 * continued packet or a fresh data packet.</p>
 */

public class OggJmfPage extends OggPage {

   final private int version;
   final private boolean continued, bos, eos;
   final private long absoluteGranulePosition;
   final private int streamSerialNumber, pageSequenceNumber, pageCheckSum;
   final private int[] segmentOffsets;
   final private int[] segmentLengths;
   final private int totalLength;
   final private byte[] header, segmentTable, data;

   private OggJmfPage(
      int version,
      boolean continued,
      boolean bos,
      boolean eos,
      long absoluteGranulePosition,
      int streamSerialNumber,
      int pageSequenceNumber,
      int pageCheckSum,
      int[] segmentOffsets,
      int[] segmentLengths,
      int totalLength,
      byte[] header,
      byte[] segmentTable,
      byte[] data) {

      this.version=version;
      this.continued=continued;
      this.bos=bos;
      this.eos=eos;
      this.absoluteGranulePosition=absoluteGranulePosition;
      this.streamSerialNumber=streamSerialNumber;
      this.pageSequenceNumber=pageSequenceNumber;
      this.pageCheckSum=pageCheckSum;
      this.segmentOffsets=segmentOffsets;
      this.segmentLengths=segmentLengths;
      this.totalLength=totalLength;
      this.header=header;
      this.segmentTable=segmentTable;
      this.data=data;
   }

   /**
    * This method is equivalent to <code>create(source, false)</code>
    *
    * @param source the byte channel from which the ogg page is generated
    * @return an ogg page created by reading data from the specified channel, starting at the current position
    * @throws OggFormatException if the data read from the specified channel is not matching the specification for an ogg page
    * @throws EndOfOggStreamException if it is not possible to read an entire ogg page from the specified channel
    * @throws IOException if some other IO error is detected when reading from the channel
    *
    * @see #create(javax.media.protocol.PullSourceStream, boolean)
    */
   public static OggJmfPage create(PullSourceStream source) throws IOException, EndOfOggStreamException, OggFormatException  {
      return create(source, false);
   }

   public static OggJmfPage create(PullSourceStream source, boolean skipData) throws IOException, EndOfOggStreamException, OggFormatException  {
      return create((Object)source, skipData);
   }

   private static OggJmfPage create(Object source, boolean skipData) throws IOException, EndOfOggStreamException, OggFormatException  {

      try {
         byte[] header=new byte[27];
         if(source instanceof RandomAccessFile) {
            ((DataInput) source).readFully(header);
         }
         else if(source instanceof PullSourceStream) {
            readFully((PullSourceStream)source, header);
         }
         else if(source instanceof InputStream) {
            readFully((InputStream)source, header);
         }

         BitInputStream bdSource=new ByteArrayBitInputStream(header);

         int capture=bdSource.getInt(32);

         if(capture!=0x5367674f) {
            //throw new FormatException("Ogg page does not start with 'OggS' (0x4f676753)");

            /*
            ** This condition is IMHO an error, but older Ogg files often contain
            ** pages with a different capture than OggS. I am not sure how to
            ** manage these pages, but the decoder seems to work properly, if
            ** the incorrect capture is simply ignored.
            */

            String cs=Integer.toHexString(capture);
            while(cs.length()<8) {
               cs="0"+cs;
            }
            cs=cs.substring(6, 8)+cs.substring(4, 6)+cs.substring(2, 4)+cs.substring(0, 2);
            char c1=(char)(Integer.valueOf(cs.substring(0, 2), 16).intValue());
            char c2=(char)(Integer.valueOf(cs.substring(2, 4), 16).intValue());
            char c3=(char)(Integer.valueOf(cs.substring(4, 6), 16).intValue());
            char c4=(char)(Integer.valueOf(cs.substring(6, 8), 16).intValue());
            System.out.println("Ogg packet header is 0x"+cs+" ("+c1+c2+c3+c4+"), should be 0x4f676753 (OggS)");
         }

         int version=bdSource.getInt(8);
         byte tmp=(byte)bdSource.getInt(8);
         boolean bf1=(tmp&1)!=0;
         boolean bos=(tmp&2)!=0;
         boolean eos=(tmp&4)!=0;
         long absoluteGranulePosition=bdSource.getLong(64);
         int streamSerialNumber=bdSource.getInt(32);
         int pageSequenceNumber=bdSource.getInt(32);
         int pageCheckSum=bdSource.getInt(32);
         int pageSegments=bdSource.getInt(8);

         //System.out.println("OggPage: "+streamSerialNumber+" / "+absoluteGranulePosition+" / "+pageSequenceNumber);

         int[] segmentOffsets=new int[pageSegments];
         int[] segmentLengths=new int[pageSegments];
         int totalLength=0;

         byte[] segmentTable=new byte[pageSegments];
         byte[] tmpBuf=new byte[1];

         for(int i=0; i<pageSegments; i++) {
            int l=0;
            if(source instanceof RandomAccessFile) {
               l=((int)((DataInput) source).readByte()&0xff);
            }
            else if(source instanceof PullSourceStream) {
               ((PullSourceStream)source).read(tmpBuf, 0, 1);
               l=(int)tmpBuf[0]&0xff;
            }
            else if(source instanceof InputStream) {
               l=((InputStream)source).read();
            }
            segmentTable[i]=(byte)l;
            segmentLengths[i]=l;
            segmentOffsets[i]=totalLength;
            totalLength+=l;
         }

         byte[] data=null;

         if(!skipData) {

            data=new byte[totalLength];
            if(source instanceof RandomAccessFile) {
               ((DataInput) source).readFully(data);
            }
            else if(source instanceof PullSourceStream) {
               readFully((PullSourceStream)source, data);
            }
            else if(source instanceof InputStream) {
               readFully((InputStream)source, data);
            }
         }

         return new OggJmfPage(version, bf1, bos, eos, absoluteGranulePosition, streamSerialNumber, pageSequenceNumber, pageCheckSum, segmentOffsets, segmentLengths, totalLength, header, segmentTable, data);
      }
      catch(EOFException e) {
         throw new EndOfOggStreamException();
      }
   }

   private static void readFully(PullSourceStream source, byte[] buffer) throws IOException {
      int total=0;
      while(total<buffer.length) {
         int read=source.read(buffer, total, buffer.length-total);
         if(read==-1) {
            throw new EndOfOggStreamException();
         }
         total+=read;
      }
   }

   private static void readFully(InputStream source, byte[] buffer) throws IOException {
      int total=0;
      while(total<buffer.length) {
         int read=source.read(buffer, total, buffer.length-total);
         if(read==-1) {
            throw new EndOfOggStreamException();
         }
         total+=read;
      }
   }

   /**
    * Returns the absolute granule position of the last complete
    * packet contained in this Ogg page, or -1 if the page contains a single
    * packet, which is not completed on this page. For pages containing Vorbis
    * data, this value is the sample index within the Vorbis stream. The Vorbis
    * stream does not necessarily start with sample index 0.
    *
    * @return the absolute granule position of the last packet completed on
    *         this page
    */


   @Override
   public long getAbsoluteGranulePosition() {
      return absoluteGranulePosition;
   }

   /**
    * Returns the stream serial number of this ogg page.
    *
    * @return this page's serial number
    */

   @Override
   public int getStreamSerialNumber() {
      return streamSerialNumber;
   }

   /**
    * Return the sequence number of this ogg page.
    *
    * @return this page's sequence number
    */

   @Override
   public int getPageSequenceNumber() {
      return pageSequenceNumber;
   }

   /**
    * Return the check sum of this ogg page.
    *
    * @return this page's check sum
    */

   @Override
   public int getPageCheckSum() {
      return pageCheckSum;
   }

   /**
    * @return the total number of bytes in the page data
    */


   @Override
   public int getTotalLength() {
      return totalLength;
   }

   /**
    * @return a ByteBuffer containing the page data
    */


   @Override
   public byte[] getData() {
      return data;
   }

   @Override
   public byte[] getHeader() {
      return header;
   }

   @Override
   public byte[] getSegmentTable() {
      return segmentTable;
   }

   @Override
   public int[] getSegmentOffsets() {
      return segmentOffsets;
   }

   @Override
   public int[] getSegmentLengths() {
      return segmentLengths;
   }

   /**
    * @return <code>true</code> if this page begins with a continued packet
    */

   @Override
   public boolean isContinued() {
      return continued;
   }

   /**
    * @return <code>true</code> if this page begins with a fresh packet
    */

   @Override
   public boolean isFresh() {
      return !continued;
   }

   /**
    * @return <code>true</code> if this page is the beginning of a logical stream
    */

   @Override
   public boolean isBos() {
      return bos;
   }

   /**
    * @return <code>true</code> if this page is the end of a logical stream
    */

   @Override
   public boolean isEos() {
      return eos;
   }

}