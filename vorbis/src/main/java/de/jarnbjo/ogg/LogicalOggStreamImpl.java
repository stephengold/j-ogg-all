/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: LogicalOggStreamImpl.java,v 1.3 2003/03/31 00:23:04 jarnbjo Exp $
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
 * $Log: LogicalOggStreamImpl.java,v $
 * Revision 1.3  2003/03/31 00:23:04  jarnbjo
 * no message
 *
 * Revision 1.2  2003/03/16 01:11:26  jarnbjo
 * no message
 *
 * Revision 1.1  2003/03/03 21:02:20  jarnbjo
 * no message
 */
package de.jarnbjo.ogg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class LogicalOggStreamImpl implements LogicalOggStream {
    final private PhysicalOggStream source;
    final private int serialNumber;

    final private ArrayList<Integer> pageNumberMapping = new ArrayList<>();
    final private ArrayList<Long> granulePositions = new ArrayList<>();

    private int pageIndex = 0;
    private OggPage currentPage;
    private int currentSegmentIndex;

    private boolean open = true;

    private String format = FORMAT_UNKNOWN;

    public LogicalOggStreamImpl(PhysicalOggStream source, int serialNumber) {
        this.source = source;
        this.serialNumber = serialNumber;
    }

    public void addPageNumberMapping(int physicalPageNumber) {
        pageNumberMapping.add(physicalPageNumber);
    }

    public void addGranulePosition(long granulePosition) {
        granulePositions.add(granulePosition);
    }

    @Override
    public synchronized void reset() throws IOException {
        currentPage = null;
        currentSegmentIndex = 0;
        pageIndex = 0;
    }

    @Override
    public synchronized OggPage getNextOggPage() throws IOException {
        if (source.isSeekable()) {
            currentPage = source.getOggPage(
                    (Integer) pageNumberMapping.get(pageIndex++));
        } else {
            currentPage = source.getOggPage(-1);
        }
        return currentPage;
    }

    @Override
    public synchronized byte[] getNextOggPacket() throws IOException {
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        int segmentLength = 0;

        if (currentPage == null) {
            currentPage = getNextOggPage();
        }

        do {
            if (currentPage == null) {
                throw new OggFormatException("Missing page in Ogg.");
            }
            if (currentSegmentIndex >= currentPage.getSegmentOffsets().length) {
                currentSegmentIndex = 0;

                if (!currentPage.isEos()) {
                    if (source.isSeekable()
                            && pageNumberMapping.size() <= pageIndex) {
                        while (pageNumberMapping.size() <= pageIndex + 10) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                // do nothing
                            }
                        }
                    }
                    currentPage = getNextOggPage();
                    if (res.size() == 0 && currentPage == null) {
                        throw new OggFormatException("Missing page in Ogg.");
                    }

                    if (res.size() == 0 && currentPage.isContinued()) {
                        boolean done = false;
                        while (!done) {
                            if (currentPage.getSegmentLengths()
                                    [currentSegmentIndex++] != 255) {
                                done = true;
                            }
                            if (currentSegmentIndex
                                    > currentPage.getSegmentTable().length) {
                                currentPage = source.getOggPage((Integer)
                                        pageNumberMapping.get(pageIndex++));
                            }
                        }
                    }
                } else {
                    throw new EndOfOggStreamException();
                }
            }
            segmentLength
                    = currentPage.getSegmentLengths()[currentSegmentIndex];
            res.write(currentPage.getData(),
                    currentPage.getSegmentOffsets()[currentSegmentIndex],
                    segmentLength);
            currentSegmentIndex++;
        } while (segmentLength == 255);

        return res.toByteArray();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        open = false;
    }

    @Override
    public long getMaximumGranulePosition() {
        Long mgp = (Long) granulePositions.get(granulePositions.size() - 1);
        return mgp;
    }

    @Override
    public synchronized long getTime() {
        return currentPage != null
                ? currentPage.getAbsoluteGranulePosition() : -1;
    }

    @Override
    public synchronized void setTime(long granulePosition) throws IOException {

        int page = 0;
        for (page = 0; page < granulePositions.size(); page++) {
            Long gp = (Long) granulePositions.get(page);
            if (gp > granulePosition) {
                break;
            }
        }

        pageIndex = page;
        currentPage = source.getOggPage(
                (Integer) pageNumberMapping.get(pageIndex++));
        currentSegmentIndex = 0;
        int segmentLength = 0;
        do {
            if (currentSegmentIndex >= currentPage.getSegmentOffsets().length) {
                currentSegmentIndex = 0;
                if (pageIndex >= pageNumberMapping.size()) {
                    throw new EndOfOggStreamException();
                }
                currentPage = source.getOggPage(
                        (Integer) pageNumberMapping.get(pageIndex++));
            }
            segmentLength
                    = currentPage.getSegmentLengths()[currentSegmentIndex];
            currentSegmentIndex++;
        } while (segmentLength == 255);
    }

    public void checkFormat(OggPage page) {
        byte[] data = page.getData();

        if (data.length >= 7
                && data[1] == 0x76
                && data[2] == 0x6f
                && data[3] == 0x72
                && data[4] == 0x62
                && data[5] == 0x69
                && data[6] == 0x73) {

            format = FORMAT_VORBIS;
        } else if (data.length >= 7
                && data[1] == 0x74
                && data[2] == 0x68
                && data[3] == 0x65
                && data[4] == 0x6f
                && data[5] == 0x72
                && data[6] == 0x61) {

            format = FORMAT_THEORA;
        } else if (data.length == 4
                && data[0] == 0x66
                && data[1] == 0x4c
                && data[2] == 0x61
                && data[3] == 0x43) {

            format = FORMAT_FLAC;
        }
    }

    @Override
    public String getFormat() {
        return format;
    }
}
