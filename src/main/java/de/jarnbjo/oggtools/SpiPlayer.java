/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: SpiPlayer.java,v 1.4 2003/08/08 19:48:40 jarnbjo Exp $
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
 * $Log: SpiPlayer.java,v $
 *
 */

package de.jarnbjo.oggtools;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;
import javax.sound.sampled.spi.*;

import de.jarnbjo.ogg.*;
import de.jarnbjo.vorbis.*;
import de.jarnbjo.flac.*;

public class SpiPlayer {

   public static void main(String[] args) {

      System.out.println("JOggSpiPlayer v0.9");
      System.out.println("Please send bug reports to Tor-Einar@Jarnbjo.de");
      System.out.println("");

      if(args.length!=1) {
         System.out.println("Usage: java -jar JOggPlayer.jar <URL>");
         System.exit(0);
      }

      try {
      	 URL url=new URL(args[0]);
      	 
		 AudioFormat audioFormat=AudioSystem.getAudioFileFormat(url).getFormat();
         AudioInputStream ais=AudioSystem.getAudioInputStream(url);

         DataLine.Info dataLineInfo=new DataLine.Info(SourceDataLine.class, audioFormat);

         SourceDataLine sourceDataLine=(SourceDataLine)AudioSystem.getLine(dataLineInfo);

         sourceDataLine.open(audioFormat);
         sourceDataLine.start();

         byte[] buffer=new byte[8192];
         int cnt=0;

         while((cnt = ais.read(buffer, 0, buffer.length)) != -1) {
            if(cnt > 0){
               sourceDataLine.write(buffer, 0, cnt);
            }
         }

         sourceDataLine.drain();
         sourceDataLine.close();

         System.exit(0);
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

}