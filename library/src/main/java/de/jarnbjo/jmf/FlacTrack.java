/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: FlacTrack.java,v 1.1 2003/03/03 22:06:12 jarnbjo Exp $
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
 * $Log: FlacTrack.java,v $
 * Revision 1.1  2003/03/03 22:06:12  jarnbjo
 * no message
 */
package de.jarnbjo.jmf;

import java.io.IOException;
import javax.media.Duration;
import javax.media.Format;
import javax.media.Time;
import javax.media.format.AudioFormat;
import de.jarnbjo.ogg.LogicalOggStream;
import de.jarnbjo.flac.MetadataBlock;
import de.jarnbjo.flac.StreamInfo;
import de.jarnbjo.util.io.BitInputStream;
import de.jarnbjo.util.io.ByteArrayBitInputStream;

public class FlacTrack extends OggTrack {
    final private LogicalOggStream oggStream;
    final private StreamInfo streamInfo;
    final private AudioFormat format;

    public FlacTrack(LogicalOggStream source, byte[] siHeaderData)
            throws IOException {
        super(source);
        oggStream = source;
        BitInputStream bd = new ByteArrayBitInputStream(siHeaderData,
                ByteArrayBitInputStream.BIG_ENDIAN);
        streamInfo = (StreamInfo) MetadataBlock.createInstance(bd);

        format = new AudioFormat(
                "audio/x-flac",
                (double) streamInfo.getSampleRate(),
                16,
                streamInfo.getChannels(),
                Format.NOT_SPECIFIED,
                Format.NOT_SPECIFIED,
                Format.NOT_SPECIFIED,
                Format.NOT_SPECIFIED,
                Format.byteArray);
    }

    @Override
    public Format getFormat() {
        return format;
    }

    @Override
    public Time getDuration() {
        long nos = oggStream.getMaximumGranulePosition();
        return nos == -1
                ? Duration.DURATION_UNKNOWN
                : new Time(nos * 1000000000L / (streamInfo.getSampleRate()));
    }

    protected int getSampleRate() {
        return streamInfo.getSampleRate();
    }
}
