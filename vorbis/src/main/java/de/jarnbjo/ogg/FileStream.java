/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: FileStream.java,v 1.1 2003/04/10 19:48:22 jarnbjo Exp $
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
 * $Log: FileStream.java,v $
 * Revision 1.1  2003/04/10 19:48:22  jarnbjo
 * no message
 */
package de.jarnbjo.ogg;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Implementation of the {@code PhysicalOggStream} interface for accessing
 * normal disk files.
 */
public class FileStream implements PhysicalOggStream {
    private boolean closed = false;
    final private RandomAccessFile source;
    final private long[] pageOffsets;

    final private HashMap logicalStreams = new HashMap();

    /**
     * Creates access to the specified file through the
     * {@code PhysicalOggStream} interface. The specified source file must have
     * been opened for reading.
     *
     * @param source the file to read from
     *
     * @throws OggFormatException if the stream format is incorrect
     * @throws IOException if some other IO error occurs when reading the file
     */
    public FileStream(RandomAccessFile source)
            throws OggFormatException, IOException {
        this.source = source;

        List po = new ArrayList();
        int pageNumber = 0;
        try {
            while (true) {
                po.add(this.source.getFilePointer());

                // skip data if pageNumber>0
                OggPage op = getNextPage(pageNumber > 0);
                if (op == null) {
                    break;
                }

                LogicalOggStreamImpl los = (LogicalOggStreamImpl)
                        getLogicalStream(op.getStreamSerialNumber());
                if (los == null) {
                    los = new LogicalOggStreamImpl(
                            this, op.getStreamSerialNumber());
                    logicalStreams.put(op.getStreamSerialNumber(), los);
                }

                if (pageNumber == 0) {
                    los.checkFormat(op);
                }

                los.addPageNumberMapping(pageNumber);
                los.addGranulePosition(op.getAbsoluteGranulePosition());

                if (pageNumber > 0) {
                    this.source.seek(
                            this.source.getFilePointer() + op.getTotalLength());
                }

                pageNumber++;
            }
        } catch (EndOfOggStreamException e) {
            // ok
        } catch (IOException e) {
            throw e;
        }
        //System.out.println("pageNumber: "+pageNumber);
        this.source.seek(0L);
        pageOffsets = new long[po.size()];
        int i = 0;
        for (Object o : po) {
            pageOffsets[i++] = (Long) o;
        }
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
        closed = true;
        source.close();
    }

    private OggPage getNextPage(boolean skipData) throws IOException {
        return OggPage.create(source, skipData);
    }

    @Override
    public OggPage getOggPage(int index) throws IOException {
        source.seek(pageOffsets[index]);
        return OggPage.create(source);
    }

    private LogicalOggStream getLogicalStream(int serialNumber) {
        return (LogicalOggStream) logicalStreams.get(serialNumber);
    }

    @Override
    public void setTime(long granulePosition) throws IOException {
        for (Object o : logicalStreams.values()) {
            LogicalOggStream los = (LogicalOggStream) o;
            los.setTime(granulePosition);
        }
    }

    /**
     * @return always {@code true}
     */
    @Override
    public boolean isSeekable() {
        return true;
    }
}
