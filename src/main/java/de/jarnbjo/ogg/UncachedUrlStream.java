/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: UncachedUrlStream.java,v 1.1 2003/04/10 19:48:22 jarnbjo Exp $
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
 * $Log: UncachedUrlStream.java,v $
 * Revision 1.1  2003/04/10 19:48:22  jarnbjo
 * no message
 *
 */

package de.jarnbjo.ogg;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *  Implementation of the <code>PhysicalOggStream</code> interface for reading
 *  an Ogg stream from a URL. This class performs only the necessary caching
 *  to provide continuous playback. Seeking within the stream is not supported.
 */

public class UncachedUrlStream implements PhysicalOggStream {

   private boolean closed=false;
   final private URLConnection source;
   final private InputStream sourceStream;
   final private Object drainLock=new Object();
   final private LinkedList pageCache=new LinkedList();
   final private long numberOfSamples=-1;

   final private HashMap logicalStreams=new HashMap();

   final private LoaderThread loaderThread;

   private static final int PAGECACHE_SIZE = 10;

    /**
     * Creates an instance of the <code>PhysicalOggStream</code> interface
     * suitable for reading an Ogg stream from a URL.
     * 
     * @param source
     * @throws OggFormatException
     * @throws IOException
     */
   public UncachedUrlStream(URL source) throws OggFormatException, IOException {

      this.source=source.openConnection();
      this.sourceStream=this.source.getInputStream();

      loaderThread=new LoaderThread(sourceStream, pageCache);
      new Thread(loaderThread).start();

      while(!loaderThread.isBosDone() || pageCache.size()<PAGECACHE_SIZE) {
         try {
            Thread.sleep(200);
         }
         catch (InterruptedException ex) {
         }
         //System.out.print("caching "+pageCache.size()+"/"+PAGECACHE_SIZE+" pages\r");
      }
      //System.out.println();
   }

   @Override
   public Collection getLogicalStreams() {
      return logicalStreams.values();
   }

   @Override
   public boolean isOpen() {
      return !closed;
   }

   @Override
   public void close() throws IOException {
      closed=true;
      sourceStream.close();
   }

   @Override
   public OggPage getOggPage(int index) throws IOException {
      while(pageCache.isEmpty()) {
         try {
            Thread.sleep(100);
         }
         catch (InterruptedException ex) {
         }
      }
      synchronized(drainLock) {
         return (OggPage)pageCache.removeFirst();
      }
   }

   private LogicalOggStream getLogicalStream(int serialNumber) {
      return (LogicalOggStream)logicalStreams.get(serialNumber);
   }

   @Override
   public void setTime(long granulePosition) throws IOException {
      throw new UnsupportedOperationException("Method not supported by this class");
   }

   public class LoaderThread implements Runnable {

      final private InputStream source;
      final private Deque pageCache;
      private RandomAccessFile drain;
      private byte[] memoryCache;

      private boolean bosDone=false;

      private int pageNumber;

      public LoaderThread(InputStream source, LinkedList pageCache) {
         this.source=source;
         this.pageCache=pageCache;
      }

      @Override
      public void run() {
         try {
            boolean eos=false;
            byte[] buffer=new byte[8192];
            while(!eos) {
               OggPage op=OggPage.create(source);
               synchronized (drainLock) {
                  pageCache.add(op);
               }

               if(!op.isBos()) {
                  bosDone=true;
               }
               if(op.isEos()) {
                  eos=true;
               }

               LogicalOggStreamImpl los=(LogicalOggStreamImpl)getLogicalStream(op.getStreamSerialNumber());
               if(los==null) {
                  los=new LogicalOggStreamImpl(UncachedUrlStream.this, op.getStreamSerialNumber());
                  logicalStreams.put(op.getStreamSerialNumber(), los);
                  los.checkFormat(op);
               }

               pageNumber++;

               while(pageCache.size()>PAGECACHE_SIZE) {
                  try {
                     Thread.sleep(200);
                  }
                  catch (InterruptedException ex) {
                  }
               }
            }
         }
         catch(EndOfOggStreamException e) {
            // ok
         }
         catch(IOException e) {
            e.printStackTrace();
         }
      }

      public boolean isBosDone() {
         return bosDone;
      }
   }

	/** 
	 *  @return always <code>false</code>
	 */

   @Override
   public boolean isSeekable() {
      return false;
   }

}