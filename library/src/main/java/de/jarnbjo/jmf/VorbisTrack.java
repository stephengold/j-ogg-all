package de.jarnbjo.jmf;

import de.jarnbjo.ogg.LogicalOggStream;
import de.jarnbjo.util.io.BitInputStream;
import de.jarnbjo.util.io.ByteArrayBitInputStream;
import de.jarnbjo.vorbis.IdentificationHeader;
import java.io.IOException;
import javax.media.Duration;
import javax.media.Format;
import javax.media.Time;
import javax.media.format.AudioFormat;

/**
 * <p>Überschrift: JOgg</p>
 * <p>Beschreibung: Java Ogg implementation</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Organisation: </p>
 *
 * @author Tor-Einar Jarnbjo
 * @version 1.0
 */
public class VorbisTrack extends OggTrack {
    final private LogicalOggStream oggStream;
    final private IdentificationHeader identificationHeader;
    final private AudioFormat format;

    public VorbisTrack(LogicalOggStream source, byte[] idHeaderData)
            throws IOException {
        super(source);
        oggStream = source;
        BitInputStream bd = new ByteArrayBitInputStream(idHeaderData);
        bd.getInt(8);
        identificationHeader = new IdentificationHeader(bd);
        format = new AudioFormat(
                "audio/x-vorbis",
                (double) identificationHeader.getSampleRate(),
                16,
                identificationHeader.getChannels(),
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
                : new Time(nos * 1000000000L
                        / ((long) identificationHeader.getSampleRate()));
    }

    protected int getSampleRate() {
        return identificationHeader.getSampleRate();
    }
}
