package org.auscope.portal.server.web.service;

public class CloudStorageException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CloudStorageException(String msg) {
        super(msg);
    }

    public CloudStorageException(String msg, Exception e) {
        super(msg, e);
    }

    public CloudStorageException() {
        super();
    }

}
