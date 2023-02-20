/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: OggParser.java,v 1.1 2003/03/03 22:06:12 jarnbjo Exp $
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
 * $Log: OggParser.java,v $
 * Revision 1.1  2003/03/03 22:06:12  jarnbjo
 * no message
 *
 */
 
package de.jarnbjo.jmf;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import javax.media.BadHeaderException;
import javax.media.Demultiplexer;
import javax.media.IncompatibleSourceException;
import javax.media.Time;
import javax.media.Track;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.Seekable;
import de.jarnbjo.ogg.LogicalOggStream;
import de.jarnbjo.ogg.OggFormatException;
import de.jarnbjo.ogg.PhysicalOggStream;
import de.jarnbjo.vorbis.VorbisFormatException;

public class OggParser implements Demultiplexer {
    private static final String DEMULTIPLEXER_NAME = "Ogg demultiplexer";

    private final ContentDescriptor[] supportedContentTypes = {
        new ContentDescriptor(ContentDescriptor.mimeTypeToPackageName("application/ogg")),
        new ContentDescriptor(ContentDescriptor.mimeTypeToPackageName("application/x-ogg"))
    };

    private Track[] tracks;

    private PullDataSource source;
    private PullSourceStream stream;
    private PhysicalOggStream oggStream;

    public OggParser() {
    }

    @Override
    public Time getDuration() {
        if (tracks == null) {
            return Time.TIME_UNKNOWN;
        }
        long max = 0;
        for (Track track : tracks) {
            if (track.getDuration().getNanoseconds() > max) {
                max = track.getDuration().getNanoseconds();
            }
        }
        return new Time(max);//Time.TIME_UNKNOWN;
    }

    @Override
    public ContentDescriptor[] getSupportedInputContentDescriptors() {
        return supportedContentTypes;
    }

    @Override
    public Track[] getTracks() throws BadHeaderException, IOException {
        if (tracks == null) {
            try {
                Collection coll = oggStream.getLogicalStreams();
                tracks = new Track[coll.size()];
                int i = 0;
                for (Iterator iter = coll.iterator(); iter.hasNext(); i++) {
                    LogicalOggStream los = (LogicalOggStream) iter.next();
                    System.out.println("type: " + los.getFormat());
                    tracks[i] = OggTrack.createInstance(los);
                }
            } catch (OggFormatException | VorbisFormatException e) {
                throw new BadHeaderException(e.getMessage());
            }
        }
        return tracks;
    }

    @Override
    public boolean isPositionable() {
        return true;
    }

    @Override
    public boolean isRandomAccess() {
        return true;
    }

    @Override
    public Time getMediaTime() {
        /* @todo implement */
        return Time.TIME_UNKNOWN;
    }

    @Override
    public Time setPosition(Time time, int rounding) {

        try {
            if (tracks[0] instanceof VorbisTrack) {
                long sampleRate = ((VorbisTrack) tracks[0]).getSampleRate();
                oggStream.setTime(time.getNanoseconds() * sampleRate / 1000000000L);
            } else if (tracks[0] instanceof FlacTrack) {
                long sampleRate = ((FlacTrack) tracks[0]).getSampleRate();
                oggStream.setTime(time.getNanoseconds() * sampleRate / 1000000000L);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* @todo implement */
        return Time.TIME_UNKNOWN;
    }

    @Override
    public void start() throws IOException {
        if (source != null) {
            source.start();
        }
    }

    @Override
    public void stop() {
        if (source != null) {
            try {
                source.stop();
            } catch (IOException e) {
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
        if (source != null) {
            try {
                source.stop();
                source.disconnect();
            } catch (IOException e) {
                // ignore
            }
            source = null;
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
            if (!(source instanceof PullDataSource)) {
                /* @todo better message */
                throw new IncompatibleSourceException("DataSource not supported: " + source);
            }

            this.source = (PullDataSource) source;

            if (this.source.getStreams() == null || this.source.getStreams().length == 0) {
                throw new IOException("DataSource has no streams.");
            }

            if (this.source.getStreams().length > 1) {
                throw new IOException("This demultiplexer only supports data sources with one stream.");
            }

            stream = this.source.getStreams()[0];
            oggStream = new OggJmfStream(stream);

            if (!(stream instanceof Seekable)) {
                /* @todo better message */
                throw new IncompatibleSourceException("Stream is not seekable.");
            }
        } catch (IncompatibleSourceException | IOException | RuntimeException e) {
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
