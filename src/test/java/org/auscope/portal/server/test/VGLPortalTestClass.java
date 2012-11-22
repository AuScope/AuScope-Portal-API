package org.auscope.portal.server.test;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Base class for unit test classes that require additional 
 * mocking capabilities provided by PowerMock to inherit from.
 *
 * Contains references to the appropriate JMock Mockery instance to utilise
 *
 * @author Richard Goh
 *
 */
@RunWith(PowerMockRunner.class)
public class VGLPortalTestClass {
    /**
     * used for generating/testing mock objects and their expectations
     */    
    protected Mockery context = new JUnit4Mockery(){{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};    
}