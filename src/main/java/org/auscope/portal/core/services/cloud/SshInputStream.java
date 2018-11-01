/**
 * 
 */
package org.auscope.portal.core.services.cloud;

import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

/**
 * @author fri096
 *
 */
public class SshInputStream extends InputStream {

    private Session session;
    private ChannelSftp channel;
    private InputStream inputStream;

    /**
     * @return
     * @throws IOException
     * @see java.io.InputStream#available()
     */
    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    /**
     * @throws IOException
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException {
        inputStream.close();
        channel.disconnect();
        session.disconnect();
    }

    /**
     * @param arg0
     * @see java.io.InputStream#mark(int)
     */
    @Override
    public synchronized void mark(int arg0) {
        inputStream.mark(arg0);
    }

    /**
     * @return
     * @see java.io.InputStream#markSupported()
     */
    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @return
     * @throws IOException
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        return inputStream.read(arg0, arg1, arg2);
    }

    /**
     * @param arg0
     * @return
     * @throws IOException
     * @see java.io.InputStream#read(byte[])
     */
    @Override
    public int read(byte[] arg0) throws IOException {
        return inputStream.read(arg0);
    }

    /**
     * @throws IOException
     * @see java.io.InputStream#reset()
     */
    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    /**
     * @param arg0
     * @return
     * @throws IOException
     * @see java.io.InputStream#skip(long)
     */
    @Override
    public long skip(long arg0) throws IOException {
        return inputStream.skip(arg0);
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return inputStream.toString();
    }

    public SshInputStream(Session session, ChannelSftp channel, InputStream inputStream) {
        if(session!=null)
            this.session=session;
        if(channel!=null)
            this.channel=channel;
        this.inputStream=inputStream;
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

}
