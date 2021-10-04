/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: CodeBook.java,v 1.3 2003/04/10 19:49:04 jarnbjo Exp $
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
 * $Log: CodeBook.java,v $
 * Revision 1.3  2003/04/10 19:49:04  jarnbjo
 * no message
 *
 * Revision 1.2  2003/03/16 01:11:12  jarnbjo
 * no message
 *
 *
 */

package de.jarnbjo.vorbis;

import java.io.*;
import java.util.*;
import de.jarnbjo.util.io.*;

class CodeBook {

   private HuffmanNode huffmanRoot;
   final private int dimensions, entries;

   final private int[] entryLengths;
   private float[][] valueVector;

   protected CodeBook(BitInputStream source) throws IOException {

      // check sync
      if(source.getInt(24)!=0x564342) {
         throw new VorbisFormatException("The code book sync pattern is not correct.");
      }

      dimensions=source.getInt(16);
      entries=source.getInt(24);

      entryLengths=new int[entries];

      boolean ordered=source.getBit();

      if(ordered) {
         int cl=source.getInt(5)+1;
         for(int i=0; i<entryLengths.length; ) {
            int num=source.getInt(Util.ilog(entryLengths.length-i));
            if(i+num>entryLengths.length) {
               throw new VorbisFormatException("The codebook entry length list is longer than the actual number of entry lengths.");
            }
            Arrays.fill(entryLengths, i, i+num, cl);
            cl++;
            i+=num;
         }
      }
      else {
         // !ordered
         boolean sparse=source.getBit();

         if(sparse) {
            for(int i=0; i<entryLengths.length; i++) {
               if(source.getBit()) {
                  entryLengths[i]=source.getInt(5)+1;
               }
               else {
                  entryLengths[i]=-1;
               }
            }
         }
         else {
            // !sparse
            for(int i=0; i<entryLengths.length; i++) {
               entryLengths[i]=source.getInt(5)+1;
            }
         }
      }

      if (!createHuffmanTree(entryLengths)) {
         throw new VorbisFormatException("An exception was thrown when building the codebook Huffman tree.");
      }

      int codeBookLookupType=source.getInt(4);

      switch(codeBookLookupType) {
      case 0:
         // codebook has no scalar vectors to be calculated
         break;
      case 1:
      case 2:
         float codeBookMinimumValue=Util.float32unpack(source.getInt(32));
         float codeBookDeltaValue=Util.float32unpack(source.getInt(32));

         int codeBookValueBits=source.getInt(4)+1;
         boolean codeBookSequenceP=source.getBit();

         int codeBookLookupValues=0;

         if(codeBookLookupType==1) {
            codeBookLookupValues=Util.lookup1Values(entries, dimensions);
         }
         else {
            codeBookLookupValues=entries*dimensions;
         }

         int[] codeBookMultiplicands =new int[codeBookLookupValues];

         for(int i=0; i<codeBookMultiplicands.length; i++) {
            codeBookMultiplicands[i]=source.getInt(codeBookValueBits);
         }

         valueVector=new float[entries][dimensions];

         if(codeBookLookupType==1) {
            for(int i=0; i<entries; i++) {
               float last=0;
               int indexDivisor=1;
               for(int j=0; j<dimensions; j++) {
                  int multiplicandOffset=
                     (i/indexDivisor)%codeBookLookupValues;
                  valueVector[i][j]=
                     codeBookMultiplicands[multiplicandOffset]*codeBookDeltaValue+codeBookMinimumValue+last;
                  if(codeBookSequenceP) {
                     last=valueVector[i][j];
                  }
                  indexDivisor*=codeBookLookupValues;
               }
            }
         }
         else {
            throw new UnsupportedOperationException();
            /* @todo implement */
         }
         break;
      default:
         throw new VorbisFormatException("Unsupported codebook lookup type: "+codeBookLookupType);
      }
   }

   private boolean createHuffmanTree(int[] entryLengths) {
      huffmanRoot=new HuffmanNode();
      for(int i=0; i<entryLengths.length; i++) {
         int el=entryLengths[i];
         if(el>0) {
            if(!huffmanRoot.setNewValue(el, i)) {
               return false;
            }
         }
      }
      return true;
   }

   protected int getDimensions() {
      return dimensions;
   }

   protected int getEntries() {
      return entries;
   }

   protected HuffmanNode getHuffmanRoot() {
      return huffmanRoot;
   }

   protected int readInt(final BitInputStream source) throws IOException {
      return source.getInt(huffmanRoot);
   }

   protected void readVvAdd(float[][] a, BitInputStream source, int offset, int length)
      throws IOException {

      int i,j;//k;//entry;
      int chptr=0;
      int ch=a.length;

      if(ch==0) {
         return;
      }

      int lim=(offset+length)/ch;

      for(i=offset/ch;i<lim;){
         final float[] ve=valueVector[source.getInt(huffmanRoot)];
         for(j=0;j<dimensions;j++){
            a[chptr++][i]+=ve[j];
            if(chptr==ch){
               chptr=0;
               i++;
	         }
         }
      }
   }
}