package org.auscope.portal.server.web.service.scm;

public interface ScmLoader {
    public <T> T loadEntry(String id, Class<T> cls);
    public Problem loadProblem(String id);
    public Toolbox loadToolbox(String id);
    public Solution loadSolution(String id);
}
