package org.auscope.portal.server.domain.nvcldataservice;

import java.io.Serializable;

/**
 * Represents a response from a NVCL Data Service getClassifications request
 * @author Josh Vote (CSIRO)
 *
 */
public class AlgorithmOutputClassification implements Serializable {

    private static final long serialVersionUID = -7430804968407946156L;
    private String classText;
    private int color;
    private int index;

    public AlgorithmOutputClassification() {
        super();
    }
    public AlgorithmOutputClassification(String classText, int color, int index) {
        super();
        this.classText = classText;
        this.color = color;
        this.index = index;
    }
    public String getClassText() {
        return classText;
    }
    public void setClassText(String classText) {
        this.classText = classText;
    }
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}
