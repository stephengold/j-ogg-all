package de.jarnbjo.oggtools;

import java.io.*;
import java.util.*;

import de.jarnbjo.ogg.*;
import de.jarnbjo.vorbis.*;

public class Ogg2Wav {

   private static final int BUFFER_SIZE = 65536;

   public static void main(String[] args) {

      if(args.length!=2) {
         System.out.println("Usage: java -jar Ogg2Wav.jar in.ogg out.wav");
         System.exit(0);
      }

      try {
         // open the file args[0] for reading and initialize an Ogg stream
         PhysicalOggStream os=new FileStream(new RandomAccessFile(args[0], "r"));

			// get the first logical Ogg stream from the file
         LogicalOggStream los=(LogicalOggStream)os.getLogicalStreams().iterator().next();

			// exit, if it is not a Vorbis stream
			if(los.getFormat()!=LogicalOggStream.FORMAT_VORBIS) {
				System.out.println("This tool only supports Ogg files with Vorbis content.");
				System.exit(0);
			}

         long t0=System.currentTimeMillis();

			// create a Vorbis stream from the logical Ogg stream
         final VorbisStream vs=new VorbisStream(los);

         // open the file args[1] for WAV output
         final RandomAccessFile outFile=new RandomAccessFile(args[1], "rw");
         outFile.setLength(0L);

         // allocate a buffer for data
         final byte[] buffer=new byte[BUFFER_SIZE];

         // number of data bytes written to the wav file
         int len=0;

         // get basic stream data from the vorbis channel
         int channels=vs.getIdentificationHeader().getChannels();
         int sampleRate=vs.getIdentificationHeader().getSampleRate();

         CommentHeader comments=vs.getCommentHeader();

         System.out.println("Ogg file:       "+args[0]);
         System.out.println("Wav file:       "+args[1]);
         System.out.println();
         System.out.println("Channels:       "+channels);
         System.out.println("Sample rate:    "+sampleRate);
         System.out.println();
         System.out.println("Encoder vendor: "+comments.getVendor());
         System.out.println("Title:          "+format(comments.getTitle()));
         System.out.println("Artist:         "+format(comments.getArtist()));

         // write "RIFF" header chunk
         outFile.write("RIFF".getBytes("ASCII"));
         outFile.write(toBytes(0)); // will be filled in later
         outFile.write("WAVE".getBytes("ASCII"));

         // write "fmt " header chunk
         outFile.write("fmt ".getBytes("ASCII"));
         outFile.write(toBytes(16));
         outFile.write(toBytes((short)1)); // PCM format
         outFile.write(toBytes((short)channels));
         outFile.write(toBytes(sampleRate));
         outFile.write(toBytes(sampleRate*channels*2));
         outFile.write(toBytes((short)(2*channels)));
         outFile.write(toBytes((short)16));

         // write "data" header
         outFile.write("data".getBytes("ASCII"));
         outFile.write(toBytes(0)); // will be filled in later

         try {
            // read pcm data from the vorbis channel and
            // write the data to the wav file
            while(true) {
               int read=vs.readPcm(buffer, 0, buffer.length);
               for(int i=0; i<read; i+=2) {
               	// swap from big endian to little endian
                  byte tB=buffer[i];
                  buffer[i]=buffer[i+1];
                  buffer[i+1]=tB;
               }
               outFile.write(buffer, 0, read);
               len+=read;
            }
         }
         catch(EndOfOggStreamException e) {
            // not really an error, but we've
            // reached the end of the vorbis stream
            // and so exit the loop
         }

         // fill the wav header with missing data:
         // write file length at file position 4
         outFile.seek(4L);
         outFile.write(toBytes(len+36));

         // write data chunk length at file position 40
         outFile.seek(40L);
         outFile.write(toBytes(len));

         // flush and close the wav file
         outFile.close();

         // close the ogg and vorbis streams
         os.close();

         System.out.println(System.currentTimeMillis()-t0);
      }
      catch(FileNotFoundException e) {
         System.out.println("The ogg file \""+args[0]+"\" was not found.");
      }
      catch(IOException e) {
         // some unexpected exception was raised during decoding
         e.printStackTrace();
         System.out.println("Error: ");
         System.out.println(e.getMessage());
      }
   }

   private static String format(String s) {
      return s==null?"<unknown>":s;
   }

   private static byte[] toBytes(int val) {
      byte[] res=new byte[4];
      res[3]=(byte)(0xff & (val >> 24));
      res[2]=(byte)(0xff & (val >> 16));
      res[1]=(byte)(0xff & (val >> 8));
      res[0]=(byte)(0xff & (val));
      return res;
   }

   private static byte[] toBytes(short val) {
      byte[] res=new byte[2];
      res[1]=(byte)(0xff & (val >> 8));
      res[0]=(byte)(0xff & (val));
      return res;
   }

}