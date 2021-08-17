package de.jarnbjo.jmf;

import java.io.*;
import javax.media.*;
import javax.media.protocol.*;

public class FlacParser implements Demultiplexer {

   private static final String DEMULTIPLEXER_NAME = "FLAC demultiplexer";

   private final ContentDescriptor[] supportedContentTypes = new ContentDescriptor[] {
      new ContentDescriptor(ContentDescriptor.mimeTypeToPackageName("application/flac")),
      new ContentDescriptor(ContentDescriptor.mimeTypeToPackageName("application/x-flac"))
   };

   private Track[] tracks;

   private PullDataSource source;
   private PullSourceStream stream;

   public FlacParser() {
   }

   @Override
   public Time getDuration() {
      return Time.TIME_UNKNOWN;
      /*
      if(tracks==null) {
         return Time.TIME_UNKNOWN;
      }
      long max=0;
      for(int i=0; i<tracks.length; i++) {
         if(tracks[i].getDuration().getNanoseconds()>max) {
            max=tracks[i].getDuration().getNanoseconds();
         }
      }
      return new Time(max);//Time.TIME_UNKNOWN;
      */
   }

   @Override
   public ContentDescriptor[] getSupportedInputContentDescriptors() {
      return supportedContentTypes;
   }

   @Override
   public Track[] getTracks() throws BadHeaderException, IOException {
      /*
      if(tracks==null) {
         try {
            Collection coll=oggStream.getLogicalStreams();
            tracks=new Track[coll.size()];
            int i=0;
            for(Iterator iter=coll.iterator(); iter.hasNext(); i++) {
               tracks[i]=OggTrack.createInstance((LogicalOggStream)iter.next());
            }
         }
         catch(OggFormatException e) {
            throw new BadHeaderException(e.getMessage());
         }
         catch(VorbisFormatException e) {
            throw new BadHeaderException(e.getMessage());
         }
      }
      */
      return tracks;
   }

   @Override
   public boolean isPositionable() {
      return false;
   }

   @Override
   public boolean isRandomAccess() {
      return false;
   }

   @Override
   public Time getMediaTime() {
      /** @todo implement */
      return Time.TIME_UNKNOWN;
   }

   @Override
   public Time setPosition(Time time, int rounding) {

      /*
      try {
         if(tracks[0] instanceof VorbisTrack) {
            long sampleRate=((VorbisTrack)tracks[0]).getSampleRate();
            oggStream.setTime(time.getNanoseconds()*sampleRate/1000000000L);
         }
         else if(tracks[0] instanceof FlacTrack) {
            long sampleRate=((FlacTrack)tracks[0]).getSampleRate();
            oggStream.setTime(time.getNanoseconds()*sampleRate/1000000000L);
         }
      }
      catch(IOException e) {
         e.printStackTrace();
      }
      */

      /** @todo implement */
      return Time.TIME_UNKNOWN;
   }

   @Override
   public void start() throws IOException {
      if(source!=null) {
         source.start();
      }
   }

   @Override
   public void stop()  {
      if(source!=null) {
         try {
            source.stop();
         }
         catch(IOException e) {
            // ignore
         }
      }
   }

   @Override
   public void open() {
      // nothing to be done
   }

   @Override
   public void close() {
      if(source!=null) {
         try {
            source.stop();
            source.disconnect();
         }
         catch(IOException e) {
            // ignore
         }
         source=null;
      }
   }

   @Override
   public void reset() {
      setPosition(new Time(0), 0);
   }

   @Override
   public String getName() {
      return DEMULTIPLEXER_NAME;
   }

   @Override
   public void setSource(DataSource source) throws IOException, IncompatibleSourceException {

      try {
         if(!(source instanceof PullDataSource)) {
            /** @todo better message */
            throw new IncompatibleSourceException("DataSource not supported: " + source);
         }

         this.source=(PullDataSource)source;

         if(this.source.getStreams()==null || this.source.getStreams().length==0) {
            throw new IOException("DataSource has no streams.");
         }

         if(this.source.getStreams().length>1) {
            throw new IOException("This demultiplexer only supports datasources with one stream.");
         }

         stream=this.source.getStreams()[0];
         //oggStream=new OggJmfStream(stream);

         if(!(stream instanceof Seekable)) {
            /** @todo better message */
            throw new IncompatibleSourceException("Stream is not seekable.");
         }
      }
      catch (IncompatibleSourceException | IOException | RuntimeException e) {
         e.printStackTrace();
         throw e;
      }
   }

   @Override
   public Object getControl(String controlType) {
      return null;
   }

   @Override
   public Object[] getControls() {
      return new Object[0];
   }

}